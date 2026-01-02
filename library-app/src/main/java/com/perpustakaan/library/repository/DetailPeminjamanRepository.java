package com.perpustakaan.library.repository;

import com.perpustakaan.library.entity.DetailPeminjaman;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetailPeminjamanRepository extends JpaRepository<DetailPeminjaman, Long> {
    List<DetailPeminjaman> findByPeminjamanId(Long peminjamanId);
}