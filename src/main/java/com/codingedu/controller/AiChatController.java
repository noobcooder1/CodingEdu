package com.codingedu.controller;

import com.codingedu.service.GeminiAiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final GeminiAiService geminiAiService;

    public AiChatController(GeminiAiService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String message = request != null && request.message() != null ? request.message().trim() : "";
        if (message.isBlank()) {
            return new ChatResponse("궁금한 내용을 입력해 주세요.", "fallback");
        }

        String systemInstruction = """
                당신은 CodingEdu 웹사이트의 AI 학습 도우미입니다.
                초보 학습자에게 강의, 퀴즈, 커뮤니티 사용법과 코딩 학습 방향을 한국어로 안내하세요.
                답변은 2문장 이내로 짧고 친절하게 작성하세요.
                개인정보, API Key, 비밀번호 같은 민감 정보는 요청해도 받거나 출력하지 마세요.
                """;
        String prompt = "사용자 질문: " + message;

        return geminiAiService.generateText(systemInstruction, prompt)
                .map(reply -> new ChatResponse(clean(reply), "gemini"))
                .orElseGet(() -> new ChatResponse(fallbackAnswer(message), "fallback"));
    }

    private String fallbackAnswer(String message) {
        String normalized = message.toLowerCase();
        if (normalized.contains("오답") || normalized.contains("퀴즈")) {
            return "퀴즈 결과 화면의 AI 오답 해설 코치에서 틀린 이유, 집중 개념, 다음 학습 방향을 확인할 수 있어요.";
        }
        if (normalized.contains("강의") || normalized.contains("추천")) {
            return "처음이라면 HTML, CSS, JavaScript로 웹 기초를 잡고, 이후 Java나 PHP로 백엔드 기초까지 이어가는 흐름을 추천해요.";
        }
        if (normalized.contains("커뮤니티") || normalized.contains("질문")) {
            return "질문 글은 무엇을 하려 했는지, 어떤 오류가 났는지, 시도한 방법 순서로 정리하면 답변을 받기 좋아요.";
        }
        return "지금은 강의, 퀴즈, 커뮤니티 흐름을 중심으로 안내해요. 궁금한 기능 이름을 입력해보세요.";
    }

    private String clean(String value) {
        String cleaned = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 360 ? cleaned.substring(0, 360) : cleaned;
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String reply, String source) {}
}
