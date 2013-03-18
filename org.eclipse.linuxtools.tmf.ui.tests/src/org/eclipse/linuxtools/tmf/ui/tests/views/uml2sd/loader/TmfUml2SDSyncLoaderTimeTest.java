/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader.TmfSyncMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for time synchronization handling.
 *
 * @author Bernd Hufmann
 */
public class TmfUml2SDSyncLoaderTimeTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Test case 001 expected values
    final static private Uml2SDTestTimestamp TC_001_TIME_VALUE       = new Uml2SDTestTimestamp(9788642228395L);
    final static private String              TC_001_MESSAGE_NAME     = "START_GAME_REPLY";
    final static private int                 TC_001_PAGE_VALUE       = 0;
    final static private int                 TC_001_START_OCCURRANCE = 6;
    final static private int                 TC_001_END_OCCURRANCE   = TC_001_START_OCCURRANCE;
    final static private String              TC_001_START_LIFELINE   = IUml2SDTestConstants.MASTER_PLAYER_NAME;
    final static private String              TC_001_END_LIFELINE     = IUml2SDTestConstants.FIRST_PLAYER_NAME;

    // Test case 002 expected values
    final static private Uml2SDTestTimestamp TC_002_TIME_VALUE       = new Uml2SDTestTimestamp(9789689830722L);
    final static private String              TC_002_MESSAGE_NAME     = "PAUSE_GAME_REQUEST";
    final static private int                 TC_002_PAGE_VALUE       = 2;
    final static private int                 TC_002_START_OCCURRANCE = 7;
    final static private int                 TC_002_END_OCCURRANCE   = TC_002_START_OCCURRANCE;
    final static private String              TC_002_START_LIFELINE   = IUml2SDTestConstants.FIRST_PLAYER_NAME;
    final static private String              TC_002_END_LIFELINE     = IUml2SDTestConstants.MASTER_PLAYER_NAME;

    // Test case 003 expected values
    final static private Uml2SDTestTimestamp TC_003_TIME_VALUE       = new Uml2SDTestTimestamp(9790750000000L);
    final static private int                 TC_003_PAGE_VALUE       = 4;

    // Test case 004 expected values
    final static private int                 TC_004_PAGE_VALUE       = 0;

    // Test case 005 expected values
    final static private int                 TC_005_PAGE_VALUE       = IUml2SDTestConstants.TOTAL_NUMBER_OF_PAGES - 1;

    // Test case 006 expected values
    final static private Uml2SDTestTimestamp TC_006_TIME_VALUE       = new Uml2SDTestTimestamp(9792420661655L);
    final static private int                 TC_006_PAGE_VALUE       = 4;
    final static private int                 TC_006_START_OCCURRANCE = IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE;
    final static private int                 TC_006_END_OCCURRANCE   = TC_006_START_OCCURRANCE;

    // Test case 007 expected values
    final static private Uml2SDTestTimestamp TC_007_TIME_VALUE       = new Uml2SDTestTimestamp(9792420756010L);
    final static private int                 TC_007_PAGE_VALUE       = 5;
    final static private int                 TC_007_START_OCCURRANCE = 1;
    final static private int                 TC_007_END_OCCURRANCE   = TC_007_START_OCCURRANCE;

    // Test case 008 expected values
    final static private Uml2SDTestTimestamp TC_008_TIME_VALUE       = new Uml2SDTestTimestamp(9788642228395L);
    final static private int                 TC_008_PAGE_VALUE       = 0;
    final static private Uml2SDTestTimestamp TC_008_START_TIME_VALUE = new Uml2SDTestTimestamp(9788642228395L);
    final static private Uml2SDTestTimestamp TC_008_END_TIME_VALUE   = new Uml2SDTestTimestamp(9789164833324L);

    // Test case 009 expected values
    final static private Uml2SDTestTimestamp TC_009_TIME_VALUE       = new Uml2SDTestTimestamp(9789689220871L);
    final static private int                 TC_009_PAGE_VALUE       = 1;
    final static private Uml2SDTestTimestamp TC_009_START_TIME_VALUE = TC_009_TIME_VALUE;
    final static private Uml2SDTestTimestamp TC_009_END_TIME_VALUE   = new Uml2SDTestTimestamp(9789773881426L);

    // Fields used in tests
    private static Uml2SDTestFacility fFacility;
    private static List<GraphNode> selection;
    private static TmfSyncMessage msg;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Initialization
     */
    @BeforeClass
    public static void setUpClass() {
        fFacility = Uml2SDTestFacility.getInstance();
        fFacility.selectExperiment();
    }

    /**
     * Cleanup
     */
    @AfterClass
    public static void tearDownClass() {
        fFacility.disposeExperiment();
        fFacility = null;
    }

    /**
     * Test Case: 001
     * Description: Verify synchToTime (exact time in page), selection of message in page
     * Verified Methods: loader.syncToTime(), loader.moveToMessage(), loader.moveToMessageInPage()
     * Expected result: Correct message is selected.
     */
    @Test
    public void verifySynchToTimeInPage() {
        fFacility.getTrace().broadcast(new TmfTimeSynchSignal(this, TC_001_TIME_VALUE));
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertEquals("synchToTime", TC_001_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull(selection);
        assertEquals("synchToTime", 1, selection.size());
        assertTrue(selection.get(0) instanceof TmfSyncMessage);
        msg = (TmfSyncMessage) selection.get(0);
        assertEquals("synchToTime", TC_001_MESSAGE_NAME, msg.getName());
        assertEquals("synchToTime", 0, TC_001_TIME_VALUE.compareTo(msg.getStartTime(), false));
        assertEquals("synchToTime", TC_001_START_OCCURRANCE, msg.getStartOccurrence());
        assertEquals("synchToTime", TC_001_END_OCCURRANCE, msg.getEndOccurrence());
        assertEquals("synchToTime", TC_001_START_LIFELINE, msg.getStartLifeline().getName());
        assertEquals("synchToTime", TC_001_END_LIFELINE, msg.getEndLifeline().getName());
    }

    /**
     * Test Case: 002
     * Description: Verify synchToTime (exact time outside of page), selection of message in page
     * Verified Methods: loader.syncToTime(), loader.moveToMessage(), loader.moveToPage()
     * Expected result: Correct message is selected.
     */
    @Test
    public void verifySynchToTimeOutsidePage() {
        fFacility.getTrace().broadcast(new TmfTimeSynchSignal(this, TC_002_TIME_VALUE));
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertEquals("synchToTime", TC_002_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull("synchToTime", selection);
        assertEquals("synchToTime", 1, selection.size());
        assertTrue("synchToTime", selection.get(0) instanceof TmfSyncMessage);
        msg = (TmfSyncMessage) selection.get(0);
        assertEquals("synchToTime", TC_002_MESSAGE_NAME, msg.getName());
        assertEquals(0, TC_002_TIME_VALUE.compareTo(msg.getStartTime(), false));
        assertEquals("synchToTime", TC_002_START_OCCURRANCE, msg.getStartOccurrence());
        assertEquals("synchToTime", TC_002_END_OCCURRANCE, msg.getEndOccurrence());
        assertEquals(TC_002_START_LIFELINE, msg.getStartLifeline().getName());
        assertEquals(TC_002_END_LIFELINE, msg.getEndLifeline().getName());
    }


    /**
     * Test Case: 003
     * Description: Verify synchToTime (timestamp doesn't exist in trace), no selection of message in page
     * Verified Methods: loader.syncToTime(), loader.moveToMessage(), loader.moveToPage()
     * Expected result: Move to correct page, currentTime is updated so that focus on the currentTime, but no selection.
     */
    @Test
    public void verifySynchToTimeNonExisting() {
        fFacility.getLoader().firstPage();

        fFacility.getTrace().broadcast(new TmfTimeSynchSignal(this, TC_003_TIME_VALUE));
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertEquals("synchToTime", 0, TC_003_TIME_VALUE.compareTo(fFacility.getLoader().getCurrentTime(), false));
        assertEquals("synchToTime", TC_003_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull("synchToTime", selection);
        assertEquals("synchToTime", 0, selection.size());
    }

    /**
     * Test Case: 004
     * Description: Verify synchToTime (timestamp < experiment time range start)
     * Verified Methods: loader.syncToTime(), loader.moveToMessage(), loader.moveToPage()
     * Expected result: Move to first page, focus on the beginning of the page, but no selection.
     */
    @Test
    public void verifySynchToTimeBeforeExpStart() {
        fFacility.getTrace().broadcast(new TmfTimeSynchSignal(this, TmfTimestamp.BIG_BANG));
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertEquals("synchToTime", TC_004_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull("synchToTime", selection);
        assertEquals("synchToTime", 0, selection.size());
    }

    /**
     * Test Case: 005
     * Description: Verify synchToTime (timestamp > experiment time range end)
     * Verified Methods: loader.syncToTime(), loader.moveToMessage(), loader.moveToPage()
     * Expected result: Move to last page, focus on the end of the page, but no selection.
     */
    @Test
    public void verifySynchToTimeAfterExpEnd() {
        fFacility.getTrace().broadcast(new TmfTimeSynchSignal(this, TmfTimestamp.BIG_CRUNCH));
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertEquals("synchToTime", TC_005_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull(selection);
        assertEquals("synchToTime", 0, selection.size());
    }

    /**
     * Test Case: 006
     * Description: Verify synchToTime (timestamp of last message in page)
     * Verified Methods: loader.syncToTime(), loader.moveToMessage(), loader.moveToPage(), loader.moveToMessageInPage()
     * Expected result: Move to correct page, selection of last message in page.
     */
    @Test
    public void verifySynchToTimeEqualsLast() {
        fFacility.getTrace().broadcast(new TmfTimeSynchSignal(this, TC_006_TIME_VALUE));
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertEquals("synchToTime", TC_006_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull("synchToTime", selection);
        assertEquals("synchToTime", 1, selection.size());
        msg = (TmfSyncMessage) selection.get(0);
        assertEquals("synchToTime", TC_006_START_OCCURRANCE, msg.getStartOccurrence());
        assertEquals("synchToTime", TC_006_END_OCCURRANCE, msg.getEndOccurrence());
    }

    /**
     * Test Case: 007
     * Description: Verify synchToTime (timestamp of first message in page)
     * Verified Methods: loader.syncToTime(), loader.moveToMessage(), loader.moveToPage()
     * Expected result: Move to correct page, selection of last message in page.
     */
    @Test
    public void verifySynchToTimeFirst() {
        fFacility.getTrace().broadcast(new TmfTimeSynchSignal(this, TC_007_TIME_VALUE));
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertEquals("synchToTime", TC_007_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull("synchToTime", selection);
        msg = (TmfSyncMessage) selection.get(0);
        assertEquals("synchToTime", 1, selection.size());
        assertEquals("synchToTime", TC_007_START_OCCURRANCE, msg.getStartOccurrence());
        assertEquals("synchToTime", TC_007_END_OCCURRANCE, msg.getEndOccurrence());
    }

    /**
     * Test Case: 008
     * Description: Verify time range signal (start, end time and current time are in same  page)
     * Verified Methods: loader.synchToTimeRange(), loader.moveToMessage(), loader.moveToMessageInPage()
     * Expected result: Move to correct page(=page of start time of range), set focus on start time of range, but no selection of message.
     */
    @Test
    public void verifyTimeRangeSamePage() {
        // 9788.642228395 (page 0) -> 9789.164833324 (page 0) with selected time 9788.642228395 (page 0)
        fFacility.getLoader().firstPage();
        TmfTimeRange range = new TmfTimeRange(TC_008_START_TIME_VALUE, TC_008_END_TIME_VALUE);
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        fFacility.getTrace().broadcast(new TmfRangeSynchSignal(this, range, TC_008_TIME_VALUE));
        assertEquals("synchToTimeRange", TC_008_PAGE_VALUE, fFacility.getLoader().currentPage());
        assertNotNull("synchToTimeRange", fFacility.getLoader().getCurrentTime());
        assertEquals("synchToTimeRange", 0, TC_008_TIME_VALUE.compareTo(fFacility.getLoader().getCurrentTime(), false));
        selection = fFacility.getSdView().getSDWidget().getSelection();
        // We actually don't want something to be selected!!!
        assertNotNull("synchToTimeRange", selection);
        assertEquals("synchToTimeRange", 0, selection.size());
    }

    /**
     * Test Case: 009
     * Description: Verify time range signal (start and end time are across 2 pages)
     * Verified Methods: loader.synchToTimeRange(), loader.moveToMessage(), loader.moveToPage()
     * Expected result: Move to correct page (=page of start time of range), set focus on start time of range, but no selection of message.
     */
    @Test
    public void verifyTimeRangeDifferentPages() {
        TmfTimeRange range = new TmfTimeRange(TC_009_START_TIME_VALUE, TC_009_END_TIME_VALUE);
        fFacility.getTrace().broadcast(new TmfRangeSynchSignal(this, range, TC_009_TIME_VALUE));
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertEquals("synchToTimeRange", TC_009_PAGE_VALUE, fFacility.getLoader().currentPage());
        assertNotNull("synchToTimeRange", fFacility.getLoader().getCurrentTime());
        assertEquals("synchToTimeRange", 0, TC_009_TIME_VALUE.compareTo(fFacility.getLoader().getCurrentTime(), false));
        selection = fFacility.getSdView().getSDWidget().getSelection();
        // We actually don't want something to be selected!!!
        assertNotNull("synchToTimeRange", selection);
        assertEquals("synchToTimeRange", 0, selection.size());
    }
}