package com.springmvc.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springmvc.annotation.ResponseBody;
import com.springmvc.context.WebApplicationContext;
import com.springmvc.handler.Handler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * dispatcher servlet
 * mvc的入口
 *
 * @author Ma ShanTao
 * @date 2021/06/27
 */
public class DispatcherServlet extends HttpServlet {

    // Web容器
    private WebApplicationContext webApplicationContext;


    @Override
    public void init() throws ServletException {
        super.init();
        // 1、加载上下文配置文件的路径。
        String contextConfigLocation = this.getInitParameter("contextConfigLocation");
        // 2、创建SpringMvc容器
        webApplicationContext = new WebApplicationContext(contextConfigLocation);
        // 3、进行初始化操作，扫描springmvc配置文件，扫描包拿到所有的类->实例化类->给类注入属性
        webApplicationContext.onRefresh();
        // 4、准备好请求映射关系，url对应控制器的哪个方法。
        webApplicationContext.initHandlerMapping();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 进行请求和分发处理
        doDispatcher(req, resp);
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
        // 1、根据用户的请求地址，去获取相应的handler
        Handler handler = webApplicationContext.getHeadler(req.getRequestURI());
        if (handler == null) {
            // 没有找到匹配的handler,返回404
            resp.setStatus(404);
            return;
        }
        try {
            // 2、执行相应的handler
            // 2.1 注入方法需要的参数
            // 2.2 调用目标方法
            Object result = handler.getMethod().invoke(handler.getController());
            // 2.3 根据返回结果进行处理
            if (result instanceof String) {
                // 跳转JSP
                String viewName = (String) result;
                // forward:/success.jsp
                if (viewName.contains(":")) {
                    // 分割出跳转类型和跳转路径
                    String viewType = viewName.split(":")[0];
                    String viewPage = viewName.split(":")[1];
                    // 如果是转发的话，就直接转发
                    if (viewType.equals("forward")) {
                        req.getRequestDispatcher(viewPage).forward(req, resp);
                    } else {
                        // redirect:/user.jsp
                        resp.sendRedirect(viewPage);
                    }
                } else {
                    // 默认就转发
                    req.getRequestDispatcher(viewName).forward(req, resp);
                }
            } else {
                // 返回JSON格式数据
                Method method = handler.getMethod();
                if (method.isAnnotationPresent(ResponseBody.class)) {
                    // 将返回值转换成 json格式数据
                    ObjectMapper objectMapper = new ObjectMapper();
                    String json = objectMapper.writeValueAsString(result);
                    resp.setContentType("text/html;charset=utf-8");
                    PrintWriter writer = resp.getWriter();
                    writer.print(json);
                    writer.flush();
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
