package com.syneronix.wallet.api.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeSwaggerController {

    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui.html";
    }
}
