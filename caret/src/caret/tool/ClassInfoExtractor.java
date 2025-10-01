package caret.tool;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class ClassInfoExtractor {

    /**
     * Extracts the name of the class extended by the given compilation unit.
     * 
     * @param unit The ICompilationUnit representing the Java source.
     * @return The name of the superclass, or "None" if no superclass is extended.
     */
    public static String getSuperclassName(ICompilationUnit unit) {
        try {
            IType[] types = unit.getTypes();
            if (types.length > 0) {
                IType type = types[0]; // Assuming the first type is the main class
                String superclassName = type.getSuperclassName();
                return (superclassName != null) ? superclassName : null;
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts the names of the interfaces implemented by the given compilation unit.
     * 
     * @param unit The ICompilationUnit representing the Java source.
     * @return A comma-separated string of interface names, or "None" if no interfaces are implemented.
     */
    public static String [] getImplementedInterfaces(ICompilationUnit unit) {
    	String[] interfaces;
        try {
            IType[] types = unit.getTypes();
            if (types.length > 0) {
                IType type = types[0]; // Assuming the first type is the main class
                interfaces = type.getSuperInterfaceNames();
                if (interfaces.length > 0) {
                    return interfaces;
                }
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return null;
    }
}
