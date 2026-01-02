package com.perpustakaan.library.controller;

import com.perpustakaan.library.entity.Pengguna;
import com.perpustakaan.library.repository.PenggunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired private PenggunaRepository penggunaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String nama,
            @RequestParam String email,
            @RequestParam String noHp,
            Model model) {

        if (penggunaRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?gagal=username_ada";
        }

        Pengguna newUser = new Pengguna();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setNamaLengkap(nama);
        newUser.setEmail(email);
        newUser.setNoTelepon(noHp);
        newUser.setPeran("ANGGOTA"); 

        penggunaRepository.save(newUser);

        return "redirect:/login?sukses=daftar";
    }
}