package com.perpustakaan.library.repository;

import com.perpustakaan.library.entity.Pengguna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List; 

public interface PenggunaRepository extends JpaRepository<Pengguna, Long> {
    // Method ajaib: Spring otomatis bikin query "SELECT * FROM pengguna WHERE username = ?"
    Optional<Pengguna> findByUsername(String username);
    List<Pengguna> findByPeran(String peran);
}