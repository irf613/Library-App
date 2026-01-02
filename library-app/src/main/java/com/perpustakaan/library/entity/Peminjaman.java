package com.perpustakaan.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "peminjaman")
public class Peminjaman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi ke tabel Pengguna
    @ManyToOne
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @Column(name = "tanggal_pinjam", nullable = false)
    private LocalDate tanggalPinjam;

    @Column(name = "tenggat_waktu")
    private LocalDate tenggatWaktu;

    @Column(name = "tanggal_kembali")
    private LocalDate tanggalKembali;

    @Column(name = "status")
    private String status;

    private Long denda = 0L;

    @OneToMany(mappedBy = "peminjaman", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<DetailPeminjaman> detailPeminjamanList;

    //FITUR HITUNG DENDA
    public Long getEstimasiDenda() {
        if ("DIKEMBALIKAN".equalsIgnoreCase(this.status)) {
            return this.denda == null ? 0L : this.denda;
        }
        
        if (this.tenggatWaktu != null) {
            long selisihHari = java.time.temporal.ChronoUnit.DAYS.between(this.tenggatWaktu, java.time.LocalDate.now());
            return selisihHari > 0 ? selisihHari * 1000 : 0L;
        }

        return 0L;
    }
}