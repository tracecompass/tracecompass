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
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.loader;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 *  Interface for testing signal handling within TmfUml2SD
 *
 *  @author Bernd Hufmann
 */
public interface IUml2SdSignalValidator {

    /**
     * @return if selection range signal is received or not
     */
    boolean isSelectionRangeSignalReceived();

    /**
     * Sets selection range signal received value
     * @param received boolean value to set
     */
    void setSelectionRangeSignalReceived(boolean received);

    /**
     * @return if window range signal is received or not
     */
    boolean isWindowRangeSignalReceived();

    /**
     * Sets window range signal received value
     * @param received boolean value to set
     */
    void setWindowRangeSignalReceived(boolean received);

    /**
     * @return whether source of signal is correct or not
     */
    boolean isSourceError();
    /**
     * Sets the source error flag.
     * @param fIsSourceError boolean value to set
     */
    void setSourceError(boolean fIsSourceError);

    /**
     * @return whether received current time is correct or not
     */
    boolean isCurrentTimeError();
    /**
     * Sets the current time error flag.
     * @param fIsCurrentTimeError boolean value to set
     */
    void setCurrentTimeError(boolean fIsCurrentTimeError);

    /**
     * @return whether received range is correct or not
     */
    boolean isRangeError();
    /**
     * Sets the range error flag.
     * @param fIsRangeError boolean value to set
     */
    void setRangeError(boolean fIsRangeError);

    /**
     * @return whether signal was received or not
     */
    boolean isSignalError();
    /**
     * Sets signal error flag.
     * @param fIsSignalError boolean value to set
     */
    void setSignalError(boolean fIsSignalError);

    /**
     * @return source of expected signal.
     */
    Object getSource();
    /**
     * Sets source of expected signal
     * @param source expected source component
     */
    void setSource(Object source);

    /**
     * @return the expected current time.
     */
    ITmfTimestamp getCurrentTime();
    /**
     * Sets the expected current time
     * @param currentTime Time to set
     */
    void setCurrentTime(ITmfTimestamp currentTime);

    /**
     * @return the expected current time range.
     */
    TmfTimeRange getCurrentRange();
    /**
     * Sets the expected current time range.
     * @param currentRange the expected current time range to set
     */
    void setCurrentRange(TmfTimeRange currentRange);

}
