/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.analysis.debuginfo;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryFile;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoAnalysisModule;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryAspect;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstEvent;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Tests for the {@link UstDebugInfoAnalysisModule}
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoAnalysisModuleTest {

    private static final @NonNull CtfTestTrace TEST_TRACE = CtfTestTrace.DEBUG_INFO3;
    private static final @NonNull CtfTestTrace INVALID_TRACE = CtfTestTrace.CYG_PROFILE;

    private LttngUstTrace fTrace;
    private UstDebugInfoAnalysisModule fModule;

    /**
     * Test setup
     */
    @Before
    public void setup() {
        fModule = new UstDebugInfoAnalysisModule();
        fTrace = new LttngUstTrace();
        try {
            fTrace.initTrace(null, CtfTmfTestTraceUtils.getTrace(TEST_TRACE).getPath(), CtfTmfEvent.class);
        } catch (TmfTraceException e) {
            /* Should not happen if tracesExist() passed */
            throw new RuntimeException(e);
        }
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fModule.dispose();
        fTrace = null;
        fModule = null;
    }

    /**
     * Test for {@link UstDebugInfoAnalysisModule#getAnalysisRequirements()}
     */
    @Test
    public void testGetAnalysisRequirements() {
        Iterable<TmfAbstractAnalysisRequirement> requirements = fModule.getAnalysisRequirements();
        assertNotNull(requirements);
        assertTrue(Iterables.isEmpty(requirements));
    }

    /**
     * Test that the analysis can execute on a valid trace.
     */
    @Test
    public void testCanExecute() {
        assertNotNull(fTrace);
        assertTrue(fModule.canExecute(fTrace));
    }

    /**
     * Test that the analysis correctly refuses to execute on an invalid trace
     * (LTTng-UST < 2.8 in this case).
     *
     * @throws TmfTraceException
     *             Should not happen
     */
    @Test
    public void testCannotExcecute() throws TmfTraceException {
        LttngUstTrace invalidTrace = new LttngUstTrace();
        invalidTrace.initTrace(null, CtfTmfTestTraceUtils.getTrace(INVALID_TRACE).getPath(), CtfTmfEvent.class);
        assertFalse(fModule.canExecute(invalidTrace));

        invalidTrace.dispose();
    }

    private void executeModule() {
        assertNotNull(fTrace);
        try {
            fModule.setTrace(fTrace);
        } catch (TmfAnalysisException e) {
            fail();
        }
        fModule.schedule();
        fModule.waitForCompletion();
    }

    /**
     * Test that basic execution of the module works well.
     */
    @Test
    public void testExecution() {
        executeModule();
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNotNull(ss);
    }

    /**
     * Test that the binary callsite aspect resolves correctly for some
     * user-defined tracepoints in the trace.
     *
     * These should be available even without the binaries with debug symbols
     * being present on the system.
     */
    @Test
    public void testBinaryCallsites() {
        assertNotNull(fTrace);
        /*
         * Fake a "trace opened" signal, so that the relevant analyses are
         * started.
         */
        TmfTraceOpenedSignal signal = new TmfTraceOpenedSignal(this, fTrace, null);
        TmfSignalManager.dispatchSignal(signal);

        /* Send a request to get the 3 events we are interested in */
        List<@NonNull LttngUstEvent> events = new ArrayList<>();
        TmfEventRequest request = new TmfEventRequest(LttngUstEvent.class, 287, 3, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                events.add((LttngUstEvent) event);
            }
        };
        fTrace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

        /* Tests that the aspects are resolved correctly */
        final UstDebugInfoBinaryAspect aspect = UstDebugInfoBinaryAspect.INSTANCE;

        String actual = checkNotNull(aspect.resolve(events.get(0))).toString();
        String expected = "/home/alexandre/src/lttng-project/lttng/trace-utils/dynamicLinking/libhello.so+0x15ae";
        assertEquals(expected, actual);

        actual = checkNotNull(aspect.resolve(events.get(1))).toString();
        expected = "/home/alexandre/src/lttng-project/lttng/trace-utils/dynamicLinking/libhello.so+0x1680";
        assertEquals(expected, actual);

        actual = checkNotNull(aspect.resolve(events.get(2))).toString();
        expected = "/home/alexandre/src/lttng-project/lttng/trace-utils/dynamicLinking/libhello.so+0x1745";
        assertEquals(expected, actual);
    }

    /**
     * Test the {@link UstDebugInfoAnalysisModule#getAllBinaries} method.
     */
    @Test
    public void testGetAllBinaries() {
        executeModule();
        List<UstDebugInfoBinaryFile> actualBinaries = Lists.newArrayList(fModule.getAllBinaries());
        List<UstDebugInfoBinaryFile> expectedBinaries = Lists.newArrayList(
                new UstDebugInfoBinaryFile("/home/alexandre/src/lttng-project/lttng/trace-utils/dynamicLinking/libhello.so", "c9ac43c6b4251b0789ada66931e33487d65f64a6", true),
                new UstDebugInfoBinaryFile("/home/alexandre/src/lttng-project/lttng/trace-utils/dynamicLinking/main.out", "0ec3294d0cacff93ec30b7161ff21efcd4cdd327", false),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/ld-2.23.so", "edfa6d46e00ca97f349fdd3333d88493d442932c", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libc-2.23.so", "369de0e1d833caa693af17f17c83ba937f0a4dad", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libdl-2.23.so", "a2adf3615338d49c702c41eb83a99ab743d2b574", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libgcc_s.so.1", "68220ae2c65d65c1b6aaa12fa6765a6ec2f5f434", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libglib-2.0.so.0.4800.0", "8b553ecf9133c50f36a7345a4924c5b97e9daa11", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/liblzma.so.5.0.0", "15aed4855920e5a0fb8791b683eb88c7e1199260", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libm-2.23.so", "5c4078c04888a418f3db0868702ecfdb35b3ad8b", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libpcre.so.3.13.2", "390b2228e9a1071bb0be285d77b6669cb37ce628", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libpthread-2.23.so", "b77847cc9cacbca3b5753d0d25a32e5795afe75b", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libresolv-2.23.so", "81ef82040e9877e63adca93b365f52a4bb831ee1", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/librt-2.23.so", "a779dbcb3a477dc0c8d09b60fac7335d396c19df", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libselinux.so.1", "62a57085aa17036efdcecf6655d9e7be566f6948", true),
                new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libz.so.1.2.8", "340b7b463f981b8a0fb3451751f881df1b0c2f74", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/indicator-transfer/indicator-transfer-service", "01c605bcf38d068d77a6e0a02cede6a50c5750b5", false),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/indicator-transfer/libbuteo-transfers.so", "a3a54ba90d7b7b67e0be9baaae3b91675b630a09", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/indicator-transfer/libdmtransfers.so", "ad8ebd563d733818f9f4a399b8d34c1b7e846f3d", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libQt5Core.so.5.5.1", "478d22fbdf6a3e0ec3b499bd2bb8e97dbed84fb6", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libQt5Xml.so.5.5.1", "3a16f2feccfdbca6a40d9bd43ae5a966bca065c7", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libaccounts-glib.so.0.1.3", "3aaeb6f74d07bd2331fa1664a90b11ca5cc1b71e", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libaccounts-qt5.so.1.2.0", "c4c36b5efb4dd5f92a53ed0a2844239f564328d7", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libboost_filesystem.so.1.58.0", "8cebf2501fd917f393ef1390777415fca9289878", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libboost_system.so.1.58.0", "bb484981ddf4152bb6f02ad3a700ec21ca3805e2", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libclick-0.4.so.0.4.0", "79609d999eab77f934f6d7d296074ea54f1410e8", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libffi.so.6.0.4", "9d9c958f1f4894afef6aecd90d1c430ea29ac34f", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libgee-0.8.so.2.5.1", "96aff01de4c5e4817d483de7278c8cc9c6d02001", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libgio-2.0.so.0.4800.0", "01d955da82f10209b515f362b2352d5a0d0b5330", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libgmodule-2.0.so.0.4800.0", "a9190276f6246d3a44ba820510ef7d469ea556b1", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libgobject-2.0.so.0.4800.0", "bce3f25958a5b88c9493990daf0c90a787444c28", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libicudata.so.55.1", "233845f0cd27642c4b2cc43979c8801b52bc00a9", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libicui18n.so.55.1", "7bf3774116dbb992ffcc2156b6f5c44f0a328f00", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libicuuc.so.55.1", "323e4878073bb4e0d7b174ae24e383ec5e05d68a", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libindicator-transfer.so.0.0.2", "4e472120f2a19343e1ff5928459edcb4a6c0b1e2", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libjson-glib-1.0.so.0.102.0", "55c87753a67b0c24922bd091f042b9d8bd4995dc", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/liblttng-ust-dl.so.0.0.0", "450c14d5a7a72780b2d88a3cf73d2e12c2774676", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/liblttng-ust-tracepoint.so.0.0.0", "e70e1748e137bb59b542e15e0722455b998355fc", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/liblttng-ust.so.0.0.0", "101115876419ead3a8bd9a9a08a6d92984015b99", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libmirclient.so.9", "b4c1509382d3ef5f7458db59710b4d30dad86b5d", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libmircommon.so.5", "25b5830854701533323f15195ae9e501ad7f772b", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libmirprotobuf.so.3", "2f6b1a56f1ac43ae001dffde4dc1a24bfee04dec", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libpcre16.so.3.13.2", "2cc13260733c5b35f311725fca88219b338d5390", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libprotobuf-lite.so.9.0.1", "625eb54ba7ccc3af18d3193e22cbe9f23dfb88e5", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libsqlite3.so.0.8.6", "d9782ba023caec26b15d8676e3a5d07b55e121ef", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libstdc++.so.6.0.21", "662f4598d7854c6731f13802486e477a7bdb09c7", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libubuntu-app-launch.so.2.0.0", "1e5753f4feda98548bd230eb07b81e330f419463", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/liburcu-bp.so.4.0.0", "d208fcd0e6e6968b8110d9c487f63ae1a400e7c2", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/liburcu-cds.so.4.0.0", "177fbd0a1b21145ad2f29bf41ba9f45e43b1ae4a", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/liburl-dispatcher.so.1.0.0", "58e7febb6e33418eee311c0bad19aace5cba0fd1", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libxkbcommon.so.0.0.0", "29ebf0cc0837b321e6a751b9b7d43ae9f8f3f0c0", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libxml2.so.2.9.3", "a155c7bc345d0e0b711be09120204bd88f475f9e", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/libzeitgeist-2.0.so.0.0.0", "211049ef6c5ed6e7bbcdc75c0a25d1f11755151d", true),
                new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/url-dispatcher/url-dispatcher", "694fcf16ee9241e66a33de671d10f854312e0c87", false));

        Comparator<UstDebugInfoBinaryFile> comparator = Comparator.comparing(UstDebugInfoBinaryFile::getFilePath);
        actualBinaries.sort(comparator);
        expectedBinaries.sort(comparator);

        /* Highlights failures more easily */
        for (int i = 0; i < expectedBinaries.size(); i++) {
            assertEquals(expectedBinaries.get(i), actualBinaries.get(i));
        }

        assertEquals(actualBinaries, expectedBinaries);
    }

}
