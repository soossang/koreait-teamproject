package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class MemberProfileImageService {

    private final MemberRepository memberRepository;
    private static final String UPLOAD_DIR = "uploads/profile-images";

    public MemberProfileImageService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String uploadProfileImage(Long memberId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = memberId + "_" + timestamp + "_" + UUID.randomUUID() + ext;

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("업로드 디렉토리를 생성할 수 없습니다.");
        }

        File dest = new File(dir, filename);
        file.transferTo(dest);

        String url = "/uploads/profile-images/" + filename;
        member.setProfileImageUrl(url);
        memberRepository.save(member);

        return url;
    }
}
