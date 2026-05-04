package io.github.openpolicyagent.opa.plugins;

import java.util.Set;

public interface Plugin {

  Set<String> validate(PluginManager manager);

  Plugin initialize(PluginManager manager);

  void start();

  void stop();
}
