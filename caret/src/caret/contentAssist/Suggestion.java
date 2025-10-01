package caret.contentAssist;

public class Suggestion {
	
	private String proposal = null;
	private String hash;
	private int offset;
	
	public Suggestion() {
		
	}
	
	public Suggestion (String proposal, String hash, int offset) {
		this.proposal = proposal;
		this.hash = hash;
		this.offset = offset;
	}
	public String getProposal(String hash, int offset) {
		if(proposal != null) {
			if(this.hash.equals(hash) && this.offset == offset )
				return proposal;
			else
				return null;
		}else{
			return null;
		}		
	}
}