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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private BukuRepository bukuRepository;
    @Autowired private PenggunaRepository penggunaRepository;
    @Autowired private PeminjamanRepository peminjamanRepository;
    @Autowired private DetailPeminjamanRepository detailPeminjamanRepository;

    // 1. DASHBOARD
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Buku> allBooks = bukuRepository.findAll();
        List<Peminjaman> allPeminjaman = peminjamanRepository.findAll();
        List<Pengguna> allAnggota = penggunaRepository.findByPeran("ANGGOTA");

        model.addAttribute("totalBuku", allBooks.size());
        model.addAttribute("totalPeminjaman", allPeminjaman.size());
        model.addAttribute("totalAnggota", allAnggota.size());

        long dendaDibayar = 0;
        long potensiDenda = 0;
        int totalStokTersedia = 0;
        int totalStokDipinjam = 0;

        for (Buku b : allBooks) {
            if (b.getStok() != null) totalStokTersedia += b.getStok();
        }

        for (Peminjaman p : allPeminjaman) {
            if ("DIKEMBALIKAN".equals(p.getStatus()) && p.getDenda() != null) {
                dendaDibayar += p.getDenda();
            } else if ("DIPINJAM".equals(p.getStatus())) {
                potensiDenda += p.getEstimasiDenda();
                if (p.getDetailPeminjamanList() != null) {
                    totalStokDipinjam += p.getDetailPeminjamanList().size();
                }
            }
        }
        
        model.addAttribute("dendaDibayar", dendaDibayar);
        model.addAttribute("potensiDenda", potensiDenda);
        model.addAttribute("jmlDipinjam", totalStokDipinjam);
        model.addAttribute("jmlTersedia", totalStokTersedia);

        // Grafik Tren Peminjaman
        Map<String, Integer> chartMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate d = today.minusMonths(i);
            String key = d.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            chartMap.put(key, 0);
        }
        for (Peminjaman p : allPeminjaman) {
            String key = p.getTanggalPinjam().format(DateTimeFormatter.ofPattern("MMM yyyy"));
            if (chartMap.containsKey(key)) {
                chartMap.put(key, chartMap.get(key) + 1);
            }
        }
        model.addAttribute("chartLabels", chartMap.keySet());
        model.addAttribute("chartValues", chartMap.values());

        return "admin/dashboard";
    }

    // 2. MANAJEMEN BUKU
    @GetMapping("/buku")
    public String daftarBuku(Model model) {
        model.addAttribute("listBuku", bukuRepository.findAll());
        return "admin/data_buku";
    }

    @GetMapping("/buku/tambah")
    public String formTambahBuku(Model model) {
        model.addAttribute("buku", new Buku());
        return "admin/tambah_buku";
    }

    @GetMapping("/buku/edit/{id}")
    public String formEditBuku(@PathVariable Long id, Model model) {
        Buku buku = bukuRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID Buku salah: " + id));
        model.addAttribute("buku", buku);
        return "admin/tambah_buku"; 
    }
    
    @GetMapping("/buku/hapus/{id}")
    public String hapusBuku(@PathVariable Long id) {
        try { bukuRepository.deleteById(id); } catch (Exception e) {}
        return "redirect:/admin/buku";
    }

    @PostMapping("/buku/simpan")
    public String simpanBuku(@ModelAttribute("buku") Buku buku,
                             @RequestParam("fileGambar") MultipartFile file,
                             @RequestParam(value = "linkCover", required = false) String linkCover) {
        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                buku.setGambar("data:image/jpeg;base64," + base64Image);
            } else if (linkCover != null && !linkCover.isEmpty()) {
                try (InputStream in = URI.create(linkCover).toURL().openStream()) {
                    byte[] bytes = in.readAllBytes();
                    String base64Image = Base64.getEncoder().encodeToString(bytes);
                    buku.setGambar("data:image/jpeg;base64," + base64Image);
                } catch (Exception e) {}
            }
            if (buku.getSedangDipinjam() == null) buku.setSedangDipinjam(false);
            bukuRepository.save(buku);
        } catch (IOException e) { e.printStackTrace(); }
        return "redirect:/admin/buku";
    }

    // 3. MANAJEMEN TRANSAKSI (PEMINJAMAN) - UPDATE TERBARU

    // A. LIST TRANSAKSI
    @GetMapping({"/peminjaman", "/transaksi"}) 
    public String daftarPeminjaman(Model model) {
        List<Peminjaman> loans = peminjamanRepository.findAll();
        model.addAttribute("listPeminjaman", loans);
        return "admin/data_peminjaman"; 
    }

    // B. AMBIL BUKU (BOOKING -> DIPINJAM)
    @PostMapping("/transaksi/ambil")
    public String prosesAmbilBuku(@RequestParam("id") Long id,
                                  @RequestParam("tenggatWaktu") String tenggatWaktuStr) {
        Peminjaman p = peminjamanRepository.findById(id).orElse(null);
        if (p != null && "DIBOOKING".equals(p.getStatus())) {
            p.setStatus("DIPINJAM");
            p.setTanggalPinjam(LocalDate.now());
            
            // Set Tenggat Waktu dari Modal
            if (tenggatWaktuStr != null && !tenggatWaktuStr.isEmpty()) {
                p.setTenggatWaktu(LocalDate.parse(tenggatWaktuStr));
            } else {
                p.setTenggatWaktu(LocalDate.now().plusDays(7));
            }
            peminjamanRepository.save(p);
        }
        return "redirect:/admin/peminjaman";
    }

    // C. KEMBALI BUKU (DIPINJAM -> DIKEMBALIKAN)
    @PostMapping("/transaksi/selesai")
    public String prosesKembaliBuku(@RequestParam("id") Long id) {
        Peminjaman p = peminjamanRepository.findById(id).orElse(null);
        if (p != null && "DIPINJAM".equals(p.getStatus())) {
            p.setStatus("DIKEMBALIKAN");
            p.setTanggalKembali(LocalDate.now());
            p.setDenda(p.getEstimasiDenda()); // Simpan denda kalau ada

            // Balikin Stok Buku
            for (DetailPeminjaman dp : p.getDetailPeminjamanList()) {
                Buku buku = dp.getBuku();
                buku.setStok(buku.getStok() + 1);
                buku.setSedangDipinjam(false); 
                bukuRepository.save(buku);
            }
            peminjamanRepository.save(p);
        }
        return "redirect:/admin/peminjaman";
    }

    // D. FORM TAMBAH MANUAL
    @GetMapping("/transaksi/tambah")
    public String formTambahPeminjaman(Model model) {
        model.addAttribute("peminjaman", new Peminjaman());
        model.addAttribute("listAnggota", penggunaRepository.findByPeran("ANGGOTA"));
        model.addAttribute("listBuku", bukuRepository.findByStokGreaterThanOrderByJudulAsc(0));
        return "admin/tambah_peminjaman";
    }

    // E. SIMPAN MANUAL
    @PostMapping("/transaksi/simpan")
    public String simpanPeminjamanManual(@ModelAttribute("peminjaman") Peminjaman peminjaman,
                                         @RequestParam("idPengguna") Long idPengguna,
                                         @RequestParam("idBuku") List<Long> listIdBuku) {
        Pengguna user = penggunaRepository.findById(idPengguna).orElse(null);
        peminjaman.setPengguna(user);
        peminjaman.setTanggalPinjam(LocalDate.now());
        peminjaman.setStatus("DIPINJAM");
        peminjaman.setDenda(0L);
        if (peminjaman.getTenggatWaktu() == null) peminjaman.setTenggatWaktu(LocalDate.now().plusDays(7));
        
        Peminjaman saved = peminjamanRepository.save(peminjaman);

        if (listIdBuku != null) {
            for (Long idBuku : listIdBuku) {
                Buku buku = bukuRepository.findById(idBuku).orElse(null);
                if (buku != null && buku.getStok() > 0) {
                    buku.setStok(buku.getStok() - 1);
                    bukuRepository.save(buku);

                    DetailPeminjaman dp = new DetailPeminjaman();
                    dp.setPeminjaman(saved);
                    dp.setBuku(buku);
                    detailPeminjamanRepository.save(dp);
                }
            }
        }
        return "redirect:/admin/peminjaman";
    }

    // 4. MANAJEMEN ANGGOTA
    @GetMapping("/anggota")
    public String dataAnggota(Model model) {
        model.addAttribute("listUser", penggunaRepository.findAll());
        return "admin/data_anggota"; 
    }

    @GetMapping("/anggota/hapus/{id}")
    public String hapusAnggota(@PathVariable Long id) {
        if (id != 1) { 
            try { penggunaRepository.deleteById(id); } catch (Exception e) {}
        }
        return "redirect:/admin/anggota";
    }
}