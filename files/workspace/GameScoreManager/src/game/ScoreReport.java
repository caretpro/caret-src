package game;

public class ScoreReport {
	
    private String userName;
    private int finalScore;

    public ScoreReport(String userName, int finalScore) {
	    this.userName = userName;
	    this.finalScore = finalScore;
	}
    
    public String getUserName() {
	  return userName;
	 }
    
    public int getFinalScore() { 
    	return finalScore; 
    }
}
