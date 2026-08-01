package game;

import java.util.List;

public interface IScoreService {
	
    int calculateFinalScore(List<Integer> data, int factor, User user);
    
    ScoreReport generateReport(GameSession session, User user);
    
    List<Integer> generateSquares(List<Integer> numbers);
    
    int findMaxValue(List<Integer> numbers);
}
