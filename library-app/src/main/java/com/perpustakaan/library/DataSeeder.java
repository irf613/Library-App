package com.perpustakaan.library;

import com.perpustakaan.library.entity.Pengguna;
import com.perpustakaan.library.repository.PenggunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Cek apakah database pengguna kosong?
        if (penggunaRepository.count() == 0) {
            
            System.out.println("⏳ Sedang membuat data user dummy...");

            // 1. BUAT AKUN ADMIN
            Pengguna admin = new Pengguna();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); 
            admin.setNamaLengkap("Administrator Perpus");
            admin.setNoTelepon("081299999999");
            admin.setPeran("ADMIN");
            admin.setEmail("admin@perpus.com");
            
            // 2. BUAT AKUN ANGGOTA 1
            Pengguna budi = new Pengguna();
            budi.setUsername("budi");
            budi.setPassword(passwordEncoder.encode("budi123"));
            budi.setNamaLengkap("Budi Santoso");
            budi.setNoTelepon("081234567890");
            budi.setPeran("ANGGOTA");
            budi.setEmail("budi@gmail.com");

            // 3. BUAT AKUN ANGGOTA 2
            Pengguna siti = new Pengguna();
            siti.setUsername("siti");
            siti.setPassword(passwordEncoder.encode("siti123"));
            siti.setNamaLengkap("Siti Aminah");
            siti.setNoTelepon("085678901234");
            siti.setPeran("ANGGOTA");
            siti.setEmail("siti@gmail.com");

            // 4. BUAT AKUN ANGGOTA 3
            Pengguna joko = new Pengguna();
            joko.setUsername("joko");
            joko.setPassword(passwordEncoder.encode("joko123"));
            joko.setNamaLengkap("Joko Anwar");
            joko.setNoTelepon("081345678901");
            joko.setPeran("ANGGOTA");
            joko.setEmail("joko@gmail.com");

            // Simpan Semuanya
            penggunaRepository.saveAll(Arrays.asList(admin, budi, siti, joko));

            System.out.println("✅ DATA USER BERHASIL DIBUAT!");
            System.out.println("-------------------------------------");
            System.out.println("Admin  : admin / admin123");
            System.out.println("User 1 : budi  / budi123");
            System.out.println("User 2 : siti  / siti123");
            System.out.println("User 3 : joko  / joko123");
            System.out.println("-------------------------------------");
        }
    }
}