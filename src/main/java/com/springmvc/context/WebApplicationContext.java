package com.springmvc.context;

import com.springmvc.annotation.AutoWired;
import com.springmvc.annotation.Controller;
import com.springmvc.annotation.RequestMapping;
import com.springmvc.annotation.Service;
import com.springmvc.handler.Handler;
import com.springmvc.xml.XmlPaser;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * web应用程序上下文
 *
 * @author Ma ShanTao
 * @date 2021/06/27
 */
public class WebApplicationContext {


    // spring-mvc.xml的路径
    private String contextConfigLocation;

    // 存放类的完整限定名
    private List<String> classNameList = new ArrayList<>();

    // 存放Bean的Map
    private HashMap<String, Object> beanMap = new HashMap<>();

    // HandlerMapping的集合，用来存url对应的controller以及执行方法
    private List<Handler> handlerList = null;

    public WebApplicationContext() {
    }

    public WebApplicationContext(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    public void onRefresh() {
        // 1、解析Spingmvc配置文件；classpath:springmvc.xml -> springmvc.xml
        String pack = XmlPaser.getbasePackage(contextConfigLocation.split(":")[1]);
        // 2、解析base-package，进行包扫描
        String[] packages = pack.split(",");
        for (String aPackage : packages) {
            executeScanPackage(aPackage);
        }
        // 3、实例化Bean
        executeInstance();
        // 4、执行自动注入
        executeAutoWired();
    }


    private void executeScanPackage(String aPackage) {
        // 根据路径名 com/jztai/controller 得到Url
        URL resource = this.getClass().getResource("/" + aPackage.replace(".", "/"));
        String path = resource.getFile();
        // 遍历该路径
        File dir = new File(path);
        for (File file : dir.listFiles()) {
            // 如果还有目录
            if (file.isDirectory()) {
                executeScanPackage(aPackage + "." + file.getName());
            } else {
                // 找到文件了
                String fileName = aPackage + "." + file.getName().replaceAll(".class", "");
                classNameList.add(fileName);
            }
        }
    }

    private void executeAutoWired() {
        try {
            // 1.遍历容器中的所有类，将带有@AutoWired的属性进行注入
            for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
                Object obj = entry.getValue();
                Field[] declaredFields = obj.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if (declaredField.isAnnotationPresent(AutoWired.class)) {
                        AutoWired autoWired = declaredField.getAnnotation(AutoWired.class);
                        String beanName = autoWired.value();
                        if ("".equals(beanName)) {
                            beanName = declaredField.getName().substring(0, 1).toLowerCase(Locale.ROOT)
                                    + declaredField.getName().substring(1);
                        }
                        // 该字段有可能是private的，所以要打开访问权限
                        declaredField.setAccessible(true);
                        declaredField.set(obj, beanMap.get(beanName));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeInstance() {
        try {
            for (String className : classNameList) {
                Class<?> clazz = Class.forName(className);
                String beanName = null;
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Controller controller = clazz.getAnnotation(Controller.class);
                    beanName = controller.value();
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);
                    beanName = service.value();
                }else {
                    // 其他注解不实例化
                    continue;
                }
                if (beanName == null || "".equals(beanName)) {
                    beanName = clazz.getSimpleName().substring(0, 1).toLowerCase(Locale.ROOT) +
                            clazz.getSimpleName().substring(1);
                }
                beanMap.put(beanName, clazz.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initHandlerMapping() {
        handlerList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            Object obj = entry.getValue();
            // 遍历所有的Controller类，拿到他的@RequestMapping
            if (obj.getClass().isAnnotationPresent(Controller.class)) {
                // 拿到所有的方法
                Method[] declaredMethods = obj.getClass().getDeclaredMethods();
                for (Method declaredMethod : declaredMethods) {
                    // 将带有@RequestMapping注解的方法进行解析
                    if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping annotation = declaredMethod.getAnnotation(RequestMapping.class);
                        // 包装好处理器，并存到集合
                        handlerList.add(new Handler(annotation.value(), obj, declaredMethod));
                    }
                }
            }
        }
    }

    public Handler getHeadler(String requestURI) {
        for (Handler handler : handlerList) {
            if (handler.getUrl().equals(requestURI)) {
                return handler;
            }
        }
        return null;
    }
}
