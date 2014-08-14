/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Marc-Andre Laperle
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow.ControlFlowView;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesView;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.SWTBotUtil;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions.ConditionHelpers;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot Smoke test for LTTng Kernel UI.
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ImportAndReadKernelSmokeTest {

    private static final String KERNEL_PERSPECTIVE_ID = "org.eclipse.linuxtools.lttng2.kernel.ui.perspective";
    private static final String TRACE_PROJECT_NAME = "test";

    private static SWTWorkbenchBot fBot;
    private static CtfTmfTestTrace ctt = CtfTmfTestTrace.SYNTHETIC_TRACE;
    private ITmfEvent fDesired1;
    private ITmfEvent fDesired2;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        SWTBotUtil.failIfUIThread();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
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
        switchKernelPerspective();
        /* Finish waiting for eclipse to load */
        SWTBotUtil.waitForJobs();
    }

    private static void switchKernelPerspective() {
        final Exception retE[] = new Exception[1];
        if (!UIThreadRunnable.syncExec(new BoolResult() {
            @Override
            public Boolean run() {
                try {
                    PlatformUI.getWorkbench().showPerspective(KERNEL_PERSPECTIVE_ID,
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
        SWTBotUtil.createProject(TRACE_PROJECT_NAME);
        openTrace();
        openEditor();
        testHV(getViewPart("Histogram"));
        testCFV((ControlFlowView) getViewPart("Control Flow"));
        testRV((ResourcesView) getViewPart("Resources"));

        fBot.closeAllEditors();
        SWTBotUtil.deleteProject(TRACE_PROJECT_NAME, fBot);
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
                    TmfOpenTraceHelper.openTraceFromPath(destinationFolder, ctt.getPath(), PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.eclipse.linuxtools.lttng2.kernel.tracetype");
                } catch (CoreException e) {
                    exception[0] = e;
                }
            }
        });
        if (exception[0] != null) {
            fail(exception[0].getMessage());
        }

        SWTBotUtil.delay(1000);
        SWTBotUtil.waitForJobs();
    }

    private void openEditor() {
        Matcher<IEditorReference> matcher = WidgetMatcherFactory.withPartName(ctt.getTrace().getName());
        IEditorPart iep = fBot.editor(matcher).getReference().getEditor(true);
        fDesired1 = getEvent(100);
        fDesired2 = getEvent(10000);
        final TmfEventsEditor tmfEd = (TmfEventsEditor) iep;
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                tmfEd.setFocus();
                tmfEd.selectionChanged(new SelectionChangedEvent(tmfEd, new StructuredSelection(fDesired1)));
            }
        });

        SWTBotUtil.waitForJobs();
        SWTBotUtil.delay(1000);
        assertNotNull(tmfEd);
    }

    private static void testCFV(ControlFlowView vp) {
        assertNotNull(vp);
    }

    private void testHV(IViewPart vp) {
        SWTBotView hvBot = (new SWTWorkbenchBot()).viewById(HistogramView.ID);
        List<SWTBotToolbarButton> hvTools = hvBot.getToolbarButtons();
        for (SWTBotToolbarButton hvTool : hvTools) {
            if (hvTool.getToolTipText().toLowerCase().contains("lost")) {
                hvTool.click();
            }
        }
        HistogramView hv = (HistogramView) vp;
        final TmfTimeSynchSignal signal = new TmfTimeSynchSignal(hv, fDesired1.getTimestamp());
        final TmfTimeSynchSignal signal2 = new TmfTimeSynchSignal(hv, fDesired2.getTimestamp());
        hv.updateTimeRange(100000);
        SWTBotUtil.waitForJobs();
        hv.currentTimeUpdated(signal);
        hv.broadcast(signal);
        SWTBotUtil.waitForJobs();
        SWTBotUtil.delay(1000);

        hv.updateTimeRange(1000000000);
        SWTBotUtil.waitForJobs();
        hv.currentTimeUpdated(signal2);
        hv.broadcast(signal2);
        SWTBotUtil.waitForJobs();
        SWTBotUtil.delay(1000);
        assertNotNull(hv);
    }

    private static void testRV(ResourcesView vp) {
        assertNotNull(vp);
    }

    private static CtfTmfEvent getEvent(int rank) {
        try (CtfTmfTrace trace = CtfTmfTestTrace.SYNTHETIC_TRACE.getTrace()) {
            ITmfContext ctx = trace.seekEvent(0);
            for (int i = 0; i < rank; i++) {
                trace.getNext(ctx);
            }
            return trace.getNext(ctx);
        }

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
}
