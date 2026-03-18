package com.codingedu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    private static final java.util.Set<String> VALID_LANGS = java.util.Set.of(
        "html", "css", "javascript", "typescript", "java", "kotlin", "c", "cpp", "swift", "python"
    );

    @GetMapping("/")
    public String index() {
        return "index"; // templates/index.html
    }

    @GetMapping("/learn")
    public String learn() {
        return "learn"; // templates/learn.html
    }

    @GetMapping("/learn/{lang}")
    public String learnDetail(@PathVariable String lang, Model model) {
        String safeLang = VALID_LANGS.contains(lang) ? lang : "html";
        model.addAttribute("lang", safeLang);
        model.addAttribute("langTitle", safeLang.substring(0, 1).toUpperCase() + safeLang.substring(1));
        return "learn-detail"; // templates/learn-detail.html
    }

    @GetMapping("/challenge")
    public String challenge() {
        return "challenge"; // templates/challenge.html
    }

}
