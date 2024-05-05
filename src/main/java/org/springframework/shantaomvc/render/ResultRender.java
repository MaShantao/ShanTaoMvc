package org.springframework.shantaomvc.render;

import org.springframework.shantaomvc.processor.RequestProcessorChain;

/**
 * 渲染请求处理结果
 *
 * @author MashanTao
 * @date 2021/11/25
 */
public interface ResultRender {
    void render(RequestProcessorChain requestProcessorChain) throws Exception;
}
