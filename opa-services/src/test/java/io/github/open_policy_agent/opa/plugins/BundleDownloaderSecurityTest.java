package io.github.open_policy_agent.opa.plugins;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import io.github.open_policy_agent.opa.config.Config;
import io.github.open_policy_agent.opa.logging.Logger;
import io.github.open_policy_agent.opa.storage.InMem;
import io.github.open_policy_agent.opa.storage.Store;

class BundleDownloaderSecurityTest {

  private HttpServer server;
  private Store store;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void download_wrongContentType_fails() throws Exception {
    byte[] bundleData = createValidBundle();
    server = startServer(exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
      exchange.sendResponseHeaders(200, bundleData.length);
      exchange.getResponseBody().write(bundleData);
      exchange.close();
    });

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
            .get(10, TimeUnit.SECONDS));
    assertTrue(ex.getCause().getMessage().contains("Content-Type"));
  }

  @Test
  void download_missingContentType_succeeds() throws Exception {
    byte[] bundleData = createValidBundle();
    server = startServer(exchange -> {
      exchange.sendResponseHeaders(200, bundleData.length);
      exchange.getResponseBody().write(bundleData);
      exchange.close();
    });

    runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
        .get(10, TimeUnit.SECONDS);
    assertNotNull(store.getBundles().get("authz"));
  }

  @Test
  void download_alternateContentType_succeeds() throws Exception {
    byte[] bundleData = createValidBundle();
    server = startServer(exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/gzip");
      exchange.sendResponseHeaders(200, bundleData.length);
      exchange.getResponseBody().write(bundleData);
      exchange.close();
    });

    runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
        .get(10, TimeUnit.SECONDS);
    assertNotNull(store.getBundles().get("authz"));
  }

  @Test
  void download_contentTypeWithParametersAndCasing_succeeds() throws Exception {
    byte[] bundleData = createValidBundle();
    server = startServer(exchange -> {
      exchange.getResponseHeaders()
          .add("Content-Type", "Application/VND.OpenPolicyAgent.Bundles; charset=utf-8");
      exchange.sendResponseHeaders(200, bundleData.length);
      exchange.getResponseBody().write(bundleData);
      exchange.close();
    });

    runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
        .get(10, TimeUnit.SECONDS);
    assertNotNull(store.getBundles().get("authz"));
  }

  @Test
  void download_bodyExceedsLimitMidStream_fails() throws Exception {
    byte[] bundleData = createValidBundle();
    server = startServer(exchange -> {
      // Omit Content-Length so the mid-stream check is exercised instead
      exchange.getResponseHeaders().add("Content-Type", "application/vnd.openpolicyagent.bundles");
      exchange.sendResponseHeaders(200, bundleData.length);
      exchange.getResponseBody().write(bundleData);
      exchange.close();
    });

    long smallLimit = bundleData.length - 1;
    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> runBundleDownload(server.getAddress().getPort(), smallLimit)
            .get(10, TimeUnit.SECONDS));
    assertTrue(ex.getCause().getMessage().contains("exceeds limit"));
  }

  @Test
  void download_validBundleWithinLimit_succeeds() throws Exception {
    byte[] bundleData = createValidBundle();
    server = startServer(exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/vnd.openpolicyagent.bundles");
      exchange.sendResponseHeaders(200, bundleData.length);
      exchange.getResponseBody().write(bundleData);
      exchange.close();
    });

    runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
        .get(10, TimeUnit.SECONDS);
    assertNotNull(store.getBundles().get("authz"));
  }

  @Test
  void download_validBundleWithContentLengthWithinLimit_succeeds() throws Exception {
    byte[] bundleData = createValidBundle();
    server = startServer(exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/vnd.openpolicyagent.bundles");
      exchange.getResponseHeaders().add("Content-Length", String.valueOf(bundleData.length));
      exchange.sendResponseHeaders(200, bundleData.length);
      exchange.getResponseBody().write(bundleData);
      exchange.close();
    });

    runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
        .get(10, TimeUnit.SECONDS);
    assertNotNull(store.getBundles().get("authz"));
  }

  @Test
  void download_notModified_succeeds() throws Exception {
    server = startServer(exchange -> {
      exchange.sendResponseHeaders(304, -1);
      exchange.close();
    });

    runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
        .get(10, TimeUnit.SECONDS);
    // 304 means "not modified" — no bundle should have been activated into the store.
    assertNull(store.getBundles().get("authz"));
  }

  @Test
  void download_serverError_fails() throws Exception {
    server = startServer(exchange -> {
      exchange.sendResponseHeaders(500, -1);
      exchange.close();
    });

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
            .get(10, TimeUnit.SECONDS));
    assertTrue(ex.getCause().getMessage().contains("500"));
  }

  @Test
  void download_notFound_fails() throws Exception {
    server = startServer(exchange -> {
      exchange.sendResponseHeaders(404, -1);
      exchange.close();
    });

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
            .get(10, TimeUnit.SECONDS));
    assertTrue(ex.getCause().getMessage().contains("404"));
  }

  @Test
  void download_connectionDroppedMidStream_fails() throws Exception {
    byte[] bundleData = createValidBundle();
    server = startServer(exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/vnd.openpolicyagent.bundles");
      exchange.sendResponseHeaders(200, bundleData.length * 2L);
      // Write partial data then abruptly close, simulating a dropped connection
      exchange.getResponseBody().write(bundleData);
      exchange.getResponseBody().flush();
      exchange.close();
    });

    assertThrows(ExecutionException.class,
        () -> runBundleDownload(server.getAddress().getPort(), 512 * 1024 * 1024L)
            .get(10, TimeUnit.SECONDS));
  }

  private CompletableFuture<Void> runBundleDownload(int port, long maxSizeBytes) {
    Config config = new Config();
    Config.ServiceConfig service =
        new Config.ServiceConfig()
            .setName("test-service")
            .setUrl("http://localhost:" + port);
    config.setServices(Collections.singletonMap("test-service", service));
    Config.BundleConfig bundleCfg =
        new Config.BundleConfig()
            .setService("test-service")
            .setResource("/bundle.tar.gz")
            .setMaxSizeBytes(maxSizeBytes);
    config.setBundles(Collections.singletonMap("authz", bundleCfg));

    Logger logger = new Logger.StandardLogger();
    this.store = new InMem();
    PluginManager manager =
        new PluginManager.Builder()
            .withId("test")
            .withStore(store)
            .withConfig(config)
            .withLogger(logger)
            .build();

    BundlePlugin bundlePlugin = (BundlePlugin) new BundlePlugin().initialize(manager);
    manager.registerPlugin("bundles", bundlePlugin);
    bundlePlugin.start();

    return bundlePlugin.getBundle("authz").getInitialActivation()
        .whenComplete((v, t) -> bundlePlugin.stop());
  }

  private HttpServer startServer(HttpHandler handler) throws IOException {
    HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    httpServer.createContext("/bundle.tar.gz", handler);
    httpServer.start();
    return httpServer;
  }

  private static byte[] createValidBundle() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {
      String planJson =
          "{\"static\":{\"strings\":[],\"files\":[]},"
              + "\"plans\":{\"plans\":[{\"name\":\"main/main\",\"blocks\":[]}]},"
              + "\"funcs\":{\"funcs\":[]}}";
      byte[] planBytes = planJson.getBytes();
      TarArchiveEntry plan = new TarArchiveEntry("plan.json");
      plan.setSize(planBytes.length);
      tarOut.putArchiveEntry(plan);
      tarOut.write(planBytes);
      tarOut.closeArchiveEntry();
      tarOut.finish();
    }
    return byteOut.toByteArray();
  }

}
