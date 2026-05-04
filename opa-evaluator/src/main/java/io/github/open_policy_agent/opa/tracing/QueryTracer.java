package io.github.open_policy_agent.opa.tracing;

import java.util.List;

public interface QueryTracer {
  void TraceEvent(Event event);

  List<Event> getEvents();
}
