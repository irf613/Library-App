package com.perpustakaan.library.controller;

import com.perpustakaan.library.entity.Buku;
import com.perpustakaan.library.entity.DetailPeminjaman;
import com.perpustakaan.library.entity.Peminjaman;
import com.perpustakaan.library.entity.Pengguna;
import com.perpustakaan.library.repository.BukuRepository;
import com.perpustakaan.library.repository.PeminjamanRepository;
import com.perpustakaan.library.repository.PenggunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PublicController {

    @Autowired
    private BukuRepository bukuRepository;
    
    @Autowired 
    private PeminjamanRepository peminjamanRepository;
    
    @Autowired 
    private PenggunaRepository penggunaRepository;

    @GetMapping({"/", "/katalog"})
    public String katalog(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "kategori", required = false) String kategori,
            Model model) {

        // 1. Ambil List Kategori untuk Dropdown Navbar
        List<String> listKategori = bukuRepository.findAllKategori();
        model.addAttribute("listKategori", listKategori);

        // 2. Logic Pencarian
        List<Buku> books;
        boolean searchMode = false;
        String judulSection = "Koleksi Buku";

        if (keyword != null && !keyword.isEmpty()) {
            books = bukuRepository.findByJudulContainingIgnoreCaseOrPenulisContainingIgnoreCase(keyword, keyword);
            searchMode = true;
            judulSection = "Hasil Pencarian: " + keyword;
        } else if (kategori != null && !kategori.isEmpty()) {
            books = bukuRepository.findByKategori(kategori);
            searchMode = true;
            judulSection = "Kategori: " + kategori;
        } else {
            // Default: Ambil semua buku
            books = bukuRepository.findAll();
        }

        model.addAttribute("books", books);
        model.addAttribute("searchMode", searchMode);
        model.addAttribute("judulSection", judulSection);

        List<Buku> semuaBuku = bukuRepository.findAll(); 

        // 3. Logic Slider (Populer - Limit 10)
        List<Buku> listPopuler = new ArrayList<>(semuaBuku);
        Collections.shuffle(listPopuler);
        model.addAttribute("bukuPopuler", listPopuler.stream()
                .limit(10)
                .collect(Collectors.toList()));

        // 4. Logic Slider (Terbaru - Limit 10)
        List<Buku> listTerbaru = new ArrayList<>(semuaBuku);
        Collections.reverse(listTerbaru);
        model.addAttribute("bukuTerbaru", listTerbaru.stream()
                .limit(10)
                .collect(Collectors.toList()));

        // 5. Logic Slider (Rekomendasi - Limit 10)
        List<Buku> listRekomendasi = new ArrayList<>(semuaBuku);
        Collections.shuffle(listRekomendasi);
        model.addAttribute("rekomendasi", listRekomendasi.stream()
                .limit(10)
                .collect(Collectors.toList()));

        return "katalog";
    }

    @GetMapping("/buku/{id}")
    public String detailBuku(@PathVariable Long id, Model model, Authentication authentication) {
        Buku buku = bukuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Buku tidak ditemukan"));
        model.addAttribute("buku", buku);
        
        //LOGIC CEK STATUS BOOKING USER
        boolean sedangDibookingUser = false;
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Pengguna user = penggunaRepository.findByUsername(username).orElse(null);
            
            if (user != null) {
                List<Peminjaman> bookings = peminjamanRepository.findByPengguna(user);
                
                for (Peminjaman p : bookings) {
                    if (p.getStatus().equals("DIBOOKING")) {
                        for (DetailPeminjaman dp : p.getDetailPeminjamanList()) {
                            if (dp.getBuku().getId().equals(id)) {
                                sedangDibookingUser = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        model.addAttribute("sedangDibookingUser", sedangDibookingUser);

        List<Buku> otherBooks = bukuRepository.findAll();
        Collections.shuffle(otherBooks);
        model.addAttribute("rekomendasi", otherBooks.stream().limit(4).collect(Collectors.toList()));

        return "detail_buku";
    }
}