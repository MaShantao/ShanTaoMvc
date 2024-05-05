package com.jztai.controller;

import com.jztai.entity.TbUser;
import com.jztai.service.UserService;
import org.springframework.shantaomvc.annotation.*;
import org.springframework.shantaomvc.type.ModelAndView;

/**
 * 用户Controller的调用
 *
 * @author MashanTao
 * @date 2021/12/04
 */
@Controller()
public class UserController {

    @AutoWired
    private UserService userService;


    /**
     * 测试JSP的通过性
     * 返回值是String类型的，默认是返回的视图名称
     *
     * @return {@link String}
     */
    @RequestMapping("/testsuccess")
    public String testsuccess() {
        userService.listUser();
        return "templates/success.jsp";
    }


    /**
     * 测试带RequestParam注解的方法可用性
     *
     * @param userName
     * @param age
     * @param address
     * @return {@link TbUser}
     */
    @RequestMapping("/testRequestParam")
    @ResponseBody
    public TbUser testRequestParam(@RequestParam("userName") String userName, @RequestParam("age") Integer age, @RequestParam("address") String address) {
        return new TbUser(userName, age, address);
    }

    /**
     * 测试部分RequestParam注解参数的可用性
     *
     * @param userName
     * @param age
     * @param address
     * @return {@link TbUser}
     */
    @RequestMapping("/testParam")
    @ResponseBody
    public TbUser testParam(String userName, @RequestParam("age") Integer age, String address) {
        return new TbUser(userName, age, address);
    }


    /**
     * 测试无参方法的调用
     *
     * @return {@link TbUser}
     */
    @RequestMapping("/testNoneParam")
    @ResponseBody
    public TbUser testNoneParam() {
        return new TbUser("userName", 11, "address");
    }

    /**
     * 测试ResponseBody注解的使用情况
     *
     * @return {@link TbUser}
     */
    @RequestMapping("/testResponseBodyController")
    @ResponseBody
    public TbUser testResponseBodyController() {
        return userService.listUserJson();
    }

    /**
     * 测试ModelAndView全部功能
     *
     * @return {@link ModelAndView}
     */
    @RequestMapping("/testModelAndView")
    public ModelAndView testModelAndView() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView("templates/ModelAndViewJsp.jsp")
                .addViewData("name", "testModelAndView")
                .addViewData("message", "看到这个就说明成功了");
        return modelAndView;
    }

    /**
     * 测试ModelAndView的View部分
     *
     * @return {@link ModelAndView}
     */
    @RequestMapping("/testModelAnd_View")
    public ModelAndView testModelAnd_View() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView("templates/ModelAndViewJsp.jsp");
        return modelAndView;
    }

    @RequestMapping("/testError")
    public void testError() {
        throw new RuntimeException("测试Controller方法抛出异常");
    }

}
