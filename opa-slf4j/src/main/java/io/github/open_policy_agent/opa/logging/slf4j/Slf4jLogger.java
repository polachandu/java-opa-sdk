package io.github.open_policy_agent.opa.logging.slf4j;

import io.github.open_policy_agent.opa.logging.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogger implements Logger {

    private final org.slf4j.Logger logger;

    /** Creates an Slf4jLogger using the default logger name. */
    public Slf4jLogger() {
        this(LoggerFactory.getLogger("io.github.open_policy_agent.opa"));
    }

    /** Creates an Slf4jLogger with a custom logger name. */
    public Slf4jLogger(String name) {
        this(LoggerFactory.getLogger(name));
    }

    /** Creates an Slf4jLogger wrapping an existing SLF4J Logger instance. */
    public Slf4jLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void debug(String fmtMessage, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(fmtMessage, args));
        }
    }

    @Override
    public void info(String fmtMessage, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format(fmtMessage, args));
        }
    }

    @Override
    public void warn(String fmtMessage, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(String.format(fmtMessage, args));
        }
    }

    @Override
    public void error(String fmtMessage, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(String.format(fmtMessage, args));
        }
    }
}