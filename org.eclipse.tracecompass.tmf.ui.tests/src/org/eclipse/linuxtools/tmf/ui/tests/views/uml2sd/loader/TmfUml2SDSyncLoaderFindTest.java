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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.Criteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader.TmfSyncMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Search Test Cases.
 *
 * @author Bernd Hufmann
 */
public class TmfUml2SDSyncLoaderFindTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Test case 002 expected values
    final static private Uml2SDTestTimestamp TC_002_TIME_VALUE       = new Uml2SDTestTimestamp(9788642104149L);
    final static private String              TC_002_MESSAGE_NAME     = "GAME_REQUEST";
    final static private int                 TC_002_PAGE_VALUE       = 0;
    final static private int                 TC_002_START_OCCURRANCE = 3;
    final static private int                 TC_002_END_OCCURRANCE   = TC_002_START_OCCURRANCE;
    final static private String              TC_002_START_LIFELINE   = IUml2SDTestConstants.FIRST_PLAYER_NAME;
    final static private String              TC_002_END_LIFELINE     = IUml2SDTestConstants.MASTER_PLAYER_NAME;

    // Test case 003 expected values
    final static private Uml2SDTestTimestamp TC_003_TIME_VALUE       = new Uml2SDTestTimestamp(9788642113228L);
    final static private String              TC_003_MESSAGE_NAME     = "GAME_REPLY";
    final static private int                 TC_003_PAGE_VALUE       = 0;
    final static private int                 TC_003_START_OCCURRANCE = 4;
    final static private int                 TC_003_END_OCCURRANCE   = TC_003_START_OCCURRANCE;
    final static private String              TC_003_START_LIFELINE   = IUml2SDTestConstants.MASTER_PLAYER_NAME;
    final static private String              TC_003_END_LIFELINE     = IUml2SDTestConstants.FIRST_PLAYER_NAME;

    // Test case 004 expected values
    final static private Uml2SDTestTimestamp TC_004_TIME_VALUE       = new Uml2SDTestTimestamp(9791893030834L);
    final static private String              TC_004_MESSAGE_NAME     = "GAME_REQUEST";
    final static private int                 TC_004_PAGE_VALUE       = 4;
    final static private int                 TC_004_START_OCCURRANCE = 19;
    final static private int                 TC_004_END_OCCURRANCE   = TC_004_START_OCCURRANCE;
    final static private String              TC_004_START_LIFELINE   = IUml2SDTestConstants.SECOND_PLAYER_NAME;
    final static private String              TC_004_END_LIFELINE     = IUml2SDTestConstants.MASTER_PLAYER_NAME;

    // Test case 005 expected values
    final static private int                 TC_005_PAGE_VALUE       = 0;
    final static private String              TC_005_LIFELINE_NAME    = IUml2SDTestConstants.FIRST_PLAYER_NAME;

    // Test case 006 expected values
    final static private int                 TC_006_PAGE_VALUE       = 4;
    final static private String              TC_006_LIFELINE_NAME    = IUml2SDTestConstants.SECOND_PLAYER_NAME;

    // Fields used in tests
    private static Uml2SDTestFacility    fFacility;
    private static Uml2SDSignalValidator fTmfComponent;
    private static Criteria criteria;
    private static List<GraphNode> selection;
    private static TmfSyncMessage msg;
    private static Lifeline lifeline;

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
        fTmfComponent = new Uml2SDSignalValidator();
    }

    /**
     * Cleanup
     */
    @AfterClass
    public static void tearDownClass() {
        fTmfComponent.dispose();
        fFacility.disposeExperiment();
        fFacility = null;
    }

    /**
     * Verify the ISDGraphNodeSupporter implementation.
     *
     * Verified Methods: loader.isNodeSupported(), loader.getNodeName()
     * Expected result: Correct values are returned, i.e. only lifelines and
     *                  sync. messages are supported.
     */
    @Test
    public void verifyISDGraphNodeSupporter() {

        fFacility.firstPage();

        assertTrue("isNodeSupported", fFacility.getLoader().isNodeSupported(ISDGraphNodeSupporter.LIFELINE));
        assertTrue("isNodeSupported", fFacility.getLoader().isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGE));
        assertFalse("isNodeSupported", fFacility.getLoader().isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGERETURN));
        assertFalse("isNodeSupported", fFacility.getLoader().isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGE));
        assertFalse("isNodeSupported", fFacility.getLoader().isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGERETURN));
        assertFalse("isNodeSupported", fFacility.getLoader().isNodeSupported(ISDGraphNodeSupporter.STOP));

        assertEquals("getNodeName", "Lifeline", fFacility.getLoader().getNodeName(ISDGraphNodeSupporter.LIFELINE, null));
        assertEquals("getNodeName", "Interaction", fFacility.getLoader().getNodeName(ISDGraphNodeSupporter.SYNCMESSAGE, null));
        assertEquals("getNodeName", "", fFacility.getLoader().getNodeName(ISDGraphNodeSupporter.SYNCMESSAGERETURN, null));
        assertEquals("getNodeName", "", fFacility.getLoader().getNodeName(ISDGraphNodeSupporter.ASYNCMESSAGE, null));
        assertEquals("getNodeName", "", fFacility.getLoader().getNodeName(ISDGraphNodeSupporter.ASYNCMESSAGERETURN, null));
        assertEquals("getNodeName", "", fFacility.getLoader().getNodeName(ISDGraphNodeSupporter.STOP, null));

        fFacility.getLoader().cancel();
    }

    /**
     * Verify 1st message find within page.
     *
     * Verified Methods: loader.find(), loader.moveToMessage()
     * Expected result: Correct message is selected
     */
    @Test
    public void verifyFirstMessage() {
        fFacility.firstPage();

        criteria = new Criteria();
        criteria.setSyncMessageSelected(true);
        criteria.setExpression("GAME_.*");

        // set expected values
        fTmfComponent.setSource(fFacility.getLoader());
        fTmfComponent.setCurrentTime(TC_002_TIME_VALUE);
        fTmfComponent.setCurrentRange(null); // not used
        fTmfComponent.setSignalReceived(false);

        fFacility.getLoader().find(criteria);
        // Wait for the selection to finish - needed due to new platform behavior in Juno
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertTrue("find", fTmfComponent.isSignalReceived());
        assertFalse("find", fTmfComponent.isSignalError());
        assertFalse("find", fTmfComponent.isCurrentTimeError());
        assertFalse("find", fTmfComponent.isSourceError());

        assertEquals("find", TC_002_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull(selection);
        assertEquals("find", 1, selection.size());
        assertTrue(selection.get(0) instanceof TmfSyncMessage);
        msg = (TmfSyncMessage) selection.get(0);
        assertEquals("find", TC_002_MESSAGE_NAME, msg.getName());
        assertEquals("find", 0, TC_002_TIME_VALUE.compareTo(msg.getStartTime(), false));
        assertEquals("find", TC_002_START_OCCURRANCE, msg.getStartOccurrence());
        assertEquals("find", TC_002_END_OCCURRANCE, msg.getEndOccurrence());
        assertEquals("find", TC_002_START_LIFELINE, msg.getStartLifeline().getName());
        assertEquals("find", TC_002_END_LIFELINE, msg.getEndLifeline().getName());

        /**
         * Verify 2nd message find within page.
         *
         * Verified Methods: loader.find(), loader.moveToMessage()
         * Expected result: Correct message is selected
         */

        // set expected values
        fTmfComponent.setSource(fFacility.getLoader());
        fTmfComponent.setCurrentTime(TC_003_TIME_VALUE);
        fTmfComponent.setCurrentRange(null); // not used

        fTmfComponent.setSignalReceived(false);

        fFacility.getLoader().find(criteria);
        // Wait for the selection to finish - needed due to new platform behavior in Juno
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertTrue("find", fTmfComponent.isSignalReceived());
        assertFalse("find", fTmfComponent.isSignalError());
        assertFalse("find", fTmfComponent.isCurrentTimeError());
        assertFalse("find", fTmfComponent.isSourceError());

        assertEquals("find", TC_003_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull(selection);
        assertEquals("find", 1, selection.size());
        assertTrue(selection.get(0) instanceof TmfSyncMessage);
        msg = (TmfSyncMessage) selection.get(0);
        assertEquals("find", TC_003_MESSAGE_NAME, msg.getName());
        assertEquals("find", 0, TC_003_TIME_VALUE.compareTo(msg.getStartTime(), false));
        assertEquals("find", TC_003_START_OCCURRANCE, msg.getStartOccurrence());
        assertEquals("find", TC_003_END_OCCURRANCE, msg.getEndOccurrence());
        assertEquals("find", TC_003_START_LIFELINE, msg.getStartLifeline().getName());
        assertEquals("find", TC_003_END_LIFELINE, msg.getEndLifeline().getName());

        /**
         * Verify 1st message across page.
         *
         * Verified Methods: loader.find(), loader.moveToPage(), loader.moveToMessage()
         * Expected result: Correct message is selected
         */
        // set expected values
        fTmfComponent.setSource(fFacility.getLoader());
        fTmfComponent.setCurrentTime(TC_004_TIME_VALUE);
        fTmfComponent.setCurrentRange(new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH)); // not used

        fTmfComponent.setSignalReceived(false);

        fFacility.getLoader().find(criteria);
        fFacility.waitForJobs(); // find across pages uses a job
        // to make sure pageRequest has been started before calling waitforCompletion()
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertTrue("find", fTmfComponent.isSignalReceived());
        assertFalse("find", fTmfComponent.isSignalError());
        assertFalse("find", fTmfComponent.isCurrentTimeError());
        assertFalse("find", fTmfComponent.isSourceError());

        assertEquals("find", TC_004_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull(selection);
        assertEquals("find", 1, selection.size());
        assertTrue(selection.get(0) instanceof TmfSyncMessage);
        msg = (TmfSyncMessage) selection.get(0);
        assertEquals("find", TC_004_MESSAGE_NAME, msg.getName());
        assertEquals("find", 0, TC_004_TIME_VALUE.compareTo(msg.getStartTime(), false));
        assertEquals("find", TC_004_START_OCCURRANCE, msg.getStartOccurrence());
        assertEquals("find", TC_004_END_OCCURRANCE, msg.getEndOccurrence());
        assertEquals("find", TC_004_START_LIFELINE, msg.getStartLifeline().getName());
        assertEquals("find", TC_004_END_LIFELINE, msg.getEndLifeline().getName());

        // cancel find and go back to first page
        fFacility.getLoader().cancel();
    }

    /**
     * Verify find of lifeline within page.
     *
     * Verified Methods: loader.find(), loader.moveToPage(), loader.moveToMessage()
     * Expected result: Correct message is selected
     */
    @Test
    public void verifyFind() {
        fFacility.firstPage();

        criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.FIRST_PLAYER_NAME);
        fFacility.getLoader().find(criteria);
        // Wait for the selection to finish - needed due to new platform behavior in Juno
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("find", TC_005_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull(selection);
        assertEquals("find", 1, selection.size());
        assertTrue(selection.get(0) instanceof Lifeline);
        lifeline = (Lifeline) selection.get(0);
        assertEquals("find", TC_005_LIFELINE_NAME, lifeline.getName());

        /**
         * Verify lifeline across page.
         *
         * Verified Methods: loader.find(), loader.moveToPage(), loader.moveToMessage()
         * Expected result: Correct message is selected
         */
        criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.SECOND_PLAYER_NAME);

        fFacility.getLoader().find(criteria);
        fFacility.waitForJobs(); // find across pages uses a job
        // to make sure pageRequest has been started before calling waitforCompletion()
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        fFacility.getLoader().waitForCompletion();
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("find", TC_006_PAGE_VALUE, fFacility.getLoader().currentPage());
        selection = fFacility.getSdView().getSDWidget().getSelection();
        assertNotNull(selection);
        assertEquals("find", 1, selection.size());
        assertTrue(selection.get(0) instanceof Lifeline);
        lifeline = (Lifeline) selection.get(0);
        assertEquals("find", TC_006_LIFELINE_NAME, lifeline.getName());

        // cancel find and go back to first page
        fFacility.getLoader().cancel();
    }

    /**
     * Verify cancel ongoing search job.
     *
     * Verified Methods: loader.find(), loader.find()
     * Expected result: Cancelled find
     */
    @Test
    public void verifyCancelSearch() {

        fFacility.firstPage();

        criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.SECOND_PLAYER_NAME);

        fFacility.getLoader().find(criteria);
        fFacility.delay(200); // to make sure job was started
        fFacility.getLoader().cancel();

        assertEquals("find", 0, fFacility.getLoader().currentPage()); // we are still at the first page

        // cancel find and go back to first page
        fFacility.getLoader().cancel();
        fFacility.firstPage();
    }
}
