package com.perpustakaan.library.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "buku")
public class Buku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String judul;

    @Column(nullable = false)
    private String penulis;
    
    @Column(length = 255) 
    private String penerbit;

    @Column(name = "tahun_terbit", nullable = false)
    private Integer tahunTerbit;

    @Column(name = "sedang_dipinjam", nullable = false)
    private Boolean sedangDipinjam;

    @Column(length = 50)
    private String kategori;

    @Column(columnDefinition = "LONGTEXT")
    private String sinopsis;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String gambar;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "halaman")
    private Integer halaman;

    @Column(name = "stok")
    private Integer stok;

    
}