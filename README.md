# 自研SpringMvc框架-ShanTaoMvc
Coding于2021年下半旬，地点：学校
## 初版
<ol>
<li>创建Maven（webapp）工程</li>
<li>创建Controller和Service</li>
<li>准备前端控制器。创建DispatcherServlet，并在web.xml中建立映射</li>
<li>创建Web容器，并且通过dom4j解析springmvc.xml</li>
<li>将解析之后的<component-scan base-package>进行实例化</li>
<li>将实例化之后的类， 进行@Autowired属性注入</li>
<li>解析所有的 @RequestMapping注解，并进行缓存</li>
<li>DispatcherServlet写对请求的操作代码：从缓存中拿url对应的执行方法，然后执行之后返回。</li>
</ol>
## 中极版

![](imgs\20211128103143.png)

在初版基础上，添加大量代码，以及将部分功能进行重构优化：

1、将项目提高到了框架层面

2、增加了请求责任链对Web的请求进行处理。

3、增加了ModelAndView，以及各种类型的结果解析器，对结果进行解析

4、使用基于枚举实现的单例模式的bean容器，最大程度上保证单例的安全，避免因反射和序列化等技术手段，造成对单例破坏的情况。

5、增加了@RequestParam以及@ResponseBoby的支持

6、使用日志框架以及Java的异常体系会源码进行规范化

### 测试

中级版本，从如下几个方法进行测试：

1、测试无参Controller层方法的调用

2、测试静态页面（静态页面存在webapp/static中）

3、测试JSP页面（JSP页面默认支持路径是webapp/templates）

4、测试Controller的有参方法调用

5、测试抛出异常的Controller的方法

6、测试@ResponseBody

7、测试依赖注入（Controller->Service->Repository）

目前存在的一些问题：

1、无法完成为List类型的请求参数注入

2、idea版本过高，我是22年的版本，没有与之适配的lombok

3、无法为没有加RequestParam形参的属性进行注入，SpringMvc在低于JDK1.8的版本,在springmvc中是通过ASM框架对于class文件进行解析。
