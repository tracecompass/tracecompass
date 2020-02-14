/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.XYDataProviderBaseTest;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.eclipse.ui.IViewPart;
import org.junit.Test;

/**
 * SWTBot tests for pin & clone for xy-charts using Counters view example.
 *
 * @author Bernd Hufmann
 * @deprecated replaced by {@link NewCounterViewPinAndCloneTest}
 */
@Deprecated
public class CounterViewPinAndCloneTest extends XYDataProviderBaseTest {

    private static final String TRACETYPE_ID = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String TRACE_NAME = "kernel_vm";
    private static final @NonNull String COUNTERS_VIEW_TITLE = "Counters (Legacy)";
    private static final @NonNull String MAIN_SERIES_NAME = "kernel_vm/Ungrouped/minor_faults";
    private static final @NonNull String COUNTERS_VIEW_ID = "org.eclipse.tracecompass.analysis.counters.ui.view.counters";
    private static final String PINNED_TO_TRACE_COUNTERS_VIEW_TITLE = COUNTERS_VIEW_TITLE + " <" + TRACE_NAME + ">";
    private static final String PINNED_TO_CTX_SWITCH_VIEW_TITLE = COUNTERS_VIEW_TITLE + " <context-switches-kernel>";

    private static final String PIN_VIEW_BUTTON_NAME = "Pin View";
    private static final String UNPIN_VIEW_BUTTON_NAME = "Unpin View";
    private static final String PIN_TO_PREFIX = "Pin to ";
    private static final String NEW_COUNTER_STACK_MENU = "New " + COUNTERS_VIEW_TITLE + " view";
    private static final String PINNED_TO_PREFIX = "pinned to ";
    private static final String CLONED_TRACE_SUFFIX = " | 2";
    private static final String CLONED_VIEW_TITLE_NAME = COUNTERS_VIEW_TITLE + " <" + TRACE_NAME + " | 2>";

    private static final int SECOND = 1000000000;
    private static final long KERNEL_START = 1363700740555978750L;
    private static final long KERNEL_TEST_START = KERNEL_START + SECOND;
    private static final long KERNEL_TEST_END = KERNEL_START + 2 * SECOND;
    private static final long KERNEL_TEST_INITIAL_END = KERNEL_START + 100000000L;

    private static final long KERNEL_INITIAL_END = KERNEL_START + 100000000L;

    private static final @NonNull TmfTimeRange RANGE = new TmfTimeRange(TmfTimestamp.fromNanos(KERNEL_TEST_START), TmfTimestamp.fromNanos(KERNEL_TEST_END));

    /**
     * Ensure the data displayed in the chart viewer reflects the tree viewer's
     * selected entries.
     */
    @Test
    public void testPinSingleTrace() {
        SWTBotView originalViewBot = getSWTBotView();

        // ensure that the view name is correct before
        assertEquals(COUNTERS_VIEW_TITLE, originalViewBot.getTitle());

        // ensure that the pin drop down is present, pin the view.
        fBot.waitUntil(new DefaultCondition() {
            WidgetNotFoundException fException;

            @Override
            public boolean test() throws Exception {
                try {
                    SWTBotToolbarDropDownButton toolbarDropDownButton = originalViewBot.toolbarDropDownButton(PIN_VIEW_BUTTON_NAME);
                    toolbarDropDownButton.menuItem(PIN_TO_PREFIX + getTestTrace().getName()).click();
                    return true;
                } catch (WidgetNotFoundException e) {
                    fException = e;
                    return false;
                }
            }

            @Override
            public String getFailureMessage() {
                return "Traces not available in toolbar drop down menu: " + fException;
            }
        });

        // Ensure that the view has been renamed. Get the view by title and ensure i    private static final int SECOND = 1000000000;
        // has the same widget as there is a renaming bug.
        assertOriginalViewTitle(PINNED_TO_TRACE_COUNTERS_VIEW_TITLE);

        originalViewBot.toolbarButton(UNPIN_VIEW_BUTTON_NAME).click();

        // Ensure that the view has been renamed. Get the view by title and ensure it
        // has the same widget as there is a renaming bug.
        assertOriginalViewTitle(COUNTERS_VIEW_TITLE);

        // Ensure that the pin button is present, pin the view.
        originalViewBot.toolbarButton(PIN_VIEW_BUTTON_NAME).click();

        // Ensure that the view has been renamed. Get the view by title and ensure it
        // has the same widget as there is a renaming bug.
        assertOriginalViewTitle(PINNED_TO_TRACE_COUNTERS_VIEW_TITLE);

        // Ensure that the pin button is present, unpin the view.
        originalViewBot.toolbarButton(UNPIN_VIEW_BUTTON_NAME).click();
        assertOriginalViewTitle(COUNTERS_VIEW_TITLE);
    }

    /**
     * Test the behavior with two traces.
     */
    @Test
    public void testPinTwoTraces() {
        SWTBotView originalViewBot = getSWTBotView();

        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(activeTrace);
        ITmfTrace kernelTestTrace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.CONTEXT_SWITCHES_KERNEL);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, kernelTestTrace.getPath(), TRACETYPE_ID);

        /// Finish waiting for the trace to index
        WaitUtils.waitForJobs();
        // wait for the editor to be ready.
        fBot.editorByTitle(kernelTestTrace.getName());

        // Assert that the pin to drop down menuItems are present for both traces.
        fBot.waitUntil(new DefaultCondition() {
            WidgetNotFoundException fException;

            @Override
            public boolean test() throws Exception {
                try {
                    SWTBotToolbarDropDownButton toolbarDropDownButton = originalViewBot.toolbarDropDownButton(PIN_VIEW_BUTTON_NAME);
                    toolbarDropDownButton.menuItem(PIN_TO_PREFIX + kernelTestTrace.getName());
                    toolbarDropDownButton.menuItem(PIN_TO_PREFIX + getTestTrace().getName()).click();
                    return true;
                } catch (WidgetNotFoundException e) {
                    fException = e;
                    return false;
                }
            }

            @Override
            public String getFailureMessage() {
                return "Traces not available in toolbar drop down menu: " + fException;
            }
        });

        /*
         * Assert that the pinned view is the kernel_vm trace despite the active trace being
         * the context-switch trace.
         */
        assertOriginalViewTitle(PINNED_TO_TRACE_COUNTERS_VIEW_TITLE);
        activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull("There should be an active trace", activeTrace);
        assertEquals("context-switches-kernel should be the active trace", kernelTestTrace.getName(), activeTrace.getName());

        // Get the window range of the kernel trace
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace kernelTrace = traceManager.getActiveTrace();
        assertNotNull(kernelTrace);

        // Switch back and forth
        SWTBotUtils.activateEditor(fBot, getTestTrace().getName());
        assertOriginalViewTitle(PINNED_TO_TRACE_COUNTERS_VIEW_TITLE);

        SWTBotUtils.activateEditor(fBot, kernelTestTrace.getName());
        assertOriginalViewTitle(PINNED_TO_TRACE_COUNTERS_VIEW_TITLE);

        IViewPart viewPart = originalViewBot.getViewReference().getView(false);
        assertTrue(viewPart instanceof TmfChartView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewPart);
        assertNotNull(chartViewer);
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, RANGE, kernelTrace));

        // Assert that the original views trace's window range did not change
        SWTBotUtils.activateEditor(fBot, getTestTrace().getName());
        SWTBotUtils.waitUntil(v -> (v.getWindowStartTime() == KERNEL_START && v.getWindowEndTime() == KERNEL_TEST_INITIAL_END), chartViewer, "Range of cloned view changed");

        // Unpin from active trace
        SWTBotUtils.activateEditor(fBot, kernelTrace.getName());
        originalViewBot.toolbarButton(UNPIN_VIEW_BUTTON_NAME).click();
        assertOriginalViewTitle(COUNTERS_VIEW_TITLE);

        originalViewBot.toolbarButton(PIN_VIEW_BUTTON_NAME).click();

        assertOriginalViewTitle(PINNED_TO_CTX_SWITCH_VIEW_TITLE);

        // Close the pinned trace
        SWTBotEditor kernelTable = fBot.editorByTitle(kernelTestTrace.getName());
        kernelTable.close();

        // Verify that view title is reset
        SWTBotUtils.waitUntil(v -> v.getReference().getPartName().equals(COUNTERS_VIEW_TITLE), originalViewBot, "View name didn't change");
        kernelTestTrace.dispose();
    }

    /**
     * Test the cloning feature.
     */
    @Test
    public void testCloneSingleTrace() {
        SWTBotView originalViewBot = getSWTBotView();
        SWTBotMenu cloneMenu = originalViewBot.viewMenu().menu(NEW_COUNTER_STACK_MENU);

        /*
         * Assert that the original editor was not renamed and that the cloned one
         * exists and is pinned to the kernel_vm trace.
         */
        cloneMenu.menu(PINNED_TO_PREFIX + getTestTrace().getName()).click();
        assertOriginalViewTitle(COUNTERS_VIEW_TITLE);
        SWTBotView clonedView = fBot.viewByTitle(PINNED_TO_TRACE_COUNTERS_VIEW_TITLE);
        assertEquals("Should not have created a new instance", 1, fBot.editors().size());
        clonedView.close();

         // Assert that a new instance is created.
        cloneMenu.menu(PINNED_TO_PREFIX + getTestTrace().getName() + " | new instance").click();
        assertOriginalViewTitle(COUNTERS_VIEW_TITLE);
        clonedView = fBot.viewByTitle(CLONED_VIEW_TITLE_NAME);
        assertEquals("Should have created a new instance", 2, fBot.editors().size());
        SWTBotEditor cloneEditor = fBot.editorByTitle(getTestTrace().getName() + CLONED_TRACE_SUFFIX);

        // Get the window range of the cloned trace
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace cloneTrace = traceManager.getActiveTrace();
        assertNotNull(cloneTrace);

        // Go back to original trace, pin it
        SWTBotUtils.activateEditor(fBot, getTestTrace().getName());
        originalViewBot.toolbarButton(PIN_VIEW_BUTTON_NAME).click();

        // Assert that the cloned trace's window range did not change
        SWTBotUtils.activateEditor(fBot, cloneTrace.getName() + CLONED_TRACE_SUFFIX);
        IViewPart viewPart = clonedView.getViewReference().getView(false);
        assertTrue(viewPart instanceof TmfChartView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewPart);
        assertNotNull(chartViewer);

        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, RANGE, getTestTrace()));
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));
        SWTBotUtils.waitUntil(v -> (v.getWindowStartTime() == KERNEL_START && v.getWindowEndTime() == KERNEL_INITIAL_END), chartViewer, "Range of cloned view changed");

        cloneEditor.close();
    }

    private void assertOriginalViewTitle(String newName) {
        Widget expectedWidget = getSWTBotView().getWidget();
        assertNotNull(expectedWidget);
        SWTBotView actualView = fBot.viewByTitle(newName);
        Widget actualWidget = actualView.getWidget();
        assertNotNull(actualWidget);
        assertEquals(expectedWidget, actualWidget);
    }

    @Override
    protected @NonNull String getMainSeriesName() {
        return MAIN_SERIES_NAME;
    }

    @Override
    protected @NonNull String getTitle() {
        return COUNTERS_VIEW_TITLE;
    }

    @Override
    protected String getViewID() {
        return COUNTERS_VIEW_ID;
    }

    @Override
    protected ITmfTrace getTestTrace() {
        return CtfTmfTestTraceUtils.getTrace(CtfTestTrace.KERNEL_VM);
    }

    @Override
    protected void disposeTestTrace() {
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.KERNEL_VM);
    }
}
