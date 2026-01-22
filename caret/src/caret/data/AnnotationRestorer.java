package caret.data;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import caret.project.java.JavaProject;
import caret.tool.Log;

public class AnnotationRestorer {

    public static boolean addGeneratedAnnotationIfMissing(
            IProject project, 
            String className, 
            String methodName,
            String agent,
            String task,
            String id,
            String timestamp) throws CoreException {
        ICompilationUnit unit = JavaProject.getClass(project, className);
        if (unit == null) return false;
        
        final boolean[] addedAnnotation = { false };
        
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(unit);
        parser.setResolveBindings(true);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

        AST ast = astRoot.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        astRoot.accept(new ASTVisitor() {
        	
            @Override
            public boolean visit(MethodDeclaration node) {
                if (node.getName().getIdentifier().equals(methodName)) {
                    if (!hasGeneratedAnnotation(node)) {
                    	Log.d("Annotation: Not detected");
                        addAnnotation(node, ast, rewrite, agent, task, id, timestamp);
                        addedAnnotation [0]= true;
                        Log.d("Annotation: Added");
                    }else {
                    	Log.d("Annotation: detected");
                    }
                }
                return super.visit(node);
            }
        });

        try {
            TextEdit edits = rewrite.rewriteAST(new Document(unit.getSource()), null);
            unit.applyTextEdit(edits, null);
            unit.save(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addedAnnotation [0];
    }

    private static boolean hasGeneratedAnnotation(MethodDeclaration node) {
        for (Object modifier : node.modifiers()) {
            if (modifier instanceof Annotation) {
                Annotation ann = (Annotation) modifier;
                if (ann.getTypeName().getFullyQualifiedName().equals("Generated")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addAnnotation(MethodDeclaration node, AST ast, ASTRewrite rewrite, 
                               String agent, String task, String id, String timestamp) {
        
        NormalAnnotation generatedAnn = ast.newNormalAnnotation();
        generatedAnn.setTypeName(ast.newSimpleName("Generated"));

        addMemberValuePair(ast, generatedAnn, "agent", agent);
        addMemberValuePair(ast, generatedAnn, "task", task);
        addMemberValuePair(ast, generatedAnn, "id", id);
        addMemberValuePair(ast, generatedAnn, "timestamp", timestamp);

        ListRewrite listRewrite = rewrite.getListRewrite(node, MethodDeclaration.MODIFIERS2_PROPERTY);
        listRewrite.insertFirst(generatedAnn, null);
    }

    private static void addMemberValuePair(AST ast, NormalAnnotation annotation, String name, String value) {
        MemberValuePair pair = ast.newMemberValuePair();
        pair.setName(ast.newSimpleName(name));
        
        StringLiteral literal = ast.newStringLiteral();
        literal.setLiteralValue(value);
        pair.setValue(literal);
        
        annotation.values().add(pair);
    }
}