package tn.esprit.spring.smartcity.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        Long userId = null;
        if (auth != null) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) userId = user.getId();
        }
        return ResponseEntity.ok(chatbotService.chat(body.get("message"), userId));
    }
}
