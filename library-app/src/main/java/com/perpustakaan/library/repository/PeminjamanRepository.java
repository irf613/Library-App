package com.perpustakaan.library.repository;

import com.perpustakaan.library.entity.Peminjaman;
import com.perpustakaan.library.entity.Pengguna;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PeminjamanRepository extends JpaRepository<Peminjaman, Long> {
    List<Peminjaman> findByPengguna(Pengguna pengguna);
    Peminjaman findFirstByPenggunaIdOrderByTanggalPinjamDesc(Long idPengguna);
    List<Peminjaman> findByPenggunaOrderByTanggalPinjamDesc(Pengguna pengguna);
}