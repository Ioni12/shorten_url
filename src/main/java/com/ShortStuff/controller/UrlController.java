package com.ShortStuff.controller;

import com.ShortStuff.service.UrlService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<String> shorten(
            @RequestBody @Valid ShortenRequest request) {
        String shortUrl = urlService.shorten(request.longUrl(), request.expirationMinutes());
        return ResponseEntity.ok(shortUrl);
    }

    @PatchMapping("/api/urls/{code}/expiration")
    public ResponseEntity<String> updateExpiration(
            @PathVariable String code,
            @RequestParam @Min(1) long minutes) {
        String shortUrl = urlService.updateExpiration(code, minutes);
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String longUrl = urlService.resolve(code);
        return ResponseEntity
                .status(302)
                .header("Location", longUrl)
                .build();
    }

    public record ShortenRequest(
            @NotBlank(message = "URL must not be blank")
            String longUrl,
            Long expirationMinutes  // optional, null = use default
    ) {}
}