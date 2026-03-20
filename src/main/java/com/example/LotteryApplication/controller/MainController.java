package com.example.LotteryApplication.controller;

import com.example.LotteryApplication.model.Record;
import com.example.LotteryApplication.model.User;
import com.example.LotteryApplication.repository.RecordRepository;
import com.example.LotteryApplication.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordRepository recordRepository;

    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<User> userOptional = userRepository.findByUsernameAndPassword(username, password);

        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        User user = userOptional.get();
        session.setAttribute("loggedInUser", user);

        if ("BOSS".equalsIgnoreCase(user.getRole())) {
            return "redirect:/boss";
        } else {
            return "redirect:/employee";
        }
    }

    @GetMapping("/employee")
    public String showEmployeePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/";
        }

        if (!"EMPLOYEE".equalsIgnoreCase(user.getRole())) {
            return "redirect:/boss";
        }

        model.addAttribute("username", user.getUsername());
        return "employee-dashboard";
    }

    @PostMapping("/save-record")
    public String saveRecord(

            @RequestParam BigDecimal gross,
                             @RequestParam BigDecimal wins,
                             @RequestParam BigDecimal shortages,
                             HttpSession session,
                             Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/";
        }

        BigDecimal adjustedGross = gross.multiply(BigDecimal.valueOf(0.7));
        BigDecimal adjustedWins = wins.multiply(BigDecimal.valueOf(240));

        BigDecimal balance = adjustedGross
                .subtract(adjustedWins)
                .subtract(shortages);

        Record record = new Record();
        record.setEmployee(user);
        record.setRecordDate(LocalDate.now());
        record.setGross(gross);
        record.setWins(wins);
        record.setShortages(shortages);
        record.setBalance(balance);

        recordRepository.save(record);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("success", "Record saved successfully");
        model.addAttribute("calculatedBalance", balance);

        return "employee-dashboard";
    }

    @GetMapping("/boss")
    public String showBossPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/";
        }

        if (!"BOSS".equalsIgnoreCase(user.getRole())) {
            return "redirect:/employee";
        }

        List<Record> records = recordRepository.findAll();
        model.addAttribute("records", records);

        return "boss-dashboard";
    }

    @PostMapping("/boss/search")
    public String searchByDate(@RequestParam String recordDate,
                               HttpSession session,
                               Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/";
        }

        LocalDate date = LocalDate.parse(recordDate);
        List<Record> records = recordRepository.findByRecordDate(date);

        model.addAttribute("records", records);
        return "boss-dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
}
}
