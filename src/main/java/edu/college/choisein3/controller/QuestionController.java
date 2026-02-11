package edu.college.choisein3.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import edu.college.choisein3.model.TestResult;
import edu.college.choisein3.model.User;
import edu.college.choisein3.repository.TestResultRepository;
import edu.college.choisein3.repository.UserRepository;
import edu.college.choisein3.service.MoralAnalysisService;

@RestController
@CrossOrigin(origins = "*")
public class QuestionController {

    @Autowired
    private MoralAnalysisService moralAnalysisService;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/questions")
    public List<Map<String, Object>> getQuestions() throws IOException {
        var resource = new ClassPathResource("data/questions_weighted.txt");
        String content = Files.readString(resource.getFile().toPath());

        String[] blocks = content.split("#\\s*\\d+");
        List<Map<String, Object>> questions = new ArrayList<>();

        Pattern qPattern = Pattern.compile("вопрос:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        Pattern oPattern = Pattern.compile("([A-D]):\\s*(.*?)\\s*\\[(.*?)\\]", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        for (String block : blocks) {
            Matcher qm = qPattern.matcher(block);
            if (!qm.find()) continue;

            Map<String, Object> q = new LinkedHashMap<>();
            q.put("question", qm.group(1).trim());

            Map<String, Object> options = new LinkedHashMap<>();
            Matcher om = oPattern.matcher(block);
            while (om.find()) {
                String letter = om.group(1).trim();
                String text = om.group(2).trim();
                String traitsRaw = om.group(3).trim();

                Map<String, Integer> moralWeights = moralAnalysisService.parseMoralWeights(traitsRaw);

                Map<String, Object> opt = new HashMap<>();
                opt.put("text", text);
                opt.put("moralWeights", moralWeights);
                options.put(letter, opt);
            }

            q.put("options", options);
            questions.add(q);
        }

        return questions;
    }

    @PostMapping("/submit-test")
    public Map<String, Object> submitTest(@RequestBody Map<String, Object> request) throws IOException {
        String userName = (String) request.get("userName");
        String userEmail = (String) request.get("userEmail"); // Если пользователь авторизован

        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) request.get("answers");

        // Получаем данные вопросов
        Map<String, Map<String, Map<String, Integer>>> questionsData = getQuestionsData();

        // Анализируем
        Map<String, Integer> moralScores = moralAnalysisService.calculateMoralScores(answers, questionsData);
        Map<String, Double> percentages = moralAnalysisService.calculatePercentages(moralScores);
        String personalityType = moralAnalysisService.determinePersonalityType(percentages);

        // Проверяем, вошёл ли пользователь
        User user = null;
        if (userEmail != null && !userEmail.isEmpty()) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        // Сохраняем результат только если пользователь авторизован
        if (user != null) {
            TestResult result = new TestResult();
            result.setUser(user);
            result.setGuestName(userName);
            result.setAnswers(objectMapper.writeValueAsString(answers));
            result.setMoralScores(objectMapper.writeValueAsString(moralScores));
            result.setPersonalityType(personalityType);
            result.setCompletedAt(LocalDateTime.now());
            testResultRepository.save(result);
        }

        // Ответ на фронтенд
        Map<String, Object> response = new HashMap<>();
        response.put("personalityType", personalityType);
        response.put("moralScores", moralScores);
        response.put("percentages", percentages);
        response.put("success", true);

        return response;
    }

    private Map<String, Map<String, Map<String, Integer>>> getQuestionsData() throws IOException {
        var resource = new ClassPathResource("data/questions_weighted.txt");
        String content = Files.readString(resource.getFile().toPath());

        Map<String, Map<String, Map<String, Integer>>> questionsData = new HashMap<>();

        String[] blocks = content.split("#\\s*\\d+");
        Pattern oPattern = Pattern.compile("([A-D]):\\s*(.*?)\\s*\\[(.*?)\\]", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < blocks.length; i++) {
            String questionId = String.valueOf(i);
            Map<String, Map<String, Integer>> questionOptions = new HashMap<>();

            Matcher om = oPattern.matcher(blocks[i]);
            while (om.find()) {
                String letter = om.group(1).trim();
                String traitsRaw = om.group(3).trim();
                Map<String, Integer> moralWeights = moralAnalysisService.parseMoralWeights(traitsRaw);
                questionOptions.put(letter, moralWeights);
            }

            if (!questionOptions.isEmpty()) {
                questionsData.put(questionId, questionOptions);
            }
        }

        return questionsData;
    }
}
