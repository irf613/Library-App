package com.perpustakaan.library.repository;

import com.perpustakaan.library.entity.Pengembalian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PengembalianRepository extends JpaRepository<Pengembalian, Long> {
}