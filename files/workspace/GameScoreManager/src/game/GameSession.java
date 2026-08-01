package game;

import java.util.List;

public class GameSession {

    private final List<Integer> scores;

    public GameSession(List<Integer> scores) {
        this.scores = List.copyOf(scores);
    }

    public List<Integer> getScores() {
        return scores;
    }
}