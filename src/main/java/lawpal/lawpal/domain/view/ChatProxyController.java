package lawpal.lawpal.domain.view;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/ui")
public class ChatProxyController {

    private final RestClient restClient;
    private final String aiBaseUrl;

    public ChatProxyController(
            @Value("${taxhelper.ai.base-url:http://127.0.0.1:8000}") String aiBaseUrl
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(180_000);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
        this.aiBaseUrl = aiBaseUrl;
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chat(@RequestBody ChatUiRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().body("{\"message\":\"질문을 입력해 주세요.\"}");
        }

        int topK = request.topK() == null ? 5 : Math.max(1, Math.min(request.topK(), 20));
        String response = restClient.post()
                .uri(aiBaseUrl + "/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("question", request.question().trim(), "top_k", topK))
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    public record ChatUiRequest(String question, Integer topK) {
    }
}
