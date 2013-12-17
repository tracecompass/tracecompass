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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.Criteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterCriteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader.TmfSyncMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Filter test cases.
 *
 * @author Bernd Hufmann
 */
public class TmfUml2SDSyncLoaderFilterTest {

    private static Uml2SDTestFacility fFacility;
    private static List<FilterCriteria> filterToSave;

    /**
     * Initialization
     */
    @BeforeClass
    public static void setUpClass() {
        fFacility = Uml2SDTestFacility.getInstance();
        fFacility.selectExperiment();

        /* Create Filter Criteria */
        filterToSave = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.FIRST_PLAYER_NAME);
        filterToSave.add(new FilterCriteria(criteria, false, false));

        criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.MASTER_PLAYER_NAME);
        filterToSave.add(new FilterCriteria(criteria, false, false));

        criteria = new Criteria();
        criteria.setSyncMessageSelected(true);
        criteria.setExpression("BALL_.*");
        filterToSave.add(new FilterCriteria(criteria, false, false));
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
     * Test Case set-up code.
     */
    @Before
    public void beforeTest(){
        // Make sure we are at the first page
        fFacility.firstPage();
    }

    /**
     * Test case clean-up code.
     */
    @After
    public void afterTest() {
        filterToSave.get(0).setActive(false);
        filterToSave.get(1).setActive(false);
        filterToSave.get(2).setActive(false);
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }

    /**
     * Verify the filter lifelines (1 out of 2 is hidden)
     *
     * Verified Methods: loader.filter()
     * Expected result: Only one lifeline is visible with no messages
     */
    @Test
    public void verifyFilter1of2() {
        // Initialize the filter
        filterToSave.get(0).setActive(true);
        // Run the filter
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("filter", 1, fFacility.getSdView().getFrame().lifeLinesCount());
        assertEquals("filter", IUml2SDTestConstants.MASTER_PLAYER_NAME, fFacility.getSdView().getFrame().getLifeline(0).getName());
        assertEquals("filter", 0, fFacility.getSdView().getFrame().syncMessageCount());
    }


    /**
     * Verify the filter lifelines (2 out of 2 are hidden)
     *
     * Verified Methods: loader.filter(), loader.fillCurrentPage()
     * Expected result: Neiter liflines nor messages are visible
     */
    @Test
    public void verifyFilter2of2() {
        // Initialize the filter
        filterToSave.get(0).setActive(true);
        filterToSave.get(1).setActive(true);
        // Run the filter
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("filter", 0, fFacility.getSdView().getFrame().lifeLinesCount());
        assertEquals("filter", 0, fFacility.getSdView().getFrame().syncMessageCount());
    }

    /**
     * Verify removal of all filters
     *
     * Verified Methods: loader.filter(), loader.fillCurrentPage()
     * Expected result: Everything is shown
     */
    @Test
    public void verifyRemoval() {
        // First set 2 filter
        filterToSave.get(0).setActive(true);
        filterToSave.get(1).setActive(true);
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        // Remove the filter
        filterToSave.get(0).setActive(false);
        filterToSave.get(1).setActive(false);
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("filter", 2, fFacility.getSdView().getFrame().lifeLinesCount());
        assertEquals("filter", IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE,
                fFacility.getSdView().getFrame().syncMessageCount());
    }

    /**
     * Verify filter of messages
     *
     * Verified Methods: loader.filter(), loader.fillCurrentPage()
     * Expected result: Only particular messages are shown
     */
    @Test
    public void verifyMessageFilter() {
        // Initialize the filter
        filterToSave.get(2).setActive(true);
        // Run the filter
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("filter", 2, fFacility.getSdView().getFrame().lifeLinesCount());
        assertEquals("filter", 6, fFacility.getSdView().getFrame().syncMessageCount());

        String messages[] = { "REGISTER_PLAYER_REQUEST", "REGISTER_PLAYER_REPLY",
                "GAME_REQUEST", "GAME_REPLY", "START_GAME_REQUEST", "START_GAME_REPLY" };

        for (int i = 0; i < messages.length; i++) {
            SyncMessage msg = fFacility.getSdView().getFrame().getSyncMessage(i);
            assertTrue("filter", msg instanceof TmfSyncMessage);
            assertEquals("filter", messages[i], msg.getName());
        }
    }

    /**
     * Verify filter lifeline (1 out of three lifelines). Note that filter was
     * set during change of page.
     *
     * Verified Methods: loader.filter(), loader.fillCurrentPage()
     * Expected result: Only 2 lifelines and their interactions are shown
     */
    @Test
    public void verifyFilter1of3() {
        filterToSave.get(0).setActive(true);
        fFacility.getLoader().filter(filterToSave);
        fFacility.setPage(IUml2SDTestConstants.PAGE_OF_ALL_LIFELINES);

        assertEquals("filter", 2, fFacility.getSdView().getFrame().lifeLinesCount());
        String lifelines[] = { IUml2SDTestConstants.MASTER_PLAYER_NAME, IUml2SDTestConstants.SECOND_PLAYER_NAME };

        for (int i = 0; i < lifelines.length; i++) {
            Lifeline line = fFacility.getSdView().getFrame().getLifeline(i);
            assertEquals("filter", lifelines[i], line.getName());
        }

        assertTrue(fFacility.getSdView().getFrame().syncMessageCount() > 0);
    }
}
