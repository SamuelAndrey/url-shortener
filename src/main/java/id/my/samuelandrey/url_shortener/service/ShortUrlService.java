package id.my.samuelandrey.url_shortener.service;

import id.my.samuelandrey.url_shortener.entity.ShortUrl;
import id.my.samuelandrey.url_shortener.model.request.CreateShortUrlRequest;
import id.my.samuelandrey.url_shortener.model.response.ShortUrlResponse;
import id.my.samuelandrey.url_shortener.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

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

                return toShortUrlResponse(savedShortUrl);

            } catch (DataIntegrityViolationException exception) {

                if (isShortCodeCollision(exception)) {
                    continue;
                }

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generated shorten URL.");
    }

    @Transactional
    public ShortUrlResponse getByShortCode(String shortCode) {

        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Shorten URL not found."));

        if (LocalDateTime.now().isAfter(shortUrl.getExpiredAt())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Shorten URL expired.");
        }

        shortUrlRepository.incrementClickCount(shortCode);

        ShortUrl updatedShortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shorten URL not found."));

        return toShortUrlResponse(updatedShortUrl);
    }

    @Transactional(readOnly = true)
    public Page<ShortUrlResponse> listShortUrl(
            Pageable pageable,
            String shortCode,
            String originalUrl
    ) {
        
        Page<ShortUrl> shortUrls = shortUrlRepository.listShortUrl(
                pageable,
                shortCode,
                originalUrl
        );

        List<ShortUrlResponse> shortUrlResponseList = shortUrls.getContent()
                .stream()
                .map(this::toShortUrlResponse)
                .toList();

        return new PageImpl<>(shortUrlResponseList, pageable, shortUrls.getTotalElements());
    }

    /**
     * ====================================
     * Private Helper
     * ====================================
     *
     * <p>Create new private helper above
     * contain all private helper.</p>
     */

    private ShortUrlResponse toShortUrlResponse(ShortUrl shortUrl) {
        return ShortUrlResponse.builder()
                .id(shortUrl.getId())
                .shortCode(shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .shortUrl(baseUrl + "/api/urls/" + shortUrl.getShortCode())
                .clickCount(shortUrl.getClickCount())
                .expiredAt(shortUrl.getExpiredAt())
                .build();
    }

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
