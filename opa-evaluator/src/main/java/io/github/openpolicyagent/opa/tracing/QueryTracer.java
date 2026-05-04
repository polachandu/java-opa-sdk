package io.github.openpolicyagent.opa.tracing;

import java.util.List;

public interface QueryTracer {
  void TraceEvent(Event event);

  List<Event> getEvents();
}
