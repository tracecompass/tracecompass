/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.pcap.ui.swtbot.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.tracecompass.internal.tmf.pcap.core.trace.PcapTrace;
import org.eclipse.tracecompass.internal.tmf.pcap.ui.NetworkingPerspectiveFactory;
import org.eclipse.tracecompass.internal.tmf.pcap.ui.stream.StreamListView;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.pcap.core.tests.shared.PcapTmfTestTrace;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot Smoke test for Pcap UI.
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ImportAndReadPcapTest {

    private static final String NETWORK_PERSPECTIVE_ID = NetworkingPerspectiveFactory.ID;
    private static final String TRACE_PROJECT_NAME = "test";

    private static SWTWorkbenchBot fBot;
    private ITmfEvent fDesired1;
    private static PcapTmfTestTrace pttt = PcapTmfTestTrace.BENCHMARK_TRACE;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {

        SWTBotUtils.initialize();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 300000; /* 300 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        final List<SWTBotView> openViews = fBot.views();
        for (SWTBotView view : openViews) {
            if (view.getTitle().equals("Welcome")) {
                view.close();
                fBot.waitUntil(ConditionHelpers.ViewIsClosed(view));
            }
        }
        /* Switch perspectives */
        switchNetworkPerspective();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Test Class teardown
     */
    @AfterClass
    public static void terminate() {
        fLogger.removeAllAppenders();
    }

    private static void switchNetworkPerspective() {
        final Exception retE[] = new Exception[1];
        if (!UIThreadRunnable.syncExec(new BoolResult() {
            @Override
            public Boolean run() {
                try {
                    PlatformUI.getWorkbench().showPerspective(NETWORK_PERSPECTIVE_ID,
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                } catch (WorkbenchException e) {
                    retE[0] = e;
                    return false;
                }
                return true;
            }
        })) {
            fail(retE[0].getMessage());
        }

    }

    /**
     * Main test case
     */
    @Test
    public void test() {
        assumeTrue(pttt.exists());
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        openTrace();
        openEditor();
        testHV(getViewPart("Histogram"));
        testStreamView(getViewPartRef("Stream List"));
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }

    private void testStreamView(IViewReference viewPart) {
        SWTBotView botView = new SWTBotView(viewPart, fBot);
        StreamListView slv = (StreamListView) getViewPart("Stream List");
        botView.setFocus();
        SWTBotTree botTree = fBot.tree();
        assertNotNull(botTree);
        final TmfSelectionRangeUpdatedSignal signal = new TmfSelectionRangeUpdatedSignal(slv, fDesired1.getTimestamp());
        slv.broadcast(signal);
        WaitUtils.waitForJobs();
        // FIXME This is a race condition:
        // TmfEventsTable launches an async exec that may be run after the wait
        // for jobs. This last delay catches it.
        SWTBotUtils.delay(1000);

    }

    private static void openTrace() {
        final Exception exception[] = new Exception[1];
        exception[0] = null;
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                try {
                    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(TRACE_PROJECT_NAME);
                    TmfTraceFolder destinationFolder = TmfProjectRegistry.getProject(project, true).getTracesFolder();
                    String absolutePath = (new File(pttt.getTrace().getPath())).getAbsolutePath();
                    TmfOpenTraceHelper.openTraceFromPath(destinationFolder, absolutePath, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.eclipse.linuxtools.tmf.pcap.core.pcaptrace");
                } catch (CoreException e) {
                    exception[0] = e;
                }
            }
        });
        if (exception[0] != null) {
            fail(exception[0].getMessage());
        }

        SWTBotUtils.delay(1000);
        WaitUtils.waitForJobs();
    }

    private void openEditor() {
        final List<IEditorReference> editorRefs = new ArrayList<>();
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IEditorReference[] ieds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                editorRefs.addAll(Arrays.asList(ieds));
            }

        });
        assertFalse(editorRefs.isEmpty());
        IEditorPart iep = null;
        for (IEditorReference ied : editorRefs) {
            if (ied.getTitle().equals(pttt.getTrace().getName())) {
                iep = ied.getEditor(true);
                break;
            }
        }
        assertNotNull(iep);
        fDesired1 = getEvent(100);
        final TmfEventsEditor tmfEd = (TmfEventsEditor) iep;
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                tmfEd.setFocus();
                tmfEd.selectionChanged(new SelectionChangedEvent(tmfEd, new StructuredSelection(fDesired1)));
            }
        });

        WaitUtils.waitForJobs();
        SWTBotUtils.delay(1000);
        assertNotNull(tmfEd);
    }

    private static void testHV(IViewPart vp) {
        assertNotNull(vp);
    }

    private static ITmfEvent getEvent(int rank) {
        PcapTrace trace = pttt.getTrace();
        ITmfContext ctx = trace.seekEvent(0);
        for (int i = 0; i < rank; i++) {
            trace.getNext(ctx);
        }
        ITmfEvent ret = trace.getNext(ctx);
        trace.dispose();
        return ret;
    }

    private static IViewPart getViewPart(final String viewTile) {
        final IViewPart[] vps = new IViewPart[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IViewReference[] viewRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
                for (IViewReference viewRef : viewRefs) {
                    IViewPart vp = viewRef.getView(true);
                    if (vp.getTitle().equals(viewTile)) {
                        vps[0] = vp;
                        return;
                    }
                }
            }
        });

        return vps[0];
    }

    private static IViewReference getViewPartRef(final String viewTile) {
        final IViewReference[] vrs = new IViewReference[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IViewReference[] viewRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
                for (IViewReference viewRef : viewRefs) {
                    IViewPart vp = viewRef.getView(true);
                    if (vp.getTitle().equals(viewTile)) {
                        vrs[0] = viewRef;
                        return;
                    }
                }
            }
        });

        return vrs[0];
    }
}
