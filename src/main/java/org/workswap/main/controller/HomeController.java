package org.workswap.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {

        model.addAttribute("requestURI", request.getRequestURI());
        return "redirect:/catalog";
    }
}
