package com.ShortStuff.service;

import com.ShortStuff.entity.Url;
import com.ShortStuff.repository.UrlRepository;
import com.ShortStuff.util.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlService {

    private final UrlRepository urlRepository;

    @Value("${app.default-expiration-minutes}")
    private long defaultExpirationMinutes;

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public String shorten(String longUrl, Long expirationMinutes) {
        long expiry = (expirationMinutes != null) ? expirationMinutes : defaultExpirationMinutes;

        Optional<Url> existing = urlRepository.findByLongUrlAndExpiresAtAfter(longUrl, LocalDateTime.now());

        if(existing.isPresent()) {
            Url url = existing.get();
            url.setExpiresAt(LocalDateTime.now().plusMinutes(expiry));
            urlRepository.save(url);
            return baseUrl + "/" + url.getCode();
        } else {
            Url url = new Url();
            url.setLongUrl(longUrl);
            url.setCode("placeholder");
            url.setExpiresAt(LocalDateTime.now().plusMinutes(expiry));
            url.setClickCount(0);

            Url saved = urlRepository.save(url);

            String code = Base62.encode(saved.getId());
            saved.setCode(code);
            urlRepository.save(saved);

            return baseUrl + "/" + code;
        }
    }

    @Transactional
    public String resolve(String code) {
        Url url = urlRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found: " + code));

        if (url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Short URL has expired: " + code);
        }

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getLongUrl();
    }

    @Transactional
    public String updateExpiration(String code, long expirationMinutes) {
        Url url = urlRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found: " + code));

        url.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        urlRepository.save(url);

        return baseUrl + "/" + url.getCode();
    }

    @Scheduled(fixedRateString = "${app.cleanup-interval-ms:60000}")
    @Transactional
    public void deleteExpiredUrls() {
        urlRepository.deleteAllExpired(LocalDateTime.now());
    }
}