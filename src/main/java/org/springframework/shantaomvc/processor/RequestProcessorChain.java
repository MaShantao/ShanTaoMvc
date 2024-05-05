package org.springframework.shantaomvc.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.shantaomvc.render.impl.DefaultResultRender;
import org.springframework.shantaomvc.render.ResultRender;
import org.springframework.shantaomvc.render.impl.InternalErrorResultRender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

/**
 * Request的处理器责任链实现类
 * 1、以责任链的模式执行注册的请求处理器
 * 2、委派给特定的Render实例对处理后的结果进行渲染
 * <p>
 * 负责封装Request的责任连，以及封装责任链的相关属性
 *
 * @author MashanTao
 * @date 2021/11/25
 */
@Slf4j
public class RequestProcessorChain {
    // 请求处理器迭代器
    private Iterator<RequestProcessor> iterator;
    // 请求的request
    private HttpServletRequest req;
    // 请求的response
    private HttpServletResponse resp;
    // http请求方法
    private String requestMethod;
    // http的请求路径
    private String requestPath;
    // http的状态码
    private int responseCode;
    // 请求结果渲染器
    private ResultRender resultRender;

    public RequestProcessorChain(Iterator<RequestProcessor> iterator, HttpServletRequest req, HttpServletResponse resp) {
        this.iterator = iterator;
        this.req = req;
        this.resp = resp;
        this.requestMethod = req.getMethod().toUpperCase();
        this.requestPath = req.getPathInfo();
        this.responseCode = HttpServletResponse.SC_OK;
    }

    public void doRequestProcessorChain() {
        // 1、通过迭代器遍历注册的请求处理器实现类列表
        try {
            while (iterator.hasNext()) {
                if (!iterator.next().process(this)) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 3、解析的期间发生异常，则交由内部异常渲染器处理
            this.resultRender = new InternalErrorResultRender(e.getMessage());
            log.error("doRequestProcessorChain error:", e);
        }
    }

    public void doRender() {
        // 1、如果请求处理器实现类均为选择合适的渲染器，则使用默认的
        if (this.resultRender == null) {
            this.resultRender = new DefaultResultRender();
        }
        // 2、调用渲染器的render方法对结果进行渲染
        try {
            resultRender.render(this);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("doRender error:", e);
            throw new RuntimeException(e);
        }
    }

    public Iterator<RequestProcessor> getIterator() {
        return iterator;
    }

    public HttpServletRequest getRequest() {
        return req;
    }

    public HttpServletResponse getResponse() {
        return resp;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ResultRender getResultRender() {
        return resultRender;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setResultRender(ResultRender resultRender) {
        this.resultRender = resultRender;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
}
