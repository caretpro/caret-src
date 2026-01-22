package caret.project.java;
import java.util.Date;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import caret.ChatView;
import caret.tool.Util;

public class MethodReplacer {

	final static ChatView chatView = ChatView.getInstance();
	private static MethodDeclaration currentMethod;
	
    public static String replaceClassMethod(String sourceCode, String methodName, String newMethodCode) {
        // Parse the source code
    	
        ASTParser parser = ASTParser.newParser(AST.JLS8); // Adjust JLS version if needed
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(sourceCode.toCharArray());
        parser.setResolveBindings(false); // Set true if working in Eclipse project context

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration method) {
                if (method.getName().getIdentifier().equals(methodName)) {
                    // Parse the new method body
                    Block newBody = ast.newBlock();
                    Statement statement = (Statement) rewriter.createStringPlaceholder(newMethodCode, ASTNode.RETURN_STATEMENT);
                    newBody.statements().add(statement);

                    // Replace the existing body
                    rewriter.set(method, MethodDeclaration.BODY_PROPERTY, newBody, null);
                }
                return super.visit(method);
            }
        });

        // Apply changes to the source code
        Document document = new Document(sourceCode);
        TextEdit edits = rewriter.rewriteAST(document, null);
        try {
            edits.apply(document);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return document.get();
    }
    
    public static String modifyClassMethod(String sourceCode, String methodName, String newMethodCode) {
        // Parse the source code
        ASTParser parser = ASTParser.newParser(AST.JLS17); // Adjust JLS version if needed
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(sourceCode.toCharArray());
        parser.setResolveBindings(false); // Set true if working in Eclipse project context

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration method) {
                if (method.getName().getIdentifier().equals(methodName)) {
                    // Parse the new method body
                    Block newBody = ast.newBlock();
                    Statement statement = (Statement) rewriter.createStringPlaceholder(newMethodCode, ASTNode.RETURN_STATEMENT);
                    newBody.statements().add(statement);
                    System.out.print("@@@newBody: \n"+newBody.toString()+"@@@");

                    // Replace the existing body
                    rewriter.set(method, MethodDeclaration.BODY_PROPERTY, newBody, null);
                }
                return super.visit(method);
            }
        });


        return cu.toString();
    }
    /*17032025
    public static String modifyMethod(String sourceCode, String methodName, String methodCode) {
        // Parse the source code
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(sourceCode.toCharArray());
        parser.setResolveBindings(false);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Parse the new method declaration
        MethodDeclaration newMethod = parseMethodDeclaration(methodCode);
        if(newMethod != null){
        	 System.out.print("@@@newMethod NO NULL");
        	if(!methodName.equals(newMethod.getName().getIdentifier())) {
        		System.out.print("@@@newMethod DIFFERENT: "+methodName+":"+newMethod.getName().getIdentifier());
        		return null;
        	}else {
        		System.out.print("@@@newMethod SAME: "+methodName+":"+newMethod.getName().getIdentifier());
        		System.out.print("@@@newMethod BODY: \n"+newMethod.getBody().toString());
        	}
        }else {
        	System.out.print("@@@newMethod NULL");
        	return null;
        }
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration method) {
                if (method.getName().getIdentifier().equals(methodName)) {
                    // Replace the existing body with the new body
                	System.out.print("@@@newMethod REPLACE: "+method.getName().getIdentifier());
                    //rewriter.set(method, MethodDeclaration.BODY_PROPERTY, newMethod.getBody(), null);
                	Block newBody = (Block) ASTNode.copySubtree(method.getAST(), newMethod.getBody());
                    method.setBody(newBody);
                	System.out.print("@@@newMethod REPLACED: "+method.getName().getIdentifier());
                }
                return super.visit(method);
            }
        });

        return cu.toString();
    }*/
    
    public static String modifyMethod(String sourceCode, String methodName, String methodCode) {
        // Parse the source code
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(sourceCode.toCharArray());
        parser.setResolveBindings(false);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Parse the new method declaration
        MethodDeclaration newMethod = parseMethodDeclaration(methodCode);
        if (newMethod == null || !methodName.equals(newMethod.getName().getIdentifier())) {
            return null;
        }
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration method) {
                if (method.getName().getIdentifier().equals(methodName)) {
                    Block newBody = (Block) ASTNode.copySubtree(method.getAST(), newMethod.getBody());
                    rewriter.replace(method.getBody(), newBody, null);
                    currentMethod = method;
                }
                return super.visit(method);
            }
        });

        // Aplicar las modificaciones al código fuente original
        Document document = new Document(sourceCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static MethodDeclaration getCurrentMethod() {
		return currentMethod;
	}

	public static String replaceMethod(String sourceCode, String methodName, String methodCode, IEditorPart editorPart) {
        // Parse the source code
    	System.out.println("### Method replaced INIT");
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(sourceCode.toCharArray());
        parser.setResolveBindings(false);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        // Parse the new method declaration
        MethodDeclaration newMethod = parseMethodDeclaration(methodCode);
        if (newMethod != null) {
            if (!methodName.equals(newMethod.getName().getIdentifier())) {
            	System.out.println("### Method "+newMethod.getName().getIdentifier()+" NOT FOUND in the source code");
                return null;
            }else {
            	System.out.println("### Method "+newMethod.getName().getIdentifier()+" FOUND in the source code:\n"+newMethod.getBody()+"\n---");
            }
        } else {
            return null;
        }

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration method) {
                if (method.getName().getIdentifier().equals(methodName)) {
                	
                	// Add annotation
                    NormalAnnotation annotation = ast.newNormalAnnotation();
                    annotation.setTypeName(ast.newSimpleName("Generated"));

                    // Add annotation properties
                    try {
                    	long timestamp = chatView.getCurrentInteraction().getTimestamp();
                    	Date date = new Date();
                    	date.setTime(timestamp);
    					addAnnotationMember(ast, annotation, "agent", chatView.getCurrentAgent().getTechnology());
    	                addAnnotationMember(ast, annotation, "task", chatView.getCurrentTask().getCode());
    	                addAnnotationMember(ast, annotation, "id", ""+timestamp+"");
    	                addAnnotationMember(ast, annotation, "timestamp", Util.getDateFormat("yyyy-MM-dd HH:mm:ss", date));
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}

                    // Insert the annotation before the method
                    ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
                    listRewrite.insertFirst(annotation, null);
                    // Replace the existing body with the new body
                	System.out.println("### Method ASTNode.copySubtree:\n"+newMethod.getBody()+"\n---");

                	
                    //Block newBody = (Block) ASTNode.copySubtree(method.getAST(), newMethod.getBody());// delete comments
                    //method.setBody(newBody);//old 
                	//rewriter.set(method, MethodDeclaration.BODY_PROPERTY, newBody, null);
                    
                    
                 // Add a Javadoc comment if not already present
                    Javadoc existingJavadoc = method.getJavadoc();
                    boolean commentExists = false;
                    if (existingJavadoc != null) {
                        for (Object tag : existingJavadoc.tags()) {
                            if (tag instanceof TagElement) {
                                for (Object fragment : ((TagElement) tag).fragments()) {
                                    if (fragment instanceof TextElement) {
                                        if (((TextElement) fragment).getText().contains(chatView.JAVADOC_LINE_CARET)) {
                                            commentExists = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (commentExists) {
                                break;
                            }
                        }
                    }

                    if (!commentExists) {
                        Javadoc javadoc = existingJavadoc != null ? (Javadoc) ASTNode.copySubtree(ast, existingJavadoc) : ast.newJavadoc();
                        TagElement tag = ast.newTagElement();
                        TextElement text = ast.newTextElement();
                        text.setText(chatView.JAVADOC_LINE_CARET);
                        tag.fragments().add(text);
                        javadoc.tags().add(tag);

                        rewriter.set(method, MethodDeclaration.JAVADOC_PROPERTY, javadoc, null);
                    }
                    
                    System.out.println("### Method replaced " + method.getName().getIdentifier()+": \n"+method.getBody());
                }
                return super.visit(method);
            }
        });
        System.out.println("###replaceAST#cuToString:\n"+cu.toString()+"@@@");
        // Apply changes to the document associated with the editor
        if (editorPart instanceof ITextEditor) {
        	System.out.println("### replaceMethod -> editorPart instanceof ITextEditor");
            ITextEditor textEditor = (ITextEditor) editorPart;
            IDocumentProvider documentProvider = textEditor.getDocumentProvider();
            IDocument document = documentProvider.getDocument(textEditor.getEditorInput());
            
            try {
                TextEdit edits = rewriter.rewriteAST(document, null);
                edits.apply(document); // Apply edits directly to the document in the editor
                System.out.println("###replaceAST#document:\n"+document.get()+"@@@");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return document.get(); // Return the updated source code
        }

        return null; // Return null if the editor is not a text editor
    }
	
	public static String replaceMethodBody(
	        String sourceCode,
	        String methodName,
	        String methodCode,
	        IEditorPart editorPart) {

	    ASTParser parser = ASTParser.newParser(AST.JLS17);
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    parser.setSource(sourceCode.toCharArray());
	    parser.setResolveBindings(false);

	    CompilationUnit cu = (CompilationUnit) parser.createAST(null);
	    AST ast = cu.getAST();
	    ASTRewrite rewriter = ASTRewrite.create(ast);

	    cu.accept(new ASTVisitor() {
	        @Override
	        public boolean visit(MethodDeclaration method) {
	            if (method.getName().getIdentifier().equals(methodName)) {

	                ASTNode placeholder =
	                        rewriter.createStringPlaceholder(
	                                methodCode,
	                                ASTNode.METHOD_DECLARATION);

	                rewriter.replace(method, placeholder, null);
	            }
	            return super.visit(method);
	        }
	    });

	    if (editorPart instanceof ITextEditor) {
	        ITextEditor textEditor = (ITextEditor) editorPart;
	        IDocument document =
	                textEditor.getDocumentProvider()
	                          .getDocument(textEditor.getEditorInput());

            StyledText styledText = (StyledText) textEditor.getAdapter(Control.class);
            int topIndex = styledText.getTopIndex();
            int caretOffset = styledText.getCaretOffset();
            
	        try {
	            TextEdit edits = rewriter.rewriteAST(document, null);
	            edits.apply(document);
                styledText.setTopIndex(topIndex);
                styledText.setCaretOffset(caretOffset);
	            return document.get();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    return null;
	}

    
    public static MethodDeclaration parseMethodDeclaration(String source) {
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(("class Temp {" + source + "}").toCharArray()); // Encapsular en una clase temporal
        parser.setResolveBindings(false);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        parser.setIgnoreMethodBodies(false);
        parser.setCompilerOptions(null);
        parser.setUnitName("Temp.java");
        parser.setEnvironment(null, null, null, true);
        
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        
        if (cu.types().isEmpty()) {
            return null;
        }
        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        if (typeDecl.getMethods().length > 0) {
            return typeDecl.getMethods()[0]; // Retorna el primer método encontrado
        }
        return null;
    }

    private static void addAnnotationMember(AST ast, NormalAnnotation annotation, String name, String value) {
        MemberValuePair pair = ast.newMemberValuePair();
        pair.setName(ast.newSimpleName(name));
        StringLiteral stringLiteral = ast.newStringLiteral();
        stringLiteral.setLiteralValue(value);
        pair.setValue(stringLiteral);
        annotation.values().add(pair);
    }
    
    public static String replaceMethodJavadoc(String sourceCode, String methodName, String methodCode, IEditorPart editorPart) {
        // Parse the source code
    	System.out.println("### Method replaced INIT");
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(sourceCode.toCharArray());
        parser.setResolveBindings(false);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        // Parse the new method declaration
        MethodDeclaration newMethod = parseMethodDeclaration(methodCode);
        if (newMethod != null) {
            if (!methodName.equals(newMethod.getName().getIdentifier())) {
            	System.out.println("### Method "+newMethod.getName().getIdentifier()+" not found in the source code");
                return null;
            }
        } else {
            return null;
        }

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration method) {
                if (method.getName().getIdentifier().equals(methodName)) {
                	
                	// Add annotation
                    NormalAnnotation annotation = ast.newNormalAnnotation();
                    annotation.setTypeName(ast.newSimpleName("Generated"));

                    // Add annotation properties
                    try {
                    	long timestamp = chatView.getCurrentInteraction().getTimestamp();
                    	Date date = new Date();
                    	date.setTime(timestamp);
    					addAnnotationMember(ast, annotation, "agent", chatView.getCurrentAgent().getTechnology());
    	                addAnnotationMember(ast, annotation, "task", chatView.getCurrentTask().getCode());
    	                addAnnotationMember(ast, annotation, "id", ""+timestamp+"");
    	                addAnnotationMember(ast, annotation, "timestamp", Util.getDateFormat("yyyy-MM-dd HH:mm:ss", date));
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}

                    // Insert the annotation before the method
                    ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
                    listRewrite.insertFirst(annotation, null);
                    // Replace the existing body with the new body
                    Block newBody = (Block) ASTNode.copySubtree(method.getAST(), newMethod.getBody());
                    //method.setBody(newBody);
                    rewriter.set(method, MethodDeclaration.BODY_PROPERTY, newBody, null);
                    
                 // Add a Javadoc comment if not already present
                    Javadoc newJavadoc = newMethod.getJavadoc();
                    Javadoc currentJavadoc = method.getJavadoc();
                	Javadoc javadoc;
                	if(newJavadoc!=null) {
                		javadoc = (Javadoc) ASTNode.copySubtree(ast, newJavadoc);
                	}else {
                		javadoc = currentJavadoc != null ? (Javadoc) ASTNode.copySubtree(ast, currentJavadoc) : ast.newJavadoc();
                	}
                	boolean commentExists = false;
                    if (currentJavadoc != null) {
                        for (Object tag : currentJavadoc.tags()) {
                            if (tag instanceof TagElement) {
                                for (Object fragment : ((TagElement) tag).fragments()) {
                                    if (fragment instanceof TextElement) {
                                        if (((TextElement) fragment).getText().contains(chatView.JAVADOC_LINE_CARET)) {
                                            commentExists = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (commentExists) {
                                break;
                            }
                        }
                    }
                    if (!commentExists) {
                    	TagElement tag = ast.newTagElement();
                        TextElement text = ast.newTextElement();
                        text.setText(chatView.JAVADOC_LINE_CARET);
                        tag.fragments().add(text);
                        javadoc.tags().add(tag);
                    }
                    rewriter.set(method, MethodDeclaration.JAVADOC_PROPERTY, javadoc, null);
                    System.out.println("### Method replaced " + method.getName().getIdentifier()+": \n"+method.getBody());
                }
                return super.visit(method);
            }
        });
        System.out.println("###replaceAST#cuToString:\n"+cu.toString()+"@@@");
        // Apply changes to the document associated with the editor
        if (editorPart instanceof ITextEditor) {
        	System.out.println("### replaceMethod -> editorPart instanceof ITextEditor");
            ITextEditor textEditor = (ITextEditor) editorPart;
            IDocumentProvider documentProvider = textEditor.getDocumentProvider();
            IDocument document = documentProvider.getDocument(textEditor.getEditorInput());

            try {
                TextEdit edits = rewriter.rewriteAST(document, null);
                edits.apply(document); // Apply edits directly to the document in the editor
                System.out.println("###replaceAST#document:\n"+document.get()+"@@@");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return document.get(); // Return the updated source code
        }

        return null; // Return null if the editor is not a text editor
    }

}


