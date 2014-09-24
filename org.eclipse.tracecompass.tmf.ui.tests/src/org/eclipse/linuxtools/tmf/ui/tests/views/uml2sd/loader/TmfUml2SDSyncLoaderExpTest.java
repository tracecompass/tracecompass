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
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterCriteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterListDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader.TmfUml2SDSyncLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for Experiment handling.
 *
 * @author Bernd Hufmann
 */
public class TmfUml2SDSyncLoaderExpTest {

    private static Uml2SDTestFacility fFacility;

    /**
     * Initialization
     */
    @BeforeClass
    public static void setUpClass() {
        fFacility = Uml2SDTestFacility.getInstance();
        // create filter criteria (incl. save)
        fFacility.createFilterCriteria();
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
     * Verify setup
     *
     * Verified Methods: loader.getTitleString()
     *                   view.getPartName()
     *                   view.getFrame()
     * Expected result: Title, view name are set properly.
     */
    @Test
    public void verifySetup() {
        assertEquals("getTitleString", "Component Interactions", fFacility.getLoader().getTitleString());
        assertEquals("getPartName", "Sequence Diagram", fFacility.getSdView().getPartName());
        assertNotNull("getFrame", fFacility.getSdView().getFrame());

        fFacility.disposeExperiment();

        fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        verifyPage(0, 0, false, false, 0);
    }

    /**
     * Verify cancellation of ongoing indexing.
     *
     * Verified Methods: loader.experimentSelected(), loader.experimentDisposed(), loader.nextPage()
     * Expected result: No exceptions during cancellation and nextPage() operation.
     *
     * Note this test is not sufficient to verify the concurrent access of the loader attributes
     * by multiple threads. Contention might happen but it's not guaranteed.
     */
    @Test
    public void verifyCancel() {
        for(int i = 0; i < 5; i++) {
            fFacility.selectExperiment(false);
            fFacility.delay(IUml2SDTestConstants.INITIAL_INDEX_DELAY);

            try {
                fFacility.disposeExperiment();
                fFacility.getLoader().nextPage(); // to test out of bounce
                // Note: To actually create an out of bound exception remove
                // safety-checks in nextPage/moveToPage of loader class
            } catch (Exception e){
                // No Exception expected
                fail("exp.select/exp.dispose");
            }
        }
    }

    /**
     * Verify disposed experiment.
     *
     * Verified Methods: loader.nextPage(),
     *                   loader.pagesCount(), loader.hasNextPage(), loader.hasPrevPage(),
     *                   frame.syncMessagesCount, frame.lifeLinesCount
     * Expected result: Page values and filter are reset.
     *
     * Note this test is not sufficient to verify the concurrent access of the loader attributes
     * by multiple threads. Contention might happen but it's not guaranteed.
     */
    @Test
    public void verifyDispose() {
        verifyPage(0, 0, false, false, 0);

        // verify that all enable filters are disabled after disposal
        List<FilterCriteria> filter = FilterListDialog.getGlobalFilters();

        for (FilterCriteria filterCriteria : filter) {
            assertFalse("exp.dispose", filterCriteria.isActive());
        }
    }

    /**
     * Verify the disposal of the loader.
     *
     * Verified Methods: loader.dispose()
     * Expected result: All providers are removed from SDView.
     */
    @Test
    public void verifyLoaderDispose() {
        fFacility.getLoader().dispose();
        assertTrue("loader.dispose", fFacility.getSdView().getSDPagingProvider() == null);
        assertTrue("loader.dispose", fFacility.getSdView().getSDFindProvider() == null);
        assertTrue("loader.dispose", fFacility.getSdView().getSDFilterProvider() == null);
        assertTrue("loader.dispose", fFacility.getSdView().getExtendedFindProvider() == null);
        assertTrue("loader.dispose", fFacility.getSdView().getExtendedFilterProvider() == null);

        // Set again loader as signal handler, which was removed by the the dispose above
        TmfSignalManager.register(fFacility.getLoader());
    }

    /**
     * Verify setViewer.
     *
     * Verified Methods: loader.setViewer
     * Expected result: Paging, find and filter provider are set
     */
    @Test
    public void testSetViewer() {
        fFacility.getLoader().setViewer(fFacility.getSdView());
        ISDPagingProvider pagingProvider = fFacility.getSdView().getSDPagingProvider();
        assertTrue("loader.setViewer", pagingProvider != null);
        assertTrue("loader.setViewer", pagingProvider instanceof ISDAdvancedPagingProvider);
        assertTrue("loader.setViewer", pagingProvider instanceof TmfUml2SDSyncLoader);

        assertTrue("loader.setViewer", fFacility.getSdView().getSDFindProvider() != null);

        assertTrue("loader.setViewer", fFacility.getSdView().getSDFilterProvider() != null);

        // All other providers are not used.
        assertTrue("loader.setViewer", fFacility.getSdView().getExtendedFindProvider() == null);
        assertTrue("loader.setViewer", fFacility.getSdView().getExtendedFilterProvider() == null);
    }

    private static void verifyPage(int currentPage, int numMsg, boolean hasNext,
            boolean hasPrev, int lifelineCount) {
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
