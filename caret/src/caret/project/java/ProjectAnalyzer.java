package caret.project.java;

import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;

public class ProjectAnalyzer {

    public static int getTotalMethods(IProject project) throws JavaModelException {
        int methodCount = 0;
        IJavaProject javaProject = JavaCore.create(project);

        for (IPackageFragment pkg : javaProject.getPackageFragments()) {
            for (ICompilationUnit unit : pkg.getCompilationUnits()) {
                for (IType type : unit.getAllTypes()) {
                    for (IMethod method : type.getMethods()) {
                        methodCount++;
                    }
                }
            }
        }
        return methodCount;
    }

    public static int getTotalClasses(IProject project) throws JavaModelException {
        int classCount = 0;
        IJavaProject javaProject = JavaCore.create(project);

        for (IPackageFragment pkg : javaProject.getPackageFragments()) {
            for (ICompilationUnit unit : pkg.getCompilationUnits()) {
                classCount += unit.getAllTypes().length;
            }
        }
        return classCount;
    }

    public static int getTotalPackages(IProject project) throws JavaModelException {
        IJavaProject javaProject = JavaCore.create(project);
        return javaProject.getPackageFragments().length;
    }
}
