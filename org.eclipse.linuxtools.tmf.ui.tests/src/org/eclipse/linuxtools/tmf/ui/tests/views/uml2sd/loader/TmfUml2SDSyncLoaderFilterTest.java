/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.Criteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterCriteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader.TmfSyncMessage;

/**
 * Filter test cases.
 *
 * @author Bernd Hufmann
 *
 */
@SuppressWarnings("nls")
public class TmfUml2SDSyncLoaderFilterTest extends TestCase {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private Uml2SDTestFacility fFacility;

    // ------------------------------------------------------------------------
    // Static methods
    // ------------------------------------------------------------------------

    /**
     * Returns test setup used when executing test case stand-alone.
     * @return Test setup class
     */
    public static Test suite() {
        return new Uml2SDTestSetup(new TestSuite(TmfUml2SDSyncLoaderFilterTest.class));
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     */
    public TmfUml2SDSyncLoaderFilterTest() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public void setUp() throws Exception {
        super.setUp();
        fFacility = Uml2SDTestFacility.getInstance();
        fFacility.selectExperiment();
    }


    @Override
    public void tearDown() throws Exception {
        fFacility.disposeExperiment();
        fFacility = null;
        super.tearDown();
    }

    /**
     * Main method with test cases.
     */
    public void testFilterHandling() {

        // Create Filter Criteria
        List<FilterCriteria> filterToSave = new ArrayList<FilterCriteria>();
        Criteria criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.FIRST_PLAYER_NAME);
        filterToSave.add(new FilterCriteria(criteria, true, false));

        criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.MASTER_PLAYER_NAME);
        filterToSave.add(new FilterCriteria(criteria, false, false));

        criteria = new Criteria();
        criteria.setSyncMessageSelected(true);
        criteria.setExpression("BALL_.*"); //$NON-NLS-1$
        filterToSave.add(new FilterCriteria(criteria, false, false));

        /*
         * Test Case: 001
         * Description: Verify the filter lifelines (1 out of 2 is hidden)
         * Verified Methods: loader.filter()
         * Expected result: Only one lifeline is visible with no messages
         */
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("filter", 1, fFacility.getSdView().getFrame().lifeLinesCount());
        assertEquals("filter", IUml2SDTestConstants.MASTER_PLAYER_NAME, fFacility.getSdView().getFrame().getLifeline(0).getName());
        assertEquals("filter", 0, fFacility.getSdView().getFrame().syncMessageCount());


        /*
         * Test Case: 002
         * Description: Verify the filter lifelines (2 out of 2 are hidden)
         * Verified Methods: loader.filter(), loader.fillCurrentPage()
         * Expected result: Neiter liflines nor messages are visible
         */
        filterToSave.get(1).setActive(true);
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("filter", 0, fFacility.getSdView().getFrame().lifeLinesCount());
        assertEquals("filter", 0, fFacility.getSdView().getFrame().syncMessageCount());

        /*
         * Test Case: 003
         * Description: Verify removal of all filters
         * Verified Methods: loader.filter(), loader.fillCurrentPage()
         * Expected result: Everything is shown
         */
        filterToSave.get(0).setActive(false);
        filterToSave.get(1).setActive(false);
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("filter", 2, fFacility.getSdView().getFrame().lifeLinesCount());
        assertEquals("filter", IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, fFacility.getSdView().getFrame().syncMessageCount());

        /*
         * Test Case: 004
         * Description: Verify filter of messages
         * Verified Methods: loader.filter(), loader.fillCurrentPage()
         * Expected result: Only particular messages are shown
         */
        filterToSave.get(2).setActive(true);
        fFacility.getLoader().filter(filterToSave);
        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);

        assertEquals("filter", 2, fFacility.getSdView().getFrame().lifeLinesCount());
        assertEquals("filter", 6, fFacility.getSdView().getFrame().syncMessageCount());

        String messages[] = { "REGISTER_PLAYER_REQUEST", "REGISTER_PLAYER_REPLY", "GAME_REQUEST", "GAME_REPLY", "START_GAME_REQUEST", "START_GAME_REPLY" };

        for (int i = 0; i < messages.length; i++) {
            SyncMessage msg = fFacility.getSdView().getFrame().getSyncMessage(i);
            assertTrue("filter", msg instanceof TmfSyncMessage);
            assertEquals("filter", messages[i], msg.getName());
        }

        /*
         * Test Case: 005
         * Description: Verify filter lifeline (1 out of three lifelines).
         *              Note that filter was set during change of page.
         * Verified Methods: loader.filter(), loader.fillCurrentPage()
         * Expected result: Only 2 lifelines and their interactions are shown
         */
        filterToSave.get(0).setActive(true);
        filterToSave.get(2).setActive(false);
        fFacility.setPage(IUml2SDTestConstants.PAGE_OF_ALL_LIFELINES);

        assertEquals("filter", 2, fFacility.getSdView().getFrame().lifeLinesCount());
        String lifelines[] = { IUml2SDTestConstants.MASTER_PLAYER_NAME, IUml2SDTestConstants.SECOND_PLAYER_NAME };

        for (int i = 0; i < lifelines.length; i++) {
            Lifeline line = fFacility.getSdView().getFrame().getLifeline(i);
            assertEquals("filter", lifelines[i], line.getName());
        }

        assertTrue(fFacility.getSdView().getFrame().syncMessageCount() > 0);

        filterToSave.get(2).setActive(false);
    }
}
