/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for logger (line sensitive!)
 *
 * @author Matthew Khouzam
 */
public class LoggerTest {

    private StringOutputStream fLog;
    private Logger fLogger;
    private StreamHandler fStreamHandler;

    private static String eventWithNoTs(String event) {
        return event.replaceFirst("\\\"ts\\\"\\:\\d+", "\"ts\":0");
    }

    private static String eventUnifyId(String event) {
        return event.replaceFirst("\\\"id\\\"\\:\\\"0x[0-9A-Fa-f]+\\\"", "\"id\":\"0x1234\"");
    }

    private static class StringOutputStream extends OutputStream {

        private List<String> fMessages = new ArrayList<>();
        private StringBuilder sb = new StringBuilder();
        private boolean secondLine = false;
        private boolean start = true;

        @Override
        public void write(int b) throws IOException {
            if (b != '\n') {
                if (secondLine) {
                    if (start) {
                        sb.append((char) b);
                    }
                    if (b == ',') {
                        start = true;
                    }
                }
            } else {
                if (secondLine) {
                    fMessages.add(eventUnifyId(eventWithNoTs(sb.toString())));
                    sb = new StringBuilder();
                    secondLine = false;
                } else {
                    secondLine = true;
                }
            }
        }

        public List<String> getMessages() {
            return fMessages;
        }
    }

    /**
     * Set up logger
     */
    @Before
    public void before() {
        fLogger = Logger.getAnonymousLogger();
        fLog = new StringOutputStream();
        fStreamHandler = new StreamHandler(fLog, new SimpleFormatter());
        fStreamHandler.setLevel(Level.FINER);
        fLogger.setLevel(Level.ALL);
        fLogger.addHandler(fStreamHandler);
    }

    /**
     * Test simple logging
     */
    @Test
    public void testHelloWorld() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.INFO, "world")) {
            // do something
            new Object();
        }
        fStreamHandler.flush();
        assertEquals("INFO: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"world\"}", fLog.getMessages().get(0));
        assertEquals("INFO: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(1));
    }

    /**
     * Test nesting
     */
    @Test
    public void testNesting() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.INFO, "foo")) {
            // do something
            new Object();
            try (TraceCompassLogUtils.ScopeLog log1 = new TraceCompassLogUtils.ScopeLog(logger, Level.INFO, "bar")) {
                // do something
                new Object();
            }
        }
        fStreamHandler.flush();
        assertEquals("INFO: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"foo\"}", fLog.getMessages().get(0));
        assertEquals("INFO: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"bar\"}", fLog.getMessages().get(1));
        assertEquals("INFO: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(2));
        assertEquals("INFO: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(3));
    }

    /**
     * Test nesting with filtering
     */
    @Test
    public void testNestingFiltered() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.FINE, "foo")) {
            // do something
            new Object();
            try (TraceCompassLogUtils.ScopeLog log1 = new TraceCompassLogUtils.ScopeLog(logger, Level.FINER, "bar")) {
                // do something
                new Object();
                try (TraceCompassLogUtils.ScopeLog log2 = new TraceCompassLogUtils.ScopeLog(logger, Level.FINEST, "baz")) {
                    // do something
                    new Object();
                }
            }
        }
        fStreamHandler.flush();
        assertEquals("FINE: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"foo\"}", fLog.getMessages().get(0));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"bar\"}", fLog.getMessages().get(1));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(2));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(3));
    }

    /**
     * Test nesting with different loglevels
     */
    @Test
    public void testNestingLogLevels() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.WARNING, "foo")) {
            try (TraceCompassLogUtils.ScopeLog log1 = new TraceCompassLogUtils.ScopeLog(logger, Level.FINE, "bar")) {
                // do something
                new Object();
            }
        }
        fStreamHandler.flush();
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"foo\"}",
                fLog.getMessages().get(0));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"bar\"}", fLog.getMessages().get(1));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(2));
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(3));
    }

    /**
     * Test nesting with additional data
     */
    @Test
    public void testNestingWithData() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.WARNING, "foo")) {
            try (TraceCompassLogUtils.ScopeLog log1 = new TraceCompassLogUtils.ScopeLog(logger, Level.FINE, "bar")) {
                // do something
                log1.addData("return", false);
            }
        }
        fStreamHandler.flush();
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"foo\"}",
                fLog.getMessages().get(0));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"bar\"}", fLog.getMessages().get(1));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"E\",\"tid\":1,\"args\":{\"return\":\"false\"}}", fLog.getMessages().get(2));
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(3));
    }

    /**
     * Test flow with filtering
     */
    @Test
    public void testFlowFiltered() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (FlowScopeLog log = new FlowScopeLog(logger, Level.FINE, "foo", "mycat")) {
            // do something
            new Object();
            try (FlowScopeLog log1 = new FlowScopeLog(logger, Level.FINER, "bar", log)) {
                // do something
                new Object();
                try (FlowScopeLog log2 = new FlowScopeLog(logger, Level.FINEST, "baz", log1)) {
                    // do something
                    new Object();
                }
            }
        }
        fStreamHandler.flush();
        assertEquals("FINE: {\"ts\":0,\"ph\":\"s\",\"tid\":1,\"name\":\"foo\",\"cat\":\"mycat\",\"id\":\"0x1234\"}", fLog.getMessages().get(0));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"s\",\"tid\":1,\"name\":\"bar\",\"cat\":\"mycat\",\"id\":\"0x1234\"}", fLog.getMessages().get(1));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"f\",\"tid\":1,\"cat\":\"mycat\",\"id\":\"0x1234\"}", fLog.getMessages().get(2));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"f\",\"tid\":1,\"cat\":\"mycat\",\"id\":\"0x1234\"}", fLog.getMessages().get(3));
    }

    /**
     * Test flow with different loglevels
     */
    @Test
    public void testFlowLogLevels() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (FlowScopeLog log = new FlowScopeLog(logger, Level.WARNING, "foo", "mydog")) {
            try (FlowScopeLog log1 = new FlowScopeLog(logger, Level.FINE, "bar", log)) {
                log1.step("barked");
                new Object();
            }
        }
        fStreamHandler.flush();
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"s\",\"tid\":1,\"name\":\"foo\",\"cat\":\"mydog\",\"id\":\"0x1234\"}",
                fLog.getMessages().get(0));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"s\",\"tid\":1,\"name\":\"bar\",\"cat\":\"mydog\",\"id\":\"0x1234\"}", fLog.getMessages().get(1));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"t\",\"tid\":1,\"name\":\"barked\",\"cat\":\"mydog\",\"id\":\"0x1234\"}", fLog.getMessages().get(2));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"f\",\"tid\":1,\"cat\":\"mydog\",\"id\":\"0x1234\"}", fLog.getMessages().get(3));
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"f\",\"tid\":1,\"cat\":\"mydog\",\"id\":\"0x1234\"}", fLog.getMessages().get(4));
    }

    /**
     * Test flow with different loglevels
     */
    @Test
    public void testFlowWithData() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (FlowScopeLog log = new FlowScopeLog(logger, Level.WARNING, "foo", "myspider")) {
            try (FlowScopeLog log1 = new FlowScopeLog(logger, Level.FINE, "bar", log)) {
                // do something
                log1.addData("return", false);
            }
        }
        fStreamHandler.flush();
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"s\",\"tid\":1,\"name\":\"foo\",\"cat\":\"myspider\",\"id\":\"0x1234\"}",
                fLog.getMessages().get(0));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"s\",\"tid\":1,\"name\":\"bar\",\"cat\":\"myspider\",\"id\":\"0x1234\"}", fLog.getMessages().get(1));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"f\",\"tid\":1,\"cat\":\"myspider\",\"id\":\"0x1234\",\"args\":{\"return\":\"false\"}}", fLog.getMessages().get(2));
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"f\",\"tid\":1,\"cat\":\"myspider\",\"id\":\"0x1234\"}", fLog.getMessages().get(3));
    }

    /**
     * Test nesting with different arguments
     */
    @Test
    public void testAttributes() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.WARNING, "foo", "Pen:Pineapple", "Apple:Pen")) {
            // do something
            new Object();
        }
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.WARNING, "foo", "Pen:Pineapple:Apple:Pen")) {
            // do something
            new Object();
        }
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.WARNING, "foo", "pen", "pineapple", "apple", "pen", "number_of_badgers", 12)) {
            // do something
            new Object();
        }
        fStreamHandler.flush();
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"foo\",\"args\":{\"Pen:Pineapple\":\"Apple:Pen\"}}",
                fLog.getMessages().get(0));
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(1));

        assertEquals("WARNING: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"foo\",\"args\":{\"msg\":\"Pen:Pineapple:Apple:Pen\"}}",
                fLog.getMessages().get(2));
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(3));

        assertEquals("WARNING: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"foo\",\"args\":{\"pen\":\"pineapple\",\"apple\":\"pen\",\"number_of_badgers\":12}}",
                fLog.getMessages().get(4));
        assertEquals("WARNING: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(5));
    }

    /**
     * Test with an odd number of args.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAttributeFail3Args() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.WARNING, "foo", "Pen:Pineapple", "Apple", "Pen")) {
            // do something
            new Object();
        }
    }

    /**
     * Test with a repeating key.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAttributeFailRepeatedArgs() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.WARNING, "foo", "badger", "badger", "badger", "badger")) {
            // do something
            new Object();
        }
    }

    /**
     * Test nesting with an exception
     */
    @Test
    public void testNestingException() {
        Logger logger = fLogger;
        assertNotNull(logger);
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(logger, Level.INFO, "foo")) {
            try (TraceCompassLogUtils.ScopeLog log1 = new TraceCompassLogUtils.ScopeLog(logger, Level.INFO, "bar")) {
                // do something
                new Object();
                throw new Exception("test");
            }
        } catch (Exception e) {
            assertEquals("test", e.getMessage());
        }
        fStreamHandler.flush();
        assertEquals("INFO: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"foo\"}", fLog.getMessages().get(0));
        assertEquals("INFO: {\"ts\":0,\"ph\":\"B\",\"tid\":1,\"name\":\"bar\"}", fLog.getMessages().get(1));
        assertEquals("INFO: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(2));
        assertEquals("INFO: {\"ts\":0,\"ph\":\"E\",\"tid\":1}", fLog.getMessages().get(3));
    }

    private static final class LivingObject {

        private final @NonNull Logger fLog;

        public LivingObject(@NonNull Logger logger) {
            fLog = logger;
            TraceCompassLogUtils.traceObjectCreation(fLog, Level.FINE, this);
        }

        @Override
        protected void finalize() throws Throwable {
            TraceCompassLogUtils.traceObjectDestruction(fLog, Level.FINE, this);
            super.finalize();
        }

    }

    /**
     * Test two objects lifecycles
     *
     * @throws Throwable
     *             error in finalizes
     */
    @Test
    public void testObjectLifespan() throws Throwable {
        Logger logger = fLogger;
        assertNotNull(logger);
        {
            LivingObject first = new LivingObject(logger);
            LivingObject second = new LivingObject(logger);
            assertNotNull(first);
            assertNotNull(second);
            // This will surely trigger some static analysis. This is for
            // testing purposes.
            first.finalize();
            second.finalize();
        }

        fStreamHandler.flush();
        assertEquals("FINE: {\"ts\":0,\"ph\":\"N\",\"tid\":1,\"name\":\"LivingObject\",\"id\":\"0x1234\"}", fLog.getMessages().get(0));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"N\",\"tid\":1,\"name\":\"LivingObject\",\"id\":\"0x1234\"}", fLog.getMessages().get(1));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"D\",\"tid\":1,\"name\":\"LivingObject\",\"id\":\"0x1234\"}", fLog.getMessages().get(2));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"D\",\"tid\":1,\"name\":\"LivingObject\",\"id\":\"0x1234\"}", fLog.getMessages().get(3));
    }

    /**
     * Test two objects lifecycles
     */
    @Test
    public void testCollectionLifespan() {
        Logger logger = fLogger;
        assertNotNull(logger);
        {
            List<String> avengers = new ArrayList<>();
            int uniqueID = TraceCompassLogUtils.traceObjectCreation(logger, Level.FINE, avengers);
            avengers.add("Cap");
            avengers.add("Arrow");
            avengers.add("Thor");
            avengers.add("Iron");
            TraceCompassLogUtils.traceObjectDestruction(logger, Level.FINE, avengers, uniqueID);

        }

        fStreamHandler.flush();
        assertEquals("FINE: {\"ts\":0,\"ph\":\"N\",\"tid\":1,\"name\":\"ArrayList\",\"id\":\"0x1234\"}", fLog.getMessages().get(0));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"D\",\"tid\":1,\"name\":\"ArrayList\",\"id\":\"0x1234\"}", fLog.getMessages().get(1));
    }

    /**
     * Test instant events
     */
    @Test
    public void testInstant() {
        Logger logger = fLogger;
        assertNotNull(logger);
        TraceCompassLogUtils.traceInstant(logger, Level.FINE, "hello", "foo", "bar");
        fStreamHandler.flush();
        assertEquals("FINE: {\"ts\":0,\"ph\":\"i\",\"tid\":1,\"name\":\"hello\",\"args\":{\"foo\":\"bar\"}}", fLog.getMessages().get(0));
    }

    /**
     * Test asynchronous messages
     */
    @Test
    public void testAsyncMessages() {
        Logger logger = fLogger;
        assertNotNull(logger);
        TraceCompassLogUtils.traceAsyncStart(logger, Level.FINE, "network connect", "net", 10);
        TraceCompassLogUtils.traceAsyncStart(logger, Level.FINER, "network lookup", "net", 10);
        TraceCompassLogUtils.traceAsyncNested(logger, Level.FINER, "network cache", "net", 10);
        // anon message
        TraceCompassLogUtils.traceAsyncStart(logger, Level.FINER, null, null, 0);
        TraceCompassLogUtils.traceAsyncEnd(logger, Level.FINER, null, null, 0);

        TraceCompassLogUtils.traceAsyncEnd(logger, Level.FINER, "network lookup", "net", 10, "OK");
        TraceCompassLogUtils.traceAsyncEnd(logger, Level.FINE, "network connect", "net", 10, "OK");

        fStreamHandler.flush();
        assertEquals("FINE: {\"ts\":0,\"ph\":\"b\",\"tid\":1,\"name\":\"network connect\",\"cat\":\"net\",\"id\":\"0x1234\"}", fLog.getMessages().get(0));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"b\",\"tid\":1,\"name\":\"network lookup\",\"cat\":\"net\",\"id\":\"0x1234\"}", fLog.getMessages().get(1));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"n\",\"tid\":1,\"name\":\"network cache\",\"cat\":\"net\",\"id\":\"0x1234\"}", fLog.getMessages().get(2));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"b\",\"tid\":1,\"id\":\"0x1234\"}", fLog.getMessages().get(3));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"e\",\"tid\":1,\"id\":\"0x1234\"}", fLog.getMessages().get(4));
        assertEquals("FINER: {\"ts\":0,\"ph\":\"e\",\"tid\":1,\"name\":\"network lookup\",\"cat\":\"net\",\"id\":\"0x1234\",\"args\":{\"msg\":\"OK\"}}", fLog.getMessages().get(5));
        assertEquals("FINE: {\"ts\":0,\"ph\":\"e\",\"tid\":1,\"name\":\"network connect\",\"cat\":\"net\",\"id\":\"0x1234\",\"args\":{\"msg\":\"OK\"}}", fLog.getMessages().get(6));
    }

}
