/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.ui.swtbot.tests.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory.SegmentStoreType;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests of the latency table to extend it to custom tables, 4 steps are needed.
 * <ol>
 * <li>Override {@link #createSegment(long, long)}</li>
 * <li>Override {@link #openTable()} to open the desired table view</li>
 * <li>Override {@link #getSegStoreProvider()} to retrieve the segment store
 * provider with the desirable aspects</li>
 * <li>Override {@link #testTsv(String[])} to test the content of the output to
 * TSV</li>
 * </ol>
 *
 * Feel free to override any test and add additional tests but remember to call
 * <code>super.test()</code> before.
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class SegmentTableTest {

    /**
     * Test table
     *
     * @author Matthew Khouzam
     */
    public static final class TestSegmentStoreTableView extends AbstractSegmentStoreTableView {
        /**
         * ID of this view
         */
        public static final String ID = "org.eclipse.tracecompass.analysis.timing.ui.swtbot.tests.table.TestSegmentStoreTableView"; //$NON-NLS-1$

        /**
         * Constructor
         */
        public TestSegmentStoreTableView() {
        }

        SegmentTableTest fTest;

        /**
         * Set the parent test
         *
         * @param test
         *            the test
         */
        public void setTest(SegmentTableTest test) {
            fTest = test;
        }

        @Override
        protected @NonNull AbstractSegmentStoreTableViewer createSegmentStoreViewer(@NonNull TableViewer tableViewer) {
            return new AbstractSegmentStoreTableViewer(tableViewer) {

                @Override
                protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(@NonNull ITmfTrace trace) {
                    return fTest.getSegStoreProvider();
                }
            };
        }
    }

    private final class SimpleSegmentStoreProvider implements ISegmentStoreProvider {
        @Override
        public void removeListener(@NonNull IAnalysisProgressListener listener) {
            // do nothing
        }

        @Override
        public @Nullable ISegmentStore<@NonNull ISegment> getSegmentStore() {
            return fSs;
        }

        @Override
        public @NonNull Iterable<@NonNull ISegmentAspect> getSegmentAspects() {
            return Collections.emptyList();
        }

        @Override
        public void addListener(@NonNull IAnalysisProgressListener listener) {
            // do nothing
        }
    }

    private AbstractSegmentStoreTableView fTableView;
    private AbstractSegmentStoreTableViewer fTable;
    private ISegmentStoreProvider fSsp;
    private final ISegmentStore<@NonNull ISegment> fSs = SegmentStoreFactory.createSegmentStore(SegmentStoreType.Fast);
    /**
     * The workbench bot used during the test
     */
    protected static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Before class, call by all subclassed
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
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", fBot);
        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Opens a latency table
     */
    @Before
    public void init() {
        setTableView(openTable());
        assertNotNull(getTableView());
        setTable(getTableView().getSegmentStoreViewer());
        assertNotNull(getTable());
        ISegmentStoreProvider segStoreProvider = getSegStoreProvider();
        assertNotNull(segStoreProvider);
        UIThreadRunnable.syncExec(() -> getTable().setSegmentProvider(segStoreProvider));
    }

    /**
     * Close the table
     */
    @After
    public void finish() {
        new SWTWorkbenchBot().viewById(getTableView().getSite().getId()).close();
    }

    /**
     * Create the table viewer to test
     *
     * @return the table viewer bot
     */
    protected AbstractSegmentStoreTableView openTable() {
        AbstractSegmentStoreTableView tableView = getTableView();
        if (tableView != null) {
            return tableView;
        }
        IViewPart vp = null;
        final IWorkbench workbench = PlatformUI.getWorkbench();
        vp = UIThreadRunnable.syncExec((Result<IViewPart>) () -> {
            try {
                return workbench.getActiveWorkbenchWindow().getActivePage().showView(TestSegmentStoreTableView.ID);
            } catch (PartInitException e) {
                return null;
            }
        });
        assertNotNull(vp);
        assertTrue(vp instanceof TestSegmentStoreTableView);
        TestSegmentStoreTableView testSegmentStoreTableView = (TestSegmentStoreTableView) vp;
        testSegmentStoreTableView.setTest(this);
        fTableView = testSegmentStoreTableView;

        return fTableView;
    }

    /**
     * Create a segment of the type supported by the table under test, with the
     * requested start and end time
     *
     * @param start
     *            start time
     * @param end
     *            end time
     * @return the segment
     */
    protected @NonNull ISegment createSegment(long start, long end) {
        return new BasicSegment(start, end);
    }

    /**
     * Test a climbing data structure.
     * <p>
     * Create segments that are progressively larger and start later. Test that
     * the "duration" column sorts well
     */
    @Test
    public void climbTest() {
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore();
        for (int i = 0; i < 100; i++) {
            fixture.add(createSegment(i, 2 * i));
        }

        assertNotNull(getTable());
        getTable().updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "99", 0, 2));
    }

    /**
     * Test a decrementing data structure.
     * <p>
     * Create segments that are progressively shorter and start sooner,
     * effectively the inverse sorted {@link #climbTest()} datastructure. Test
     * that the "duration" column sorts well
     */
    @Test
    public void decrementingTest() {
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore();
        for (int i = 100; i >= 0; i--) {
            fixture.add(createSegment(i, 2 * i));
        }
        assertNotNull(getTable());
        getTable().updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "100", 0, 2));
    }

    /**
     * Test small table
     * <p>
     * Test table with 2 segments. Duration sort is tested.
     */
    @Test
    public void smallTest() {
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore();
        for (int i = 1; i >= 0; i--) {
            fixture.add(createSegment(i, 2 * i));
        }
        assertNotNull(getTable());
        getTable().updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1", 0, 2));
    }

    /**
     * Test large table
     * <p>
     * Test table with over 9000 segments. Duration sort is tested.
     */
    @Test
    public void largeTest() {
        final int size = 1000000;
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore();
        for (int i = 0; i < size; i++) {
            fixture.add(createSegment(i, 2 * i));
        }
        assertNotNull(getTable());
        getTable().updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "999,999", 0, 2));
    }

    /**
     * Test table with segments that have durations spread into a random (white
     * noise) distribution
     * <p>
     * Test table with a random distribution of segments. Duration sort is
     * tested.
     */
    @Test
    public void noiseTest() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        final int size = 1000000;
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore();
        for (int i = 0; i < size; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            int end = start + Math.abs(rnd.nextInt(1000000));
            fixture.add(createSegment(start, end));
        }
        assertNotNull(getTable());
        getTable().updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "374,153", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "999,999", 0, 2));
    }

    /**
     * Test table with segments that have durations spread into a gaussian
     * (normal) distribution
     * <p>
     * Test table with a gaussian distribution of segments. Duration sort is
     * tested.
     */
    @Test
    public void gaussianNoiseTest() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore();
        for (int i = 1; i <= 1000000; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            final int delta = Math.abs(rnd.nextInt(1000));
            int end = start + delta * delta;
            fixture.add(createSegment(start, end));
        }
        assertNotNull(getTable());
        getTable().updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "23,409", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "998,001", 0, 2));
    }

    /**
     * Test table with an on-disk segment store that is lazy loaded in the table
     *
     * @throws IOException
     */
    @Test
    public void onDiskSegStoreTest() throws IOException {
        Path segmentFile = Files.createTempFile("tmpSegStore", ".tmp");
        try {
            final int size = 1000000;
            ISegmentStore<@NonNull BasicSegment> fixture = SegmentStoreFactory.createOnDiskSegmentStore(segmentFile, BasicSegment.BASIC_SEGMENT_READ_FACTORY);
            for (int i = 0; i < size; i++) {
                fixture.add(new BasicSegment(i, 2 * i));
            }
            assertNotNull(getTable());
            getTable().updateModel(fixture);
            SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
            fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
            tableBot.header("Duration").click();
            fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
            tableBot.header("Duration").click();
            // FIXME: Should be 999,999, but sorting on disk does not work well yet
            fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "818,799", 0, 2));
        } finally {
            Files.delete(segmentFile);
        }
    }

    /**
     * Test creating a tsv
     *
     * @throws NoSuchMethodException
     *             Error creating the tsv
     * @throws IOException
     *             no such file or the file is locked.
     */
    @Test
    public void testWriteToTsv() throws NoSuchMethodException, IOException {

        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore();
        for (int i = 1; i <= 20; i++) {
            int start = i;
            final int delta = i;
            int end = start + delta * delta;
            fixture.add(createSegment(start, end));
        }
        assertNotNull(getTable());
        getTable().updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1", 0, 2));
        SWTWorkbenchBot swtWorkbenchBot = new SWTWorkbenchBot();
        SWTBotView viewBot = swtWorkbenchBot.viewById(getTableView().getSite().getId());
        String[] lines = extractTsv(viewBot);
        testTsv(lines);
        List<String> actionResult = Arrays.asList(lines);
        String absolutePath = TmfTraceManager.getTemporaryDirPath() + File.separator + "syscallLatencyTest.testWriteToTsv.tsv";
        TmfFileDialogFactory.setOverrideFiles(absolutePath);
        SWTBotMenu menuBot = viewBot.viewMenu().menu("Export to TSV");
        try {
            assertTrue(menuBot.isEnabled());
            assertTrue(menuBot.isVisible());
            menuBot.click();

            try (BufferedReader br = new BufferedReader(new FileReader(absolutePath))) {
                List<String> actual = br.lines().collect(Collectors.toList());
                assertEquals("Both reads", actionResult, actual);
            }
        } finally {
            new File(absolutePath).delete();
        }

    }

    private String[] extractTsv(SWTBotView viewBot) throws NoSuchMethodException, SecurityException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertNotNull(os);
        Class<@NonNull AbstractSegmentStoreTableView> clazz = AbstractSegmentStoreTableView.class;
        Method method = clazz.getDeclaredMethod("exportToTsv", java.io.OutputStream.class);
        method.setAccessible(true);
        final Exception[] except = new Exception[1];
        UIThreadRunnable.syncExec(() -> {
            try {
                method.invoke(getTableView(), os);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                except[0] = e;
            }
        });
        assertNull(except[0]);
        @SuppressWarnings("null")
        String[] lines = String.valueOf(os).split(System.getProperty("line.separator"));
        return lines;
    }

    /**
     * Test the TSV generated. For each line, including the header, it should be
     * asserted that it is equal to the expected line
     *
     * @param lines
     *            every entry, starting with the header
     */
    protected void testTsv(String[] lines) {
        assertNotNull(lines);
        assertEquals("number of lines", 21, lines.length);
        assertEquals("header", "Start Time\tEnd Time\tDuration", lines[0]);
        // not a straight up string compare due to time zones. Kathmandu and
        // Eucla have 15 minute time zones.
        assertTrue("line 1", lines[1].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s001\\t\\d\\d:\\d\\d:00.000 000 002\\t1"));
        assertTrue("line 2", lines[2].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s002\\t\\d\\d:\\d\\d:00.000 000 006\\t4"));
        assertTrue("line 3", lines[3].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s003\\t\\d\\d:\\d\\d:00.000 000 012\\t9"));
        assertTrue("line 4", lines[4].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s004\\t\\d\\d:\\d\\d:00.000 000 020\\t16"));
        assertTrue("line 5", lines[5].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s005\\t\\d\\d:\\d\\d:00.000 000 030\\t25"));
        assertTrue("line 6", lines[6].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s006\\t\\d\\d:\\d\\d:00.000 000 042\\t36"));
        assertTrue("line 7", lines[7].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s007\\t\\d\\d:\\d\\d:00.000 000 056\\t49"));
        assertTrue("line 8", lines[8].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s008\\t\\d\\d:\\d\\d:00.000 000 072\\t64"));
        assertTrue("line 9", lines[9].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s009\\t\\d\\d:\\d\\d:00.000 000 090\\t81"));
        assertTrue("line 10", lines[10].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s010\\t\\d\\d:\\d\\d:00.000 000 110\\t100"));
        assertTrue("line 11", lines[11].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s011\\t\\d\\d:\\d\\d:00.000 000 132\\t121"));
        assertTrue("line 12", lines[12].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s012\\t\\d\\d:\\d\\d:00.000 000 156\\t144"));
        assertTrue("line 13", lines[13].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s013\\t\\d\\d:\\d\\d:00.000 000 182\\t169"));
        assertTrue("line 14", lines[14].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s014\\t\\d\\d:\\d\\d:00.000 000 210\\t196"));
        assertTrue("line 15", lines[15].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s015\\t\\d\\d:\\d\\d:00.000 000 240\\t225"));
        assertTrue("line 16", lines[16].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s016\\t\\d\\d:\\d\\d:00.000 000 272\\t256"));
        assertTrue("line 17", lines[17].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s017\\t\\d\\d:\\d\\d:00.000 000 306\\t289"));
        assertTrue("line 18", lines[18].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s018\\t\\d\\d:\\d\\d:00.000 000 342\\t324"));
        assertTrue("line 19", lines[19].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s019\\t\\d\\d:\\d\\d:00.000 000 380\\t361"));
        assertTrue("line 20", lines[20].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s020\\t\\d\\d:\\d\\d:00.000 000 420\\t400"));
    }

    /**
     * Gets the table view
     *
     * @return the table view
     */
    protected AbstractSegmentStoreTableView getTableView() {
        return fTableView;
    }

    /**
     * Sets the table view
     *
     * @param tableView
     *            the table view
     */
    protected void setTableView(AbstractSegmentStoreTableView tableView) {
        fTableView = tableView;
    }

    /**
     * Gets the table viewer
     *
     * @return the table viewer
     */
    protected AbstractSegmentStoreTableViewer getTable() {
        return fTable;
    }

    /**
     * Set the table viewer
     *
     * @param table
     *            the table viewer
     */
    protected void setTable(AbstractSegmentStoreTableViewer table) {
        fTable = table;
    }

    /**
     * get the segment store provider
     *
     * @return the segment store provider
     */
    protected ISegmentStoreProvider getSegStoreProvider() {
        ISegmentStoreProvider ssp = fSsp;
        if (ssp == null) {
            ssp = new SimpleSegmentStoreProvider();
            fSsp = ssp;
        }
        return ssp;
    }
}