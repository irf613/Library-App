package com.perpustakaan.library.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "detail_peminjaman")
public class DetailPeminjaman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi ke tabel Peminjaman (Foreign Key: id_peminjaman)
    @ManyToOne
    @JoinColumn(name = "id_peminjaman", nullable = false)
    private Peminjaman peminjaman;

    // Relasi ke tabel Buku (Foreign Key: id_buku)
    @ManyToOne
    @JoinColumn(name = "id_buku", nullable = false)
    private Buku buku;
}