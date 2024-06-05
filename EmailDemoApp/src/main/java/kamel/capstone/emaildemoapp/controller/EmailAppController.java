package kamel.capstone.emaildemoapp.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kamel.capstone.emaildemoapp.model.Email;
import kamel.capstone.emaildemoapp.model.User;
import kamel.capstone.emaildemoapp.service.EmailAppService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/email-demo-app")
public class EmailAppController {
    private final EmailAppService emailAppService;

    public EmailAppController(
            @Qualifier("ServiceImpl") EmailAppService emailAppService
    ) {
        this.emailAppService = emailAppService;
    }

    @PostMapping("/register")
    public RedirectView registerUser(@ModelAttribute User user) {
        if (emailAppService.registerUser(user))
            return new RedirectView("/email-demo-app/login");

        return new RedirectView("/email-demo-app/register");
    }

    @PostMapping("/login")
    public RedirectView login(@ModelAttribute User user, HttpServletResponse response) {
        String token = emailAppService.login(user);
        if (token.isEmpty())
            return new RedirectView("/email-demo-app/login");

        response.addCookie(new Cookie("Authorization", token));
        return new RedirectView("/email-demo-app/home");
    }

    @PostMapping("/compose")
    public RedirectView compose(@ModelAttribute Email email, Model model) {
        if (emailAppService.compose(email))
            return new RedirectView("/email-demo-app/home");

        model.addAttribute("error", "Failed to send email.");
        return new RedirectView("/email-demo-app/compose");
    }
}
