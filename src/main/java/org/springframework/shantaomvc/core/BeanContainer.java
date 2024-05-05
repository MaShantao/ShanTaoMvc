package org.springframework.shantaomvc.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.shantaomvc.annotation.AutoWired;
import org.springframework.shantaomvc.annotation.Controller;
import org.springframework.shantaomvc.annotation.Repository;
import org.springframework.shantaomvc.annotation.Service;
import org.springframework.shantaomvc.util.ClassUtil;
import org.springframework.shantaomvc.util.ValidationUtil;
import org.springframework.shantaomvc.xml.XmlPaser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean容器
 *
 * @author MashanTao
 * @date 2021/11/26
 */
@Slf4j
public class BeanContainer {

    // Bean容器
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();

    // 存放类的完整限定名,存放所有需要加载的类的限定名
    private List<Class> classList = new ArrayList<>();

    private volatile boolean isLoaded = false;


    // 加载Bean的注册列表,标识需要加载的注解
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATIONS =
            Arrays.asList(Controller.class, Service.class, Repository.class);

    private static enum ContainerHolder {
        HOLDER;
        private BeanContainer instance;

        private ContainerHolder() {
            instance = new BeanContainer();
        }
    }

    public static BeanContainer getInstance() {
        return ContainerHolder.HOLDER.instance;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * 加载指定包下的bean
     *
     * @param contextConfigLocation
     */
    public void loadBeans(String contextConfigLocation) {
        if (isLoaded()) {
            log.warn("Bean的容器已经加载过了");
            return;
        }
        // 1、解析Spingmvc配置文件；classpath:springmvc.xml -> springmvc.xml
        String pack = XmlPaser.getbasePackage(contextConfigLocation.split(":")[1]);
        // 2、解析base-package，进行包扫描，扫描出所有的Class对象
        String[] packages = pack.split(",");
        Set<Class<?>> classSet = new HashSet<>();
        for (String packageName : packages) {
            try {
                ClassUtil.extractPackageClass(packageName, classSet);
            } catch (RuntimeException e) {
                e.printStackTrace();
                log.error("从contextConfigLocation中加载" + packageName + "包下的Class发生错误");
            }
        }
        if (ValidationUtil.isEmpty(classSet)) {
            log.error("从contextConfigLocation中加载不到任何Class");
            throw new RuntimeException("contextConfigLocation中加载不到任何Class");
        }
        // 3、实例化Bean
        executeInstances(classSet);
        // 4、执行自动注入
        executeAutoWired();
        isLoaded = true;
    }


    /**
     * 执行实例化
     *
     * @param classSet
     */
    private void executeInstances(Set<Class<?>> classSet) {
        for (Class<?> clazz : classSet) {
            for (Class<? extends Annotation> annotationClazz : BEAN_ANNOTATIONS) {
                if (clazz.isAnnotationPresent(annotationClazz)) {
                    beanMap.put(clazz, ClassUtil.newInstance(clazz, true));
                }
            }
        }
    }

    /**
     * 添加一个class对象及其bean实例
     *
     * @param clazz
     * @param bean
     * @return {@link Object}
     */
    public Object addBean(Class<?> clazz, Object bean) {
        return beanMap.put(clazz, bean);
    }

    /**
     * 执行依赖注入
     */
    private void executeAutoWired() {
        if (ValidationUtil.isEmpty(beanMap)) {
            log.warn("Bean容器为空");
            return;
        }
        try {
            // 1.遍历容器中的所有类，将带有@AutoWired的属性进行注入
            for (Class<?> clazz : this.getClasses()) {
                Field[] declaredFields = clazz.getDeclaredFields();
                if (ValidationUtil.isEmpty(declaredFields)) {
                    continue;
                }
                for (Field declaredField : declaredFields) {
                    if (declaredField.isAnnotationPresent(AutoWired.class)) {
                        AutoWired autoWired = declaredField.getAnnotation(AutoWired.class);
                        String autowiredValue = autoWired.value();
                        Class<?> fieldClass = declaredField.getType();
                        Object fieldValue = getFieldInstance(fieldClass, autowiredValue);
                        if (fieldValue == null) {
                            throw new RuntimeException("找不到需要注入的相关属性字段，该属性是fieldClass" + fieldClass);
                        } else {
                            Object targetBean = this.getBean(clazz);
                            ClassUtil.setField(declaredField, targetBean, fieldValue, true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("执行依赖注入时出错");
            throw new RuntimeException("执行依赖注入时出错");
        }
    }

    public Object getBean(Class<?> clazz) {
        return beanMap.get(clazz);
    }


    /**
     * 得到容器中的所有bean实例
     *
     * @param clazz
     * @return {@link Object}
     */
    public Object getBeans(Class<?> clazz) {
        return beanMap.values();
    }

    public Set<Class<?>> getClassByAnnotation(Class<? extends Annotation> annotation) {
        // 1、获取beanMap的所有class对象
        Set<Class<?>> classSet = getClasses();
        if (ValidationUtil.isEmpty(classSet)) {
            log.warn("beanMap中没有元素");
            return null;
        }
        // 2、通过注解筛选被注解标记的class对象，并添加到classSet里
        Set<Class<?>> selectedClassSet = new HashSet<>();
        for (Class<?> clazz : classSet) {
            // 类是否有相关的注解标记
            if (clazz.isAnnotationPresent(annotation)) {
                selectedClassSet.add(clazz);
            }
        }
        return selectedClassSet.size() == 0 ? null : selectedClassSet;
    }


    private Object getFieldInstance(Class<?> fieldClass, String autowiredValue) {
        // 1、根据字段的类型去获取Bean
        Object fieldValue = this.getBean(fieldClass);
        if (fieldValue != null) {
            // 直接根据Bean的类型就可以找到注入的属性
            return fieldValue;
        } else {
            // 寻找fieldClass的子类
            Class<?> implementedClass = getImplementedClass(fieldClass, autowiredValue);
            if (implementedClass != null) {
                return this.getBean(implementedClass);
            } else {
                return null;
            }
        }

    }

    /**
     * 寻找该类型对应的实现类的Class对应实例对象
     *
     * @param fieldClass
     * @param autowiredValue
     * @return {@link Class}<{@link ?}>
     */
    private Class<?> getImplementedClass(Class<?> fieldClass, String autowiredValue) {
        // 1、寻找fieldClass的实现类的实例对象
        Set<Class<?>> classSet = this.getClassesBySuper(fieldClass);
        if (!ValidationUtil.isEmpty(classSet)) {
            // 如果没有指定按照名字注入，此时必须只有唯一的一个实现类，否则抛出异常
            if (ValidationUtil.isEmpty(autowiredValue)) {
                if (classSet.size() == 1)
                    return classSet.iterator().next();
                // 如果没有指定按照名字注入，并且有多个实现类，那么就抛出异常
                throw new RuntimeException(fieldClass +
                        "有多个实现类，注入的时候出现冲突，需要为其指定按照Name进行注入以区分");
            } else {
                // 如果配置了按照Name进行注入，那就先走根据Name进行注入的逻辑
                for (Class clazz : classSet) {
                    // 得到类的名称之后，按照驼峰命名方法把第一个字符变成小写
                    String className = clazz.getSimpleName();
                    className = className.substring(0, 1).toLowerCase().concat(className.substring(1));
                    if (autowiredValue.equals(className)) {
                        return clazz;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 得到BeanMap的所有key组成的集合
     *
     * @return {@link Set}<{@link Class}<{@link ?}>>
     */
    public Set<Class<?>> getClasses() {
        return beanMap.keySet();
    }

    /**
     * 获取bean容器的大小
     *
     * @return int
     */
    public int size() {
        return beanMap.size();
    }

    /**
     * 根据父类/接口，得到其实现子类
     *
     * @param superClass
     * @return {@link Set}<{@link Class}<{@link ?}>>
     */
    private Set<Class<?>> getClassesBySuper(Class<?> superClass) {
        // 1、获取BeanMap的所有Class对象
        Set<Class<?>> classSet = getClasses();
        if (ValidationUtil.isEmpty(classSet)) {
            log.warn(superClass + "：找不到其实现类");
            return null;
        }
        // 2、找到classSet中fieldClass的所有子类，并返回
        Set<Class<?>> selectedClassSet = new HashSet<>();
        for (Class<?> clazz : classSet) {
            // 判断classSet中的元素是否是传入的接口或者类的子类
            if (superClass.isAssignableFrom(clazz) && !clazz.equals(superClass)) {
                selectedClassSet.add(clazz);
            }
        }
        return selectedClassSet.size() == 0 ? null : selectedClassSet;
    }
}
