package id.my.samuelandrey.url_shortener.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortUrlResponse {

    private Long id;

    private String originalUrl;

    private String shortCode;

    private String shortUrl;

    private Long clickCount;

    private LocalDateTime expiredAt;

}
