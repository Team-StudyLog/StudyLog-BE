package org.example.studylog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @GetMapping("/")
    public String index(){
        return "index.html";
    }

    @GetMapping("/main")
    @ResponseBody
    public String main() {
        return "로그인 성공!";
    }

    @GetMapping("/success")
    @ResponseBody
    public String success(){
        return "요청 성공!";
    }

}
