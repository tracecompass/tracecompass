/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for signal handling.
 *
 * @author Bernd Hufmann
 */
public class TmfUml2SDSyncLoaderSignalTest {

    private static Uml2SDTestFacility fFacility;
    private static Uml2SDSignalValidator fTmfComponent;

    private static TmfTimeRange range;
    private static TmfTimestamp rangeWindow;
    private static TmfTimestamp currentTime;

    /**
     * Initialization
     */
    @BeforeClass
    public static void setUpClass() {
        fFacility = Uml2SDTestFacility.getInstance();
        fFacility.selectExperiment();

        range = new TmfTimeRange(new Uml2SDTestTimestamp(9789689220871L), new Uml2SDTestTimestamp(9789773881426L));
        // Get range window for tests below
        rangeWindow = (TmfTimestamp) range.getEndTime().getDelta(range.getStartTime());
        currentTime = new Uml2SDTestTimestamp(9789773782043L);

        fFacility.getTrace().broadcast(new TmfRangeSynchSignal(fFacility, range));
        fFacility.getTrace().broadcast(new TmfTimeSynchSignal(fFacility, currentTime));
        fFacility.delay(IUml2SDTestConstants.BROADCAST_DELAY);

        fTmfComponent = new Uml2SDSignalValidator();
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
     * Description: Verify that time range signal is send with correct values when going to first page
     * Verified Methods: broadcast()
     * Expected result: Time range sync signal is sent with correct range and current time.
     */
    @Test
    public void verifyFirstPageSignal() {
        currentTime = new Uml2SDTestTimestamp(9788641608418L);
        range = new TmfTimeRange(currentTime, new Uml2SDTestTimestamp(currentTime.getValue() + rangeWindow.getValue()));

        fTmfComponent.setSignalError(false);
        fTmfComponent.setSignalReceived(false);
        fTmfComponent.setCurrentTimeError(false);
        fTmfComponent.setRangeError(false);
        fTmfComponent.setSourceError(false);

        // set expected values
        fTmfComponent.setSource(fFacility.getLoader());
        fTmfComponent.setCurrentTime(currentTime);
        fTmfComponent.setCurrentRange(range);

        fFacility.firstPage();
        assertTrue("TmfRangeSynchSignal",  fTmfComponent.isSignalReceived());
        assertFalse("TmfRangeSynchSignal", fTmfComponent.isSignalError());
        assertFalse("TmfRangeSynchSignal", fTmfComponent.isCurrentTimeError());
        assertFalse("TmfRangeSynchSignal", fTmfComponent.isSourceError());
        assertFalse("TmfRangeSynchSignal", fTmfComponent.isRangeError());
    }

    /**
     * Test Case: 002
     * Description: Verify that time sync signal is sent correctly after selection
     * Verified Methods: loader.broadcast(), testSelectionChanged
     * Expected result: Time sync signal is sent with correct current time.
     */
    @Test
    public void verifySelectionSignal() {
        fTmfComponent.setSignalReceived(false);

        int count = fFacility.getSdView().getFrame().syncMessageCount();
        assertEquals("Test Preparation", IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, count);
        GraphNode node = fFacility.getSdView().getFrame().getSyncMessage(3);

        // set expected values
        fTmfComponent.setSource(fFacility.getLoader());
        fTmfComponent.setCurrentTime(new Uml2SDTestTimestamp(9788642113228L));
        fTmfComponent.setCurrentRange(null); // not used

        fFacility.getSdView().getSDWidget().moveTo(node); // selects the given node
        // Wait for the selection to finish - needed due to new platform behavior in Juno
		fFacility.delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        assertTrue("TmfTimeSynchSignal", fTmfComponent.isSignalReceived());
        assertFalse("TmfTimeSynchSignal", fTmfComponent.isSignalError());
        assertFalse("TmfTimeSynchSignal", fTmfComponent.isCurrentTimeError());
        assertFalse("TmfTimeSynchSignal", fTmfComponent.isSourceError());

        fTmfComponent.setSignalReceived(false);

        fTmfComponent.dispose();
    }
}
