package com.ShortStuff.controller;

import com.ShortStuff.repository.UrlRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class KeepAliveController {

    private final long startTime = System.currentTimeMillis();

    private final UrlRepository urlRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public KeepAliveController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    // GET /health
    @GetMapping("/health")
    public Map<String, Object> health() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax  = memoryBean.getHeapMemoryUsage().getMax();
        long uptimeMs = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("uptimeSeconds", uptimeMs / 1000);
        response.put("heapUsedMB", heapUsed / (1024 * 1024));
        response.put("heapMaxMB", heapMax / (1024 * 1024));
        response.put("heapUsedPercent", Math.round((double) heapUsed / heapMax * 100));
        response.put("systemLoadAverage", osBean.getSystemLoadAverage());
        response.put("db", checkDb());
        response.put("totalUrls", getTotalUrls());
        response.put("checkedAt", Instant.now().toString());

        return response;
    }

    // GET /time
    @GetMapping("/time")
    public Map<String, Object> time() {
        Instant now = Instant.now();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("utc", now.toString());
        response.put("unix", now.getEpochSecond());
        response.put("unixMs", now.toEpochMilli());
        response.put("formatted", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(now));
        response.put("uptimeSeconds", (System.currentTimeMillis() - startTime) / 1000);

        return response;
    }

    // Ping the DB with a lightweight native query
    private Map<String, Object> checkDb() {
        Map<String, Object> db = new LinkedHashMap<>();
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            db.put("status", "CONNECTED");
        } catch (Exception e) {
            db.put("status", "ERROR");
            db.put("error", e.getMessage());
        }
        return db;
    }

    // Useful metric specific to ShortStuff — total URLs in the system
    private long getTotalUrls() {
        try {
            return urlRepository.count();
        } catch (Exception e) {
            return -1;
        }
    }
}