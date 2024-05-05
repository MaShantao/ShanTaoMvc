package org.springframework.shantaomvc.render.impl;

import org.springframework.shantaomvc.processor.RequestProcessorChain;
import org.springframework.shantaomvc.render.ResultRender;
import org.springframework.shantaomvc.type.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ViewResultRender implements ResultRender {

    private ModelAndView modelAndView;

    public ViewResultRender(Object mv) {
        if (mv instanceof ModelAndView) {
            // 1、如果入参类型是ModelAndView，则直接赋值给成员变量
            this.modelAndView = (ModelAndView) mv;
        } else if (mv instanceof String) {
            // 2、如果入参类型是String，则认为是视图
            this.modelAndView = new ModelAndView().setView((String) mv);
        } else {
            // 3、针对其他情况，则直接抛出异常
            throw new RuntimeException("Controller的方法返回了非法的结果类型异常");
        }
    }

    /**
     * 按照请求处理结果，按照视图路径转发对应视图进行展示
     *
     * @param requestProcessorChain
     * @throws Exception
     */
    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        HttpServletRequest request = requestProcessorChain.getRequest();
        HttpServletResponse response = requestProcessorChain.getResponse();
        String viewName = modelAndView.getView();
        Map<String, Object> model = modelAndView.getModel();
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        // forward:/success.jsp
        if (viewName.contains(":")) {
            // 分割出跳转类型和跳转路径
            String viewType = viewName.split(":")[0];
            String viewPage = viewName.split(":")[1];
            // 如果是转发的话，就直接转发
            if (viewType.equals("forward")) {
                request.getRequestDispatcher(viewPage).forward(request, response);
            } else {
                // redirect:/user.jsp
                response.sendRedirect(viewPage);
            }
        } else {
            // 默认就转发
            request.getRequestDispatcher(viewName).forward(request, response);
        }
    }
}
