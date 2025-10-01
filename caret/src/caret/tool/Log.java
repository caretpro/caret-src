package caret.tool;
public class Log {

    public static boolean enableVerbose = true;
    public static boolean enableDebug = true;
    public static boolean enableInfo = true;
    public static boolean enableWarn = true;
    public static boolean enableError = true;
    public static boolean enableOptional = true;

    public static boolean stackTraceEnabled = true;

    private static void print(String level, String message) {
        if (!stackTraceEnabled) {
            System.out.println("[" + level.toUpperCase() + "] " + message);
            return;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
            if (!simpleClassName.equals("Thread") && !simpleClassName.equals("Log")) {
                String methodName = element.getMethodName();
                int lineNumber = element.getLineNumber();
                System.out.println(
                    String.format("[%s] %s.%s():%d - %s",
                        level.toUpperCase(), simpleClassName, methodName, lineNumber, message));
                break;
            }
        }
    }
    
    private static void print(String level, String message, StringBuilder sb) {
        if (!stackTraceEnabled) {
        	String text = "[" + level.toUpperCase() + "] " + message;
            System.out.println(text);
            sb.append(text+"\n");
            return;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
            if (!simpleClassName.equals("Thread") && !simpleClassName.equals("Log")) {
                String methodName = element.getMethodName();
                int lineNumber = element.getLineNumber();
                String text = String.format("[%s] %s.%s():%d - %s",
                        level.toUpperCase(), simpleClassName, methodName, lineNumber, message);
                System.out.println(text);
                sb.append(text+"\n");
                break;
            }
        }
    }

    public static void v(String message) {
        if (enableVerbose) print("VERBOSE", message);
    }
    
    public static void v(String message, StringBuilder sb) {
        if (enableVerbose) print("VERBOSE", message, sb);
    }

    public static void d(String message) {
        if (enableDebug) print("DEBUG", message);
    }
    
    public static void d(String message, StringBuilder sb) {
        if (enableVerbose) print("DEBUG", message, sb);
    }
    
    public static void i(String message) {
        if (enableInfo) print("INFO", message);
    }

    public static void i(String message, StringBuilder sb) {
        if (enableVerbose) print("INFO", message, sb);
    }
    
    public static void w(String message) {
        if (enableWarn) print("WARN", message);
    }

    public static void w(String message, StringBuilder sb) {
        if (enableVerbose) print("WARN", message, sb);
    }
    
    public static void e(String message) {
        if (enableError) print("ERROR", message);
    }

    public static void e(String message, StringBuilder sb) {
        if (enableVerbose) print("ERROR", message, sb);
    }
    
    public static void o(String message) {
        o(message, "OPTIONAL");
    }

    public static void o(String message, StringBuilder sb) {
        if (enableVerbose) print("OPTIONAL", message, sb);
    }
    
    public static void o(String message, String label) {
        if (enableOptional) print(label.toUpperCase(), message);
    }
    
    public static void o(String message, String label, StringBuilder sb) {
        if (enableOptional) print(label.toUpperCase(), message, sb);
    }
}
