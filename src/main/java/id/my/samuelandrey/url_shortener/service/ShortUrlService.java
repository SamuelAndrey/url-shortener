package id.my.samuelandrey.url_shortener.service;

import id.my.samuelandrey.url_shortener.entity.ShortUrl;
import id.my.samuelandrey.url_shortener.model.request.CreateShortUrlRequest;
import id.my.samuelandrey.url_shortener.model.response.ShortUrlResponse;
import id.my.samuelandrey.url_shortener.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import static id.my.samuelandrey.url_shortener.utility.UrlConstant.CHARACTER_LENGTH;
import static id.my.samuelandrey.url_shortener.utility.UrlConstant.MAX_RETRY;

@Service
public class ShortUrlService {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    private final SecureRandom random = new SecureRandom();


    @Transactional
    public ShortUrlResponse create(CreateShortUrlRequest request) {

        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {

            String generateCode = generateCode();

            ShortUrl shortUrl = new ShortUrl();
            shortUrl.setOriginalUrl(request.getUrl());
            shortUrl.setShortCode(generateCode);
            shortUrl.setClickCount(0L);
            shortUrl.setCreatedAt(LocalDateTime.now());
            shortUrl.setExpiredAt(LocalDateTime.now().plusYears(100));

            try {

                ShortUrl savedShortUrl = shortUrlRepository.saveAndFlush(shortUrl);

                return ShortUrlResponse.builder()
                        .id(savedShortUrl.getId())
                        .shortCode(savedShortUrl.getShortCode())
                        .originalUrl(savedShortUrl.getOriginalUrl())
                        .shortUrl(baseUrl + "/api/urls/" + shortUrl.getShortCode())
                        .expiredAt(shortUrl.getExpiredAt())
                        .build();

            } catch (DataIntegrityViolationException exception) {

                if (isShortCodeCollision(exception)) {
                    continue;
                }

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()
                );
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generated shorten URL."
        );
    }


    @Transactional
    public ShortUrlResponse getByShortCode(String shortCode) {

        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Shorten URL not found.")
                );

        if (LocalDateTime.now().isAfter(shortUrl.getExpiredAt())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Shorten URL expired."
            );
        }

        shortUrlRepository.incrementClickCount(shortCode);

        ShortUrl updatedShortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shorten URL not found."
                ));

        return ShortUrlResponse.builder()
                .id(updatedShortUrl.getId())
                .shortCode(updatedShortUrl.getShortCode())
                .originalUrl(updatedShortUrl.getOriginalUrl())
                .shortUrl(baseUrl + "/api/urls/" + updatedShortUrl.getShortCode())
                .clickCount(updatedShortUrl.getClickCount())
                .expiredAt(updatedShortUrl.getExpiredAt())
                .build();
    }

    /**
     * ====================================
     * PRIVATE HELPER
     * ====================================
     *
     * <p>Create new private helper above
     * contain all private helper.</p>
     */


    private String generateCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "0123456789";

        StringBuilder result = new StringBuilder(CHARACTER_LENGTH);

        for (int i = 0; i < CHARACTER_LENGTH; i++) {
            int index = random.nextInt(characters.length());
            result.append(characters.charAt(index));
        }

        return result.toString();
    }


    private boolean isShortCodeCollision(DataIntegrityViolationException exception) {

        // TODO: REGISTER OTHER COLLISION

        return true;
    }
}
