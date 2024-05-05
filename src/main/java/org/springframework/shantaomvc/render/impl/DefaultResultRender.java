package org.springframework.shantaomvc.render.impl;

import org.springframework.shantaomvc.processor.RequestProcessorChain;
import org.springframework.shantaomvc.render.ResultRender;

/**
 * 没有返回值的Controller的方法执行结果
 *
 * @author MashanTao
 * @date 2021/11/27
 */
public class DefaultResultRender implements ResultRender {
    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        requestProcessorChain.getResponse().setStatus(requestProcessorChain.getResponseCode());
    }
}
