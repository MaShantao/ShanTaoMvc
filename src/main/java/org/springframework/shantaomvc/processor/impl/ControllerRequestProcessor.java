package org.springframework.shantaomvc.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.shantaomvc.annotation.Controller;
import org.springframework.shantaomvc.annotation.RequestMapping;
import org.springframework.shantaomvc.annotation.RequestParam;
import org.springframework.shantaomvc.annotation.ResponseBody;
import org.springframework.shantaomvc.core.BeanContainer;
import org.springframework.shantaomvc.processor.RequestProcessor;
import org.springframework.shantaomvc.processor.RequestProcessorChain;
import org.springframework.shantaomvc.render.ResultRender;
import org.springframework.shantaomvc.render.impl.JsonResultRender;
import org.springframework.shantaomvc.render.impl.ResourceNotFoundResultRender;
import org.springframework.shantaomvc.render.impl.ViewResultRender;
import org.springframework.shantaomvc.type.ControllerMethod;
import org.springframework.shantaomvc.type.RequestPathInfo;
import org.springframework.shantaomvc.util.ConvertUtil;
import org.springframework.shantaomvc.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller的请求处理器
 * 主要负责处理Controller的请求
 *
 * @author MashanTao
 * @date 2021/11/25
 */
@Slf4j
public class ControllerRequestProcessor implements RequestProcessor {

    // IOC容器
    private BeanContainer beanContainer;

    // 请求和controller方法的映射集合
    private Map<RequestPathInfo, ControllerMethod> pathInfoControllerMethodMap = new ConcurrentHashMap<>();

    /**
     * 无参构造函数：
     * 依靠容器的能力，建立起请求路径、请求方法与Controller方法实例的映射
     */
    public ControllerRequestProcessor() {
        this.beanContainer = BeanContainer.getInstance();
        Set<Class<?>> requestMappingSet =
                beanContainer.getClassByAnnotation(Controller.class);
        initPathControllerMethodMap(requestMappingSet);
    }

    private void initPathControllerMethodMap(Set<Class<?>> requestMappingSet) {
        if (ValidationUtil.isEmpty(requestMappingSet)) {
            return;
        }
        // 1、遍历所有被@RequestMapping标记的类，获取类上面注解的属性值作为一级路径
        for (Class<?> requestMappingClass : requestMappingSet) {
            String basePath = "";
            if (requestMappingClass.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping controllerRequest = requestMappingClass.getAnnotation(RequestMapping.class);
                basePath = controllerRequest.value();
                if (!basePath.startsWith("/")) {
                    basePath = "/" + basePath;
                }
            }
            // 2、遍历类里所有被@RequestMapping标记的方法，获取方法上面该注解的属性值，作为二级路径
            Method[] methods = requestMappingClass.getMethods();
            if (ValidationUtil.isEmpty(methods)) continue;
            for (Method method : methods) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping methodRequest = method.getAnnotation(RequestMapping.class);
                    String methodPath = methodRequest.value();
                    if (!methodPath.startsWith("/")) {
                        methodPath = "/" + methodPath;
                    }
                    String url = basePath + methodPath;
                    // 3、解析方法里被@RequestParam标记的参数
                    // 3.1、获取该注解的属性值，作为参数名
                    // 3.2、获取被标记的参数的数据类型，建立参数名和参数类型的映射
                    Map<String, Class<?>> methodParams = new HashMap<>();
                    Parameter[] parameters = method.getParameters();
                    if (!ValidationUtil.isEmpty(parameters)) {
                        for (Parameter parameter : parameters) {
                            RequestParam param = parameter.getAnnotation(RequestParam.class);
                            String parameterName = null;
                            if (param == null) {
                                parameterName = parameter.getName();
                            } else {
                                parameterName = param.value();
                                if (ValidationUtil.isEmpty(parameterName)) {
                                    throw new RuntimeException(requestMappingClass + "的" + method + "方法的" + parameter +
                                            "的RequestParam的Value属性为空，RequestParam的Value属性不能为空");
                                }
                            }
                            methodParams.put(parameterName, parameter.getType());
                        }
                    }

                    // 4、将获取道德信息封装成RequestPathInfo实例和ControllerMethod实例，放置到映射表
                    String requestMethod = String.valueOf(methodRequest.method());
                    RequestPathInfo requestPathInfo = new RequestPathInfo(requestMethod, url);
                    if (this.pathInfoControllerMethodMap.containsKey(requestPathInfo)) {
                        log.warn("url:" + url + "出现冲突，后者会替换前者" + requestMappingClass + methodRequest);
                    }
                    ControllerMethod controllerMethod = new ControllerMethod(requestMappingClass, method, methodParams);
                    this.pathInfoControllerMethodMap.put(requestPathInfo, controllerMethod);
                }
            }
        }

    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        // 1、解析HttpServerRequest的请求方法，请求路径，获取对应ControllerMethod实例
        String requestPath = requestProcessorChain.getRequestPath();
        String requestMethod = requestProcessorChain.getRequestMethod().toUpperCase();
        ControllerMethod controllerMethod = this.pathInfoControllerMethodMap.get(new RequestPathInfo(requestMethod, requestPath));
        if (controllerMethod == null) {
            requestProcessorChain.setResultRender(new ResourceNotFoundResultRender(requestMethod, requestPath));
            return false;
        }
        // 2、解析请求参数，并传递给获取到的ControllerMethod实例去执行
        Object result = invokeControllerMethod(controllerMethod, requestProcessorChain.getRequest());
        // 3、根据处理的结果，选择对应的render进行渲染
        setResultRender(result, controllerMethod, requestProcessorChain);
        return false;
    }

    private void setResultRender(Object result, ControllerMethod controllerMethod, RequestProcessorChain requestProcessorChain) {
        if (result == null) {
            return;
        }
        ResultRender resultRender;
        // 查看类上或者方法上是否有ResponseBody注解，如果有的话，就将其认定为是JsonResultRender
        boolean isJsonControllerMethod = controllerMethod.getInvokeMethod().isAnnotationPresent(ResponseBody.class)
                || controllerMethod.getControllerClass().isAnnotationPresent(ResponseBody.class);
        if (isJsonControllerMethod) {
            resultRender = new JsonResultRender(result);
        } else {
            resultRender = new ViewResultRender(result);
        }
        requestProcessorChain.setResultRender(resultRender);
    }

    private Object invokeControllerMethod(ControllerMethod controllerMethod, HttpServletRequest request) {
        // 1、从请求里面获取GET或者POST的参数名机器对应的值
        Map<String, String> requestParamMap = new HashMap<>();
        // GET、POST方法的请求参数获取方式
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> parameter : parameterMap.entrySet()) {
            if (!ValidationUtil.isEmpty(parameter.getValue())) {
                // 只支持一个参数对应一个值的形式
                requestParamMap.put(parameter.getKey(), parameter.getValue()[0]);
            }
        }
        // 2、根据获取到的请求参数名及其对应的值，以及ControllerMethod里面的参数和类型的映射关系，去实例化出方法对应的参数
        List<Object> methodParams = new ArrayList<>();
        Method invokeMrthod = controllerMethod.getInvokeMethod();
        Map<String, Class<?>> methodParamMap = controllerMethod.getMethodParameters();
        Parameter[] parameters = invokeMrthod.getParameters();

        for (Parameter parameter : parameters) {
            String paramName = parameter.getName();
            Class<?> type = parameter.getType();
            Object value;
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                // 有注解存在的话
                paramName = parameter.getAnnotation(RequestParam.class).value();
            }
            String requestValue = requestParamMap.get(paramName);
            if (requestValue == null) {
                // 将请求里的参数值转成适配于参数类型的空值
                value = ConvertUtil.primitiveNull(type);
            } else {
                value = ConvertUtil.convert(type, requestValue);
            }
            methodParams.add(value);
        }

        // 3、执行Controller里面的方法冰返回结果
        Object controller = beanContainer.getBean(controllerMethod.getControllerClass());
        invokeMrthod.setAccessible(true);
        Object result;
        try {
            if (methodParamMap.isEmpty()) {
                result = invokeMrthod.invoke(controller);
            } else {
                result = invokeMrthod.invoke(controller, methodParams.toArray());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            // 如果是调用异常的话，需要通过e.getTartgetException()
            throw new RuntimeException(e.getTargetException());
        }
        return result;
    }

}
