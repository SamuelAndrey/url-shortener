package id.my.samuelandrey.url_shortener.controller;


import id.my.samuelandrey.url_shortener.model.request.CreateShortUrlRequest;
import id.my.samuelandrey.url_shortener.model.response.GeneralBodyResponse;
import id.my.samuelandrey.url_shortener.model.response.ShortUrlResponse;
import id.my.samuelandrey.url_shortener.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    @PostMapping(
            path = "/api/urls",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GeneralBodyResponse> create(
            @Valid @RequestBody CreateShortUrlRequest request
    ) {
        ShortUrlResponse response = shortUrlService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        GeneralBodyResponse.builder()
                            .code(HttpStatus.CREATED.value())
                            .status(HttpStatus.CREATED.getReasonPhrase())
                            .message("Successfully created short url.")
                            .data(response)
                            .build()
                );
    }

    @GetMapping(
            path = "api/urls/{shortCode}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GeneralBodyResponse> get(
            @Valid @PathVariable("shortCode") String shortCode
    ) {
        ShortUrlResponse response = shortUrlService.getByShortCode(shortCode);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        GeneralBodyResponse.builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK.getReasonPhrase())
                                .message("Successfully get short url.")
                                .data(response)
                                .build()
                );

    }


}
