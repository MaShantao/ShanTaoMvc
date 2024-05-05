package org.springframework.shantaomvc.context;

import org.springframework.shantaomvc.core.BeanContainer;

/**
 * web应用程序上下文
 *
 * @author Ma ShanTao
 * @date 2021/06/27
 */
public class WebApplicationContext {

    // spring-mvc.xml的路径
    private String contextConfigLocation;

    private final BeanContainer beanContainer = BeanContainer.getInstance();

    public WebApplicationContext() {
    }

    public WebApplicationContext(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    public void onRefresh() {
        beanContainer.loadBeans(contextConfigLocation);
    }

    public String getContextConfigLocation() {
        return contextConfigLocation;
    }

    public BeanContainer getBeanContainer() {
        return beanContainer;
    }

    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }
    //    public void initHandlerMapping() {
//        handlerList = new ArrayList<>();
//        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
//            Object obj = entry.getValue();
//            // 遍历所有的Controller类，拿到他的@RequestMapping
//            if (obj.getClass().isAnnotationPresent(Controller.class)) {
//                // 拿到所有的方法
//                Method[] declaredMethods = obj.getClass().getDeclaredMethods();
//                for (Method declaredMethod : declaredMethods) {
//                    // 将带有@RequestMapping注解的方法进行解析
//                    if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
//                        RequestMapping annotation = declaredMethod.getAnnotation(RequestMapping.class);
//                        // 包装好处理器，并存到集合
//                        handlerList.add(new Handler(annotation.value(), obj, declaredMethod));
//                    }
//                }
//            }
//        }
//    }

//    public Handler getHeadler(String requestURI) {
//        for (Handler handler : handlerList) {
//            if (handler.getUrl().equals(requestURI)) {
//                return handler;
//            }
//        }
//        return null;
//    }
}
