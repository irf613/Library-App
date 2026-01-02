package com.perpustakaan.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "pengembalian")
public class Pengembalian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @Column(name = "tanggal_kembali", nullable = false)
    private LocalDate tanggalKembali;
}