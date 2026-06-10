package caret.tool;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class PDEBuilder {

    public static void main(String[] args) throws Exception {

        // Path to the Eclipse installation that includes PDE
        String eclipseHome = "/path/to/eclipse";

        // Workspace containing plugins, features and releng projects
        String workspace = "/path/to/workspace";

        // Output directory for the build
        String buildDirectory = "/path/to/output-build";
        // Base location (usually the same as eclipseHome)
        String baseLocation = eclipseHome;

        runPDEBuild(
                eclipseHome,
                workspace,
                buildDirectory,
                baseLocation
        );

        System.out.println("✔ PDE build completed successfully");
    }

    /**
     * Executes Eclipse in headless mode using PDE Build.
     */
    private static void runPDEBuild(
            String eclipseHome,
            String workspace,
            String buildDirectory,
            String baseLocation
    ) throws IOException, InterruptedException {

        String eclipseExecutable = eclipseHome + File.separator + "eclipse";

        ProcessBuilder processBuilder = new ProcessBuilder(
                eclipseExecutable,
                "-nosplash",
                "-application", "org.eclipse.pde.build.Build",
                "-data", workspace,
                "-buildDirectory", buildDirectory,
                "-baseLocation", baseLocation
        );

        // Merge stdout and stderr and forward them to the console
        processBuilder.redirectErrorStream(true);
        processBuilder.inheritIO();

        System.out.println("Starting Eclipse PDE headless build...");
        System.out.println("Command: " + Arrays.toString(processBuilder.command().toArray()));

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    "PDE build failed with exit code " + exitCode
            );
        }
    }
}
