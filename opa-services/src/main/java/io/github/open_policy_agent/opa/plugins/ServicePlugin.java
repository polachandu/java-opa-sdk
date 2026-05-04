package io.github.open_policy_agent.opa.plugins;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import io.github.open_policy_agent.opa.config.Config;
import io.github.open_policy_agent.opa.logging.Logger;

public final class ServicePlugin implements Plugin {

  private final Map<String, Service> services = new HashMap<>();
  private PluginManager manager;

  public ServicePlugin() {}

    public Set<String> validate(PluginManager manager) {
    Set<String> errors = new HashSet<>();

    if (manager.getConfig().getServices() == null || manager.getConfig().getServices().isEmpty()) {
      return errors; // No services configured is valid
    }

    for (Map.Entry<String, Config.ServiceConfig> entry :
        manager.getConfig().getServices().entrySet()) {
      String serviceName = entry.getKey();
      Config.ServiceConfig service = entry.getValue();

      // Set the name from the map key if not already set
      if (service.getName() == null || service.getName().isEmpty()) {
        service.setName(serviceName);
      }

      if (service.getUrl() == null || service.getUrl().isEmpty()) {
        errors.add("Service '" + serviceName + "' has missing or empty URL");
      }
      Credential credential = getCredential(service);
      if (credential != null && credential.getType().equals(Credential.Type.NOT_SUPPORTED)) {
        errors.add("Service '" + serviceName + "' has unsupported credential type");
      }
    }
    return errors;
  }

  public Plugin initialize(PluginManager manager) {
    ServicePlugin plugin = new ServicePlugin();
    plugin.manager = manager;

    if (manager.getConfig().getServices() == null || manager.getConfig().getServices().isEmpty()) {
      return plugin;
    }

    for (Map.Entry<String, Config.ServiceConfig> entry :
        manager.getConfig().getServices().entrySet()) {
      String serviceName = entry.getKey();
      Config.ServiceConfig service = entry.getValue();

      // Set the name from the map key if not already set
      if (service.getName() == null || service.getName().isEmpty()) {
        service.setName(serviceName);
      }

      // Build HttpClient with optional insecure TLS support
      HttpClient.Builder clientBuilder =
          HttpClient.newBuilder()
              .followRedirects(HttpClient.Redirect.NORMAL)
              .version(HttpClient.Version.HTTP_2)
              .connectTimeout(Duration.ofSeconds(service.getResponseHeaderTimeoutSeconds()));

      // Configure insecure TLS if enabled (for development/testing only)
      if (service.isAllowInsecureTLS()) {
        try {
          SSLContext sslContext = SSLContext.getInstance("TLS");
          sslContext.init(
              null,
              new TrustManager[] {
                new X509TrustManager() {
                  public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                  public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                  public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                  }
                }
              },
              new SecureRandom());

          clientBuilder.sslContext(sslContext);
          manager
              .getLogger()
              .warn(
                  "Service '%s' has insecure TLS enabled - this should only be used in development",
                  serviceName);
        } catch (Exception e) {
          throw new PluginInitializationException(
                  "Failed to configure insecure TLS for service '" + serviceName + "'", e)
              .withContext("serviceName", serviceName);
        }
      }

      HttpClient client = clientBuilder.build();

      plugin.services.put(
          service.getName(),
          new Service(client, manager.getLogger())
              .setName(service.getName())
              .setUrl(service.getUrl())
              .setCredentials(getCredential(service)));
    }

    return plugin;
  }

  public void start() {
    // Services plugin is ready immediately as it's stateless
    manager.updatePluginStatus("services", PluginManager.Status.OK);
  }

  @Override
  public void stop() {
    // Services plugin has no resources to clean up (no scheduler)
    manager.getLogger().info("Stopping services plugin...");
  }

  /**
   * Get a service by name.
   *
   * @param serviceName the name of the service
   * @return the Service instance or null if not found
   */
  public Service getService(String serviceName) {
    return services.get(serviceName);
  }

  private Credential getCredential(Config.ServiceConfig service) {
    if (service.getCredentials() == null) {
      return null;
    }

    if (service.getCredentials().getBearer() != null) {
      return new BearerCredential().setToken(service.getCredentials().getBearer().getToken());
    }
    return new Credential() {
      @Override
      public Type getType() {
        return Type.NOT_SUPPORTED;
      }

      @Override
      public Builder modifyRequest(Builder builder) {
        throw new UnsupportedOperationException("Credential Not supported.");
      }
    };
  }

  interface Credential {

    Type getType();

    Builder modifyRequest(Builder builder);

    enum Type {
      BEARER,
      NOT_SUPPORTED
    }
  }

  public static class Service {
    private final Logger logger;
    private String name;
    private String url;
    private int responseHeaderTimeoutSeconds = 10;
    private boolean allowInsecureTLS = false;
    private Credential credentials;
    private final HttpClient client;

    private Service(HttpClient client, Logger logger) {
      this.client = client;
      this.logger = logger;
    }

    void post(String path, String body) {

      Builder builder =
          HttpRequest.newBuilder()
              .uri(buildUri(path))
              .header("Content-Type", "application/json")
              .header("Accept", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body));

      if (credentials != null) {
        builder = credentials.modifyRequest(builder);
      }
      HttpRequest request = builder.build();

      client
          .sendAsync(request, HttpResponse.BodyHandlers.ofString())
          .thenAccept(
              resp -> {
                logger.debug("POST request sent successfully: " + resp.statusCode());
              })
          .exceptionally(
              (e) -> {
                logger.error("Failed to send POST request: " + e.getMessage());
                return null;
              });
    }

    /**
     * Build a properly formatted URI by combining the base URL with the path.
     *
     * <p>Handles trailing/leading slashes and validates the resulting URI.
     *
     * @param path the path to append to the base URL
     * @return a normalized URI
     * @throws IllegalArgumentException if the resulting URI is invalid
     */
    private URI buildUri(String path) {
      String baseUrl = getUrl();

      // Handle null or empty path
      if (path == null || path.isEmpty()) {
        path = "/";
      }

      // Ensure proper slash handling
      if (baseUrl.endsWith("/") && path.startsWith("/")) {
        path = path.substring(1);
      } else if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
        path = "/" + path;
      }

      try {
        return URI.create(baseUrl + path).normalize();
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Invalid URI: " + baseUrl + path + " - " + e.getMessage(), e);
      }
    }

    public HttpClient getClient() {
      return client;
    }

    public String getName() {
      return name;
    }

    public Service setName(String name) {
      this.name = name;
      return this;
    }

    public String getUrl() {
      return url;
    }

    public Service setUrl(String url) {
      this.url = url;
      return this;
    }

    public int getResponseHeaderTimeoutSeconds() {
      return responseHeaderTimeoutSeconds;
    }

    public Service setResponseHeaderTimeoutSeconds(int responseHeaderTimeoutSeconds) {
      this.responseHeaderTimeoutSeconds = responseHeaderTimeoutSeconds;
      return this;
    }

    public boolean isAllowInsecureTls() {
      return allowInsecureTLS;
    }

    public Service setAllowInsecureTls(boolean allowInsecureTls) {
      this.allowInsecureTLS = allowInsecureTls;
      return this;
    }

    public Credential getCredentials() {
      return credentials;
    }

    public Service setCredentials(Credential credentials) {
      this.credentials = credentials;
      return this;
    }
  }

  static class BearerCredential implements Credential {
    private String token;
    private String tokenPath;
    private String scheme = "Bearer";

    private BearerCredential() {}

    @Override
    public Type getType() {
      return Type.BEARER;
    }

    @Override
    public Builder modifyRequest(Builder builder) {
      if (token != null) {
        builder.header("Authorization", scheme + " " + token);
      }
      return builder;
    }

    public String getToken() {
      return token;
    }

    public BearerCredential setToken(String token) {
      this.token = token;
      return this;
    }

    public String getTokenPath() {
      return tokenPath;
    }

    public BearerCredential setTokenPath(String tokenPath) {
      this.tokenPath = tokenPath;
      return this;
    }

    public String getScheme() {
      return scheme;
    }

    public BearerCredential setScheme(String scheme) {
      this.scheme = scheme;
      return this;
    }
  }
}
