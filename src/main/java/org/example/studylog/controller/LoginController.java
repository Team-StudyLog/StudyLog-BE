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

    // 메인 페이지 구현에 따라 주석 처리 - 채민
//    @GetMapping("/main")
//    @ResponseBody
//    public String main() {
//        return "메인 페이지";
//    }

    @GetMapping("/success")
    @ResponseBody
    public String success(){
        return "요청 성공!";
    }

    @GetMapping("/signup")
    @ResponseBody
    public String signup(){
        return "회원가입 페이지";
    }


}
