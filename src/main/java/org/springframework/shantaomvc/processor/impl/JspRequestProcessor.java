package org.springframework.shantaomvc.processor.impl;

import org.springframework.shantaomvc.processor.RequestProcessor;
import org.springframework.shantaomvc.processor.RequestProcessorChain;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

/**
 * Jsp的请求处理器
 * 主要负责解析JSP的请求
 *
 * @author MashanTao
 * @date 2021/11/25
 */
public class JspRequestProcessor implements RequestProcessor {

    // jsp请求的RequestDispatcher的名称
    private static final String JSP_SERVLET = "jsp";

    // Jsp请求资源路径前缀
    private static final String JSP_RESOURCE_PREFIX = "/templates/";

    /**
     * jsp的RequestDispatcher,处理jsp资源
     */
    private RequestDispatcher jspServlet;

    public JspRequestProcessor(ServletContext servletContext) {
        jspServlet = servletContext.getNamedDispatcher(JSP_SERVLET);
        if (null == jspServlet) {
            throw new RuntimeException("there is no jsp servlet");
        }
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        if (isJspResource(requestProcessorChain.getRequestPath())) {
            jspServlet.forward(requestProcessorChain.getRequest(), requestProcessorChain.getResponse());
            return false;
        }
        return true;
    }

    /**
     * 是否请求的是jsp资源
     */
    private boolean isJspResource(String url) {
        return url.startsWith(JSP_RESOURCE_PREFIX);
    }
}