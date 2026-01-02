package com.perpustakaan.library.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "detail_pengembalian")
public class DetailPengembalian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_pengembalian", nullable = false)
    private Pengembalian pengembalian;

    @ManyToOne
    @JoinColumn(name = "id_buku", nullable = false)
    private Buku buku;
}