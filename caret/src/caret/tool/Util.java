package caret.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import caret.data.Interaction;

import com.google.gson.JsonElement;
public class Util {

	public static String getDateFormat(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public static String getDateFormat(String format, Date date) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	public static String getDateFormat(String format, long timestamp) {
		Date date = new Date();
		date.setTime(timestamp);
		return getDateFormat(format, date);
	}
	

	
	public static String shell(String command){
    	String result ="";
    	try {
    		Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            result=output.toString();
        } catch (Exception e) {
        	System.out.println("ERROR SHELL: "+e.getMessage());
        }
    	return result;
    }
	
	public static String codeToLine(String text, boolean quote) {
		//text = text.replace("\n", " ");
		//text = text.replace("\t", " ");
		//text = text.replaceAll("\\r\\n|\\r|\\n|\\t", " ");
		text = text.replace("\0", "X");
		text = text.replaceAll("\\r", "\\\\r");
		text = text.replaceAll("\\n", "\\\\n");
		text = text.replaceAll("\\t", "\\\\t");
		//text = text.replaceAll("\u0000", "\\\\0");
		if(quote) {
			text = text.replace("\"", "\\\"");
			//text = text.replace("\'", "\\'");	
		}
		text = text.replace("  ", " ");
        while(text.indexOf("  ")>=0) {
			text=text.replaceAll("  ", " ");
		}
		return text;
	}
	
	public static String toCapitalize(String text){
		text = text.substring(0,1).toUpperCase()+text.substring(1);
		return text;
	}
	
	public static String getClassName(String filename) {
    	int endIndex = filename.lastIndexOf(".");
    	if(endIndex>0) {
    		return filename.substring(0, endIndex);
    	}
    	return null;
    }
	
	public static Interaction[] getArrayInteractions( ArrayList <Interaction> interactions) {
		Interaction [] arrayInteractions = new Interaction[interactions.size()];
		for( int i=0; i<arrayInteractions.length; i++) {
			arrayInteractions[i]=interactions.get(i);
		}
		return arrayInteractions;
	}
	
	public static boolean saveLog(String path, String content) {
		BufferedWriter bw = null;
	    FileWriter fw = null;
	    try {
	        File file = new File(path);
	        if (!file.exists()) {
	            file.createNewFile();
	        }
	        fw = new FileWriter(file.getAbsoluteFile(), false); // false -> rewrite the file
	        bw = new BufferedWriter(fw);
	        bw.write(content);
	        System.out.println("Saved log!");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        try {
	            if (bw != null)
	                bw.close();
	            if (fw != null)
	                fw.close();
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
	    return true;
	}
	
	public static boolean saveLog(String path, String filename, String content) {
	    BufferedWriter bw = null;
	    FileWriter fw = null;
	    try {
	        // Create the .log directory inside the given path
	        File logDir = new File(path, ".log");
	        if (!logDir.exists()) {
	            logDir.mkdirs(); // Create directory if it doesn't exist
	        }

	        // Create the log file inside the .log directory
	        File file = new File(logDir, filename);
	        fw = new FileWriter(file, false); // false -> overwrite the file
	        bw = new BufferedWriter(fw);
	        bw.write(content);
	        System.out.println("Saved log: " + file.getAbsolutePath());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        try {
	            if (bw != null) bw.close();
	            if (fw != null) fw.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
	    return true;
	}
	
	public static String codeToDialog(String text) {
		text = text.replace("&", "&&");
		text = text.replaceAll("^\\n\\n|^\\n", "");
		return text;
	}
	
	public static String getJSON(String text) {
		String code = null;
		int indexCode;
		int endCode;
		try {
			Boolean isJsonObject = false;
			try {
				isJsonObject = JsonParser.parseString(text).isJsonObject();
			} catch (Exception e) {
				//System.out.println("ERROR isJsonObject 1: "+e.getMessage());
			}
			if(isJsonObject) {
				code = text;
			}else {
				indexCode = text.indexOf("```json");
				if(indexCode>=0) {
					indexCode += 7;
				}else {
					indexCode = text.indexOf("```");
					if(indexCode>=0) {
						indexCode += 3;
					}
				}
			    endCode = text.indexOf("```",indexCode);
			    if(endCode>indexCode) {
				    code = text.substring(indexCode, endCode-1);
			    }else {
					indexCode = text.indexOf(":");
					if(indexCode>=0 && text.indexOf("{",indexCode+6)>=0) {
						indexCode += 1;
						endCode = text.lastIndexOf("}");
						if(endCode>indexCode) {
							code = text.substring(indexCode, endCode+1);
						}
					}
				}
			}
			if(code!=null){
				code = code.trim();
				try {
					isJsonObject = JsonParser.parseString(text).isJsonObject();
					if(isJsonObject) {
						System.out.println("JSON FORMAT OK");
					}
				} catch (Exception e) {
					//System.out.println("ERROR isJsonObject 2: "+e.getMessage());
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR GET CODE: "+e.getMessage());
		}
	    return code;
	}
	
	public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
	
	public static String highlightJavaSyntax(String code) {
        String keywordPattern = "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|null|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\\b";
        String typePattern = "\\b([A-Z][a-zA-Z0-9]*)\\b";
        String stringPattern = "\"(.*?)\"";
        String commentPattern = "//[^\n]*|/\\*(?:.|[\\n\\r])*?\\*/";
        String numberPattern = "\\b\\d+\\b";

        code = code.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        code = code.replaceAll(keywordPattern, "<span class='keyword'>$0</span>");
        code = code.replaceAll(stringPattern, "<span class='string'>$0</span>");
        code = code.replaceAll(commentPattern, "<span class='comment'>$0</span>");
        code = code.replaceAll(numberPattern, "<span class='number'>$0</span>");

        String style="<style TYPE='text/css'>\n"
        		+ "        .keyword { color: blue; font-weight: bold; }\n"
        		+ "        .type { color: darkorange; }\n"
        		+ "        .string { color: darkgreen; }\n"
        		+ "        .comment { color: gray; font-style: italic; }\n"
        		+ "        .number { color: darkred; }\n"
        		+ "   </style>";
        String html="<html><body><pre><code>" + code + "</code></pre></body></html>";
        //System.out.println(html);
        return html;
    }
	
	public static String getDiff(String originalSource, String modifiedSource) {
        String[] originalLines = originalSource.split("\n");
        String[] modifiedLines = modifiedSource.split("\n");
        List<String> diffLines = new ArrayList<>();
        int i = 0, j = 0;
        while (i < originalLines.length && j < modifiedLines.length) {
            if (originalLines[i].equals(modifiedLines[j])) {
                i++;
                j++;
            } else {
                diffLines.add(modifiedLines[j]);
                j++;
            }
        }
        while (j < modifiedLines.length) {
            diffLines.add(modifiedLines[j]);
            j++;
        }
        return String.join("\n", diffLines);
    }
	
	public static String getLineContent(IDocument document, int offset) {
		try {
			int lineNumber = document.getLineOfOffset(offset);
	        IRegion lineInfo = document.getLineInformation(lineNumber);
	        int lineOffset = lineInfo.getOffset();
	        int lineLength = lineInfo.getLength();
	        return document.get(lineOffset, lineLength);
		} catch (Exception e) {
			return null;
		}
        
    }
	
	public static void applySyntaxHighlighting(StyledText styledText) {
        String[] keywords = {
            "public", "private", "protected", "class", "interface", "extends", "implements",
            "static", "final", "void", "abstract", "synchronized", "volatile", "transient",
            "if", "else", "for", "while", "do", "switch", "case", "default",
            "try", "catch", "finally", "throw", "throws", "return", "break", "continue",
            "new", "this", "super", "import", "package", "instanceof", "enum", "assert",
            "boolean", "byte", "char", "short", "int", "long", "float", "double", "String",
            "null", "true", "false"
        };

        String text = styledText.getText();
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineStartIndex = styledText.getOffsetAtLine(i);

            if (line.trim().startsWith("//")) {
            }

            for (String keyword : keywords) {
                int index = line.indexOf(keyword);
                while (index != -1) {
                    boolean isWordBoundaryStart = (index == 0 || !Character.isJavaIdentifierPart(line.charAt(index - 1)));
                    boolean isWordBoundaryEnd = (index + keyword.length() == line.length() || !Character.isJavaIdentifierPart(line.charAt(index + keyword.length())));

                    if (isWordBoundaryStart && isWordBoundaryEnd) {
                        StyleRange styleRange = new StyleRange();
                        styleRange.start = lineStartIndex + index; 
                        styleRange.length = keyword.length();
                        styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA); 
                        styleRange.fontStyle = SWT.BOLD; 
                        styledText.setStyleRange(styleRange);
                    }
                    
                    index = line.indexOf(keyword, index + 1);
                }
            }
        }
    }
	
	public static void applySyntaxHighlighting(StyledText styledText, int start, int length) {
	    String[] keywords = {
	        "public", "private", "protected", "class", "interface", "extends", "implements",
	        "static", "final", "void", "abstract", "synchronized", "volatile", "transient",
	        "if", "else", "for", "while", "do", "switch", "case", "default",
	        "try", "catch", "finally", "throw", "throws", "return", "break", "continue",
	        "new", "this", "super", "import", "package", "instanceof", "enum", "assert",
	        "boolean", "byte", "char", "short", "int", "long", "float", "double", "String",
	        "null", "true", "false"
	    };

	    String text = styledText.getText();

	    int end = start + length;

	    for (String keyword : keywords) {
	        int index = text.indexOf(keyword, start);
	        while (index != -1 && index < end) {
	            boolean isWordBoundaryStart = (index == 0 || !Character.isJavaIdentifierPart(text.charAt(index - 1)));
	            boolean isWordBoundaryEnd = (index + keyword.length() == text.length() || !Character.isJavaIdentifierPart(text.charAt(index + keyword.length())));

	            if (isWordBoundaryStart && isWordBoundaryEnd) {
	                StyleRange styleRange = new StyleRange();
	                styleRange.start = index;
	                styleRange.length = keyword.length();
	                styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);
	                styleRange.fontStyle = SWT.BOLD;
	                styledText.setStyleRange(styleRange);
	            }

	            index = text.indexOf(keyword, index + 1);
	        }
	    }
	}
	
	public static void applySyntaxHighlighting(StyledText styledText, int start, int length, Color foreground, Color background, int fontStyle) {
	    String[] keywords = {
	        "public", "private", "protected", "class", "interface", "extends", "implements",
	        "static", "final", "void", "abstract", "synchronized", "volatile", "transient",
	        "if", "else", "for", "while", "do", "switch", "case", "default",
	        "try", "catch", "finally", "throw", "throws", "return", "break", "continue",
	        "new", "this", "super", "import", "package", "instanceof", "enum", "assert",
	        "boolean", "byte", "char", "short", "int", "long", "float", "double", "String",
	        "null", "true", "false"
	    };

	    String text = styledText.getText();
	    int end = start + length;

	    StyleRange range = new StyleRange();
	    range.start = start;
	    range.length = length;
	    range.background = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
	    styledText.setStyleRange(range);

	    for (String keyword : keywords) {
	        int index = text.indexOf(keyword, start);
	        while (index != -1 && index < end) {
	            boolean isWordBoundaryStart = (index == 0 || !Character.isJavaIdentifierPart(text.charAt(index - 1)));
	            boolean isWordBoundaryEnd = (index + keyword.length() == text.length() || !Character.isJavaIdentifierPart(text.charAt(index + keyword.length())));

	            if (isWordBoundaryStart && isWordBoundaryEnd) {
	                StyleRange keywordStyleRange = new StyleRange();
	                keywordStyleRange.start = index;
	                keywordStyleRange.length = keyword.length();

	                keywordStyleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);
	                keywordStyleRange.background = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
	                keywordStyleRange.fontStyle = SWT.BOLD;

	                styledText.setStyleRange(keywordStyleRange);
	            }

	            index = text.indexOf(keyword, index + 1);
	        }
	    }
	}

	
	public static String trim(String code) {
	    if (code == null) {
	        return null;
	    }
	    return code.replaceAll("^[\\t\\n\\r]+|[\\t\\n\\r]+$", "");
	}

	public static void highlightDifferences(StyledText oldCode, StyledText newCode) {
        String[] oldLines = oldCode.getText().split("\\R"); // 
        String[] newLines = newCode.getText().split("\\R");

        oldLines = filterEmptyLines(oldLines);
        newLines = filterEmptyLines(newLines);

        highlightFromTop(oldLines, newLines, newCode);
        
        highlightFromBottom(oldLines, newLines, newCode);
    }

    private static String[] filterEmptyLines(String[] lines) {
        return java.util.Arrays.stream(lines)
            .filter(line -> !line.trim().isEmpty()) 
            .map(line -> normalizeLine(line)) 
            .toArray(String[]::new); 
    }

    private static String normalizeLine(String line) {
        return line.trim().replaceAll("\\s+", " ");
    }

    private static void highlightFromTop(String[] oldLines, String[] newLines, StyledText newCode) {
        for (int i = 0; i < newLines.length; i++) {
            if (i >= oldLines.length || !newLines[i].equals(oldLines[i])) {
                
                highlightLine(newCode, i, newLines[i]);
            }
        }
    }

    private static void highlightFromBottom(String[] oldLines, String[] newLines, StyledText newCode) {
        
        for (int i = newLines.length - 1; i >= 0; i--) {
            if (i >= oldLines.length || !newLines[i].equals(oldLines[i])) {
                
                highlightLine(newCode, i, newLines[i]);
            }
        }
    }

    private static void highlightLine(StyledText newCode, int lineIndex, String lineContent) {
        int lineOffset = newCode.getOffsetAtLine(lineIndex);
        int lineLength = lineContent.length();

        int totalLineLength = getLineTotalLength(newCode, lineIndex);

        applySyntaxHighlighting(newCode, lineOffset, totalLineLength, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA), Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW), SWT.BOLD);
    }

    private static int getLineTotalLength(StyledText newCode, int lineIndex) {
        int totalLines = newCode.getLineCount();
        if (lineIndex + 1 >= totalLines) {
            return (newCode.getText().length()- 1) - newCode.getOffsetAtLine(lineIndex);// -1(without })
        }
        int lineEndOffset = newCode.getOffsetAtLine(lineIndex + 1);
        return lineEndOffset - newCode.getOffsetAtLine(lineIndex);
    }
    
    public static List<String> readJsonFilesFromDirectory(String directoryPath) {
        List<String> jsonContents = new ArrayList<>();
        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {
            System.out.println("The path provided is not a directory: " + directoryPath);
            return jsonContents;
        }

        // Filter to get only .json files in the root directory
        File[] jsonFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles != null) {
            for (File jsonFile : jsonFiles) {
                try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        content.append(line).append(System.lineSeparator());
                    }
                    jsonContents.add(content.toString().trim()); // Add file content to the list
                } catch (Exception e) {
                    System.err.println("Error reading file: " + jsonFile.getName());
                    e.printStackTrace();
                }
            }
        }

        return jsonContents;
    }
    
    public static MethodPosition findMethodPosition2(String methodSource, String sourceClass) {
        try {
            Document document = new Document(sourceClass);
            int startIndex = sourceClass.indexOf(methodSource);
            if (startIndex == -1) {
                return null;
            }
            
            int lineStart = document.getLineOfOffset(startIndex) + 1;
            int lineEnd = document.getLineOfOffset(startIndex + methodSource.length()) + 1;
            
            return new MethodPosition("", lineStart, lineEnd);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static MethodPosition findMethodPosition(String methodName, String sourceClass) {
        try {
            Document document = new Document(sourceClass);
            String regex = "(?m)^(\\s*.*?\\b" + Pattern.quote(methodName) + "\\s*\\()";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sourceClass);
            
            if (matcher.find()) {
                int startIndex = matcher.start();
                int lineStart = document.getLineOfOffset(startIndex) + 1;
                
                int braceCount = 0;
                boolean insideMethod = false;
                for (int i = startIndex; i < sourceClass.length(); i++) {
                    char c = sourceClass.charAt(i);
                    if (c == '{') {
                        braceCount++;
                        insideMethod = true;
                    }
                    if (c == '}') {
                        braceCount--;
                        if (insideMethod && braceCount == 0) {
                            int lineEnd = document.getLineOfOffset(i) + 1;
                            return new MethodPosition(methodName, lineStart, lineEnd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean similarCode(String oldMethod, String newMethod) {
    	if(Util.codeToLine(oldMethod, false) != Util.codeToLine(newMethod, false)) {
    		return true;
    	}else {
    		return false;
    	}
    }
    
    public static MethodPosition findMethodPositionBySignature(String methodSignature, String sourceClass) {
        try {
            Document document = new Document(sourceClass);

            String normalizedSignature = methodSignature.replaceAll("\\s+", " ").trim();

            String regex = Pattern.quote(normalizedSignature).replace(" ", "\\s+"); // permite flexibilidad en los espacios
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sourceClass);

            if (matcher.find()) {
                int startIndex = matcher.start();
                int lineStart = document.getLineOfOffset(startIndex) + 1;

                // Localizar el final del método recorriendo los brackets
                int braceCount = 0;
                boolean insideMethod = false;

                for (int i = matcher.end(); i < sourceClass.length(); i++) {
                    char c = sourceClass.charAt(i);
                    if (c == '{') {
                        braceCount++;
                        insideMethod = true;
                    } else if (c == '}') {
                        braceCount--;
                        if (insideMethod && braceCount == 0) {
                            int lineEnd = document.getLineOfOffset(i) + 1;
                            return new MethodPosition(methodSignature, lineStart, lineEnd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFormattedDate(long durationInMillis) {
         long minutes = durationInMillis / 60000;
         long seconds = (durationInMillis % 60000) / 1000;
         long millis = durationInMillis % 1000;
         return minutes+"min "+seconds+"s "+millis+"ms";
    }
    
    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}
