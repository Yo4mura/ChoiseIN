package edu.college.choisein3.controller;

import edu.college.choisein3.model.TestResult;
import edu.college.choisein3.model.User;
import edu.college.choisein3.repository.TestResultRepository;
import edu.college.choisein3.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TestResultRepository testResultRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/log")
    public String showLoginPage() {
        return "log"; // templates/login.html
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "log"; // просто возвращает страницу регистрации
    }

    @GetMapping("/login")
    public String showLoginPageAlt() {
        return "log"; // возвращает ту же страницу входа
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        boolean success = authService.registerUser(name, email, password);
        model.addAttribute("registered", success);
        return success ? "redirect:/log" : "log";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
            @RequestParam String password,
            Model model) {
        return authService.login(email, password)
                .map(user -> {
                    // ✅ Передаем email через параметр, чтобы JS сохранил его в localStorage
                    return "redirect:/?email=" + user.getEmail();
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Неверный логин или пароль");
                    return "log";
                });
    }

    @GetMapping("/profile")
    public String showProfilePage(@RequestParam(value = "email", required = false) String email,
            Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/log";
        }

        Optional<User> userOpt = authService.findUserByEmail(email);
        if (userOpt.isEmpty()) {
            return "redirect:/log";
        }

        User user = userOpt.get();
        List<TestResult> results = testResultRepository.findByUser(user);

        model.addAttribute("userName", user.getName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("totalTests", results.size());

        // Вычисляем средние показатели за все тесты
        Map<String, Double> averageStats = new HashMap<>();
        int validResults = 0;
        for (TestResult result : results) {
            try {
                Map<String, Integer> moralScores = objectMapper.readValue(
                        result.getMoralScores(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {
                }
                );

                int totalScore = moralScores.values().stream().mapToInt(Integer::intValue).sum();
                if (totalScore > 0) {
                    validResults++;
                    for (Map.Entry<String, Integer> entry : moralScores.entrySet()) {
                        double percentage = (entry.getValue() * 100.0 / totalScore);
                        averageStats.put(entry.getKey(),
                                averageStats.getOrDefault(entry.getKey(), 0.0) + percentage);
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки парсинга
            }
        }

        // Вычисляем средние значения
        final int finalValidResults = validResults;
        List<Map<String, Object>> averageStatsList = new ArrayList<>();
        if (finalValidResults > 0) {
            averageStatsList = averageStats.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> stat = new HashMap<>();
                        stat.put("label", entry.getKey());
                        stat.put("value", String.format("%.0f", entry.getValue() / finalValidResults));
                        return stat;
                    })
                    .sorted((a, b) -> {
                        double valA = Double.parseDouble((String) a.get("value"));
                        double valB = Double.parseDouble((String) b.get("value"));
                        return Double.compare(valB, valA);
                    })
                    .limit(5)
                    .collect(Collectors.toList());
        }

        model.addAttribute("averageStats", averageStatsList);

        // Обрабатываем последние 5 результатов для отображения
        List<Map<String, Object>> recentResults = results.stream()
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
                .limit(5)
                .map(result -> {
                    Map<String, Object> resultData = new HashMap<>();
                    resultData.put("personalityType", result.getPersonalityType());
                    resultData.put("completedAt", result.getCompletedAt());

                    try {
                        // Парсим answers для определения количества вопросов
                        Map<String, String> answers = objectMapper.readValue(
                                result.getAnswers(),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                        }
                        );
                        resultData.put("questionsCount", answers.size());

                        // Парсим moralScores для вычисления процентов
                        Map<String, Integer> moralScores = objectMapper.readValue(
                                result.getMoralScores(),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {
                        }
                        );

                        // Вычисляем проценты
                        int totalScore = moralScores.values().stream().mapToInt(Integer::intValue).sum();

                        List<Map<String, Object>> topStats = moralScores.entrySet().stream()
                                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                .limit(3)
                                .map(entry -> {
                                    Map<String, Object> stat = new HashMap<>();
                                    stat.put("label", entry.getKey());
                                    // Вычисляем проценты вместо абсолютных значений
                                    double percentage = totalScore > 0 ? (entry.getValue() * 100.0 / totalScore) : 0;
                                    stat.put("value", String.format("%.0f", percentage));
                                    return stat;
                                })
                                .collect(Collectors.toList());

                        resultData.put("topStats", topStats);
                    } catch (Exception e) {
                        resultData.put("questionsCount", 0);
                        resultData.put("topStats", new ArrayList<>());
                    }

                    return resultData;
                })
                .collect(Collectors.toList());

        model.addAttribute("recentResults", recentResults);

        return "profile";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/log";
    }
}
