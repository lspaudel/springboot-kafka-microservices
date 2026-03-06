package net.kafkajava.base_domains.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static ApiResponse ok(String message, Object data) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse error(String message, Object data) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}
