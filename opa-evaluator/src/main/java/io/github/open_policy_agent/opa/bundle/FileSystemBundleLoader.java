package io.github.open_policy_agent.opa.bundle;

import io.github.open_policy_agent.opa.storage.Store;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileSystemBundleLoader implements BundleLoader {
  private final String id;
  private final Path directory;

  public FileSystemBundleLoader(String id, Path directory) {
    this.id = id;
    this.directory = directory;
  }

  @Override
  public Bundle load(Store store) {
    if (!Files.isDirectory(directory)) {
      throw new IllegalArgumentException(
          "Bundle directory does not exist or is not a directory: " + directory);
    }
    if (!Files.isReadable(directory)) {
      throw new IllegalArgumentException("Bundle directory is not readable: " + directory);
    }

    BundleAssembler assembler = new BundleAssembler();

    try {
      Path planPath = directory.resolve("plan.json");
      if (Files.isRegularFile(planPath)) {
        try (InputStream in = Files.newInputStream(planPath)) {
          assembler.loadPlan(in);
        }
      }

      Path manifestPath = directory.resolve(".manifest");
      if (Files.isRegularFile(manifestPath)) {
        try (InputStream in = Files.newInputStream(manifestPath)) {
          assembler.loadManifest(in);
        }
      }

      try (Stream<Path> walk = Files.walk(directory)) {
        walk.filter(Files::isRegularFile)
            .forEach(
                p -> {
                  try {
                    String relativePath =
                        directory.relativize(p).toString().replace('\\', '/');
                    String fileName = p.getFileName().toString();

                    if (fileName.equals("data.json")) {
                      // Path prefix is the directory portion (empty for root data.json)
                      int lastSlash = relativePath.lastIndexOf('/');
                      String dataPath = lastSlash < 0 ? "" : relativePath.substring(0, lastSlash);
                      try (InputStream in = Files.newInputStream(p)) {
                        assembler.loadData(dataPath, in);
                      }
                    } else if (fileName.endsWith(".rego")) {
                      String content = Files.readString(p);
                      assembler.addRego(relativePath, content);
                    }
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                });
      }

      return assembler.finish(id, store);

    } catch (UncheckedIOException e) {
      throw new IllegalArgumentException(
          "Error reading bundle directory: " + e.getCause().getMessage(), e.getCause());
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Error reading bundle directory: " + e.getMessage(), e);
    }
  }
}
