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

import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;

/**
 *  Class to implement that certain signals are sent as well as are sent with correct content.
 */
public class Uml2SDSignalValidator extends TmfComponent implements IUml2SdSignalValidator {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private int fSignalDepth = 0;
    private boolean fIsSignalReceived = false;
    private boolean fIsSignalError = false;
    private boolean fIsSourceError = false;
    private boolean fIsCurrentTimeError = false;
    private boolean fIsRangeError = false;

    private Object fSource = null;
    private TmfTimestamp fCurrentTimestamp = null;
    private TmfTimeRange fCurrentTimeRange = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public Uml2SDSignalValidator() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @TmfSignalHandler
    public void synchToTime(TmfTimeSynchSignal signal) {
        // Set results so that it can be validated in the test case
        setSignalReceived(true);
        setSourceError(getSource() != signal.getSource());
        setCurrentTimeError(!getCurrentTime().equals(signal.getCurrentTime()));
    }

    @TmfSignalHandler
    public void synchToTimeRange(TmfRangeSynchSignal signal) {
        // Set results so that it can be validated in the test case
        setSignalReceived(true);
        setSourceError(getSource() != signal.getSource());
        setCurrentTimeError(!getCurrentTime().equals(signal.getCurrentTime()));
        setRangeError(!getCurrentRange().equals(signal.getCurrentRange()));
    }

    @TmfSignalHandler
    public void startSynch(TmfStartSynchSignal signal) {
        fSignalDepth++;
        // make sure that the signal which is send by the loader class is not handled by the loader class
        // after receiving it. i.e. it must not trigger a another signal

        // Set results so that it can be validated in the test case
        setSignalError(fSignalDepth > 1);
    }

    @TmfSignalHandler
    public void endSynch(TmfEndSynchSignal signal) {
        fSignalDepth = fSignalDepth > 0 ? fSignalDepth - 1 : 0;
    }

    @Override
    public boolean isSignalReceived() {
        return fIsSignalReceived;
    }

    @Override
    public void setSignalReceived(boolean received) {
        fIsSignalReceived = received;
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
    public TmfTimestamp getCurrentTime() {
        return fCurrentTimestamp;
    }

    @Override
    public void setCurrentTime(TmfTimestamp currentTime) {
        fCurrentTimestamp = currentTime == null ? null : new TmfTimestamp(currentTime);
    }

    @Override
    public TmfTimeRange getCurrentRange() {
        return fCurrentTimeRange;
    }

    @Override
    public void setCurrentRange(TmfTimeRange currentRange) {
        fCurrentTimeRange = currentRange == null ? null : new TmfTimeRange(currentRange);
    }
}

