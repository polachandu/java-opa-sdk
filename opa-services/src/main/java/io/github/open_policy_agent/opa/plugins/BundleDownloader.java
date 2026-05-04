package io.github.open_policy_agent.opa.plugins;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import io.github.open_policy_agent.opa.config.Config;

/**
 * Base class for downloading and activating bundles from remote or local sources.
 *
 * <p>Handles common functionality for bundle downloads including:
 *
 * <ul>
 *   <li>HTTP/HTTPS downloads with ETag caching
 *   <li>file:// URI and filesystem path support
 *   <li>File modification time tracking
 *   <li>Polling with configurable intervals
 *   <li>Initial activation futures
 * </ul>
 *
 * <p>Subclasses must implement {@link #activateBundle(byte[])} to define how the downloaded bundle
 * data should be processed.
 */
public abstract class BundleDownloader {
  protected final String name;
  protected final PluginManager manager;
  protected final HttpClient httpClient;
  protected final CompletableFuture<Void> initialActivation;

  protected String service;
  protected String resource;
  protected Config.PollingConfig polling;
  protected String etag;
  protected long lastModifiedTime = 0;

  protected BundleDownloader(String name, PluginManager manager) {
    this.name = name;
    this.manager = manager;
    this.httpClient =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    this.initialActivation = new CompletableFuture<>();
  }

  public BundleDownloader setService(String service) {
    this.service = service;
    return this;
  }

  public BundleDownloader setResource(String resource) {
    this.resource = resource;
    return this;
  }

  public BundleDownloader setPolling(Config.PollingConfig polling) {
    this.polling = polling;
    return this;
  }

  /**
   * Start polling for bundle updates.
   *
   * @param scheduler the scheduler to use for periodic downloads
   * @return a future that completes when the initial bundle is downloaded and activated
   */
  public CompletableFuture<Void> startPolling(ScheduledExecutorService scheduler) {
    int minDelay =
        (polling != null && polling.getMinDelaySeconds() != null)
            ? polling.getMinDelaySeconds()
            : 60;
    int maxDelay =
        (polling != null && polling.getMaxDelaySeconds() != null)
            ? polling.getMaxDelaySeconds()
            : 120;

    // Schedule initial download
    scheduler.schedule(this::downloadBundle, 0, TimeUnit.SECONDS);

    // Schedule periodic downloads
    scheduler.scheduleAtFixedRate(this::downloadBundle, minDelay, maxDelay, TimeUnit.SECONDS);

    return initialActivation;
  }

  /**
   * Download the bundle from the configured service and resource.
   *
   * <p>Handles HTTP/HTTPS downloads with ETag caching, file:// URIs, and filesystem paths. Calls
   * {@link #activateBundle(byte[])} when new bundle data is available.
   */
  protected void downloadBundle() {
    try {
      Config.ServiceConfig serviceConfig = manager.getConfig().getService(service);
      if (serviceConfig == null) {
        manager.getLogger().error("Bundle '%s': Service '%s' not found", name, service);
        if (!initialActivation.isDone()) {
          initialActivation.completeExceptionally(
              new RuntimeException("Service '" + service + "' not found"));
        }
        return;
      }

      String baseUrl = serviceConfig.getUrl();
      String fullPath;

      // Check if baseUrl is a URI scheme (http://, https://, file://)
      if (baseUrl.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
        if (baseUrl.endsWith("/") || resource.startsWith("/")) {
          fullPath = baseUrl + resource;
        } else {
          fullPath = baseUrl + "/" + resource;
        }
        URI uri = URI.create(fullPath);

        // Handle file:// URIs
        if ("file".equalsIgnoreCase(uri.getScheme())) {
          handleFileDownload(Paths.get(uri));
          return;
        }

        // Handle HTTP/HTTPS URIs
        handleHttpDownload(uri);
      } else {
        // It's a file path (relative or absolute)
        Path basePath = Paths.get(baseUrl);
        Path filePath = basePath.resolve(resource);
        handleFileDownload(filePath);
      }

    } catch (Exception e) {
      manager.getLogger().error("Bundle '%s': Download error: %s", name, e.getMessage());
      if (!initialActivation.isDone()) {
        initialActivation.completeExceptionally(e);
      }
    }
  }

  /**
   * Handle downloading from a file:// URI or filesystem path.
   *
   * @param filePath the path to the file
   */
  private void handleFileDownload(Path filePath) throws IOException {
    FileTime currentModTime = Files.getLastModifiedTime(filePath);
    long currentModTimeMillis = currentModTime.toMillis();

    if (lastModifiedTime != 0 && lastModifiedTime == currentModTimeMillis) {
      manager.getLogger().debug("Bundle '%s': File not modified, skipping activation", name);
      if (!initialActivation.isDone()) {
        initialActivation.complete(null);
      }
      return;
    }

    byte[] bundleData = Files.readAllBytes(filePath);
    activateBundle(bundleData);
    lastModifiedTime = currentModTimeMillis;

    if (!initialActivation.isDone()) {
      initialActivation.complete(null);
    }
  }

  /**
   * Handle downloading from an HTTP/HTTPS URI.
   *
   * @param uri the URI to download from
   */
  private void handleHttpDownload(URI uri) throws IOException, InterruptedException {
    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", "application/vnd.openpolicyagent.bundles")
            .GET();

    if (etag != null) {
      requestBuilder.header("If-None-Match", etag);
    }

    HttpRequest request = requestBuilder.build();
    HttpResponse<byte[]> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

    if (response.statusCode() == 304) {
      manager.getLogger().debug("Bundle '%s': Not modified (ETag match)", name);
      if (!initialActivation.isDone()) {
        initialActivation.complete(null);
      }
      return;
    }

    if (response.statusCode() == 200) {
      response.headers().firstValue("ETag").ifPresent(newEtag -> this.etag = newEtag);
      activateBundle(response.body());
      if (!initialActivation.isDone()) {
        initialActivation.complete(null);
      }
    } else {
      String errorMsg = "Download failed with status " + response.statusCode();
      manager.getLogger().error("Bundle '%s': %s", name, errorMsg);
      if (!initialActivation.isDone()) {
        initialActivation.completeExceptionally(new RuntimeException(errorMsg));
      }
    }
  }

  /**
   * Process and activate a downloaded bundle.
   *
   * <p>This method is called when new bundle data is available (either from an HTTP download, or
   * when a local file has been modified).
   *
   * @param bundleData the raw bundle bytes (typically a gzipped tarball)
   */
  protected abstract void activateBundle(byte[] bundleData);
}
