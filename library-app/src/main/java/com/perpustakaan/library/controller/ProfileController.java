package com.perpustakaan.library.controller;

import com.perpustakaan.library.entity.Peminjaman;
import com.perpustakaan.library.entity.Pengguna;
import com.perpustakaan.library.repository.PeminjamanRepository;
import com.perpustakaan.library.repository.PenggunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    @Autowired private PenggunaRepository penggunaRepository;
    @Autowired private PeminjamanRepository peminjamanRepository;
    @Autowired private PasswordEncoder passwordEncoder; // Pastikan SecurityConfig sudah ada Bean ini

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        Pengguna user = getUser(authentication);
        model.addAttribute("user", user);

        // Ambil semua transaksi
        List<Peminjaman> semuaTransaksi = peminjamanRepository.findByPenggunaOrderByTanggalPinjamDesc(user);

        // Pisahkan: Mana yang BOOKING, mana yang RIWAYAT (Dipinjam/Dikembalikan/Batal)
        List<Peminjaman> listBooking = semuaTransaksi.stream()
                .filter(p -> p.getStatus().equals("DIBOOKING"))
                .collect(Collectors.toList());

        List<Peminjaman> listRiwayat = semuaTransaksi.stream()
                .filter(p -> !p.getStatus().equals("DIBOOKING")) // Sisanya masuk riwayat
                .collect(Collectors.toList());

        model.addAttribute("listBooking", listBooking);
        model.addAttribute("listRiwayat", listRiwayat);

        return "profile";
    }

    // UPDATE BIODATA (Nama, Email, HP, Username)
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String nama,
            @RequestParam String email,
            @RequestParam String noHp,
            @RequestParam String username,
            Authentication authentication) {
        
        Pengguna user = getUser(authentication);
        user.setNamaLengkap(nama);
        user.setEmail(email);
        user.setNoTelepon(noHp);
        user.setUsername(username);
        
        penggunaRepository.save(user);
        
        return "redirect:/profile?sukses=update";
    }

    // GANTI PASSWORD
    @PostMapping("/profile/password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            Authentication authentication) {
        
        Pengguna user = getUser(authentication);

        // Cek Password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "redirect:/profile?gagal=password_salah";
        }

        // Simpan Password Baru (Dienkripsi)
        user.setPassword(passwordEncoder.encode(newPassword));
        penggunaRepository.save(user);

        return "redirect:/profile?sukses=password";
    }

    private Pengguna getUser(Authentication auth) {
        return penggunaRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}