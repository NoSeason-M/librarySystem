package com.library.librarysystem.service;

import com.library.librarysystem.dto.auth.CaptchaResponse;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory captcha service (no Redis needed).
 * Can be swapped to Redis later by replacing the ConcurrentHashMap store.
 */
@Service
public class CaptchaService {

    private final ConcurrentHashMap<String, String> captchaStore = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long CAPTCHA_TTL_MS = 5 * 60 * 1000L; // 5 min
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public CaptchaResponse generateCaptcha() {
        String code = generateCode(4);
        String key = java.util.UUID.randomUUID().toString();
        String imageBase64 = generateImage(code);

        captchaStore.put(key, code);
        // Schedule expiry — simple approach: check in a cleanup thread
        scheduleCleanup(key);

        return new CaptchaResponse(key, "data:image/png;base64," + imageBase64);
    }

    public boolean validateCaptcha(String key, String code) {
        if (key == null || code == null) return false;
        String expected = captchaStore.get(key);
        if (expected == null) return false;
        boolean valid = expected.equalsIgnoreCase(code.trim());
        if (valid) {
            captchaStore.remove(key); // one-time use
        }
        return valid;
    }

    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String generateImage(String code) {
        try {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // Background
            g.setColor(Color.decode("#E8F4FD"));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // Noise lines
            g.setColor(Color.decode("#4A9FD8"));
            g.setStroke(new BasicStroke(1.5f));
            for (int i = 0; i < 3; i++) {
                int x1 = RANDOM.nextInt(WIDTH);
                int y1 = RANDOM.nextInt(HEIGHT);
                int x2 = RANDOM.nextInt(WIDTH);
                int y2 = RANDOM.nextInt(HEIGHT);
                g.drawLine(x1, y1, x2, y2);
            }

            // Text
            g.setColor(Color.decode("#4A9FD8"));
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(code);
            int x = (WIDTH - textWidth) / 2;
            int y = (HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(code, x, y);

            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate captcha image", e);
        }
    }

    private void scheduleCleanup(String key) {
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(CAPTCHA_TTL_MS);
                captchaStore.remove(key);
            } catch (InterruptedException ignored) {}
        });
    }
}
