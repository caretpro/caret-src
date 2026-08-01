package game;

import java.util.List;
import java.util.ArrayList;

public class ScoreService implements IScoreService {
	
	public int calculateFinalScore(List<Integer> data, int factor, User user) {
	    if (data == null || data.isEmpty()) return 0;
	
	    int sum = 0;
	    for (Integer v : data) {
	        if (v != null && v > 0) {
	            sum += v;
	        }
	    }
	
	    String level = user != null ? user.getLevel() : null;
	    double adjustment = "Low".equals(level) ? 0.8 : "High".equals(level) ? 1.2 : 1.0;
	
	    return (int) (sum * factor * adjustment);
	}

	public ScoreReport generateReport(GameSession session, User user) {
        if (session == null) {
            String name = (user == null) ? "Unknown" : user.getName();
            return new ScoreReport(name, 0);
        }

        List<Integer> data = session.getScores();
        String name = (user == null || user.getName() == null) ? "Unknown" : user.getName();
        if (data == null || data.isEmpty()) {
            return new ScoreReport(name, 0);
        }

        String level = (user == null) ? null : user.getLevel();
        double adjustment = "Low".equals(level) ? 0.8
                : "High".equals(level) ? 1.2
                : 1.0;

        int sum = 0;
        for (Integer v : data) {
            if (v != null && v > 0) {
                sum += v;
            }
        }

        int finalScore = (int) (sum * 2 * adjustment);
        return new ScoreReport(name, finalScore);
    }
	
    public List<Integer> generateSquares(List<Integer> numbers) {
	    if (numbers == null || numbers.isEmpty()) {
	        return new ArrayList<>();
	    }
	    return numbers.stream()
	            .map(n -> n == null ? null : n * n)
	            .toList();
	}

    public int findMaxValue(List<Integer> numbers) {
        if (numbers == null) return Integer.MIN_VALUE;
        int max = Integer.MIN_VALUE;
        for (Integer n : numbers) {
            if (n != null && n > max) {
                max = n;
            }
        }
        return max;
    }
}
