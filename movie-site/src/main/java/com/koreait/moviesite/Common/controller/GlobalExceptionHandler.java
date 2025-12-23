package com.koreait.moviesite.Common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ✅ Spring에서 "업로드 용량 초과"로 자주 떨어지는 예외
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUpload(MaxUploadSizeExceededException ex, RedirectAttributes ra) {
        log.warn("[Upload] MaxUploadSizeExceededException: {}", ex.getMessage());
        ra.addAttribute("uploadError", "tooLarge");
        return "redirect:/board/write";
    }

    // ✅ 일부 환경에서는 MultipartException으로 한번 감싸져서 떨어짐
    @ExceptionHandler(MultipartException.class)
    public String handleMultipart(MultipartException ex, RedirectAttributes ra) {
        log.warn("[Upload] MultipartException: {}", ex.getMessage());
        ra.addAttribute("uploadError", "tooLarge");
        return "redirect:/board/write";
    }
}
