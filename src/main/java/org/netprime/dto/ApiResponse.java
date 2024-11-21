package org.netprime.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private Map<String, Object> extraData = null;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
