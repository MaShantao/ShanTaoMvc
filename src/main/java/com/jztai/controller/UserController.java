package com.jztai.controller;

import com.jztai.entity.TbUser;
import com.jztai.service.UserService;
import com.springmvc.annotation.AutoWired;
import com.springmvc.annotation.Controller;
import com.springmvc.annotation.RequestMapping;
import com.springmvc.annotation.ResponseBody;

@Controller(value = "userController")
public class UserController {

    @AutoWired(value = "userService")
    private UserService userService;


    @RequestMapping(value = "/listUser")
    public String listUser() {
        userService.listUser();
        return "success.jsp";
    }

    @RequestMapping(value = "/getById")
    @ResponseBody
    public TbUser listUserJson() {
        return userService.listUserJson();
    }

}
