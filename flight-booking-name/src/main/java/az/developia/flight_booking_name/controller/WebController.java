package az.developia.flight_booking_name.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    @GetMapping({"/", ""})
    public String index() {
        return "redirect:/login.html";
    }

    @GetMapping("/index.html")
    public String indexHtml() {
        return "redirect:/login.html";
    }

    @GetMapping("/home")
    public String home() {
        return "forward:/main.html";
    }

    @GetMapping("/profile")
    public String profile() {
        return "forward:/customerProfile.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping("/booking")
    public String booking() {
        return "forward:/main.html";
    }

    @GetMapping("/payment")
    public String payment() {
        return "forward:/payment.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/register.html";
    }
}
