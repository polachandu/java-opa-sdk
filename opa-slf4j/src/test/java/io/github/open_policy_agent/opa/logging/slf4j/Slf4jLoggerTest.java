package io.github.open_policy_agent.opa.logging.slf4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Slf4jLoggerTest {

    private static final String LOGGER_NAME = "io.github.open_policy_agent.opa.test";

    private ListAppender listAppender;
    private Slf4jLogger slf4jLogger;

    @BeforeEach
    void setUp() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        listAppender = new ListAppender("TestListAppender");
        listAppender.start();

        LoggerConfig loggerConfig = new LoggerConfig(LOGGER_NAME, org.apache.logging.log4j.Level.DEBUG, true);
        loggerConfig.addAppender(listAppender, org.apache.logging.log4j.Level.DEBUG, null);
        config.addLogger(LOGGER_NAME, loggerConfig);
        ctx.updateLoggers();

        slf4jLogger = new Slf4jLogger(LOGGER_NAME);
    }

    @AfterEach
    void tearDown() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        config.removeLogger(LOGGER_NAME);
        ctx.updateLoggers();
        listAppender.stop();
    }

    @Test
    void debug_logsFormattedMessage() {
        slf4jLogger.debug("Hello %s", "world");
        assertThat(listAppender.getMessages()).containsExactly("Hello world");
    }

    @Test
    void info_logsFormattedMessage() {
        slf4jLogger.info("Count: %d", 42);
        assertThat(listAppender.getMessages()).containsExactly("Count: 42");
    }

    @Test
    void warn_logsFormattedMessage() {
        slf4jLogger.warn("%s has %d items", "cart", 3);
        assertThat(listAppender.getMessages()).containsExactly("cart has 3 items");
    }

    @Test
    void error_logsFormattedMessage() {
        slf4jLogger.error("Failed: %s (code %d)", "timeout", 504);
        assertThat(listAppender.getMessages()).containsExactly("Failed: timeout (code 504)");
    }

    @Test
    void eachLevel_logsAtCorrectLevel() {
        slf4jLogger.debug("d");
        slf4jLogger.info("i");
        slf4jLogger.warn("w");
        slf4jLogger.error("e");

        assertThat(listAppender.getEvents())
                .extracting(LogEvent::getLevel)
                .containsExactly(
                        org.apache.logging.log4j.Level.DEBUG,
                        org.apache.logging.log4j.Level.INFO,
                        org.apache.logging.log4j.Level.WARN,
                        org.apache.logging.log4j.Level.ERROR
                );
    }

    @Test
    void disabledLevel_doesNotLog() {
        // Reconfigure logger to WARN level only
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        config.removeLogger(LOGGER_NAME);

        LoggerConfig loggerConfig = new LoggerConfig(LOGGER_NAME, org.apache.logging.log4j.Level.WARN, false);
        loggerConfig.addAppender(listAppender, org.apache.logging.log4j.Level.WARN, null);
        config.addLogger(LOGGER_NAME, loggerConfig);
        ctx.updateLoggers();

        slf4jLogger.debug("should not appear");
        slf4jLogger.info("should not appear");
        slf4jLogger.warn("should appear");
        slf4jLogger.error("should appear");

        assertThat(listAppender.getMessages()).containsExactly("should appear", "should appear");
    }

    @Test
    void defaultConstructor_usesDefaultLoggerName() {
        // Configure the default logger name
        String defaultName = "io.github.open_policy_agent.opa";
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        ListAppender defaultAppender = new ListAppender("DefaultAppender");
        defaultAppender.start();

        LoggerConfig loggerConfig = new LoggerConfig(defaultName, org.apache.logging.log4j.Level.DEBUG, true);
        loggerConfig.addAppender(defaultAppender, org.apache.logging.log4j.Level.DEBUG, null);
        config.addLogger(defaultName, loggerConfig);
        ctx.updateLoggers();

        try {
            Slf4jLogger defaultLogger = new Slf4jLogger();
            defaultLogger.info("default logger message");
            assertThat(defaultAppender.getMessages()).containsExactly("default logger message");
        } finally {
            config.removeLogger(defaultName);
            ctx.updateLoggers();
            defaultAppender.stop();
        }
    }

    @Test
    void customLoggerName_isHonored() {
        String customName = "my.custom.logger";
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        ListAppender customAppender = new ListAppender("CustomAppender");
        customAppender.start();

        LoggerConfig loggerConfig = new LoggerConfig(customName, org.apache.logging.log4j.Level.DEBUG, true);
        loggerConfig.addAppender(customAppender, org.apache.logging.log4j.Level.DEBUG, null);
        config.addLogger(customName, loggerConfig);
        ctx.updateLoggers();

        try {
            Slf4jLogger customLogger = new Slf4jLogger(customName);
            customLogger.info("custom message");
            assertThat(customAppender.getMessages()).containsExactly("custom message");
        } finally {
            config.removeLogger(customName);
            ctx.updateLoggers();
            customAppender.stop();
        }
    }

    /**
     * Simple in-memory appender for capturing log events in tests.
     */
    private static class ListAppender extends AbstractAppender {
        private final List<LogEvent> events = Collections.synchronizedList(new ArrayList<>());

        ListAppender(String name) {
            super(name, null, null, true, null);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }

        List<LogEvent> getEvents() {
            return Collections.unmodifiableList(new ArrayList<>(events));
        }

        List<String> getMessages() {
            List<String> messages = new ArrayList<>();
            for (LogEvent event : events) {
                messages.add(event.getMessage().getFormattedMessage());
            }
            return messages;
        }
    }
}