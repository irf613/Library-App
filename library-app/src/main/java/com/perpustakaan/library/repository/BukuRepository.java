package com.perpustakaan.library.repository;

import com.perpustakaan.library.entity.Buku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BukuRepository extends JpaRepository<Buku, Long> {

    // --- 1. METHOD BARU (SOLUSI ERROR) ---
    // Ini yang dicari sama Controller: Cari Judul ATAU Penulis sekaligus
    List<Buku> findByJudulContainingIgnoreCaseOrPenulisContainingIgnoreCase(String judul, String penulis);

    // --- 2. METHOD LAMA ABANG (TETAP DISIMPAN) ---
    List<Buku> findByJudulContainingIgnoreCase(String keyword);
    List<Buku> findByPenulisContainingIgnoreCase(String keyword);
    
    List<Buku> findTop5ByOrderByIdDesc(); // Bisa dipake buat "Terbaru"
    
    List<Buku> findByKategori(String kategori);
    List<Buku> findTop4ByKategori(String kategori);
    
    List<Buku> findBySedangDipinjamFalse();

    // Query Random (Bagus buat rekomendasi)
    @Query(value = "SELECT * FROM buku ORDER BY RAND() LIMIT 4", nativeQuery = true)
    List<Buku> findRandomBooks();

    // Query Populer Beneran (Berdasarkan jumlah peminjaman)
    @Query("SELECT dp.buku FROM DetailPeminjaman dp GROUP BY dp.buku ORDER BY COUNT(dp) DESC")
    List<Buku> findBukuPopuler(Pageable pageable);

    // Query Kategori Unik (Buat Dropdown Navbar)
    @Query("SELECT DISTINCT b.kategori FROM Buku b WHERE b.kategori IS NOT NULL")
    List<String> findAllKategori();
    List<Buku> findByStokGreaterThanOrderByJudulAsc(Integer stok);
    
}