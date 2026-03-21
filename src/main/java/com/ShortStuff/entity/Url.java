package com.ShortStuff.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private long clickCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getLongUrl() { return longUrl; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public long getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setClickCount(long clickCount) { this.clickCount = clickCount; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String code;
        private String longUrl;
        private LocalDateTime expiresAt;
        private long clickCount;

        public Builder code(String code) { this.code = code; return this; }
        public Builder longUrl(String longUrl) { this.longUrl = longUrl; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder clickCount(long clickCount) { this.clickCount = clickCount; return this; }

        public Url build() {
            Url url = new Url();
            url.code = this.code;
            url.longUrl = this.longUrl;
            url.expiresAt = this.expiresAt;
            url.clickCount = this.clickCount;
            return url;
        }
    }
}