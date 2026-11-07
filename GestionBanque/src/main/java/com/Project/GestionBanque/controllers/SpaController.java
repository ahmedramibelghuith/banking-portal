package com.Project.GestionBanque.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
  @GetMapping({"/", "/login", "/register", "/home", "/admin"})
  public String app() {
    return "forward:/index.html";
  }
}
