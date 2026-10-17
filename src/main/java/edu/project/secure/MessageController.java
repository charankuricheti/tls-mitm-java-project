package edu.project.secure;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MessageController {

    @GetMapping("/health")
    public String health() {
        return "Secure HTTPS connection is working.";
    }

    @PostMapping("/messages")
    public String sendMessage(@Valid @RequestBody MessageRequest request) {
        return "Message received securely: " + request.message();
    }
}
