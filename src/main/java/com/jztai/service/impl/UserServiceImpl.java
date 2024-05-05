package com.jztai.service.impl;

import com.jztai.entity.TbUser;
import com.jztai.service.UserService;
import org.springframework.shantaomvc.annotation.Service;


@Service(value = "userService")
public class UserServiceImpl implements UserService {
    @Override
    public TbUser listUserJson() {
        return new TbUser("老王", 18, "天堂");
    }

    @Override
    public void listUser() {

        System.out.println("调用UserServiceImpl的查询所有用户的方法");
    }
}
