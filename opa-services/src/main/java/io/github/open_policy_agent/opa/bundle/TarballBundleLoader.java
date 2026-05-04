package io.github.open_policy_agent.opa.bundle;

import io.github.open_policy_agent.opa.storage.Store;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class TarballBundleLoader implements BundleLoader {
  private final Path path;
  private final byte[] data;
  private final String id;

  public TarballBundleLoader(String id, Path path) {
    this.path = path;
    this.data = null;
    this.id = id;
  }

  public TarballBundleLoader(String id, byte[] data) {
    this.path = null;
    this.data = data;
    this.id = id;
  }

  public Bundle load(Store store) {
    if (data != null) {
      return createBundleFromStream(id, new ByteArrayInputStream(data), store);
    }
    if (path == null) {
      throw new IllegalArgumentException("No bundle path or data provided");
    }
    if (!Files.exists(path)) {
      throw new IllegalArgumentException("The bundle does not exist: " + path);
    }
    if (!Files.isReadable(path)) {
      throw new IllegalArgumentException("The bundle is not readable: " + path);
    }
    try (var in = Files.newInputStream(path)) {
      byte[] magic = new byte[3];
      if (in.read(magic) != 3
          || magic[0] != (byte) 0x1F
          || magic[1] != (byte) 0x8B
          || magic[2] != (byte) 0x08) {
        throw new IllegalArgumentException("The bundle is not a tar.gz file: " + path);
      }
      return createBundleFromStream(id, Files.newInputStream(path), store);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading the bundle: " + e.getMessage());
    }
  }

  private Bundle createBundleFromStream(String id, InputStream in, Store store) {
    BundleAssembler assembler = new BundleAssembler();
    try (GZIPInputStream gzipIn = new GZIPInputStream(in);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn, true)) {
      org.apache.commons.compress.archivers.tar.TarArchiveEntry entry;
      while ((entry = tarIn.getNextTarEntry()) != null) {
        String entryName =
            entry.getName().startsWith("/") ? entry.getName().substring(1) : entry.getName();
        if (!entry.isDirectory() && entryName.equals("plan.json")) {
          byte[] entryBytes = tarIn.readAllBytes();
          assembler.loadPlan(new ByteArrayInputStream(entryBytes));
        }
        if (!entry.isDirectory()
            && (entryName.equals("data.json") || entryName.endsWith("/data.json"))) {
          byte[] entryBytes = tarIn.readAllBytes();
          int lastSlash = entryName.lastIndexOf('/');
          String dataPath = lastSlash < 0 ? "" : entryName.substring(0, lastSlash);
          assembler.loadData(dataPath, new ByteArrayInputStream(entryBytes));
        }
        if (!entry.isDirectory() && entryName.equals(".manifest")) {
          byte[] entryBytes = tarIn.readAllBytes();
          assembler.loadManifest(new ByteArrayInputStream(entryBytes));
        }
        if (!entry.isDirectory() && entryName.endsWith(".rego")) {
          byte[] entryBytes = tarIn.readAllBytes();
          assembler.addRego(entryName, new String(entryBytes));
        }
      }
      return assembler.finish(id, store);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error extracting bundle: " + e.getMessage(), e);
    }
  }
}
