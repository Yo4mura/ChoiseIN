package edu.college.choisein3.service;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class MoralAnalysisService {

    // 10 моральных категорий
    private static final List<String> MORAL_CATEGORIES = Arrays.asList(
            "Альтруизм", "Эгоизм", "Прагматизм", "Идеализм", "Справедливость",
            "Лояльность", "Сочувствие", "Холодный_расчёт", "Честность", "Гибкость"
    );

    /**
     * Анализирует ответы пользователя и вычисляет баллы по моральным категориям
     */
    public Map<String, Integer> calculateMoralScores(Map<String, String> userAnswers,
                                                     Map<String, Map<String, Map<String, Integer>>> questionsData) {
        Map<String, Integer> scores = new HashMap<>();

        // Инициализируем все категории нулевыми баллами
        for (String category : MORAL_CATEGORIES) {
            scores.put(category, 0);
        }

        // Проходим по всем ответам пользователя
        for (Map.Entry<String, String> entry : userAnswers.entrySet()) {
            String questionId = entry.getKey();
            String answerLetter = entry.getValue();

            // Получаем данные вопроса
            Map<String, Map<String, Integer>> questionData = questionsData.get(questionId);
            if (questionData != null && questionData.containsKey(answerLetter)) {
                Map<String, Integer> answerWeights = questionData.get(answerLetter);

                // Добавляем баллы к соответствующим категориям
                for (Map.Entry<String, Integer> weightEntry : answerWeights.entrySet()) {
                    String category = weightEntry.getKey();
                    Integer weight = weightEntry.getValue();

                    scores.put(category, scores.get(category) + weight);
                }
            }
        }

        return scores;
    }

    /**
     * Вычисляет процентное распределение баллов
     */
    public Map<String, Double> calculatePercentages(Map<String, Integer> scores) {
        int totalScore = scores.values().stream().mapToInt(Integer::intValue).sum();

        if (totalScore == 0) {
            return MORAL_CATEGORIES.stream()
                    .collect(Collectors.toMap(category -> category, category -> 0.0));
        }

        return scores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (double) entry.getValue() / totalScore * 100
                ));
    }

    /**
     * Определяет тип личности на основе доминирующих моральных качеств
     */
    public String determinePersonalityType(Map<String, Double> percentages) {
        // Сортируем категории по убыванию процентов
        List<Map.Entry<String, Double>> sortedCategories = percentages.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        if (sortedCategories.isEmpty()) {
            return "Неопределенный тип";
        }

        String topCategory = sortedCategories.get(0).getKey();
        double topPercentage = sortedCategories.get(0).getValue();

        // Если есть вторая по значимости категория с разницей менее 10%
        if (sortedCategories.size() > 1) {
            String secondCategory = sortedCategories.get(1).getKey();
            double secondPercentage = sortedCategories.get(1).getValue();

            if (topPercentage - secondPercentage < 10.0) {
                return formatPersonalityType(topCategory, secondCategory);
            }
        }

        return formatPersonalityType(topCategory);
    }

    private String formatPersonalityType(String primary, String secondary) {
        return getPersonalityDescription(primary) + " " + getPersonalityDescription(secondary);
    }

    private String formatPersonalityType(String primary) {
        return getPersonalityDescription(primary);
    }

    private String getPersonalityDescription(String category) {
        return switch (category) {
            case "Альтруизм" ->
                    "Альтруист";
            case "Эгоизм" ->
                    "Индивидуалист";
            case "Прагматизм" ->
                    "Прагматик";
            case "Идеализм" ->
                    "Идеалист";
            case "Справедливость" ->
                    "Справедливый";
            case "Лояльность" ->
                    "Лояльный";
            case "Сочувствие" ->
                    "Сочувствующий";
            case "Холодный_расчёт" ->
                    "Расчётливый";
            case "Честность" ->
                    "Честный";
            case "Гибкость" ->
                    "Гибкий";
            default ->
                    category;
        };
    }

    /**
     * Парсит веса из строки формата "Альтруизм=2, Сочувствие=1"
     */
    public Map<String, Integer> parseMoralWeights(String weightsString) {
        Map<String, Integer> weights = new HashMap<>();

        if (weightsString == null || weightsString.trim().isEmpty()) {
            return weights;
        }

        String[] pairs = weightsString.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=");
            if (keyValue.length == 2) {
                try {
                    String category = keyValue[0].trim();
                    int weight = Integer.parseInt(keyValue[1].trim());
                    weights.put(category, weight);
                } catch (NumberFormatException e) {
                    // Игнорируем некорректные значения
                }
            }
        }

        return weights;
    }
}
