package com.mj_learning.controller;

import com.mj_learning.entities.User;
import com.mj_learning.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {
    @Autowired
    UserMapper userMapper;
    @GetMapping({"/","/index"})
    public String index(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        String token="";
        if (cookies!=null)
        {
            for (Cookie cookie:cookies)
            {
                if (cookie.getName().equals("token"))
                {
                    token=cookie.getValue();
                }
            }
            if (!token.equals(""))
            {
                User user=userMapper.findUserByToken(token);
                if (user!=null)
                {
                    System.out.println("index获取到user:"+user.toString());
                    request.getSession().setAttribute("user",user);
                }
            }
        }

        return  "index";
    }
}
