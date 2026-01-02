package com.perpustakaan.library.controller;

import com.perpustakaan.library.entity.Buku;
import com.perpustakaan.library.entity.DetailPeminjaman;
import com.perpustakaan.library.entity.Peminjaman;
import com.perpustakaan.library.entity.Pengguna;
import com.perpustakaan.library.repository.BukuRepository;
import com.perpustakaan.library.repository.DetailPeminjamanRepository;
import com.perpustakaan.library.repository.PeminjamanRepository;
import com.perpustakaan.library.repository.PenggunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class UserController {

    @Autowired private BukuRepository bukuRepository;
    @Autowired private PenggunaRepository penggunaRepository;
    @Autowired private PeminjamanRepository peminjamanRepository;
    @Autowired private DetailPeminjamanRepository detailPeminjamanRepository;

    //PROSES BOOKING (USER)
    @PostMapping("/booking")
    public String prosesBooking(@RequestParam("bukuId") Long bukuId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        String username = authentication.getName();
        Pengguna user = penggunaRepository.findByUsername(username).orElseThrow();
        Buku buku = bukuRepository.findById(bukuId).orElseThrow();

        if (buku.getStok() <= 0) return "redirect:/buku/" + bukuId + "?gagal=stok_habis";

        List<Peminjaman> historiUser = peminjamanRepository.findByPengguna(user);
        for (Peminjaman p : historiUser) {
            if ("DIBOOKING".equals(p.getStatus()) || "DIPINJAM".equals(p.getStatus())) {
                for (DetailPeminjaman dp : p.getDetailPeminjamanList()) {
                    if (dp.getBuku().getId().equals(bukuId)) return "redirect:/buku/" + bukuId + "?gagal=sudah_ada";
                }
            }
        }

        Peminjaman p = new Peminjaman();
        p.setPengguna(user);
        p.setTanggalPinjam(LocalDate.now());
        p.setTenggatWaktu(LocalDate.now().plusDays(1));
        p.setStatus("DIBOOKING");
        peminjamanRepository.save(p);

        DetailPeminjaman dp = new DetailPeminjaman();
        dp.setPeminjaman(p);
        dp.setBuku(buku);
        detailPeminjamanRepository.save(dp);

        buku.setStok(buku.getStok() - 1);
        bukuRepository.save(buku);

        return "redirect:/buku/" + bukuId + "?sukses=booking";
    }

    //CANCEL BOOKING (USER)
    @PostMapping("/booking/cancel")
    public String batalBooking(@RequestParam("bukuId") Long bukuId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        String username = authentication.getName();
        Pengguna user = penggunaRepository.findByUsername(username).orElseThrow();
        
        List<Peminjaman> historiUser = peminjamanRepository.findByPengguna(user);
        for (Peminjaman p : historiUser) {
            if ("DIBOOKING".equals(p.getStatus())) {
                for (DetailPeminjaman dp : p.getDetailPeminjamanList()) {
                    if (dp.getBuku().getId().equals(bukuId)) {
                        p.setStatus("DIBATALKAN");
                        peminjamanRepository.save(p);
                        
                        Buku buku = bukuRepository.findById(bukuId).orElse(null);
                        if (buku != null) {
                            buku.setStok(buku.getStok() + 1);
                            bukuRepository.save(buku);
                        }
                        return "redirect:/buku/" + bukuId + "?sukses=batal";
                    }
                }
            }
        }
        return "redirect:/buku/" + bukuId + "?gagal=gagal_batal";
    }
}