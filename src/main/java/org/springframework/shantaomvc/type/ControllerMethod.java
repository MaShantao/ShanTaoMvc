package org.springframework.shantaomvc.type;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 待执行的Controller及其方法实例和参数的映射
 *
 * @author MashanTao
 * @date 2021/11/26
 */
public class ControllerMethod {

    // Controller对应的Class对象
    private Class<?> controllerClass;

    // 执行的Controller方法实例
    private Method invokeMethod;

    // 方法的参数名称以及对应的参数类型
    private Map<String, Class<?>> methodParameters;

    public ControllerMethod(Class<?> controllerClass, Method invokeMethod, Map<String, Class<?>> methodParameters) {
        this.controllerClass = controllerClass;
        this.invokeMethod = invokeMethod;
        this.methodParameters = methodParameters;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Method getInvokeMethod() {
        return invokeMethod;
    }

    public void setInvokeMethod(Method invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public Map<String, Class<?>> getMethodParameters() {
        return methodParameters;
    }

    public void setMethodParameters(Map<String, Class<?>> methodParameters) {
        this.methodParameters = methodParameters;
    }
}
