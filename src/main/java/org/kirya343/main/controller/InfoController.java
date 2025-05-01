package org.kirya343.main.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InfoController {

    @GetMapping("/info")
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
        return "info";
    }
}
