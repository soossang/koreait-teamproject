package com.koreait.moviesite.RankingGenreBoard.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties에 없으면 기본 uploads/board 사용
    @Value("${app.upload.board-dir:uploads/board}")
    private String boardUploadDir;

    @PostConstruct
    public void ensureUploadDir() {
        try {
            Path dir = Paths.get(boardUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패: " + boardUploadDir, e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(boardUploadDir)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString(); // file:///C:/.../uploads/board/ 형태

        registry.addResourceHandler("/uploads/board/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
