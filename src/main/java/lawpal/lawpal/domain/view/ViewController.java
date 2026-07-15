package lawpal.lawpal.domain.view;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @GetMapping({"/", "/home"})
    public String home(HttpSession session, Model model) {
        model.addAttribute("loggedIn", session.getAttribute("userEmail") != null);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, HttpSession session) {
        session.setAttribute("userEmail", email);
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String email, HttpSession session) {
        session.setAttribute("userEmail", email);
        return "redirect:/";
    }
}
