package edu.project.secure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @NotBlank(message = "message must not be blank")
        @Size(max = 1000, message = "message must be 1000 characters or fewer")
        String message
) {}
