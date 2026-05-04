package io.github.open_policy_agent.opa.plugins;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.open_policy_agent.opa.config.Config;
import io.github.open_policy_agent.opa.logging.Logger;
import io.github.open_policy_agent.opa.storage.InMem;
import io.github.open_policy_agent.opa.storage.Store;

/**
 * Comprehensive unit tests for ServicePlugin.
 *
 * <p>Tests validation, initialization, credential configuration, and HTTP client setup.
 */
class ServicePluginTest {

  private PluginManager manager;
  private Logger mockLogger;
  private Store store;
  private Config config;

  @BeforeEach
  void setUp() {
    mockLogger = mock(Logger.class);
    store = new InMem();
    config = new Config();
  }

  @Test
  void validate_noServicesConfigured_returnsNoErrors() {
    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Set<String> errors = plugin.validate(manager);

    assertTrue(errors.isEmpty());
  }

  @Test
  void validate_missingUrl_returnsError() {
    Config.ServiceConfig service = new Config.ServiceConfig().setName("test-service");
    config.setServices(Collections.singletonMap("test-service", service));

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Set<String> errors = plugin.validate(manager);

    assertFalse(errors.isEmpty());
    assertTrue(errors.stream().anyMatch(e -> e.contains("missing or empty URL")));
  }

  @Test
  void validate_validServiceWithUrl_returnsNoErrors() {
    Config.ServiceConfig service =
        new Config.ServiceConfig().setName("test-service").setUrl("https://example.com");
    config.setServices(Collections.singletonMap("test-service", service));

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Set<String> errors = plugin.validate(manager);

    assertTrue(errors.isEmpty());
  }

  @Test
  void validate_validServiceWithBearerToken_returnsNoErrors() {
    Config.ServiceConfig service =
        new Config.ServiceConfig().setName("test-service").setUrl("https://example.com");

    Config.CredentialsConfig credentials = new Config.CredentialsConfig();
    credentials.setBearer(new Config.BearerConfig().setToken("test-token"));
    service.setCredentials(credentials);

    config.setServices(Collections.singletonMap("test-service", service));

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Set<String> errors = plugin.validate(manager);

    assertTrue(errors.isEmpty());
  }

  @Test
  void validate_serviceNameFromMapKey_setsName() {
    // Service without name set - should be set from map key
    Config.ServiceConfig service = new Config.ServiceConfig().setUrl("https://example.com");
    config.setServices(Collections.singletonMap("my-service", service));

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Set<String> errors = plugin.validate(manager);

    assertTrue(errors.isEmpty());
    assertEquals("my-service", service.getName());
  }

  @Test
  void initialize_noServices_returnsPlugin() {
    // Empty services map
    config.setServices(new HashMap<>());

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Plugin initialized = plugin.initialize(manager);

    assertNotNull(initialized);
    assertInstanceOf(ServicePlugin.class, initialized);
  }

  @Test
  void initialize_withServices_createsHttpClients() {
    Config.ServiceConfig service1 =
        new Config.ServiceConfig()
            .setName("service1")
            .setUrl("https://example1.com")
            .setResponseHeaderTimeoutSeconds(30);

    Config.ServiceConfig service2 =
        new Config.ServiceConfig()
            .setName("service2")
            .setUrl("https://example2.com")
            .setAllowInsecureTLS(true);

    Map<String, Config.ServiceConfig> services = new HashMap<>();
    services.put("service1", service1);
    services.put("service2", service2);
    config.setServices(services);

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Plugin initialized = plugin.initialize(manager);

    assertNotNull(initialized);
    assertInstanceOf(ServicePlugin.class, initialized);
  }

  @Test
  void initialize_withBearerCredentials_setsCredentials() {
    Config.ServiceConfig service =
        new Config.ServiceConfig().setName("test-service").setUrl("https://example.com");

    Config.CredentialsConfig credentials = new Config.CredentialsConfig();
    credentials.setBearer(new Config.BearerConfig().setToken("my-secret-token"));
    service.setCredentials(credentials);

    config.setServices(Collections.singletonMap("test-service", service));

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Plugin initialized = plugin.initialize(manager);

    assertNotNull(initialized);
    assertInstanceOf(ServicePlugin.class, initialized);
  }

  @Test
  void start_setsStatusOk() {
    Config.ServiceConfig service =
        new Config.ServiceConfig().setName("test-service").setUrl("https://example.com");
    config.setServices(Collections.singletonMap("test-service", service));

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    plugin = (ServicePlugin) plugin.initialize(manager);
    plugin.start();

    assertEquals(PluginManager.Status.OK, manager.getPluginStatus("services"));
  }

  @Test
  void validate_multipleServices_allValid_returnsNoErrors() {
    Map<String, Config.ServiceConfig> services = new HashMap<>();

    services.put(
        "service1", new Config.ServiceConfig().setName("service1").setUrl("https://example1.com"));

    services.put(
        "service2", new Config.ServiceConfig().setName("service2").setUrl("https://example2.com"));

    services.put(
        "service3", new Config.ServiceConfig().setName("service3").setUrl("https://example3.com"));

    config.setServices(services);

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Set<String> errors = plugin.validate(manager);

    assertTrue(errors.isEmpty());
  }

  @Test
  void initialize_nullServices_returnsPlugin() {
    // Minimal config: new Config() with no services set (getServices() returns null)
    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Plugin initialized = plugin.initialize(manager);

    assertNotNull(initialized);
    assertInstanceOf(ServicePlugin.class, initialized);
  }

  @Test
  void validate_multipleServices_oneInvalid_returnsError() {
    Map<String, Config.ServiceConfig> services = new HashMap<>();

    services.put(
        "service1", new Config.ServiceConfig().setName("service1").setUrl("https://example1.com"));

    services.put("service2", new Config.ServiceConfig().setName("service2")); // Missing URL

    config.setServices(services);

    manager =
        new PluginManager.Builder()
            .withId("test-opa")
            .withStore(store)
            .withConfig(config)
            .withLogger(mockLogger)
            .build();

    ServicePlugin plugin = new ServicePlugin();
    Set<String> errors = plugin.validate(manager);

    assertFalse(errors.isEmpty());
    assertTrue(errors.stream().anyMatch(e -> e.contains("service2")));
    assertTrue(errors.stream().anyMatch(e -> e.contains("missing or empty URL")));
  }
}
