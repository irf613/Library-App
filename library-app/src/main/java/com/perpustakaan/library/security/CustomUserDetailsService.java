package com.perpustakaan.library.security;

import com.perpustakaan.library.entity.Pengguna;
import com.perpustakaan.library.repository.PenggunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Cari user di database
        Pengguna user = penggunaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username atau password salah"));

        // Set Role default USER kalau kosong
        String role = (user.getPeran() == null) ? "USER" : user.getPeran();

        // Return object User bawaan Spring Security
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(role)
                .build();
    }
}