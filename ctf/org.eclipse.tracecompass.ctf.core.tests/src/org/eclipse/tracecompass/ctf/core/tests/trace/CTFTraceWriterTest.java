/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceUtils;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceWriter;
import org.eclipse.tracecompass.internal.ctf.core.trace.Utils;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * CTFTraceWriter test cases
 *
 * @author Bernd Hufmann
 *
 */
@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class CTFTraceWriterTest {

    private static File fTempDir;

        // Trace details
        private static final long CLOCK_OFFSET = 1332166405241713987L;
        private static final int TOTAL_NB_EVENTS = 695319;
        private static final long LAST_EVENT_TIME = 1332170692664579801L;

        // Stream 0 values
        private static final long STREAM0_FIRST_PACKET_TIME = CLOCK_OFFSET + 4277170993912L;
        private static final long STREAM0_FIRST_EVENT_TIME = 1332170682440316151L;
        private static final long STREAM0_LAST_EVENT_TIME = 1332170682702066969L;
        private static final int STREAM0_FIRST_PACKET_NB_EVENTS = 14219;

        // Stream 1 values
        private static final long STREAM1_FIRST_PACKET_TIME = CLOCK_OFFSET + 4277171555436L;
        private static final int STREAM1_FIRST_PACKET_NB_EVENTS = 8213;
        private static final long STREAM1_FIRST_EVENT_TIME = 1332170682440133097L;
        private static final long STREAM1_FIFTH_PACKET_TIME = CLOCK_OFFSET + 4277970712221L;
        private static final long STREAM1_TENTH_PACKET_TIME = CLOCK_OFFSET + 4279440048309L;
        private static final long STREAM1_FIFTH_PACKET_FIRST_EVENT_TIME = 1332170682702069762L;
        private static final long STREAM1_TENTH_PACKET_LAST_EVENT_TIME = 1332170685256508077L;

        // Miscellaneous
        private static final int NB_EVENTS_SEVERAL_PACKETS = 167585;

        // Test parameters
        private String fName;
        private long fStartTime;
        private long fEndTime;
        private int fNbEvents;
        private long fFirstEventTime;
        private long fLastEventTime;

    /**
     * Gets a list of test case parameters.
     *
     * @return The list of test parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getTestParams() {
        final List<Object[]> params = new LinkedList<>();

        addParams(params, "WHOLE_TRACE",
                            0,
                            Long.MAX_VALUE,
                            TOTAL_NB_EVENTS,
                            STREAM1_FIRST_EVENT_TIME,
                            LAST_EVENT_TIME);

        addParams(params, "NO_EVENTS_USING_INVERTED_TIME",
                            Long.MAX_VALUE, Long.MIN_VALUE,
                            0,
                            -1,
                            -1);

        addParams(params, "STREAM0_FIRST_PACKET_TIME",
                            STREAM0_FIRST_PACKET_TIME,
                            STREAM0_FIRST_PACKET_TIME,
                            STREAM0_FIRST_PACKET_NB_EVENTS,
                            STREAM0_FIRST_EVENT_TIME,
                            STREAM0_LAST_EVENT_TIME);

        addParams(params, "BOTH_STREAMS_FIRST_PACKET_ONLY",
                            STREAM0_FIRST_PACKET_TIME,
                            STREAM1_FIRST_PACKET_TIME,
                            STREAM0_FIRST_PACKET_NB_EVENTS + STREAM1_FIRST_PACKET_NB_EVENTS,
                            STREAM1_FIRST_EVENT_TIME,
                            STREAM0_LAST_EVENT_TIME);

        addParams(params, "BOTH_STREAMS_SEVERAL_PACKETS",
                STREAM1_FIFTH_PACKET_TIME,
                STREAM1_TENTH_PACKET_TIME,
                NB_EVENTS_SEVERAL_PACKETS,
                STREAM1_FIFTH_PACKET_FIRST_EVENT_TIME,
                STREAM1_TENTH_PACKET_LAST_EVENT_TIME);

        return params;
    }

    private static void addParams(List<Object[]> params, String name, long startTime, long endTime, int nbEvents, long firstEventTime, long lastEventTime) {
        Object array[] = new Object[] { name, startTime, endTime, nbEvents, firstEventTime, lastEventTime };
        params.add(array);
    }

    @BeforeClass
    public static void beforeClass() {
        String property = System.getProperty("osgi.instance.area"); //$NON-NLS-1$
        File dir = null;
        if (property != null) {
            try {
                dir = URIUtil.toFile(URIUtil.fromString(property));
                dir = new File(dir.getAbsolutePath() + File.separator);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            } catch (URISyntaxException e) {
            }
        }
        if (dir == null) {
            dir = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$)
        }
        String tempDir = dir.getAbsolutePath() + File.separator + "testcases" + File.separator;
        fTempDir = new File(tempDir);
        if (!fTempDir.exists()) {
            fTempDir.mkdirs();
        }
    }

    public CTFTraceWriterTest (String name, long startTime, long endTime, int nbEvents, long firstEventTime, long lastEventTime) {
        fName = name;
        fStartTime = startTime;
        fEndTime = endTime;
        fNbEvents = nbEvents;
        fFirstEventTime = firstEventTime;
        fLastEventTime = lastEventTime;
    }

    /**
     * Test various time ranges
     */
    @Test
    public void testKernelTrace() {
            try {
                CTFTrace trace = CtfTestTraceUtils.getTrace(CtfTestTrace.KERNEL);
                CTFTraceWriter ctfWriter = new CTFTraceWriter(checkNotNull(trace));
                String traceName = createTraceName(fName);
                ctfWriter.copyPackets(fStartTime, fEndTime, traceName);

                File metadata = new File(traceName + Utils.SEPARATOR + "metadata");
                assertTrue("metadata", metadata.exists());

                CTFTrace outTrace = new CTFTrace(traceName);
                int count = 0;
                Long start = null;
                long end = 0;
                try (CTFTraceReader reader = new CTFTraceReader(outTrace)) {
                    while(reader.hasMoreEvents()) {
                        count++;
                        IEventDefinition def = reader.getCurrentEventDef();
                        end = def.getTimestamp();
                        if (start == null) {
                            start = reader.getStartTime();
                        }
                        reader.advance();
                    }
                    end = outTrace.getClock().getClockOffset() + end;
                }

                if (fFirstEventTime >= 0) {
                    assertEquals("first event time", Long.valueOf(fFirstEventTime), start);
                }
                if (fLastEventTime >= 0) {
                    assertEquals("last event time", fLastEventTime, end);
                }
                assertEquals(toString(), fNbEvents, count);

                if (fNbEvents == 0) {
                    assertFalse("channel0", getChannelFile(traceName, 0).exists());
                    assertFalse("channel1", getChannelFile(traceName, 1).exists());
                }

            } catch (CTFException e) {
                fail(e.getMessage());
            }
    }

    private static File getChannelFile(String path, int id) {
        File channel = new File(path + Utils.SEPARATOR + "channel_" + String.valueOf(id));
        return channel;
    }

    private static String createTraceName(String testCase) {
        return fTempDir.getAbsolutePath() + File.separator + testCase.toString();
    }

}
