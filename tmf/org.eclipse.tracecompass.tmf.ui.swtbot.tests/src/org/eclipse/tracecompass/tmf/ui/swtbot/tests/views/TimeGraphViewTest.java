/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.QualitativePaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.presentation.SequentialPaletteProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
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
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * Test for Timegraph views in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TimeGraphViewTest {

    private static final Logger fLogger = Logger.getRootLogger();

    private static final RGB HAIR = ImageHelper.adjustExpectedColor(new RGB(0, 64, 128));
    private static final RGB HAT = ImageHelper.adjustExpectedColor(new RGB(0, 255, 0));
    private static final RGB LASER = ImageHelper.adjustExpectedColor(new RGB(255, 0, 0));

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

    private SWTBotView fViewBot;

    private TmfTraceStub fTrace;

    private Rectangle fBounds;

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

    private static void resetViews() {
        SWTBotUtils.switchToTracingPerspective();

        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.closeAllEditors();
        SWTBotUtils.closeView("welcome", bot);
    }

    /**
     * Set up for test
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();
        resetViews();
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
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
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.closeAllEditors();
        for (SWTBotView viewBot : bot.views()) {
            viewBot.close();
        }
        SWTBotUtils.openView(TimeGraphViewStub.ID);
        fViewBot = bot.viewById(TimeGraphViewStub.ID);

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
        SWTBotTimeGraph tgBot = new SWTBotTimeGraph(fViewBot.bot());

        // Wait for trace to be loaded
        fViewBot.bot().waitUntil(new TgConditionHelper(t -> tgBot.getEntries().length == 0));
        fBounds = getBounds();
        UIThreadRunnable.syncExec(() -> TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, trace, null)));
        // Wait for trace to be loaded
        fViewBot.bot().waitUntil(new TgConditionHelper(t -> tgBot.getEntries().length >= 2));

        resetTimeRange(bot);
        // Make sure the thumb is over 1 in size
        bot.waitUntil(new TgConditionHelper(t -> fViewBot.bot().slider().getThumb() > 1));
    }

    private void resetTimeRange(SWTWorkbenchBot bot) {
        TmfTimeRange fullTimeRange = fTrace.getTimeRange();
        TmfTimeRange smallerRange = new TmfTimeRange(TmfTimestamp.fromNanos(20), TmfTimestamp.fromNanos(100));
        TmfWindowRangeUpdatedSignal signal = new TmfWindowRangeUpdatedSignal(this, smallerRange);

        TimeGraphViewStub view = getView();
        // This is an oddity: the signals may be lost if a view is not initialized, so
        // they are send in the condition.
        //
        // NOTE: maybe use a shorter poll delay?
        //
        // Workflow: it should always fail at first and then a signal is sent.
        // it will either time out or work.
        bot.waitUntil(new TgConditionHelper(t -> {
            if (smallerRange.equals(view.getWindowRange())) {
                return true;
            }
            TmfSignalManager.dispatchSignal(signal);
            return false;
        }));
        // same as above: re-run as much as you need.
        bot.waitUntil(new TgConditionHelper(t -> {
            if (fullTimeRange.equals(view.getWindowRange())) {
                return true;
            }
            fViewBot.toolbarButton("Reset the Time Scale to Default").click();
            return false;
        }));
    }

    private Rectangle getBounds() {
        Rectangle bounds = UIThreadRunnable.syncExec((Result<Rectangle>) () -> {
            Control control = (Control) fViewBot.getWidget();
            Rectangle ctrlRelativeBounds = control.getBounds();
            Point res = control.toDisplay(new Point(0, 0));
            ctrlRelativeBounds.x = res.x;
            ctrlRelativeBounds.y = res.y;
            return ctrlRelativeBounds;
        });
        return bounds;
    }

    private TimeGraphViewStub getView() {
        IWorkbenchPart part = fViewBot.getViewReference().getPart(true);
        assertTrue(part.getClass().getCanonicalName(), part instanceof TimeGraphViewStub);
        TimeGraphViewStub stubView = (TimeGraphViewStub) part;
        return stubView;
    }

    /**
     * Clean up after a test, reset the views and reset the states of the timegraph
     * by pressing reset on all the resets of the legend
     */
    @After
    public void after() {

        // reset all
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotShell legendShell = bot.shell(LEGEND_NAME);
        SWTBot legendBot = legendShell.bot();

        for (int i = 0; i < StubPresentationProvider.STATES.length; i++) {
            legendBot.button(i).click();
        }
        legendBot.button(OK_BUTTON).click();
        TmfTraceStub trace = fTrace;
        assertNotNull(trace);
        TmfSignalManager.dispatchSignal(new TmfTraceClosedSignal(this, trace));
        bot.waitUntil(Conditions.shellCloses(legendShell));
        fViewBot.close();
        bot.waitUntil(ConditionHelpers.ViewIsClosed(fViewBot));
        fTrace.dispose();
    }

    /**
     * Put things back the way they were
     */
    @AfterClass
    public static void afterClass() {
        resetViews();
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
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        resetTimeRange(bot);
        Rectangle bounds = fBounds;

        ImageHelper ref = ImageHelper.grabImage(bounds);

        // Set the widths to 0.25

        fViewBot.toolbarButton(SHOW_LEGEND).click();
        SWTBotShell legendShell = bot.shell(LEGEND_NAME);
        legendShell.activate();
        SWTBot legendBot = legendShell.bot();
        legendBot.scale(5).setValue(100);
        legendShell.bot().button(OK_BUTTON).click();
        bot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange(bot);

        // Take another picture
        ImageHelper thick = ImageHelper.waitForNewImage(bounds, ref);

        // Compare with the original, they should be different
        int refCount = ref.getHistogram().count(LASER);
        int thickCount = thick.getHistogram().count(LASER);
        assertTrue(String.format("Count of \"\"LASER\"\" (%s) did not get change despite change of width before: %d after:%d histogram:%s", LASER, refCount, thickCount, Multisets.copyHighestCountFirst(thick.getHistogram())), thickCount > refCount);

        // reset all
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        legendShell = bot.shell(LEGEND_NAME);
        legendBot = legendShell.bot();
        legendBot.button(5).click();
        legendBot.button(OK_BUTTON).click();
        bot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange(bot);

        // take a third picture
        ImageHelper reset = ImageHelper.waitForNewImage(bounds, thick);

        // Compare with the original, they should be the same
        int resetCount = reset.getHistogram().count(LASER);
        assertEquals("Count of \"\"LASER\"\" did not get change despite reset of width", refCount, resetCount);
    }

    /**
     * Test the legend operation. Change sliders and reset, do not change colors as
     * there is not mock of the color picker yet
     *
     * TODO: mock color picker
     */
    @Test
    public void testLegend() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        resetTimeRange(bot);
        Rectangle bounds = fBounds;

        ImageHelper ref = ImageHelper.grabImage(bounds);

        // Set the widths to 0.25

        fViewBot.toolbarButton(SHOW_LEGEND).click();
        SWTBotShell legendShell = bot.shell(LEGEND_NAME);
        legendShell.activate();
        SWTBot legendBot = legendShell.bot();
        legendBot.scale(2).setValue(25);
        legendShell.bot().button(OK_BUTTON).click();
        bot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange(bot);

        // Take another picture
        ImageHelper skinny = ImageHelper.waitForNewImage(bounds, ref);

        /* Compare with the original, they should be different */
        int refCount = ref.getHistogram().count(HAIR);
        int skinnyCount = skinny.getHistogram().count(HAIR);
        assertTrue(String.format("Count of \"\"HAIR\"\" (%s) did not get change despite change of width before: %d after:%d histogram:%s", HAIR, refCount, skinnyCount, Multisets.copyHighestCountFirst(skinny.getHistogram())), skinnyCount < refCount);

        // reset all
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        legendShell = bot.shell(LEGEND_NAME);
        legendBot = legendShell.bot();
        legendBot.button(2).click();
        legendBot.button(OK_BUTTON).click();
        bot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange(bot);

        // take a third picture
        ImageHelper reset = ImageHelper.waitForNewImage(bounds, skinny);

        // Compare with the original, they should be the same
        int resetCount = reset.getHistogram().count(HAIR);
        assertEquals("Count of \"HAIR\" did not get change despite reset of width", refCount, resetCount);
    }

    /**
     * Test the legend and export operations. Resize states, take screenshots and
     * compare, they should be different. then reset the sizes and compare, they
     * should be the same.
     *
     * NOTE: This utterly fails in GTK3.
     *
     * @throws IOException
     *             file not found, someone deleted files while the test is running
     */
    @Test
    public void testExport() throws IOException {

        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        resetTimeRange(bot);
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
        bot.waitUntil(new FileWritten(ref, MIN_FILE_SIZE));

        /* Set the widths to skinny */
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        SWTBotShell legendShell = bot.shell(LEGEND_NAME);
        legendShell.activate();
        SWTBot legendBot = legendShell.bot();
        legendBot.scale(2).setValue(50);
        legendShell.bot().button(OK_BUTTON).click();
        bot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange(bot);

        /* Take another picture */
        TmfFileDialogFactory.setOverrideFiles(skinny.getAbsolutePath());
        fViewBot.viewMenu(EXPORT_MENU).click();
        ImageHelper skinnyImage = ImageHelper.fromFile(skinny);
        bot.waitUntil(new FileWritten(skinny, MIN_FILE_SIZE));

        /* Compare with the original, they should be different */
        int refCount = refImage.getHistogram().count(HAIR);
        int skinnyCount = skinnyImage.getHistogram().count(HAIR);
        assertTrue(String.format("Count of \"\"HAIR\"\" (%s) did not get change despite change of width before: %d after:%d histogram:%s", HAIR, refCount, skinnyCount, Multisets.copyHighestCountFirst(skinnyImage.getHistogram())), skinnyCount < refCount);

        /* reset all */
        fViewBot.toolbarButton(SHOW_LEGEND).click();
        legendShell = bot.shell(LEGEND_NAME);
        legendBot = legendShell.bot();
        legendBot.button(2).click();
        legendBot.button(OK_BUTTON).click();
        bot.waitUntil(Conditions.shellCloses(legendShell));
        resetTimeRange(bot);

        /* take a third picture */
        TmfFileDialogFactory.setOverrideFiles(reset.getAbsolutePath());
        fViewBot.viewMenu(EXPORT_MENU).click();
        bot.waitUntil(new FileWritten(reset, MIN_FILE_SIZE));
        ImageHelper resetImage = ImageHelper.fromFile(reset);

        /* Compare with the original, they should be the same */
        int resetCount = resetImage.getHistogram().count(HAIR);
        assertEquals("Count of \"HAIR\" did not get change despite reset of width", refCount, resetCount);
    }

    private static class PaletteIsPresent extends DefaultCondition  {

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
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        resetTimeRange(bot);
        TimeGraphViewStub view = getView();
        Rectangle bounds = fBounds;
        IPaletteProvider paletteBlue = SequentialPaletteProvider.create(new RGBAColor(0x23, 0x67, 0xf3, 0xff), 5);
        UIThreadRunnable.syncExec(() -> view.setPresentationProvider(new PalettedPresentationProvider() {
            @Override
            public IPaletteProvider getPalette() {
                return paletteBlue;
            }
        }));
        List<RGB> rgbs = Lists.transform(paletteBlue.get(), a->RGBAUtil.fromInt(a.toInt()).rgb);
        fViewBot.bot().waitUntil(new PaletteIsPresent(rgbs, bounds));

        IPaletteProvider paletteGreen = SequentialPaletteProvider.create(new RGBAColor(0x23, 0xf3, 0x67, 0xff), 5);
        UIThreadRunnable.syncExec(() -> view.setPresentationProvider(new PalettedPresentationProvider() {
            @Override
            public IPaletteProvider getPalette() {
                return paletteGreen;
            }
        }));
        rgbs = Lists.transform(paletteGreen.get(), a->RGBAUtil.fromInt(a.toInt()).rgb);
        fViewBot.bot().waitUntil(new PaletteIsPresent(rgbs, bounds));

        IPaletteProvider rotating = new QualitativePaletteProvider.Builder().setAttenuation(0.5f).setBrightness(1.0f).setNbColors(4).build();
        UIThreadRunnable.syncExec(() -> view.setPresentationProvider(new PalettedPresentationProvider() {
            @Override
            public IPaletteProvider getPalette() {
                return rotating;
            }
        }));
        rgbs = Lists.transform(rotating.get(), a->RGBAUtil.fromInt(a.toInt()).rgb);
        fViewBot.bot().waitUntil(new PaletteIsPresent(rgbs, bounds));
    }

    /**
     * Integration test for the time event filtering dialog
     */
    @Test
    public void testTimegraphEventFiltering() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        resetTimeRange(bot);

        SWTBot viewBot = fViewBot.bot();
        SWTBotTimeGraph timegraph = new SWTBotTimeGraph(viewBot);
        assertTrue("timegraph visible", timegraph.isVisible());
        timegraph.setFocus();

        Rectangle bounds = fBounds;

        ImageHelper ref = ImageHelper.grabImage(bounds);

        timegraph.setFocus();
        //Press '/' to open the filter dialog
        timegraph.pressShortcut(KeyStroke.getInstance('/'));

        SWTBotShell dialogShell = viewBot.shell("Time Event Filter").activate();
        SWTBot shellBot = dialogShell.bot();
        SWTBotText text = shellBot.text();
        text.setText("Hat1");
        bot.waitWhile(fTimeGraphIsDirty);

        timegraph.setFocus();
        ImageHelper filtered = ImageHelper.waitForNewImage(bounds, ref);

        /* Compare with the original, they should be different */
        int refHatCount = ref.getHistogram().count(HAT);
        int filteredHatCount = filtered.getHistogram().count(HAT);
        int refHairCount = ref.getHistogram().count(HAIR);
        int filteredHairCount = filtered.getHistogram().count(HAIR);
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

}
