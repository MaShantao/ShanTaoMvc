package org.springframework.shantaomvc.render.impl;

import com.alibaba.fastjson.JSON;
import org.springframework.shantaomvc.processor.RequestProcessorChain;
import org.springframework.shantaomvc.render.ResultRender;

import java.io.PrintWriter;

/**
 * @author MashanTao
 * @date 2021/11/27
 */
public class JsonResultRender implements ResultRender {

    private Object jsonData;

    public JsonResultRender(Object jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        // 设置响应头
        requestProcessorChain.getResponse().setContentType("application/json");
        requestProcessorChain.getResponse().setCharacterEncoding("UTF-8");
        // 响应流写入经过fastjson格式化之后的处理结果
        try (PrintWriter writer = requestProcessorChain.getResponse().getWriter()) {
            String json = JSON.toJSONString(jsonData);
            writer.write(json);
            writer.flush();
        }
    }

}
