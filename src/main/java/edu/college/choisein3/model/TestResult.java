package edu.college.choisein3.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 связь с пользователем (если вошёл в систему)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // если пользователь гость
    private String guestName;

    @Column(columnDefinition = "TEXT")
    private String answers; // JSON строка с ответами пользователя

    @Column(columnDefinition = "TEXT")
    private String moralScores; // JSON строка с баллами по категориям

    @Column(columnDefinition = "TEXT")
    private String personalityType; // Определенный тип личности

    private LocalDateTime completedAt;

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getMoralScores() {
        return moralScores;
    }

    public void setMoralScores(String moralScores) {
        this.moralScores = moralScores;
    }

    public String getPersonalityType() {
        return personalityType;
    }

    public void setPersonalityType(String personalityType) {
        this.personalityType = personalityType;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}


