package med.voll.api.controller;


import org.springframework.web.bind.annotation.*;

import java.util.Scanner;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public String olaMundo(){

        return "Hello World Spring!";

    }





}
