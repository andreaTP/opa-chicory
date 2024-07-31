import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class OpaCli {

    private static Path baseSourceFolder = Path.of("src", "test", "resources", "fixtures");
    private static Path baseDestFolder = Path.of( "target", "compiled-policies");

    private OpaCli() {}

    public static Path compile(String regoFolder, String... entrypoints) throws IOException {
        var sourceFolder = baseSourceFolder.resolve(regoFolder);
        var plainName = Files.list(sourceFolder)
                .filter(f -> f.toFile().getName().endsWith(".rego"))
                .findFirst()
                .get()
                .toFile()
                .getName()
                .replace(".rego", "");
        var targetFolder = baseDestFolder.resolve(plainName);
        targetFolder.toFile().mkdirs();
        var targetBundle = baseDestFolder.resolve(plainName).resolve("bundle.tar.gz");

        List<String> command = new ArrayList<>();
        command.add("opa");
        command.add("build");
        command.add(sourceFolder.toFile().getAbsolutePath());
        command.add("-o");
        command.add(targetBundle.toFile().getAbsolutePath());
        command.add("-t");
        command.add("wasm");
        for (var entrypoint: entrypoints) {
            command.add("-e");
            command.add(entrypoint);
        }

        System.out.println("DEBUG Going to execute command: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File("."));
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

        // TODO: problems un tarring the folder ... maybe I should use command line ...
        Files.createDirectories(targetFolder);
        System.out.println("targetFolder " + targetFolder.toFile().getAbsolutePath());

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(targetBundle.toFile()));
             TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {
            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                var entryName = entry.getName();
                Path extractTo = targetFolder.resolve(entryName);
                if (entry.isDirectory()) {
                    // skip directories
                } else {
                    System.out.println("DEBUG " + extractTo.toAbsolutePath());
                    Files.copy(tar, extractTo, REPLACE_EXISTING);
                }
            }
        }

        return targetFolder;
    }

}
