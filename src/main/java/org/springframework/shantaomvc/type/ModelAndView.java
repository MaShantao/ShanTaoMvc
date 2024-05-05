package org.springframework.shantaomvc.type;

import java.util.HashMap;
import java.util.Map;

/**
 * MVC的ModelAndView
 *
 * @author MashanTao
 * @date 2021/11/27
 */
public class ModelAndView {
    // 页面所在的路径
    private String view;

    // 页面的data数据
    private Map<String, Object> model = new HashMap<>();

    public String getView() {
        return view;
    }

    public ModelAndView setView(String view) {
        this.view = view;
        return this;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }

    public ModelAndView addViewData(String name, String value) {
        model.put(name, value);
        return this;
    }
}
