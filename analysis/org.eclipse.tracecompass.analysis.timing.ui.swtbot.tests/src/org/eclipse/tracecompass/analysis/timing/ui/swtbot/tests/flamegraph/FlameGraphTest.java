/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.swtbot.tests.flamegraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.analysis.timing.core.tests.flamegraph.AggregationTreeTest;
import org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph.FlameGraphPresentationProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph.FlameGraphView;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.IHandlerService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * Unit tests for the flame graph view
 *
 * @author Matthew Khouzam
 */
public class FlameGraphTest extends AggregationTreeTest {

    private static final String FLAMEGRAPH_ID = FlameGraphView.ID;
    private SWTWorkbenchBot fBot;
    private SWTBotView fView;
    private FlameGraphView fFg;
    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private TimeGraphViewer fTimeGraphViewer;

    /**
     * Initialization
     */
    @BeforeClass
    public static void beforeClass() {

        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBotTest");
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", bot);
        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Open a flamegraph
     */
    @Before
    public void before() {
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.openView(FLAMEGRAPH_ID);
        SWTBotView view = fBot.viewById(FLAMEGRAPH_ID);
        assertNotNull(view);
        fView = view;
        FlameGraphView flamegraph = UIThreadRunnable.syncExec((Result<FlameGraphView>) () -> {
            IViewPart viewRef = fView.getViewReference().getView(true);
            return (viewRef instanceof FlameGraphView) ? (FlameGraphView) viewRef : null;
        });
        assertNotNull(flamegraph);
        fTimeGraphViewer = flamegraph.getTimeGraphViewer();
        assertNotNull(fTimeGraphViewer);
        maximize(flamegraph);
        fFg = flamegraph;
    }

    /**
     * Maximize a view by reference
     * <p>
     * TODO: if this is useful, maybe uplift to SWTViewBot
     *
     * @param view
     *            the view reference
     */
    private static void maximize(@NonNull IViewPart view) {
        assertNotNull(view);
        IWorkbenchPartSite site = view.getSite();
        assertNotNull(site);
        // The annotation is to make the compiler not complain.
        @Nullable Object handlerServiceObject = site.getService(IHandlerService.class);
        assertTrue(handlerServiceObject instanceof IHandlerService);
        IHandlerService handlerService = (IHandlerService) handlerServiceObject;
        try {
            handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_MAXIMIZE_ACTIVE_VIEW_OR_EDITOR, null);
        } catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
            fail(e.getMessage());
        }
    }

    private void loadFlameGraph() {
        UIThreadRunnable.syncExec(() -> fFg.buildFlameGraph(getCga()));
        try {
            fFg.waitForUpdate();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private ITimeGraphEntry selectRoot() {
        UIThreadRunnable.syncExec(() -> fTimeGraphViewer.selectNextItem());
        ITimeGraphEntry entry = fTimeGraphViewer.getSelection();
        assertNotNull(entry);
        return entry;
    }

    private static ITimeEvent getFirstEvent(ITimeGraphEntry actualEntry) {
        Optional<@NonNull ? extends ITimeEvent> actualEventOpt = StreamSupport.stream(Spliterators.spliteratorUnknownSize(actualEntry.getTimeEventsIterator(), Spliterator.NONNULL), false)
                .filter(i -> (i instanceof TimeEvent)).filter(j -> !(j instanceof NullTimeEvent))
                .findFirst();
        assertTrue(actualEventOpt.isPresent());
        ITimeEvent actualEvent = actualEventOpt.get();
        return actualEvent;
    }

    @Override
    public void emptyStateSystemTest() {
        super.emptyStateSystemTest();
        loadFlameGraph();
        ITimeGraphEntry entry = fTimeGraphViewer.getSelection();
        assertNull(entry);
    }

    @Override
    public void cascadeTest() {
        super.cascadeTest();
        loadFlameGraph();
        ITimeGraphEntry entry = selectRoot();
        assertEquals(3, entry.getChildren().size());
        ITimeGraphEntry actualEntry = entry.getChildren().get(1);
        ITimeEvent actualEvent = getFirstEvent(actualEntry);
        assertNotNull(actualEvent);
        assertEquals(996, actualEvent.getDuration());

    }

    @Override
    public void mergeFirstLevelCalleesTest() {
        super.mergeFirstLevelCalleesTest();
        loadFlameGraph();
        ITimeGraphEntry entry = selectRoot();
        assertEquals(3, entry.getChildren().size());
        ITimeGraphEntry actualEntry = entry.getChildren().get(1);
        ITimeEvent actualEvent = getFirstEvent(actualEntry);
        assertNotNull(actualEvent);
        assertEquals(80, actualEvent.getDuration());
    }

    @Override
    public void multiFunctionRootsSecondTest() {
        super.multiFunctionRootsSecondTest();
        loadFlameGraph();
        ITimeGraphEntry entry = selectRoot();
        assertEquals(2, entry.getChildren().size());
        ITimeGraphEntry actualEntry = entry.getChildren().get(1);
        ITimeEvent actualEvent = getFirstEvent(actualEntry);
        assertNotNull(actualEvent);
        assertEquals(10, actualEvent.getDuration());
    }

    @Override
    public void mergeSecondLevelCalleesTest() {
        super.mergeSecondLevelCalleesTest();
        loadFlameGraph();
        ITimeGraphEntry entry = selectRoot();
        assertEquals(4, entry.getChildren().size());
        ITimeGraphEntry actualEntry = entry.getChildren().get(1);
        ITimeEvent actualEvent = getFirstEvent(actualEntry);
        assertNotNull(actualEvent);
        assertEquals(90, actualEvent.getDuration());
    }

    @Override
    public void multiFunctionRootsTest() {
        super.multiFunctionRootsTest();
        loadFlameGraph();
        ITimeGraphEntry entry = selectRoot();
        assertEquals(2, entry.getChildren().size());
        ITimeGraphEntry actualEntry = entry.getChildren().get(1);
        ITimeEvent actualEvent = getFirstEvent(actualEntry);
        assertNotNull(actualEvent);
        assertEquals(10, actualEvent.getDuration());
    }

    /**
     * Also test statistics tooltip
     */
    @Override
    public void treeTest() {
        super.treeTest();
        loadFlameGraph();
        ITimeGraphEntry entry = selectRoot();
        assertEquals(3, entry.getChildren().size());
        ITimeGraphEntry actualEntry = entry.getChildren().get(1);
        ITimeEvent actualEvent = getFirstEvent(actualEntry);
        assertNotNull(actualEvent);
        assertEquals(80, actualEvent.getDuration());
        Map<String, String> tooltip = new FlameGraphPresentationProvider().getEventHoverToolTipInfo(actualEvent);
        assertNull(tooltip);
        tooltip = new FlameGraphPresentationProvider().getEventHoverToolTipInfo(actualEvent, 5);
        assertTrue(tooltip.toString().contains("duration=80 ns"));
        assertTrue(tooltip.toString().contains("duration=40 ns"));
    }

    /**
     * Takes too much ram
     */
    @Ignore
    @Override
    public void largeTest() {
        // Do nothing
    }

}
