package caret.tool;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Document;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class SyntaxValidator {

    public static boolean hasSyntaxError(IDocument document) {
        return hasSyntaxError(document.get());
    }
    
    public static boolean hasSyntaxError(String source) {
        try {
            ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
            parser.setSource(source.toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);

            IProblem[] problems = compilationUnit.getProblems();
            for (IProblem problem : problems) {
                if (problem.isError()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public static boolean hasSyntaxError(IFile file) {
        try {
            if (file.getFileExtension().equals("java")) {

                IMarker[] markers = file.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IFile.DEPTH_INFINITE);
                for (IMarker marker : markers) {
                    int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
                    if (severity == IMarker.SEVERITY_ERROR) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
