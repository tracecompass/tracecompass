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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test cases for pages handling.
 *
 * @author Bernd Hufmann
 *
 */
public class TmfUml2SDSyncLoaderPagesTest extends TestCase {

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
        return new Uml2SDTestSetup(new TestSuite(TmfUml2SDSyncLoaderPagesTest.class));
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     */
    public TmfUml2SDSyncLoaderPagesTest() {
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
    public void testPageHandling() {

        verifyPagesCount();
        verifyNextPage();
        verifyPrevPage();
        verifyFirstPage();
        verifyLastPage();
        verifyPageNumberChanged();

    }

     private void verifyPagesCount() {

        /*
         * Test Case: 001
         * Description: Test number of pages.
         * Verified Methods: loader.pagesCount().
         * Expected result: ITestConstants.TOTAL_NUMBER_OF_PAGES of pages
         */
        assertEquals(IUml2SDTestConstants.TOTAL_NUMBER_OF_PAGES, fFacility.getLoader().pagesCount());
    }

    private void verifyNextPage() {
        // assuming we are at the first page

        /*
         * Test Case: 002
         * Description: Tests next page feature.
         * Verified Methods: loader.nextPage(), loader.fillCurrentPage(), loader.pagesCount(),
         *                   loader.hasNextPage(), loader.hasPrevPage(),
         *                   frame.syncMessagesCount, frame.lifeLinesCount
         * Expected result: Expected values are return.
         */

        for(int i = 0; i < IUml2SDTestConstants.TOTAL_NUMBER_OF_PAGES-2; i++) {
            fFacility.nextPage();

            if (i+1 == IUml2SDTestConstants.PAGE_OF_ALL_LIFELINES) {
                verifyPage(i+1, IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, true, true, IUml2SDTestConstants.NUM_OF_ALL_LIFELINES);
            }
            else {
                verifyPage(i+1, IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, true, true);
            }
        }

        // Last Page
        fFacility.nextPage();
        verifyPage(IUml2SDTestConstants.TOTAL_NUMBER_OF_PAGES - 1, IUml2SDTestConstants.NUM_MESSAGES_OF_LAST_PAGE, false, true);

        // Check for index out of bounce
        try {
            fFacility.getLoader().nextPage();
        } catch (Exception e) {
            fail();
        }

        fFacility.firstPage();
    }


    private void verifyPrevPage() {
        // assuming we are at the first page

        /*
         * Test Case: 003
         * Description: Test previous page feature.
         * Verified Methods: loader.prevPage(), loader.fillCurrentPage(), loader.pagesCount(),
         *                   loader.hasNextPage(), loader.hasPrevPage(),
         *                   frame.syncMessagesCount, frame.lifeLinesCount
         * Expected result: Expected values are return.
         */

        // Last Page
        fFacility.lastPage();
        assertEquals(IUml2SDTestConstants.TOTAL_NUMBER_OF_PAGES - 1, fFacility.getLoader().currentPage());
        assertEquals(IUml2SDTestConstants.NUM_MESSAGES_OF_LAST_PAGE, fFacility.getSdView().getFrame().syncMessageCount());
        assertFalse(fFacility.getLoader().hasNextPage());
        assertTrue(fFacility.getLoader().hasPrevPage());
        assertEquals(2, fFacility.getSdView().getFrame().lifeLinesCount());

        for(int i = IUml2SDTestConstants.TOTAL_NUMBER_OF_PAGES-2; i > 0; i--) {
            fFacility.prevPage();
            if (i == IUml2SDTestConstants.PAGE_OF_ALL_LIFELINES) {
                verifyPage(i, IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, true, true, IUml2SDTestConstants.NUM_OF_ALL_LIFELINES);
            } else {
                verifyPage(i, IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, true, true);
            }
        }

        fFacility.prevPage();
        verifyPage(0, IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, true, false);

        // Check for index out of bounce
        try {
            fFacility.getLoader().prevPage();
        } catch (Exception e) {
            fail();
        }
    }

    private void verifyFirstPage() {
        fFacility.lastPage();
        /*
         * Test Case: 004
         * Description: Test first page feature.
         * Verified Methods: loader.firstPage(), loader.fillCurrentPage(), loader.pagesCount(),
         *                   loader.hasNextPage(), loader.hasPrevPage(),
         *                   frame.syncMessagesCount, frame.lifeLinesCount
         * Expected result: Expected values are return.
         */

        // First Page
        fFacility.firstPage();
        verifyPage(0, IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, true, false);
    }


    private void verifyLastPage() {
        /*
         * Test Case: 005
         * Description: Test last page feature.
         * Verified Methods: loader.lastPage(), loader.pagesCount(), loader.hasNextPage(), loader.hasPrevPage()
         *                   frame.syncMessagesCount, frame.lifeLinesCount
         * Expected result: Expected values are return.
         */
        fFacility.lastPage();
        verifyPage(IUml2SDTestConstants.TOTAL_NUMBER_OF_PAGES - 1, IUml2SDTestConstants.NUM_MESSAGES_OF_LAST_PAGE, false, true);
        fFacility.firstPage();
    }

    private void verifyPageNumberChanged() {

        /*
         * Test Case: 006
         * Description: Test move to any page feature.
         * Verified Methods: loader.pageNumberChanged(), loader.fillCurrentPage(), loader.pagesCount(),
         *                   loader.hasNextPage(), loader.hasPrevPage(),
         *                   frame.syncMessagesCount, frame.lifeLinesCount
         * Expected result: Expected values are return.
         */

        // any page
        fFacility.setPage(IUml2SDTestConstants.PAGE_OF_ALL_LIFELINES);
        verifyPage(IUml2SDTestConstants.PAGE_OF_ALL_LIFELINES, IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, true, true, IUml2SDTestConstants.NUM_OF_ALL_LIFELINES);
        fFacility.firstPage();
    }

    private void verifyPage(int currentPage, int numMsg, boolean hasNext, boolean hasPrev) {
        verifyPage(currentPage, numMsg, hasNext, hasPrev, IUml2SDTestConstants.DEFAULT_NUM_LIFELINES);
    }

    @SuppressWarnings("nls")
    private void verifyPage(int currentPage, int numMsg, boolean hasNext, boolean hasPrev, int lifelineCount) {
        assertEquals("currentPage", currentPage, fFacility.getLoader().currentPage());
        assertEquals("syncMessageCount, ", numMsg, fFacility.getSdView().getFrame().syncMessageCount());
        if (hasNext) {
            assertTrue("hasNextpage", fFacility.getLoader().hasNextPage());
        } else {
            assertFalse("hasNextPage", fFacility.getLoader().hasNextPage());
        }
        if (hasPrev) {
            assertTrue("hasPrevPage", fFacility.getLoader().hasPrevPage());
        } else {
            assertFalse("hasPrevPage", fFacility.getLoader().hasPrevPage());
        }
        assertEquals("lifeLinesCount", lifelineCount, fFacility.getSdView().getFrame().lifeLinesCount());
    }
}
