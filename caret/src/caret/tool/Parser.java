package caret.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	public static int TYPE_CONTENT=1;
	public static int TYPE_FILENAME=2;
	
	public static String getClassName(String source, int type) {
		
		String className = null;
		
		if(type == TYPE_CONTENT) {
			// Remove comments
	        String _source = source.replaceAll("(?s)/\\*.*?\\*/", "")  // block comments
	                                  .replaceAll("//.*", "");           // line comments

	        // Match class/interface/etc. with optional modifiers before '{'
	        String regex = "\\b(class|interface|enum|record)\\s+(\\w+)\\b(?:[^\\{]*?)\\{";
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(_source);

	        if (matcher.find()) {
	        	className = matcher.group(2); // Class/interface/etc. name
	        }
		}
		if(type == TYPE_FILENAME) {
			int endIndex = source.lastIndexOf(".");
	    	if(endIndex>0) {
	    		className = source.substring(0, endIndex);
	    	}
		}
		
		return className;
	}
	
}
