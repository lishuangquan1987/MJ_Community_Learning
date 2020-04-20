package com.mj_learning.controller;

import com.alibaba.fastjson.JSON;
import com.mj_learning.entities.GitHubUser;
import com.mj_learning.entities.RequestAccessTokenParam;
import com.mj_learning.entities.User;
import com.mj_learning.helper.OkhttpHelper;
import com.mj_learning.mapper.UserMapper;
import com.squareup.okhttp.internal.http.OkHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
public class AuthController {

    @Value("${github.client_id}")
    private String client_id;
    @Value("${github.redirect_uri}")
    private String redirect_uri;
    @Value("${github.client_secret}")
    private String client_secret;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/github_login")
    public String login()
    {
        System.out.println(client_id);
        System.out.println(redirect_uri);
        System.out.println(client_secret);

        String uri= String.format("https://github.com/login/oauth/authorize?client_id={}&redirect_uri={}&state=test&scope=user", client_id,redirect_uri);
        return "redirect:"+uri;
    }
    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code,
                           HttpServletRequest request,
                           HttpServletResponse response)
    {
        //1.接受参数code
        System.out.println("callback->code:"+code);
        //2.通过post请求https://github.com/login/oauth/access_token，传入code,获取token
        RequestAccessTokenParam param=new RequestAccessTokenParam();
        param.setClient_id(client_id);
        param.setClient_secret(client_secret);
        param.setCode(code);
        param.setRedirect_uri(redirect_uri);
        param.setState("test");
        HttpSession session = request.getSession();

        String json= JSON.toJSONString(param);
        System.out.println("param:"+json);
        String tokenResult;
        try {
            tokenResult = OkhttpHelper.Post("https://github.com/login/oauth/access_token",json);
            System.out.println("请求token返回的字符串："+tokenResult);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            session.setAttribute("error","获取token失败");
            return "redirect:/";
        }


        //3.根据获取的token,get请求https://api.github.com/user?access_token=获取用户信息
        /*
        {
          "login": "lishuangquan1987",
          "id": 15365329,
          "node_id": "MDQ6VXNlcjE1MzY1MzI5",
          "avatar_url": "https://avatars3.githubusercontent.com/u/15365329?v=4",
          "gravatar_id": "",
          "url": "https://api.github.com/users/lishuangquan1987",
          "html_url": "https://github.com/lishuangquan1987",
          "followers_url": "https://api.github.com/users/lishuangquan1987/followers",
          "following_url": "https://api.github.com/users/lishuangquan1987/following{/other_user}",
          "gists_url": "https://api.github.com/users/lishuangquan1987/gists{/gist_id}",
          "starred_url": "https://api.github.com/users/lishuangquan1987/starred{/owner}{/repo}",
          "subscriptions_url": "https://api.github.com/users/lishuangquan1987/subscriptions",
          "organizations_url": "https://api.github.com/users/lishuangquan1987/orgs",
          "repos_url": "https://api.github.com/users/lishuangquan1987/repos",
          "events_url": "https://api.github.com/users/lishuangquan1987/events{/privacy}",
          "received_events_url": "https://api.github.com/users/lishuangquan1987/received_events",
          "type": "User",
          "site_admin": false,
          "name": null,
          "company": null,
          "blog": "",
          "location": "Guangzhou,China",
          "email": "294388344@qq.com",
          "hireable": null,
          "bio": null,
          "public_repos": 23,
          "public_gists": 0,
          "followers": 1,
          "following": 5,
          "created_at": "2015-10-28T16:07:35Z",
          "updated_at": "2020-04-18T13:32:31Z",
          "private_gists": 0,
          "total_private_repos": 0,
          "owned_private_repos": 0,
          "disk_usage": 388978,
          "collaborators": 0,
          "two_factor_authentication": false,
          "plan": {
            "name": "free",
            "space": 976562499,
            "collaborators": 0,
            "private_repos": 10000
          }
        }
         */
        String userJson="";
        tokenResult=getAccessToken(tokenResult);//解析token
        if(tokenResult.length()!=40)
        {
            System.out.println("解析后的token:"+tokenResult+"  长度不为40");
            session.setAttribute("error","token格式错误");
            return "redirect:/";
        }
        try {
            userJson= OkhttpHelper.Get("https://api.github.com/user?access_token="+tokenResult);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            session.setAttribute("error","获取user失败");
            return "redirect:/";
        }
        System.out.println("请求user返回的字符串："+userJson);
        //4.储存用户信息
        GitHubUser user=JSON.parseObject(userJson,GitHubUser.class);

        session.removeAttribute("error");

        //储存用户信息
        User u=new User();
        u.setAccount_id(String.valueOf(user.getId()));
        u.setName(user.getLogin());
        u.setToken(UUID.randomUUID().toString());
        u.setGmt_created(System.currentTimeMillis());
        u.setGmt_modified(u.getGmt_created());
        userMapper.insert(u);

        Cookie cookie = new Cookie("token",u.getToken());
        response.addCookie(cookie);//将令牌加入到cookie

        return "redirect:/";
    }
    private String getAccessToken(String responseStr)
    {
        String[] strs=responseStr.split("&");
        return strs[0].split("=")[1];
    }
}
