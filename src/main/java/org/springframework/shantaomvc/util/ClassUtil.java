package org.springframework.shantaomvc.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Set;

@Slf4j
public class ClassUtil {

    /**
     * 根据类名去加载类的Class对象
     *
     * @param className
     * @return {@link Class}<{@link ?}>
     */
    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("load class error:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据Class去创建类的实例
     *
     * @param clazz      Class对象
     * @param accessible 是否支持创建出私有的Class对象的实例
     * @return {@link T}
     */
    public static <T> T newInstance(Class<?> clazz, boolean accessible) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(accessible);
            return (T) constructor.newInstance();
        } catch (Exception e) {
            log.error("newInstance error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取ClassLoader
     *
     * @return {@link ClassLoader}
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * @param emptyClassSet
     * @param aPackage
     */
    private static void extractClassFile(Set<Class<?>> emptyClassSet, String aPackage) {
        // 根据路径名 com/jztai/controller 得到Url
        ClassLoader classLoader = ClassUtil.getClassLoader();
        URL resource = classLoader.getResource("/" + aPackage.replace(".", "/"));
        String path = resource.getFile();
        // 遍历该路径
        File dir = new File(path);
        for (File file : dir.listFiles()) {
            // 如果还有目录
            if (file.isDirectory()) {
                extractClassFile(emptyClassSet, aPackage + "." + file.getName());
            } else {
                // 找到文件了
                String className = aPackage + "." + file.getName().replaceAll(".class", "");
                emptyClassSet.add(ClassUtil.loadClass(className));
            }
        }
    }

    /**
     * 提取包下面的所有类，将类添加到Set集合里面
     *
     * @param packageName
     * @param classSet
     */
    public static void extractPackageClass(String packageName, Set<Class<?>> classSet) {
        // 0、对传入的packageName进行判空操作
        if (ValidationUtil.isEmpty(packageName)) {
            throw new RuntimeException("指定的扫描包不能为空");
        }
        // 1、获取到类的加载器
        ClassLoader classLoader = getClassLoader();
        // 2、通过类加载器获取到加载的资源
        URL url = classLoader.getResource(packageName.replace(".", "/"));
        if (url == null) {
            log.warn("无法从包: " + packageName + "中检索到任何Class");
            return;
        }
        // 3、提取packageName包下的所有Class
        extractClassFile(classSet, packageName);
    }

    /**
     * 给对象的字段通过反射的方式注入
     *
     * @param field
     * @param target
     * @param value
     * @param accessible
     */
    public static void setField(Field field, Object target, Object value, boolean accessible) {
        field.setAccessible(accessible);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            log.error("setField error", e);
            throw new RuntimeException(e);
        }
    }

}
