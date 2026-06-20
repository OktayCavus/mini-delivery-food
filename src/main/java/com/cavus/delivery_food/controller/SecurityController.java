package com.cavus.delivery_food.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {

@GetMapping("/springsecurity")
    public String springSecurity(){
    return "spring sec";
}
}
