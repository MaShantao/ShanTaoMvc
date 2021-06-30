1、创建Maven（webapp）工程
2、创建Controller和Service
3、准备前端控制器。创建DispatcherServlet，并在web.xml中建立映射
4、创建Web容器，并且通过dom4j解析springmvc.xml
5、将解析之后的<component-scan base-package>进行实例化
6、将实例化之后的类，进行@Autowired属性注入
7、解析所有的 @RequestMapping注解，并进行缓存
8、DispatcherServlet写对请求的操作代码：从缓存中拿url对应的执行方法，然后执行之后返回。
