package com.codingedu.controller;

import com.codingedu.entity.User;
import com.codingedu.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 로그인 화면
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html
    }

    // 회원가입 화면
    @GetMapping("/register")
    public String registerPage() {
        return "register"; // templates/register.html
    }

    // 회원가입 폼 제출시 DB 저장 처리
    @PostMapping("/register")
    public String registerProcess(User user, Model model) {
        // 아이디 중복 체크
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "이미 존재하는 아이디입니다.");
            return "register";
        }
        
        // 닉네임 중복 체크
        if (userRepository.findByNickname(user.getNickname()).isPresent()) {
            model.addAttribute("error", "이미 사용하는 닉네임입니다.");
            return "register";
        }

        // 1234 -> 해시 암호화 $2a$10$wI...
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // DB 저장
        userRepository.save(user);

        // 가입 완료 후 로그인 페이지로 이동
        return "redirect:/login?registered=true";
    }
}
