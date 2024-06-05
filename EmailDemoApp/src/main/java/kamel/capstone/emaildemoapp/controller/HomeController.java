package kamel.capstone.emaildemoapp.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kamel.capstone.emaildemoapp.model.Email;
import kamel.capstone.emaildemoapp.model.User;
import kamel.capstone.emaildemoapp.service.EmailAppService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/email-demo-app")
public class HomeController {
    private final EmailAppService emailAppService;

    public HomeController(EmailAppService emailAppService) {
        this.emailAppService = emailAppService;
    }

    @GetMapping("/register")
    public String registerPage(HttpServletResponse response) {
        deleteAuthCookie(response);
        return "registration_page";
    }

    @GetMapping({"/login", "/logout"})
    public String loginPage(HttpServletResponse response) {
        deleteAuthCookie(response);
        return "login_page";
    }

    @GetMapping("/home")
    public String homePage(HttpServletRequest request, Model model) {
        if (getToken(request).isPresent()) {
            try {
                Cookie cookie = getToken(request).get();
                String token = cookie.getValue();
                List<Email> emailList = emailAppService.getEmailsByToken(token);
                model.addAttribute("emailList", emailList);
                return "home_page";
            } catch (UserPrincipalNotFoundException e) {
                return "login_page";
            }
        }
        return "login_page";
    }

    @GetMapping("/compose")
    public String composePage(HttpServletRequest request, Model model) {
        if (getToken(request).isPresent()){
            try {
                Cookie cookie = getToken(request).get();
                User sender = emailAppService.getUserByToken(cookie.getValue());
                model.addAttribute("sender", sender.getUsername());
                return  "compose_page";
            } catch (UserPrincipalNotFoundException e) {
                return "login_page";
            }
        }
        return "login_page";
    }

    private Optional<Cookie> getToken(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("Authorization"))
                .findFirst();
    }

    private void deleteAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
