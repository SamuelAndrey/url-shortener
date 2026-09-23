package id.my.samuelandrey.url_shortener.controller;

import id.my.samuelandrey.url_shortener.model.response.GeneralBodyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<GeneralBodyResponse> apiException(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(
                        GeneralBodyResponse.builder()
                                .code(exception.getStatusCode().value())
                                .status(exception.getBody().getTitle())
                                .message(exception.getReason())
                                .build()
                );
    }
}
