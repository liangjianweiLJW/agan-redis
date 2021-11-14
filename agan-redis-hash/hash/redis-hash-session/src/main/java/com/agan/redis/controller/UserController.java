package com.agan.redis.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController {

    Map<String, User> userMap = new HashMap<>();

    public UserController() {
        //初始化2个用户，用于模拟登录
        User u1=new User(1,"user1","user1");
        userMap.put("user1",u1);
        User u2=new User(2,"user2","user2");
        userMap.put("user2",u2);
    }

    @GetMapping(value = "/login")
    public String login(String username, String password, HttpSession session) {
        //模拟数据库的查找
        User user = this.userMap.get(username);
        if (user != null) {
            if (!password.equals(user.getPassword())) {
                return "用户名或密码错误！！！";
            } else {
                session.setAttribute(session.getId(), user);
                log.info("登录成功{}",user);
            }
        } else {
            return "用户名或密码错误！！！";
        }
        return "登录成功！！！";
    }

    /**
     * 通过用户名查找用户
     */
    @GetMapping(value = "/find/{username}")
    public User find(@PathVariable String username) {
        User user=this.userMap.get(username);
        log.info("通过用户名={},查找出用户{}",username,user);
        return user;
    }

    /**
     *拿当前用户的session
     */
    @GetMapping(value = "/session")
    public String session(HttpSession session) {
        log.info("当前用户的session={}",session.getId());
        return session.getId();
    }

    /**
     * 退出登录
     */
    @GetMapping(value = "/logout")
    public String logout(HttpSession session) {
        log.info("退出登录session={}",session.getId());
        session.removeAttribute(session.getId());
        return "成功退出！！";
    }

}
