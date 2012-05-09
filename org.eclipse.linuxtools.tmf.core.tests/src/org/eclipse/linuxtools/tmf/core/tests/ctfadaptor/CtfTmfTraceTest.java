package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocation;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfTraceTest</code> contains tests for the class <code>{@link CtfTmfTrace}</code>.
 *
 * @generatedBy CodePro at 03/05/12 2:29 PM
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CtfTmfTraceTest {
    private static final String PATH = TestParams.getPath();

    /**
     * Run the CtfTmfTrace() constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCtfTmfTrace_1()
        throws Exception {

        CtfTmfTrace result = new CtfTmfTrace();

        // add additional test code here
        assertNotNull(result);
        assertEquals(null, result.getEventType());
        assertEquals(50000, result.getCacheSize());
        assertEquals(0L, result.getNbEvents());
        assertEquals(0L, result.getStreamingInterval());
        assertEquals(null, result.getStateSystem());
        assertEquals(null, result.getResource());
        assertEquals(1000, result.getQueueSize());
        assertEquals(null, result.getType());
    }

    /**
     * Run the ITmfContext armRequest(ITmfDataRequest<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testArmRequest_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        ITmfDataRequest<CtfTmfEvent> request = new TmfCoalescedEventRequest(ITmfEvent.class);

        ITmfContext result = fixture.armRequest(request);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }


    /**
     * @return
     * @throws TmfTraceException
     */
    private CtfTmfTrace initTrace() throws TmfTraceException {
        CtfTmfTrace fixture = new CtfTmfTrace();
        fixture.initTrace((IResource) null, PATH, CtfTmfEvent.class);
        return fixture;
    }

    /**
     * Run the ITmfContext armRequest(ITmfDataRequest<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testArmRequest_2()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        ITmfDataRequest<CtfTmfEvent> request = new TmfCoalescedEventRequest(ITmfEvent.class);

        ITmfContext result = fixture.armRequest(request);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext armRequest(ITmfDataRequest<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testArmRequest_3()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        ITmfDataRequest<CtfTmfEvent> request = new TmfCoalescedEventRequest(ITmfEvent.class);

        ITmfContext result = fixture.armRequest(request);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext armRequest(ITmfDataRequest<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testArmRequest_4()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        ITmfDataRequest<CtfTmfEvent> request = new TmfCoalescedEventRequest(ITmfEvent.class);

        ITmfContext result = fixture.armRequest(request);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }


    /**
     * Run the void broadcast(TmfSignal) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testBroadcast_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        TmfSignal signal = new TmfEndSynchSignal(1);

        fixture.broadcast(signal);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
    }


    /**
     * Run the void dispose() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testDispose_1()
        throws Exception {
        CtfTmfTrace fixture = new CtfTmfTrace();

        fixture.dispose();

    }

    /**
     * Run the int getCacheSize() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetCacheSize_1()
        throws Exception {
        CtfTmfTrace fixture = new CtfTmfTrace();

        int result = fixture.getCacheSize();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertEquals(50000, result);
    }

    /**
     * Run the ITmfLocation<Comparable> getCurrentLocation() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetCurrentLocation_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        CtfLocation result = (CtfLocation) fixture.getCurrentLocation();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNull(result);
    }

    @Test
    public void testSeekEventLoc_1() throws TmfTraceException {
        CtfTmfTrace fixture = initTrace();
        CtfLocation loc = null;
        fixture.seekEvent(loc);
        assertNotNull(fixture);
    }

    @Test
    public void testSeekEventLoc_2() throws TmfTraceException {
        CtfTmfTrace fixture = initTrace();
        CtfLocation loc = new CtfLocation(new CtfTmfTimestamp(0L));
        fixture.seekEvent(loc);
        assertNotNull(fixture);
    }


    /**
     * Run the ITmfTimestamp getEndTime() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetEndTime_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        ITmfTimestamp result = fixture.getEndTime();
        assertNotNull(result);
    }

    /**
     * Run the String[] getEnvNames() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetEnvNames_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        String[] result = fixture.getEnvNames();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the String getEnvValue(String) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetEnvValue_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        String key = "tracer_name"; //$NON-NLS-1$

        String result = fixture.getEnvValue(key);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertEquals("\"lttng-modules\"",result); //$NON-NLS-1$
    }

    /**
     * Run the Class<CtfTmfEvent> getEventType() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetEventType_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        Class<CtfTmfEvent> result = fixture.getEventType();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNull(result);
    }

    /**
     * Run the double getLocationRatio(ITmfLocation<?>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetLocationRatio_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        CtfLocation location = new CtfLocation(new Long(1L));
        location.setLocation(new Long(1L));

        double result = fixture.getLocationRatio(location);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertEquals(Double.POSITIVE_INFINITY, result, 0.1);
    }

    /**
     * Run the String getName() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetName_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        String result = fixture.getName();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the String getName() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetName_2()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        String result = fixture.getName();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the String getName() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetName_3()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        String result = fixture.getName();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the int getNbEnvVars() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetNbEnvVars_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        int result = fixture.getNbEnvVars();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertEquals(8, result);
    }

    /**
     * Run the long getNbEvents() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetNbEvents_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        long result = fixture.getNbEvents();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertEquals(0L, result);
    }

    /**
     * Run the CtfTmfEvent getNext(ITmfContext) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetNext_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        ITmfContext context = fixture.seekEvent(0);

        CtfTmfEvent result = fixture.getNext(context);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetPath_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        String result = fixture.getPath();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the IResource getResource() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetResource_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        IResource result = fixture.getResource();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNull(result);
    }

    /**
     * Run the ITmfTimestamp getStartTime() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetStartTime_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        ITmfTimestamp result = fixture.getStartTime();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the IStateSystemQuerier getStateSystem() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetStateSystem_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IStateSystemQuerier result = fixture.getStateSystem();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNull(result);
    }

    /**
     * Run the long getStreamingInterval() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetStreamingInterval_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        long result = fixture.getStreamingInterval();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertEquals(0L, result);
    }

    /**
     * Run the TmfTimeRange getTimeRange() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetTimeRange_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        TmfTimeRange result = fixture.getTimeRange();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the void initTrace(IResource,String,Class<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testInitTrace_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        IResource resource = null;
        String path = PATH;
        Class<CtfTmfEvent> eventType = CtfTmfEvent.class;

        fixture.initTrace(resource, path, eventType);

        assertNotNull(fixture);
    }

    /**
     * Run the void initTrace(IResource,String,Class<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testInitTrace_2()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IResource resource = null;
        String path = PATH;
        Class<CtfTmfEvent> eventType = CtfTmfEvent.class;

        fixture.initTrace(resource, path, eventType);

        assertNotNull(fixture);
    }

    /**
     * Run the void initTrace(IResource,String,Class<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testInitTrace_3()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IResource resource = null;
        String path = PATH;
        Class<CtfTmfEvent> eventType = CtfTmfEvent.class;

        fixture.initTrace(resource, path, eventType);

        assertNotNull(fixture);
    }

    /**
     * Run the void initTrace(IResource,String,Class<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testInitTrace_4()
        throws Exception {
        CtfTmfTrace fixture = initTrace();

        IResource resource = null;
        String path = PATH;
        Class<CtfTmfEvent> eventType = CtfTmfEvent.class;

        fixture.initTrace(resource, path, eventType);

        assertNotNull(fixture);
    }

    /**
     * Run the void initTrace(IResource,String,Class<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testInitTrace_5()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IResource resource = null;
        String path = PATH;
        Class<CtfTmfEvent> eventType = CtfTmfEvent.class;

        fixture.initTrace(resource, path, eventType);

        assertNotNull(fixture);
    }

    /**
     * Run the void initTrace(IResource,String,Class<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testInitTrace_6()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IResource resource = null;
        String path = PATH;
        Class<CtfTmfEvent> eventType = CtfTmfEvent.class;

        fixture.initTrace(resource, path, eventType);

        assertNotNull(fixture);
    }

    /**
     * Run the void initTrace(IResource,String,Class<CtfTmfEvent>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testInitTrace_7()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IResource resource = null;
        String path = PATH;
        Class<CtfTmfEvent> eventType = CtfTmfEvent.class;

        fixture.initTrace(resource, path, eventType);

        assertNotNull(fixture);
    }

    /**
     * Run the CtfTmfEvent readNextEvent(ITmfContext) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testReadNextEvent_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        ITmfContext context = fixture.seekEvent(0);

        CtfTmfEvent result = fixture.readNextEvent(context);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(double) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSeekEvent_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        double ratio = 1.0;

        ITmfContext result = fixture.seekEvent(ratio);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(long) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSeekEvent_2()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        long rank = 1L;

        ITmfContext result = fixture.seekEvent(rank);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(ITmfTimestamp) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSeekEvent_3()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        ITmfTimestamp timestamp = new TmfTimestamp();

        ITmfContext result = fixture.seekEvent(timestamp);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertNotNull(result);
    }

//    /**
//     * Run the ITmfContext seekEvent(ITmfLocation<?>) method test.
//     *
//     * @throws Exception
//     *
//     * @generatedBy CodePro at 03/05/12 2:29 PM
//     */
//    @Test
//    public void testSeekEvent_4()
//        throws Exception {
//        CtfTmfTrace fixture = initTrace();
//        fixture.setStartTime(new TmfTimestamp());
//        CtfIterator ctfIterator = new CtfIterator(new CtfTmfTrace());
//        CtfLocation ctfLocation = new CtfLocation(new Long(1L));
//        ctfLocation.setLocation(new Long(1L));
//        ctfIterator.setLocation(ctfLocation);
//        fixture.iterator = ctfIterator;
//        fixture.ss = new StateHistorySystem(new HistoryTreeBackend(new File(PATH)), true);
//        fixture.startSynch(new TmfStartSynchSignal(1));
//        fixture.fNbEvents = 1L;
//        ITmfLocation<Comparable> location = new CtfLocation(new Long(1L));
//
//        ITmfContext result = fixture.seekEvent(location);
//
//        // add additional test code here
//        // An unexpected exception was thrown in user code while executing this test:
//        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
//        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
//        assertNotNull(result);
//    }
//
//    /**
//     * Run the ITmfContext seekEvent(ITmfLocation<?>) method test.
//     *
//     * @throws Exception
//     *
//     * @generatedBy CodePro at 03/05/12 2:29 PM
//     */
//    @Test
//    public void testSeekEvent_5()
//        throws Exception {
//        CtfTmfTrace fixture = initTrace();
//        CtfIterator ctfIterator = new CtfIterator(new CtfTmfTrace());
//        CtfLocation ctfLocation = new CtfLocation(new Long(1L));
//        ITmfContext result = fixture.seekEvent(ctfLocation);
//        assertNotNull(result);
//    }



    /**
     * Run the boolean validate(IProject,String) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testValidate_1()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IProject project = null;
        String path = PATH;

        boolean result = fixture.validate(project, path);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertTrue(result);
    }

    /**
     * Run the boolean validate(IProject,String) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testValidate_2()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IProject project = null;
        String path = PATH;

        boolean result = fixture.validate(project, path);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertTrue(result);
    }

    /**
     * Run the boolean validate(IProject,String) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testValidate_3()
        throws Exception {
        CtfTmfTrace fixture = initTrace();
        IProject project = null;
        String path = PATH;

        boolean result = fixture.validate(project, path);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException: Path must be a valid directory
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace.initTrace(CtfTmfTrace.java:98)
        assertTrue(result);
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws Exception
     *         if the initialization fails for some reason
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Before
    public void setUp()
        throws Exception {
        // add additional set up code here
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception
     *         if the clean-up fails for some reason
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @After
    public void tearDown()
        throws Exception {
        // Add additional tear down code here
    }

    /**
     * Launch the test.
     *
     * @param args the command line arguments
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfTmfTraceTest.class);
    }
}