package caret.data;
import org.eclipse.jface.text.ITextSelection;
public class CodeFragment {

	int startline;
	int endline;
	int offset;
	int length;
	String methodName;

	public CodeFragment(ITextSelection iTextSelection) {
		this.startline = iTextSelection.getStartLine();
		this.endline = iTextSelection.getEndLine();
		this.offset = iTextSelection.getOffset();
		this.length = iTextSelection.getLength();
	}
	
	public int getStartline() {
		return startline;
	}

	public void setStartline(int startline) {
		this.startline = startline;
	}

	public int getEndline() {
		return endline;
	}

	public void setEndline(int endline) {
		this.endline = endline;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
}
