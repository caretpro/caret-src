package caret.tool;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JavaFileReplacer {

    private final String baseProjectName;

    public JavaFileReplacer(String baseProjectName) {
        this.baseProjectName = baseProjectName;
    }

    public void replaceJavaFiles(File baseDir) {
        String solutionProjectName = baseProjectName + "-solution";

        File sourceProjectDir = new File(baseDir, baseProjectName);
        File solutionProjectDir = new File(baseDir, solutionProjectName);

        if (!sourceProjectDir.exists() || !solutionProjectDir.exists()) {
            System.err.println("One of the project directories does not exist.");
            return;
        }

        replaceJavaFilesRecursive(sourceProjectDir, solutionProjectDir, sourceProjectDir);
    }
    
    private void replaceJavaFilesRecursive(File currentSource, File solutionRoot, File sourceRoot) {
        for (File file : currentSource.listFiles()) {
            if (file.isDirectory()) {
                replaceJavaFilesRecursive(file, solutionRoot, sourceRoot);
            } else if (file.getName().endsWith(".java")) {
                try {
                	
                    Path relativePath = sourceRoot.toPath().relativize(file.toPath());
                    File targetFile = new File(solutionRoot, relativePath.toString());

                    if (targetFile.exists()) {
                        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Replaced: " + targetFile.getPath());
                    } else {
                        System.out.println("Not found in solution project, skipped: " + relativePath);
                    }
                } catch (IOException e) {
                    System.err.println("Error copying file: " + file.getPath());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: JavaFileReplacer <project_name> <base_directory>");
            return;
        }

        String projectName = args[0];
        File baseDir = new File(args[1]);

        JavaFileReplacer replacer = new JavaFileReplacer(projectName);
        replacer.replaceJavaFiles(baseDir);
    }
}
