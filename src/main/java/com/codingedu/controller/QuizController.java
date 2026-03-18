package com.codingedu.controller;

import com.codingedu.entity.Choice;
import com.codingedu.entity.Question;
import com.codingedu.entity.Quiz;
import com.codingedu.entity.QuizResult;
import com.codingedu.entity.User;
import com.codingedu.security.CustomUserDetails;
import com.codingedu.service.QuizService;
import com.codingedu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    public QuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    // 1. 퀴즈 목록
    @GetMapping("/quiz")
    public String list(@RequestParam(name = "difficulty", defaultValue = "all") String difficulty,
                       Model model) {
        model.addAttribute("quizzes", quizService.getQuizzesByDifficulty(difficulty));
        model.addAttribute("currentDifficulty", difficulty);
        return "quiz";
    }

    // 2. 퀴즈 풀기 페이지 (로그인 필요)
    @GetMapping("/quiz/{id}")
    public String take(@PathVariable(name = "id") Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("quiz", quizService.getQuizById(id));
        return "quiz-take";
    }

    // 3. 퀴즈 제출
    @PostMapping("/quiz/{id}/submit")
    public String submit(@PathVariable(name = "id") Long id,
                         HttpServletRequest request,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        Map<Long, Long> userAnswers = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (key.startsWith("answer_") && values.length > 0) {
                try {
                    Long questionId = Long.parseLong(key.substring(7));
                    Long choiceId = Long.parseLong(values[0]);
                    userAnswers.put(questionId, choiceId);
                } catch (NumberFormatException ignored) {}
            }
        });

        User user = userService.findByUsername(userDetails.getUsername());
        QuizResult result = quizService.submitQuiz(id, userAnswers, user);
        redirectAttributes.addFlashAttribute("userAnswers", userAnswers);
        return "redirect:/quiz/" + id + "/result/" + result.getId();
    }

    // 4. 퀴즈 결과 페이지
    @GetMapping("/quiz/{id}/result/{resultId}")
    public String result(@PathVariable(name = "id") Long id,
                         @PathVariable(name = "resultId") Long resultId,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {
        if (userDetails == null) return "redirect:/login";

        QuizResult quizResult = quizService.getResultById(resultId);
        if (!quizResult.getUser().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/quiz";
        }

        Quiz quiz = quizService.getQuizById(id);

        // flash attribute에서 userAnswers 읽기 (없으면 null)
        @SuppressWarnings("unchecked")
        Map<Long, Long> userAnswers = (Map<Long, Long>) model.getAttribute("userAnswers");

        // 템플릿에서 사용하기 쉬운 뷰 모델 생성
        List<QuestionView> questionViews = buildQuestionViews(quiz, userAnswers);

        model.addAttribute("quizResult", quizResult);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questionViews", questionViews);
        return "quiz-result";
    }

    // ── 헬퍼: 문제별 채점 뷰 모델 ───────────────────────────────────
    private List<QuestionView> buildQuestionViews(Quiz quiz, Map<Long, Long> userAnswers) {
        List<QuestionView> views = new ArrayList<>();
        for (Question question : quiz.getQuestions()) {
            Long selectedId = (userAnswers != null) ? userAnswers.get(question.getId()) : null;
            boolean isCorrect = false;
            List<ChoiceView> choiceViews = new ArrayList<>();
            for (Choice choice : question.getChoices()) {
                boolean selected = choice.getId().equals(selectedId);
                if (selected && choice.isCorrect()) isCorrect = true;
                String status;
                if (choice.isCorrect()) status = "correct";
                else if (selected) status = "wrong-selected";
                else status = "neutral";
                choiceViews.add(new ChoiceView(choice, status));
            }
            String cardStatus = (selectedId == null) ? "unanswered" : (isCorrect ? "correct" : "wrong");
            views.add(new QuestionView(question, choiceViews, cardStatus, isCorrect));
        }
        return views;
    }

    // ── 뷰 모델 레코드 ────────────────────────────────────────────────
    public record QuestionView(Question question, List<ChoiceView> choices, String status, boolean correct) {}
    public record ChoiceView(Choice choice, String status) {}
}
