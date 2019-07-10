/**********************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.results.IntResult;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.QualitativePaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.presentation.SequentialPaletteProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ImageHelper;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * Test for Timegraph views in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TimeGraphViewTest {

    private static final Logger fLogger = Logger.getRootLogger();

    private static RGB fHair;
    private static RGB fHat;
    private static RGB fLaser;

    private static final int MIN_FILE_SIZE = 1000;

    /**
     * The legend tooltip
     */
    private static final String SHOW_LEGEND = "Show Legend";
    /**
     * Legend title
     */
    private static final String LEGEND_NAME = "Legend";
    /**
     * OK button
     */
    private static final String OK_BUTTON = "OK";

    /**
     * Export to png button
     */
    private static final String EXPORT_MENU = "Export...";

    /**
     * File extension
     */
    private static final String EXTENSION = ".png";

    /**
     * Reference image
     */
    private static final String REFERENCE_LOC = "reference";
    /**
     * image after making the line thinner
     */
    private static final String SKINNY_LOC = "skinny";
    /**
     * Image after resetting
     */
    private static final String RESET_LOC = "reset";

    private static final TmfTimeRange INITIAL_WINDOW_RANGE = new TmfTimeRange(TmfTimestamp.fromNanos(20), TmfTimestamp.fromNanos(100));
    /**
     * Legend id key
     */
    private static final String LEGEND_ENTRY_KEY = "legend.entry.key";

    /**
     * Hair entry id
     */
    private static final String HAIR_ID = "HAIR";
    /**
     * Laser entry id
     */
    private static final String QUOTE_LASER_UNQUOTE = "\"LASER\"";

    private SWTBotView fViewBot;

    private SWTBotTimeGraph fTimeGraph;

    private TmfTraceStub fTrace;

    private Rectangle fBounds;

    private SWTWorkbenchBot fBot;

    private final ICondition fTimeGraphIsDirty = new ConditionHelper() {

        @Override
        public boolean test() throws Exception {
            SWTBotView viewBot = fViewBot;
            if (viewBot == null) {
                return false;
            }
            return getView().isDirty();
        }
    };

    /**
     * Set up for test
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fHair = ImageHelper.adjustExpectedColor(new RGB(0, 64, 128));
        fHat = ImageHelper.adjustExpectedColor(new RGB(0, 255, 0));
        fLaser = ImageHelper.adjustExpectedColor(new RGB(255, 0, 0));
    }

    /**
     * Before the test is run, make the view see the items.
     *
     * Reset the perspective and close all the views.
     *
     * @throws TmfTraceException
     *             could not load a trace
     */
    @Before
    public void before() throws TmfTraceException {
        fBot = new SWTWorkbenchBot();
        fBot.closeAllEditors();
        for (SWTBotView viewBot : fBot.views()) {
            viewBot.close();
        }
        SWTBotUtils.openView(TimeGraphViewStub.ID);
        fViewBot = fBot.viewById(TimeGraphViewStub.ID);

        fViewBot.show();
        fTrace = new TmfTraceStub() {

            @Override
            public @NonNull String getName() {
                return "Stub";
            }

            @Override
            public TmfContext seekEvent(ITmfLocation location) {
                return new TmfContext();
            }
        };
        fTrace.setStartTime(TmfTimestamp.fromNanos(0));

        fTrace.setEndTime(TmfTimestamp.fromNanos(180));

        TmfTraceStub trace = fTrace;
        trace.initialize(null, "", ITmfEvent.class);
        assertNotNull(trace);
        fTimeGraph = new SWTBotTimeGraph(fViewBot.bot());

        // Wait for trace to be loaded
        fViewBot.bot().waitUntil(new TgConditionHelper(t -> fTimeGraph.getEntries().length == 0));
        fBounds = getBounds();
        UIThreadRunnable.syncExec(() -> TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, trace, null)));
        // Wait for trace to be loaded
        fViewBot.bot().waitUntil(new TgConditionHelper(t -> fTimeGraph.getEntries().length >= 2));

        resetTimeRange();
        // Make sure the thumb is over 1 in size
        fBot.waitUntil(new TgConditionHelper(t -> fViewBot.bot().slider().getThumb() > 1));
    }

    private void resetTimeRange() {
        TmfTimeRange fullTimeRange = fTrace.getTimeRange();
        TmfWindowRangeUpdatedSignal signal = new TmfWindowRangeUpdatedSignal(this, INITIAL_WINDOW_RANGE);

        TimeGraphViewStub view = getView();
        // This is an oddity: the signals may be lost if a view is not
        // initialized, so
        // they are send in the condition.
        //
        // NOTE: maybe use a shorter poll delay?
        //
        // Workflow: it should always fail at first and then a signal is sent.
        // it will either time out or work.
        fBot.waitUntil(new TgConditionHelper(t -> {
            if (INITIAL_WINDOW_RANGE.equals(view.getWindowRange())) {
                return true;
            }
            TmfSignalManager.dispatchSignal(signal);
            return false;
        }));
        // same as above: re-run as much as you need.
        fBot.waitUntil(new TgConditionHelper(t -> {
            if (fullTimeRange.equals(view.getWindowRange())) {
                return true;
            }
            fViewBot.toolbarButton("Reset the Time Scale to Default").click();
            return false;
        }));
    }

    private Rectangle getBounds() {
        return UIThreadRunnable.syncExec((Result<Rectangle>) () -> {
            Control control = fTimeGraph.widget;
            Rectangle ctrlRelativeBounds = control.getBounds();
            Point res = control.toDisplay(new Point(fTimeGraph.getNameSpace(), 0));
            ctrlRelativeBounds.width -= fTimeGraph.getNameSpace();
            ctrlRelativeBounds.x = res.x;
            ctrlRelativeBounds.y = res.y;
            return ctrlRelativeBounds;
        });
    }

    private TimeGraphViewStub getView() {
        IWorkbenchPart part = fViewBot.getViewReference().getPart(true);
        assertTrue(part.getClass().getCanonicalName(), part instanceof TimeGraphViewStub);
        TimeGraphViewStub stubView = (TimeGraphViewStub) part;
        return stubView;
    }

    /**
     * Clean up after a test, reset the views and reset the states of the
     * timegraph by pressing reset on all the resets of the legend
     */
    @After
    public void after() {

        // reset all
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        SWTBotShell legendShell = fBot.shell(LEGEND_NAME);
        SWTBot legendBot = legendShell.bot();

        for (int i = 0; i < StubPresentationProvider.STATES.length; i++) {
            SWTBotButton resetButton = legendBot.button(i);
            if (resetButton.isEnabled()) {
                resetButton.click();
            }
        }
        legendBot.button(OK_BUTTON).click();
        TmfTraceStub trace = fTrace;
        assertNotNull(trace);
        UIThreadRunnable.syncExec(() -> TmfSignalManager.dispatchSignal(new TmfTraceClosedSignal(this, trace)));
        fBot.waitUntil(Conditions.shellCloses(legendShell));
        fViewBot.close();
        fBot.waitUntil(ConditionHelpers.viewIsClosed(fViewBot));
        fTrace.dispose();
    }

    /**
     * Put things back the way they were
     */
    @AfterClass
    public static void afterClass() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.closeAllEditors();
        fLogger.removeAllAppenders();
    }

    /**
     * Test the legend operation for an arrow. Change sliders and reset, do not
     * change colors as there is not mock of the color picker yet
     *
     * TODO: mock color picker
     *
     * TODO: make stable
     */
    @Test
    public void testLegendArrow() {
        resetTimeRange();
        Rectangle bounds = fBounds;

        ImageHelper ref = ImageHelper.grabImage(bounds);

        // Set the widths to 0.25

        fViewBot.toolbarButton(SHOW_LEGEND).click();
        SWTBotShell legendShell = fBot.shell(LEGEND_NAME);
        legendShell.activate();
        SWTBot legendBot = legendShell.bot();
        assertFalse(legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) QUOTE_LASER_UNQUOTE).isEnabled());
        int defaultValue = legendBot.scaleWithId(LEGEND_ENTRY_KEY, QUOTE_LASER_UNQUOTE).getValue();
        legendBot.scaleWithId(LEGEND_ENTRY_KEY, QUOTE_LASER_UNQUOTE).setValue(100);
        assertTrue(legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) QUOTE_LASER_UNQUOTE).isEnabled());
        legendShell.bot().button(OK_BUTTON).click();
        fBot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange();

        // Take another picture
        ImageHelper thick = ImageHelper.waitForNewImage(bounds, ref);

        // Compare with the original, they should be different
        int refCount = ref.getHistogram().count(fLaser);
        int thickCount = thick.getHistogram().count(fLaser);
        assertTrue(String.format("Count of \"\"LASER\"\" (%s) did not get change despite change of width before: %d after:%d histogram:%s", fLaser, refCount, thickCount, Multisets.copyHighestCountFirst(thick.getHistogram())), thickCount > refCount);

        // reset all
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        legendShell = fBot.shell(LEGEND_NAME);
        legendBot = legendShell.bot();
        assertTrue(legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) QUOTE_LASER_UNQUOTE).isEnabled());
        legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) QUOTE_LASER_UNQUOTE).click();
        assertEquals(defaultValue, legendBot.scaleWithId(LEGEND_ENTRY_KEY, QUOTE_LASER_UNQUOTE).getValue());
        assertFalse(legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) QUOTE_LASER_UNQUOTE).isEnabled());
        legendBot.button(OK_BUTTON).click();
        fBot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange();

        // take a third picture
        ImageHelper reset = ImageHelper.waitForNewImage(bounds, thick);

        // Compare with the original, they should be the same
        int resetCount = reset.getHistogram().count(fLaser);
        assertEquals("Count of \"\"LASER\"\" did not get change despite reset of width", refCount, resetCount);
    }

    /**
     * Test the event styling methods, should put a box around the events.
     */
    @Test
    public void testEventStyling() {
        resetTimeRange();
        Rectangle bounds = fBounds;
        ImageHelper precollapse = ImageHelper.grabImage(bounds);
        SWTBotTimeGraph tg = fTimeGraph;
        tg.getEntry("Plumber guy").collapse();
        TimeGraphViewStub view = getView();
        // take a first saved picture, the original is pre-collapse
        ImageHelper ref = ImageHelper.waitForNewImage(bounds, precollapse);
        /*
         * Regex, not a question
         */
        view.setFilterRegex("row?");
        UIThreadRunnable.syncExec(() -> view.restartZoomThread());
        // Take another picture/
        ImageHelper highlighted = ImageHelper.waitForNewImage(bounds, ref);
        view.setFilterRegex(null);
        UIThreadRunnable.syncExec(() -> view.restartZoomThread());
        // take a third picture
        ImageHelper reset = ImageHelper.waitForNewImage(bounds, highlighted);
        ImageHelper diff = ref.diff(reset);
        assertTrue((double) diff.getHistogram().count(new RGB(0, 0, 0)) / diff.getHistogram().size() > 0.99);
        ImageHelper delta = highlighted.diff(reset);
        tg.expandAll();
        assertTrue("Some highlighting", delta.getHistogram().elementSet().size() > 1);
    }

    /**
     * Test the legend operation. Change sliders and reset, do not change colors
     * as there is not mock of the color picker yet
     *
     * TODO: mock color picker
     */
    @Test
    public void testLegend() {
        resetTimeRange();
        Rectangle bounds = fBounds;

        ImageHelper ref = ImageHelper.grabImage(bounds);

        // Set the widths to 0.25

        fViewBot.toolbarButton(SHOW_LEGEND).click();
        SWTBotShell legendShell = fBot.shell(LEGEND_NAME);
        legendShell.activate();
        SWTBot legendBot = legendShell.bot();
        assertFalse(legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) HAIR_ID).isEnabled());
        int defaultValue = legendBot.scaleWithId(LEGEND_ENTRY_KEY, HAIR_ID).getValue();
        legendBot.scaleWithId(LEGEND_ENTRY_KEY, HAIR_ID).setValue(25);
        assertTrue(legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) HAIR_ID).isEnabled());
        legendShell.bot().button(OK_BUTTON).click();
        fBot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange();

        // Take another picture
        ImageHelper skinny = ImageHelper.waitForNewImage(bounds, ref);

        /* Compare with the original, they should be different */
        int refCount = ref.getHistogram().count(fHair);
        int skinnyCount = skinny.getHistogram().count(fHair);
        assertTrue(String.format("Count of \"\"HAIR\"\" (%s) did not get change despite change of width before: %d after:%d histogram:%s", fHair, refCount, skinnyCount, Multisets.copyHighestCountFirst(skinny.getHistogram())), skinnyCount < refCount);

        // reset all
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        legendShell = fBot.shell(LEGEND_NAME);
        legendBot = legendShell.bot();
        assertTrue(legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) HAIR_ID).isEnabled());
        legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) HAIR_ID).click();
        assertEquals(defaultValue, legendBot.scaleWithId(LEGEND_ENTRY_KEY, HAIR_ID).getValue());
        assertFalse(legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) HAIR_ID).isEnabled());
        legendBot.button(OK_BUTTON).click();
        fBot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange();

        // take a third picture
        ImageHelper reset = ImageHelper.waitForNewImage(bounds, skinny);

        // Compare with the original, they should be the same
        int resetCount = reset.getHistogram().count(fHair);
        assertEquals("Count of \"HAIR\" did not get change despite reset of width", refCount, resetCount);
    }

    /**
     * Test the legend and export operations. Resize states, take screenshots
     * and compare, they should be different. then reset the sizes and compare,
     * they should be the same.
     *
     * NOTE: This utterly fails in GTK3.
     *
     * @throws IOException
     *             file not found, someone deleted files while the test is
     *             running
     */
    @Ignore
    @Test
    public void testExport() throws IOException {

        resetTimeRange();
        /*
         * Set up temp files
         */
        File ref = File.createTempFile(REFERENCE_LOC, EXTENSION);
        File skinny = File.createTempFile(SKINNY_LOC, EXTENSION);
        File reset = File.createTempFile(RESET_LOC, EXTENSION);
        ref.deleteOnExit();
        skinny.deleteOnExit();
        reset.deleteOnExit();

        /* Take a picture */
        TmfFileDialogFactory.setOverrideFiles(ref.getAbsolutePath());
        fViewBot.viewMenu(EXPORT_MENU).click();
        ImageHelper refImage = ImageHelper.fromFile(ref);
        fBot.waitUntil(new FileWritten(ref, MIN_FILE_SIZE));

        /* Set the widths to skinny */
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        SWTBotShell legendShell = fBot.shell(LEGEND_NAME);
        legendShell.activate();
        SWTBot legendBot = legendShell.bot();
        legendBot.scaleWithId(LEGEND_ENTRY_KEY, HAIR_ID).setValue(50);
        legendShell.bot().button(OK_BUTTON).click();
        fBot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange();

        /* Take another picture */
        TmfFileDialogFactory.setOverrideFiles(skinny.getAbsolutePath());
        fViewBot.viewMenu(EXPORT_MENU).click();
        ImageHelper skinnyImage = ImageHelper.fromFile(skinny);
        fBot.waitUntil(new FileWritten(skinny, MIN_FILE_SIZE));

        /* Compare with the original, they should be different */
        int refCount = refImage.getHistogram().count(fHair);
        int skinnyCount = skinnyImage.getHistogram().count(fHair);
        assertTrue(String.format("Count of \"\"HAIR\"\" (%s) did not get change despite change of width before: %d after:%d histogram:%s", fHair, refCount, skinnyCount, Multisets.copyHighestCountFirst(skinnyImage.getHistogram())),
                skinnyCount < refCount);

        /* reset all */
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        legendShell = fBot.shell(LEGEND_NAME);
        legendBot = legendShell.bot();
        legendBot.buttonWithId(LEGEND_ENTRY_KEY, (Object) HAIR_ID).click();
        legendBot.button(OK_BUTTON).click();
        fBot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange();

        /* take a third picture */
        TmfFileDialogFactory.setOverrideFiles(reset.getAbsolutePath());
        fViewBot.viewMenu(EXPORT_MENU).click();
        fBot.waitUntil(new FileWritten(reset, MIN_FILE_SIZE));
        ImageHelper resetImage = ImageHelper.fromFile(reset);

        /* Compare with the original, they should be the same */
        int resetCount = resetImage.getHistogram().count(fHair);
        assertEquals("Count of \"HAIR\" did not get change despite reset of width", refCount, resetCount);
    }

    /**
     * Test expand and collapes of a timegraph view
     */
    @Test
    public void testExpandAndCollapse() {
        String pg = "Plumber guy";
        String hpc = "Hungry pie chart";
        String element = "row2";
        int totalItems = 17;

        resetTimeRange();
        SWTBotTimeGraph timegraph = fTimeGraph;
        assertEquals(totalItems, getVisibleItems(timegraph));

        SWTBotTimeGraphEntry[] entries = null;
        entries = timegraph.getEntries();
        assertNotNull(entries);
        assertNotNull(timegraph.getEntry(hpc, element));

        timegraph.collapseAll();
        entries = timegraph.getEntries();
        assertEquals(3, getVisibleItems(timegraph));

        timegraph.getEntry(pg).select();
        fireKey(timegraph, true, '+');
        assertEquals(10, getVisibleItems(timegraph));

        timegraph.getEntry(pg).select();
        fireKey(timegraph, true, '-');
        assertEquals(3, getVisibleItems(timegraph));

        timegraph.getEntry(hpc).select();
        fireKey(timegraph, true, '+');
        assertEquals(10, getVisibleItems(timegraph));
        assertNotNull(timegraph.getEntry(hpc, element));

        timegraph.getEntry(pg).select();
        fireKey(timegraph, true, '*');
        timegraph.getEntry(hpc).select();
        fireKey(timegraph, true, '*');
        assertEquals(totalItems, getVisibleItems(timegraph));
        assertNotNull(timegraph.getEntry(hpc, element));
    }

    /**
     * Test vertical zoom in and out
     */
    @Test
    public void testVerticalZoom() {
        resetTimeRange();

        int threshold = 10;
        SWTBotTimeGraph timegraph = fTimeGraph;
        Rectangle bounds = fBounds;

        ImageHelper ref = ImageHelper.grabImage(bounds);

        fireKeyAndWait(timegraph, bounds, false, '+', SWT.CTRL);
        fireKeyAndWait(timegraph, bounds, false, '-', SWT.CTRL);

        ImageHelper bigSmall = ImageHelper.grabImage(bounds);

        ImageHelper diff = ref.diff(bigSmall);
        // 3% of the image
        threshold = (int) (diff.getHistogram().size() * 0.03);
        List<RGB> colors = filter(diff.getHistogram(), threshold);
        assertEquals(colors.toString(), 1, colors.size());

        fireKeyAndWait(timegraph, bounds, false, '+', SWT.CTRL);
        fireKeyAndWait(timegraph, bounds, false, '+', SWT.CTRL);

        ImageHelper bigBig = ImageHelper.grabImage(bounds);
        diff = ref.diff(bigBig);
        colors = filter(diff.getHistogram(), threshold);
        assertNotEquals(colors.toString(), 1, colors.size());

        fireKeyAndWait(timegraph, bounds, false, '+', SWT.CTRL);
        fireKeyAndWait(timegraph, bounds, false, '-', SWT.CTRL);
        fireKeyAndWait(timegraph, bounds, false, '0', SWT.CTRL);

        ImageHelper bigSmallReset = ImageHelper.grabImage(bounds);
        diff = ref.diff(bigSmallReset);
        colors = filter(diff.getHistogram(), threshold);
        assertEquals(colors.toString(), 1, colors.size());

        fireKeyAndWait(timegraph, bounds, false, '-', SWT.CTRL);
        fireKeyAndWait(timegraph, bounds, false, '-', SWT.CTRL);

        ImageHelper smallSmall = ImageHelper.grabImage(bounds);
        diff = ref.diff(smallSmall);
        colors = filter(diff.getHistogram(), threshold);
        assertNotEquals(colors.toString(), 1, colors.size());

        fireKeyAndWait(timegraph, bounds, false, '-', SWT.CTRL);
        fireKeyAndWait(timegraph, bounds, false, '+', SWT.CTRL);
        fireKeyAndWait(timegraph, bounds, false, '0', SWT.CTRL);

        ImageHelper smallBigReset = ImageHelper.grabImage(bounds);
        diff = ref.diff(smallBigReset);
        colors = filter(diff.getHistogram(), threshold);
        assertEquals(colors.toString(), 1, colors.size());
    }

    private static List<RGB> filter(Multiset<RGB> histogram, int threshold) {
        return histogram.elementSet().stream().filter(e -> histogram.count(e) > threshold).collect(Collectors.toList());
    }

    /**
     * Test horizontal zoom, we can see a rounding error
     */
    @Test
    public void testHorizontalZoom() {
        resetTimeRange();
        SWTBotTimeGraph timegraph = fTimeGraph;

        TimeGraphViewStub view = getView();
        timegraph.setFocus();

        assertEquals(80, getDuration(view.getWindowRange()));
        fireKeyInGraph(timegraph, '=');
        fViewBot.bot().waitUntil(new WindowRangeCondition(view, 52));
        fireKeyInGraph(timegraph, '+');
        fViewBot.bot().waitUntil(new WindowRangeCondition(view, 34));
        fireKeyInGraph(timegraph, '-');
        fViewBot.bot().waitUntil(new WindowRangeCondition(view, 51));
        fireKeyInGraph(timegraph, '-');
        fViewBot.bot().waitUntil(new WindowRangeCondition(view, 77));
        /*
         * Note that 'w' and 's' zooming is based on mouse position. Just check
         * if window range was increased or decreased to avoid inaccuracy due to
         * the mouse position in test environment.
         */
        long previousRange = getDuration(view.getWindowRange());
        fireKeyInGraph(timegraph, 'w');
        fViewBot.bot().waitUntil(new WindowRangeUpdatedCondition(view, previousRange, false));
        previousRange = getDuration(view.getWindowRange());
        fireKeyInGraph(timegraph, 's');
        fViewBot.bot().waitUntil(new WindowRangeUpdatedCondition(view, previousRange, true));
    }

    /**
     * Test name space navigation using the keyboard.
     */
    @Test
    public void testKeyboardNamespaceNavigation() {
        String pg = "Plumber guy";
        resetTimeRange();
        SWTBotTimeGraph timegraph = fTimeGraph;
        timegraph.getEntry(pg).select();
        fireKey(timegraph, true, SWT.ARROW_DOWN);
        assertEquals("[Hat1]", timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.ARROW_DOWN);
        assertEquals("[Hat2]", timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.ARROW_DOWN);
        assertEquals("[Head1]", timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.ARROW_UP);
        assertEquals("[Hat2]", timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.ARROW_DOWN);
        assertEquals("[Head1]", timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.HOME);
        assertEquals('[' + pg + ']', timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.PAGE_DOWN);
        assertNotEquals('[' + pg + ']', timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.PAGE_UP);
        assertEquals('[' + pg + ']', timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.END);
        assertEquals("[pulse]", timegraph.selection().get(0).toString());
        fireKey(timegraph, true, SWT.HOME);
        assertEquals('[' + pg + ']', timegraph.selection().get(0).toString());
    }

    /**
     * Test the enter key, which toggles expand/collapse.
     */
    @Test
    public void testCollapseExpandUsingEnter() {
        String pg = "Plumber guy";
        resetTimeRange();
        SWTBotTimeGraph timegraph = fTimeGraph;
        assertEquals(0, timegraph.selection().columnCount());
        timegraph.getEntry(pg).select();

        assertEquals(1, timegraph.selection().columnCount());
        assertEquals(17, getVisibleItems(timegraph));

        fireKey(timegraph, true, SWT.CR);
        assertEquals(1, timegraph.selection().columnCount());
        assertEquals('[' + pg + ']', timegraph.selection().get(0).toString());
        assertEquals(10, getVisibleItems(timegraph));

        fireKey(timegraph, true, SWT.CR);
        assertEquals(1, timegraph.selection().columnCount());
        assertEquals('[' + pg + ']', timegraph.selection().get(0).toString());
        assertEquals(17, getVisibleItems(timegraph));

        timegraph.getEntry(pg, "Hat1").select();
        fireKey(timegraph, true, SWT.CR);
        assertEquals(1, timegraph.selection().columnCount());
        assertEquals("[Hat1]", timegraph.selection().get(0).toString());
        assertEquals(17, getVisibleItems(timegraph));
    }

    /**
     * Test the line entries
     */
    @Test
    public void testTimeLine() {
        String lines = "pulse";
        resetTimeRange();
        SWTBotTimeGraph timegraph = fTimeGraph;
        assertEquals(0, timegraph.selection().columnCount());
        ImageHelper currentImage = ImageHelper.waitForNewImage(fBounds, null);
        SWTBotTimeGraphEntry entry = timegraph.getEntry(lines);
        // make sure it's visible
        entry = timegraph.getEntry(lines).select();
        ImageHelper.waitForNewImage(fBounds, currentImage);
        Rectangle rect = entry.absoluteLocation();
        ImageHelper image = ImageHelper.grabImage(rect);
        ImmutableMultiset<RGB> ms = Multisets.copyHighestCountFirst(image.getHistogram());
        int black = ms.count(new RGB(0, 0, 0));
        RGB bgColor = ms.elementSet().iterator().next();
        int bgCount = ms.count(bgColor);
        float actual = ((float) black) / (bgCount);
        assertEquals(0.113f, actual, 0.05f);
    }

    /**
     * Test zoom to selection
     */
    @Test
    public void testZoomToSelection() {
        resetTimeRange();
        SWTBotTimeGraph timegraph = fTimeGraph;

        TimeGraphViewStub view = getView();
        timegraph.setFocus();

        assertEquals(80, getDuration(view.getWindowRange()));

        /* set selection to trace start time */
        ITmfTimestamp selStartTime = TmfTimestamp.fromNanos(30L);
        ITmfTimestamp selEndTime = TmfTimestamp.fromNanos(80L);
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, selStartTime, selEndTime));
        timeGraphIsReadyCondition(new TmfTimeRange(selStartTime, selEndTime));
        fireKeyInGraph(timegraph, 'z');
        fViewBot.bot().waitUntil(new WindowRangeCondition(view, 50));
    }

    /**
     * Test 'a' and 'd' navigation
     */
    @Test
    public void testKeyboardNavigation() {
        resetTimeRange();
        SWTBotTimeGraph timegraph = fTimeGraph;

        TimeGraphViewStub view = getView();
        timegraph.setFocus();

        assertEquals(80, getDuration(view.getWindowRange()));

        TmfTimeRange updatedWindowRange = new TmfTimeRange(TmfTimestamp.fromNanos(40), TmfTimestamp.fromNanos(120));

        // move to the right
        fireKeyInGraph(timegraph, 'd');
        fViewBot.bot().waitUntil(new TgConditionHelper(t -> updatedWindowRange.equals(view.getWindowRange())));

        // move to the left
        fireKeyInGraph(timegraph, 'a');
        fViewBot.bot().waitUntil(new TgConditionHelper(t -> INITIAL_WINDOW_RANGE.equals(view.getWindowRange())));
    }

    private static long getDuration(TmfTimeRange refRange) {
        return refRange.getEndTime().toNanos() - refRange.getStartTime().toNanos();
    }

    // Slow but safe
    private static void fireKeyAndWait(SWTBotTimeGraph timegraph, Rectangle bounds, boolean inNameSpace, int c, int... modifiers) {
        ImageHelper ref = ImageHelper.grabImage(bounds);
        fireKey(timegraph, inNameSpace, c, modifiers);
        ImageHelper.waitForNewImage(bounds, ref);
    }

    private static void fireKey(SWTBotTimeGraph timegraph, boolean inNameSpace, int c, int... modifiers) {
        Event event = new Event();
        event.widget = timegraph.widget;
        if ((c & SWT.KEYCODE_BIT) == 0 && c != SWT.CR) {
            event.character = (char) c;
        } else {
            event.keyCode = c;
        }
        event.doit = true;
        UIThreadRunnable.syncExec(() -> {
            event.display = Display.getCurrent();
            resetMousePosition(timegraph, inNameSpace, new MouseEvent(event));
            KeyEvent e = new KeyEvent(event);
            for (int modifier : modifiers) {
                e.stateMask |= modifier;
            }
            timegraph.widget.keyPressed(e);
        });
        UIThreadRunnable.syncExec(() -> {
            event.display = Display.getCurrent();
            resetMousePosition(timegraph, inNameSpace, new MouseEvent(event));
            KeyEvent e = new KeyEvent(event);
            for (int modifier : modifiers) {
                e.stateMask |= modifier;
            }
            timegraph.widget.keyReleased(e);
        });
    }

    private static void fireKeyInGraph(SWTBotTimeGraph timegraph, char c, int... modifiers) {
        timegraph.setFocus();
        // Move mouse to middle of the timegraph
        timegraph.moveMouseToWidget();
        int mask = 0;
        for (int modifier : modifiers) {
            mask |= modifier;
        }
        timegraph.pressShortcut(mask, c);
    }

    private static void resetMousePosition(SWTBotTimeGraph timegraph, boolean inNs, MouseEvent mouseEvent) {
        Rectangle rect = timegraph.widget.getBounds();
        if (!inNs) {
            mouseEvent.x = rect.width - 1;
        } else {
            mouseEvent.x = 1;
        }
        mouseEvent.y = rect.height / 2 + rect.y;
        timegraph.widget.mouseMove(mouseEvent);
    }

    private static class PaletteIsPresent extends DefaultCondition {

        private String fFailureMessage;
        private List<RGB> fRgbs;
        private Rectangle fRect;

        public PaletteIsPresent(List<RGB> rgbs, Rectangle bounds) {
            fRgbs = rgbs;
            fRect = bounds;
        }

        @Override
        public boolean test() throws Exception {
            ImageHelper image = ImageHelper.grabImage(fRect);
            if (image == null) {
                fFailureMessage = "Grabbed image is null";
                return false;
            }
            Multiset<RGB> histogram = image.getHistogram();
            for (RGB rgb : fRgbs) {
                if (histogram.count(rgb) <= 0) {
                    fFailureMessage = "Color not found: " + rgb;
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getFailureMessage() {
            return fFailureMessage;
        }
    }

    /**
     * Test time graph with color palettes
     */
    @Ignore
    @Test
    public void testPalettes() {
        resetTimeRange();
        TimeGraphViewStub view = getView();
        Rectangle bounds = fBounds;
        IPaletteProvider paletteBlue = SequentialPaletteProvider.create(new RGBAColor(0x23, 0x67, 0xf3, 0xff), 5);
        UIThreadRunnable.syncExec(() -> view.setPresentationProvider(new PalettedPresentationProvider() {
            @Override
            public IPaletteProvider getPalette() {
                return paletteBlue;
            }
        }));
        List<RGB> rgbs = Lists.transform(paletteBlue.get(), a -> RGBAUtil.fromInt(a.toInt()).rgb);
        fViewBot.bot().waitUntil(new PaletteIsPresent(rgbs, bounds));

        IPaletteProvider paletteGreen = SequentialPaletteProvider.create(new RGBAColor(0x23, 0xf3, 0x67, 0xff), 5);
        UIThreadRunnable.syncExec(() -> view.setPresentationProvider(new PalettedPresentationProvider() {
            @Override
            public IPaletteProvider getPalette() {
                return paletteGreen;
            }
        }));
        rgbs = Lists.transform(paletteGreen.get(), a -> RGBAUtil.fromInt(a.toInt()).rgb);
        fViewBot.bot().waitUntil(new PaletteIsPresent(rgbs, bounds));

        IPaletteProvider rotating = new QualitativePaletteProvider.Builder().setAttenuation(0.5f).setBrightness(1.0f).setNbColors(4).build();
        UIThreadRunnable.syncExec(() -> view.setPresentationProvider(new PalettedPresentationProvider() {
            @Override
            public IPaletteProvider getPalette() {
                return rotating;
            }
        }));
        rgbs = Lists.transform(rotating.get(), a -> RGBAUtil.fromInt(a.toInt()).rgb);
        fViewBot.bot().waitUntil(new PaletteIsPresent(rgbs, bounds));
    }

    /**
     * Integration test for the time event filtering dialog
     */
    @Test
    public void testTimegraphEventFiltering() {
        SWTWorkbenchBot bot = fBot;
        resetTimeRange();

        SWTBot viewBot = fViewBot.bot();
        SWTBotTimeGraph timegraph = fTimeGraph;
        assertTrue("timegraph visible", timegraph.isVisible());
        timegraph.setFocus();

        Rectangle bounds = fBounds;

        ImageHelper ref = ImageHelper.grabImage(bounds);

        timegraph.setFocus();
        // Move mouse to middle of the timegraph
        timegraph.moveMouseToWidget();
        // Press '/' to open the filter dialog
        timegraph.pressShortcut(KeyStroke.getInstance('/'));

        SWTBotShell dialogShell = viewBot.shell("Time Event Filter").activate();
        SWTBot shellBot = dialogShell.bot();
        SWTBotText text = shellBot.text();
        text.setText("Hat1");
        bot.waitWhile(fTimeGraphIsDirty);

        timegraph.setFocus();
        ImageHelper filtered = ImageHelper.waitForNewImage(bounds, ref);

        /* Compare with the original, they should be different */
        int refHatCount = ref.getHistogram().count(fHat);
        int filteredHatCount = filtered.getHistogram().count(fHat);
        int refHairCount = ref.getHistogram().count(fHair);
        int filteredHairCount = filtered.getHistogram().count(fHair);
        assertTrue("Count of \"HAT\" did not decrease to non-zero", filteredHatCount < refHatCount && filteredHatCount > 0);
        assertTrue("Count of \"HAIR\" did not decrease to zero", filteredHairCount < refHairCount && filteredHairCount == 0);

        int count = getVisibleItems(timegraph);

        dialogShell = viewBot.shell("Time Event Filter").activate();
        shellBot = dialogShell.bot();
        text = shellBot.text();
        text.setFocus();
        SWTBotUtils.pressShortcut(text, Keystrokes.CR);

        bot.waitWhile(fTimeGraphIsDirty);
        int newCount = getVisibleItems(timegraph);
        assertTrue("Fewer entries should be visible here. Current value is " + newCount + " previous was " + count, newCount < count);

    }

    private static int getVisibleItems(SWTBotTimeGraph timegraph) {
        return UIThreadRunnable.syncExec(Display.getDefault(), new IntResult() {
            @Override
            public Integer run() {
                int count = 0;
                TimeGraphControl control = timegraph.widget;
                ITimeGraphEntry[] expandedElements = control.getExpandedElements();
                for (ITimeGraphEntry entry : expandedElements) {
                    Rectangle itemBounds = control.getItemBounds(entry);
                    if (itemBounds.height > 0) {
                        count++;
                    }
                }
                return count;
            }
        });
    }

    private void timeGraphIsReadyCondition(@NonNull TmfTimeRange selectionRange) {
        IWorkbenchPart part = fViewBot.getViewReference().getPart(false);
        fBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) part, selectionRange, selectionRange.getEndTime()));
    }

    private abstract class ConditionHelper implements ICondition {
        @Override
        public final String getFailureMessage() {
            return null;
        }

        @Override
        public final void init(SWTBot bot) {
            // Do nothing
        }
    }

    private class TgConditionHelper extends ConditionHelper {

        private final Predicate<Object> fTestLogic;

        public TgConditionHelper(Predicate<Object> testLogic) {
            assertNotNull(testLogic);
            fTestLogic = testLogic;
        }

        @Override
        public final boolean test() throws Exception {
            if (!fTimeGraphIsDirty.test()) {
                return true;
            }
            return fTestLogic.test(null);
        }

    }

    class FileWritten extends ConditionHelper {

        private File fFile;
        private int fAmount;

        /**
         * constructor
         *
         * @param file
         *            the file
         * @param amount
         *            the minimum size of number of bytes
         */
        public FileWritten(File file, int amount) {
            fFile = file;
            fAmount = amount;

        }

        @Override
        public boolean test() throws Exception {
            return fFile.length() >= fAmount;
        }
    }

    private class WindowRangeCondition extends DefaultCondition {
        TimeGraphViewStub fView;
        long fExpectedRange;

        public WindowRangeCondition(TimeGraphViewStub view, long expectedRange) {
            fView = view;
            fExpectedRange = expectedRange;
        }

        @Override
        public boolean test() throws Exception {
            return getDuration(fView.getWindowRange()) == fExpectedRange;
        }

        @Override
        public String getFailureMessage() {
            return "Expected window range (" + fExpectedRange + ") not achieved. Actual=" + getDuration(fView.getWindowRange());
        }
    }

    private class WindowRangeUpdatedCondition extends DefaultCondition {
        TimeGraphViewStub fView;
        long fPreviousRange;
        boolean fIsIncreased;

        public WindowRangeUpdatedCondition(TimeGraphViewStub view, long previousRange, boolean increased) {
            fView = view;
            fPreviousRange = previousRange;
            fIsIncreased = increased;
        }

        @Override
        public boolean test() throws Exception {
            long newRange = getDuration(fView.getWindowRange());
            if (fIsIncreased) {
                return newRange > fPreviousRange;
            }
            return newRange < fPreviousRange;
        }

        @Override
        public String getFailureMessage() {
            return "Window range didn't " + (fIsIncreased ? "increase" : "decrease") +
                    " (previous: " + fPreviousRange + ", actual: " + getDuration(fView.getWindowRange()) + ")";
        }
    }

}
