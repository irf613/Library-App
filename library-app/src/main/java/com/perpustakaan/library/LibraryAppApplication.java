package com.perpustakaan.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

@SpringBootApplication
public class LibraryAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryAppApplication.class, args);
    }

    // --- FITUR ANTI-LIMIT UPLOAD ---
    // Ini pengganti settingan di application.properties yang error tadi.
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setProperty("maxParameterCount", "-1"); // Unlimited Parameter
                connector.setProperty("maxPostSize", "-1");       // Unlimited Ukuran File
                connector.setProperty("maxSwallowSize", "-1");    // Telan semua data
            });
        };
    }
}