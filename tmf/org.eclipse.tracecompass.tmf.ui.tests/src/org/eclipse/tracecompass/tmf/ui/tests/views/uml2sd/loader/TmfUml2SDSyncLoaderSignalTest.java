/*******************************************************************************
 * Copyright (c) 2011, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.GraphNode;
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

    /**
     * Initialization
     */
    @BeforeClass
    public static void setUpClass() {
        fFacility = Uml2SDTestFacility.getInstance();
        fFacility.init();
        fFacility.selectExperiment();

        range = new TmfTimeRange(new Uml2SDTestTimestamp(9789689220871L), new Uml2SDTestTimestamp(9789773881426L));
        // Get range window for tests below
        rangeWindow = (TmfTimestamp) range.getEndTime().getDelta(range.getStartTime());
        TmfTimestamp currentTime = new Uml2SDTestTimestamp(9789773782043L);

        fFacility.getTrace().broadcast(new TmfWindowRangeUpdatedSignal(fFacility, range));
        fFacility.getTrace().broadcast(new TmfSelectionRangeUpdatedSignal(fFacility, currentTime));
        fFacility.delay(IUml2SDTestConstants.BROADCAST_DELAY);

        fTmfComponent = new Uml2SDSignalValidator();
    }

    /**
     * Cleanup
     */
    @AfterClass
    public static void tearDownClass() {
        fFacility.disposeExperiment();
        fFacility.dispose();
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
        TmfTimestamp currentTime = new Uml2SDTestTimestamp(9788641608418L);
        range = new TmfTimeRange(currentTime, new Uml2SDTestTimestamp(currentTime.getValue() + rangeWindow.getValue()));

        fTmfComponent.setSignalError(false);
        fTmfComponent.setSourceError(false);
        fTmfComponent.setRangeError(false);
        fTmfComponent.setWindowRangeSignalReceived(false);

        // set expected values
        fTmfComponent.setSource(fFacility.getLoader());
        fTmfComponent.setCurrentRange(range);

        fFacility.firstPage();

        WaitUtils.waitUntil(validator -> validator.isWindowRangeSignalReceived(), fTmfComponent, "Window range signal not received");
        assertFalse("TmfRangeSynchSignal", fTmfComponent.isSignalError());
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

        int count = fFacility.getSdView().getFrame().syncMessageCount();
        assertEquals("Test Preparation", IUml2SDTestConstants.MAX_MESSEAGES_PER_PAGE, count);
        GraphNode node = fFacility.getSdView().getFrame().getSyncMessage(3);

        fTmfComponent.setSignalError(false);
        fTmfComponent.setSourceError(false);
        fTmfComponent.setCurrentTimeError(false);
        fTmfComponent.setSelectionRangeSignalReceived(false);

        // set expected values
        fTmfComponent.setSource(fFacility.getLoader());
        fTmfComponent.setCurrentTime(new Uml2SDTestTimestamp(9788642113228L));

        fFacility.getSdView().getSDWidget().moveTo(node); // selects the given node

        WaitUtils.waitUntil(validator -> validator.isSelectionRangeSignalReceived(), fTmfComponent, "Selection range signal not received");
        assertFalse("TmfTimeSynchSignal", fTmfComponent.isSignalError());
        assertFalse("TmfTimeSynchSignal", fTmfComponent.isSourceError());
        assertFalse("TmfTimeSynchSignal", fTmfComponent.isCurrentTimeError());

        fTmfComponent.dispose();
    }
}
