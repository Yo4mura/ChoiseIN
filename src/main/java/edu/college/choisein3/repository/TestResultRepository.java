package edu.college.choisein3.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.college.choisein3.model.TestResult;
import edu.college.choisein3.model.User;
import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByUser(User user);
}
