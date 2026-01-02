package com.perpustakaan.library.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@RestController
public class IsbnController {

    @GetMapping("/api/buku/cari-isbn")
    public ResponseEntity<?> cariBukuByIsbn(@RequestParam String isbn) {
        try {
            // 1. Panggil Google Books API
            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            // 2. Parsing JSON dari Google
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Cek apakah buku ditemukan
            if (root.path("totalItems").asInt() == 0) {
                return ResponseEntity.badRequest().body("Buku tidak ditemukan di Google Books.");
            }

            // 3. Ambil data buku pertama
            JsonNode volumeInfo = root.path("items").get(0).path("volumeInfo");

            // 4. Masukkan ke Map biar rapi saat dikirim ke Frontend
            Map<String, Object> data = new HashMap<>();
            data.put("judul", volumeInfo.path("title").asText());
            data.put("penerbit", volumeInfo.path("publisher").asText("Tidak Diketahui"));
            data.put("tahun", volumeInfo.path("publishedDate").asText().substring(0, 4)); // Ambil tahun saja
            data.put("halaman", volumeInfo.path("pageCount").asInt(0));
            data.put("sinopsis", volumeInfo.path("description").asText());
            
            // Ambil Penulis
            if (volumeInfo.has("authors")) {
                StringBuilder penulis = new StringBuilder();
                for (JsonNode author : volumeInfo.path("authors")) {
                    if (penulis.length() > 0) penulis.append(", ");
                    penulis.append(author.asText());
                }
                data.put("penulis", penulis.toString());
            } else {
                data.put("penulis", "Tanpa Penulis");
            }

            //AMBIL KATEGORI
            if (volumeInfo.has("categories")) {
                data.put("kategori", volumeInfo.path("categories").get(0).asText());
            } else {
                data.put("kategori", "Umum");
            }

            //AMBIL COVER BUKU
            // Cek apakah ada link gambar
            if (volumeInfo.has("imageLinks")) {
                String coverUrl = volumeInfo.path("imageLinks").path("thumbnail").asText();
                // Google kadang kasih link 'http', ubah jadi 'https' biar aman
                coverUrl = coverUrl.replace("http://", "https://"); 
                data.put("cover", coverUrl);
            } else {
                data.put("cover", "");
            }
            
            return ResponseEntity.ok(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error saat mengambil data: " + e.getMessage());
        }
    }
}