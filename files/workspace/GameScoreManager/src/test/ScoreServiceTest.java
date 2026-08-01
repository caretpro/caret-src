package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.GameSession;
import game.ScoreReport;
import game.ScoreService;
import game.User;

public class ScoreServiceTest {

    private ScoreService scoreService;
    private User lowLevelUser;
    private User mediumLevelUser;
    private User highLevelUser;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService();
        lowLevelUser = new User("Alice", "Low");
        mediumLevelUser = new User("Bob", "Medium");
        highLevelUser = new User("Charlie", "High");
    }

    @Test
    void calculateFinalScore_shouldApplyLowLevelAdjustment() {
        List<Integer> data = List.of(10, -5, 20);
        int result = scoreService.calculateFinalScore(data, 2, lowLevelUser);
        assertEquals(48, result);
    }

    @Test
    void calculateFinalScore_shouldApplyMediumLevelAdjustment() {
        List<Integer> data = List.of(5, 5, 10);
        int result = scoreService.calculateFinalScore(data, 3, mediumLevelUser);
        assertEquals(60, result);
    }

    @Test
    void calculateFinalScore_shouldApplyHighLevelAdjustment() {
        List<Integer> data = List.of(10, 10);
        int result = scoreService.calculateFinalScore(data, 1, highLevelUser);
        assertEquals(24, result);
    }

    @Test
    void generateReport_shouldReturnCorrectFinalScore() {
        List<Integer> scores = List.of(5, 10, -3);
        GameSession session = new GameSession(scores);
        ScoreReport report = scoreService.generateReport(session, highLevelUser);
        assertEquals("Charlie", report.getUserName());
        assertEquals(36, report.getFinalScore());
    }

    @Test
    void findMaxValue_shouldReturnMaximumValue() {
        List<Integer> numbers = List.of(3, 7, 2, 9, 1);
        int result = scoreService.findMaxValue(numbers);
        assertEquals(9, result);
    }
}
