package caret.project.java;

import org.eclipse.jdt.core.dom.*;

public class ASTMethodExtractor {

    /**
     * Extracts the code of a specific method from a Java source code string.
     * If the code is a valid class, it analyzes the class and searches for the method.
     * If the code is just a method, it directly validates and returns it.
     *
     * @param javaCode   The Java source code as a String.
     * @param methodName The name of the method to extract.
     * @return The extracted method code, or null if not found.
     */
    public static String extractMethod(String javaCode, String methodName) {
        // Parse the source code using Eclipse JDT AST
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setSource(javaCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        // Check for syntax errors
        if (cu.getProblems().length>0) {
            // If not a valid class, check if it's a standalone method
            return extractStandaloneMethod(javaCode, methodName);
        }

        // Traverse the AST to find the method
        MethodFinder finder = new MethodFinder(methodName);
        cu.accept(finder);

        return finder.getMethodCode();
    }

    private static String extractStandaloneMethod(String methodCode, String methodName) {
        // Wrap the standalone method in a dummy class
        String wrappedCode = "public class DummyClass { " + methodCode + " }";

        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setSource(wrappedCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        // Traverse the AST to find the method in the dummy class
        MethodFinder finder = new MethodFinder(methodName);
        cu.accept(finder);

        return finder.getMethodCode();
    }

    private static class MethodFinder extends ASTVisitor {
        private final String methodName;
        private String methodCode;

        public MethodFinder(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            if (node.getName().getIdentifier().equals(methodName)) {
                methodCode = node.toString();
            }
            return super.visit(node);
        }

        public String getMethodCode() {
            return methodCode;
        }
    }

}

