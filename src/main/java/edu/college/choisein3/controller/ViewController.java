package edu.college.choisein3.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class ViewController {
    @GetMapping("/")
    public String showHomePage(){
        return "index";
    }
    @GetMapping("/result")
    public String showResultPage(Model model) {
        model.addAttribute("profileType", "Прагматичный Альтруист");
        model.addAttribute("altruism", 85);
        model.addAttribute("pragmatism", 72);
        model.addAttribute("justice", 68);
        return "result";
    }
}
