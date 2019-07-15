/*******************************************************************************
 * Copyright (c) 2011, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.loader;

import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
import org.eclipse.tracecompass.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.ui.tests.TmfUITestPlugin;

/**
 *  Class to implement that certain signals are sent as well as are sent with correct content.
 *
 *  @author Bernd Hufmann
 */
public class Uml2SDSignalValidator extends TmfComponent implements IUml2SdSignalValidator {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private int fSignalDepth = 0;
    private boolean fIsSelectionRangeSignalReceived = false;
    private boolean fIsWindowRangeSignalReceived = false;
    private boolean fIsSignalError = false;
    private boolean fIsSourceError = false;
    private boolean fIsCurrentTimeError = false;
    private boolean fIsRangeError = false;

    private Object fSource = null;
    private ITmfTimestamp fCurrentTimestamp = null;
    private TmfTimeRange fCurrentTimeRange = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     */
    public Uml2SDSignalValidator() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Signal handler for time synch signal.
     * @param signal the signal to handle.
     */
    @TmfSignalHandler
    public void synchToTime(TmfSelectionRangeUpdatedSignal signal) {
        // Set results so that it can be validated in the test case
        setSourceError(getSource() != signal.getSource());
        if (isSourceError()) {
            TmfUITestPlugin.getDefault().logError("Source Error: source:" + signal.getSource() + ", expected " + getSource());
        }
        setCurrentTimeError(!getCurrentTime().equals(signal.getBeginTime()));
        if (isCurrentTimeError()) {
            TmfUITestPlugin.getDefault().logError("Current Time Error: begin time:" + signal.getBeginTime() + ", expected " + getCurrentTime());
        }
        setSelectionRangeSignalReceived(true);
    }

    /**
     * Signal handler for window range signal.
     * @param signal the signal to handle.
     */
    @TmfSignalHandler
    public void synchToTimeRange(TmfWindowRangeUpdatedSignal signal) {
        // Set results so that it can be validated in the test case
        if (getSource() != null) {
            setSourceError(getSource() != signal.getSource());
            if (isSourceError()) {
                TmfUITestPlugin.getDefault().logError("Source Error: source:" + signal.getSource() + ", expected " + getSource());
            }
        }
        if (getCurrentRange() != null) {
            setRangeError(!getCurrentRange().equals(signal.getCurrentRange()));
            if (isRangeError()) {
                TmfUITestPlugin.getDefault().logError("Range Error: current range:" + signal.getCurrentRange() + ", expected " + getCurrentRange());
            }
        }
        setWindowRangeSignalReceived(true);
    }

    /**
     * Signal handler for handling start synch signal.
     * @param signal the signal to handle.
     */
    @TmfSignalHandler
    public void startSynch(TmfStartSynchSignal signal) {
        fSignalDepth++;
        // make sure that the signal which is send by the loader class is not handled by the loader class
        // after receiving it. i.e. it must not trigger a another signal

        // Set results so that it can be validated in the test case
        setSignalError(fSignalDepth > 1);
        if (isSignalError()) {
            TmfUITestPlugin.getDefault().logError("Signal Error: signal depth:" + fSignalDepth);
        }
    }

    /**
     * Signal handler for handling end synch signal.
     * @param signal the signal to handle.
     */
    @TmfSignalHandler
    public void endSynch(TmfEndSynchSignal signal) {
        fSignalDepth = fSignalDepth > 0 ? fSignalDepth - 1 : 0;
    }

    @Override
    public boolean isSelectionRangeSignalReceived() {
        return fIsSelectionRangeSignalReceived;
    }

    @Override
    public void setSelectionRangeSignalReceived(boolean received) {
        fIsSelectionRangeSignalReceived = received;
    }

    @Override
    public boolean isWindowRangeSignalReceived() {
        return fIsWindowRangeSignalReceived;
    }

    @Override
    public void setWindowRangeSignalReceived(boolean received) {
        fIsWindowRangeSignalReceived = received;
    }

    @Override
    public boolean isSourceError() {
        return fIsSourceError;
    }

    @Override
    public void setSourceError(boolean fIsSourceError) {
        this.fIsSourceError = fIsSourceError;
    }

    @Override
    public boolean isCurrentTimeError() {
        return fIsCurrentTimeError;
    }

    @Override
    public void setCurrentTimeError(boolean fIsCurrentTimeError) {
        this.fIsCurrentTimeError = fIsCurrentTimeError;
    }

    @Override
    public boolean isRangeError() {
        return fIsRangeError;
    }

    @Override
    public void setRangeError(boolean fIsRangeError) {
        this.fIsRangeError = fIsRangeError;
    }

    @Override
    public boolean isSignalError() {
        return fIsSignalError;
    }

    @Override
    public void setSignalError(boolean fIsSignalError) {
        this.fIsSignalError = fIsSignalError;
    }

    @Override
    public Object getSource() {
        return fSource;
    }

    @Override
    public void setSource(Object source) {
        fSource = source;
    }

    @Override
    public ITmfTimestamp getCurrentTime() {
        return fCurrentTimestamp;
    }

    @Override
    public void setCurrentTime(ITmfTimestamp currentTime) {
        fCurrentTimestamp = currentTime;
    }

    @Override
    public TmfTimeRange getCurrentRange() {
        return fCurrentTimeRange;
    }

    @Override
    public void setCurrentRange(TmfTimeRange currentRange) {
        fCurrentTimeRange = currentRange == null ? null : currentRange;
    }
}

