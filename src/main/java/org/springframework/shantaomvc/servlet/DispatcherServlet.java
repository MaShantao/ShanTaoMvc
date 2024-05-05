package org.springframework.shantaomvc.servlet;

import org.springframework.shantaomvc.context.WebApplicationContext;
import org.springframework.shantaomvc.processor.RequestProcessor;
import org.springframework.shantaomvc.processor.RequestProcessorChain;
import org.springframework.shantaomvc.processor.impl.ControllerRequestProcessor;
import org.springframework.shantaomvc.processor.impl.JspRequestProcessor;
import org.springframework.shantaomvc.processor.impl.PreRequestProcessor;
import org.springframework.shantaomvc.processor.impl.StaticResourceRequestProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    // 请求处理器责任链
    private final List<RequestProcessor> requestProcessors = new ArrayList<>();

    /**
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        super.init();
        // 1、加载上下文配置文件的路径。
        String contextConfigLocation = this.getInitParameter("contextConfigLocation");
        // 2、创建SpringMvc容器
        webApplicationContext = new WebApplicationContext(contextConfigLocation);
        // 3、进行初始化操作，扫描springmvc配置文件，扫描包拿到所有的类->实例化类->给类注入属性
        webApplicationContext.onRefresh();
        // 4、初始化责任链
        requestProcessors.add(new PreRequestProcessor());
        requestProcessors.add(new StaticResourceRequestProcessor(getServletContext()));
        requestProcessors.add(new JspRequestProcessor(getServletContext()));
        // ControllerRequestProcessor放在最后是因为这个处理器的执行比较耗时
        // 他需要将请求和Controller的方法进行匹配，放在后面以确保请求被前面的Processor拦截处理之后，才能最终执行Controller
        requestProcessors.add(new ControllerRequestProcessor());
    }
    
    /**
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 注意super.service(req, resp);不能加，因为他内部会进行处理，导致java.lang.IllegalStateException: 响应提交后无法调用sendError()
        // 1、创建责任链对象实例
        RequestProcessorChain requestProcessorChain = new RequestProcessorChain(requestProcessors.iterator(), req, resp);
        // 2、通过责任链模式来以此调用请求处理器对请求进行处理
        requestProcessorChain.doRequestProcessorChain();
        // 3、对处理结果进行渲染
        requestProcessorChain.doRender();
    }
}
