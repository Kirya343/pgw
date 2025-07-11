package org.workswap.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class TermsController {

    @GetMapping("/terms")
    public String showTermsForm() {
        return "terms";
    }

    @GetMapping("/privacy-policy")
    public String showPrivacyPolicyForm() {
        return "privacy-policy";
    }
}
