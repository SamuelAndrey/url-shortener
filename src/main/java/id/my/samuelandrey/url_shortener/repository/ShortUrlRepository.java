package id.my.samuelandrey.url_shortener.repository;


import id.my.samuelandrey.url_shortener.entity.ShortUrl;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
}
