package com.github.andreaTP.opa.chicory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;

public class OpaCli {

    public static Path baseSourceFolder = Path.of("src", "test", "resources", "fixtures");
    private static Path baseDestFolder = Path.of("target", "compiled-policies");

    private static String bundleName = "bundle.tar.gz";

    private OpaCli() {}

    public static Path compile(String regoFolder, String... entrypoints) throws IOException {
        return compile(regoFolder, false, entrypoints);
    }

    public static Path compile(String regoFolder, boolean capabilities, String... entrypoints)
            throws IOException {
        var sourceFolder = baseSourceFolder.resolve(regoFolder);
        var plainName =
                Files.list(sourceFolder)
                        .filter(f -> f.toFile().getName().endsWith(".rego"))
                        .findFirst()
                        .get()
                        .toFile()
                        .getName()
                        .replace(".rego", "");
        var targetFolder = baseDestFolder.resolve(regoFolder);
        if (targetFolder.toFile().exists()) {
            FileUtils.deleteDirectory(targetFolder.toFile());
        }
        targetFolder.toFile().mkdirs();
        var targetBundle = baseDestFolder.resolve(regoFolder).resolve(bundleName);

        List<String> command = new ArrayList<>();
        command.add("opa");
        command.add("build");
        command.add(".");
        command.add("-o");
        command.add(targetBundle.toFile().getAbsolutePath());
        command.add("-t");
        command.add("wasm");
        if (capabilities) {
            var capabilitiesFile = sourceFolder.resolve("capabilities.json");
            command.add("--capabilities");
            command.add(capabilitiesFile.toFile().getAbsolutePath());
        }
        for (var entrypoint : entrypoints) {
            command.add("-e");
            command.add(entrypoint);
        }
        // System.out.println("Going to execute command: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(sourceFolder.toFile());
        pb.inheritIO();
        Process ps;
        try {
            ps = pb.start();
            ps.waitFor(10, TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<String> tarCommand = new ArrayList<>();
        tarCommand.add("tar");
        tarCommand.add("-xf");
        tarCommand.add(bundleName);
        // System.out.println("Going to execute command: " + String.join(" ", tarCommand));

        ProcessBuilder tarPb = new ProcessBuilder(tarCommand);
        tarPb.directory(targetFolder.toFile());
        tarPb.inheritIO();
        Process tarPs;
        try {
            tarPs = tarPb.start();
            tarPs.waitFor(10, TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return targetFolder;
    }
}
