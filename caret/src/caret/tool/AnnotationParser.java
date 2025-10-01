package caret.tool;

import java.util.*;

public class AnnotationParser {
    public static Map<String, String> parseAnnotation(String annotation) {
        Map<String, String> result = new LinkedHashMap<>();

        String[] pairs = annotation.split(",\\s*");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                result.put(key, value);
            }
        }
        return result;
    }

}
