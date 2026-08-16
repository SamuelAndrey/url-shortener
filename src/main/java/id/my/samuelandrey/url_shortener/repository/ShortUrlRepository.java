package id.my.samuelandrey.url_shortener.repository;


import id.my.samuelandrey.url_shortener.entity.ShortUrl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE ShortUrl s
        SET s.clickCount = s.clickCount + 1
        WHERE s.shortCode = :shortCode
        """)
    void incrementClickCount(@Param("shortCode") String shortCode);


    @Query(value = """
        SELECT *
        FROM short_urls s
        WHERE s.expired_at > CURRENT_TIMESTAMP
        AND (
            :shortCode IS NULL
            OR s.short_code LIKE CONCAT('%', :shortCode, '%')
        )
        AND (
            :originalUrl IS NULL
            OR s.original_url LIKE CONCAT('%', :originalUrl, '%')
        )
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM short_urls s
        WHERE s.expired_at > CURRENT_TIMESTAMP
        AND (
            :shortCode IS NULL
            OR s.short_code LIKE CONCAT('%', :shortCode, '%')
        )
        AND (
            :originalUrl IS NULL
            OR s.original_url LIKE CONCAT('%', :originalUrl, '%')
        )
        """,
        nativeQuery = true
    )
    Page<ShortUrl> listShortUrl(
            Pageable pageable,
            @Param("shortCode") String shortCode,
            @Param("originalUrl") String originalUrl
    );
}
