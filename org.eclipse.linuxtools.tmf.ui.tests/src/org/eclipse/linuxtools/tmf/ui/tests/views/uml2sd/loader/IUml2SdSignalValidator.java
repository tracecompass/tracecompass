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
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 *  Interface for testing signal handling within TmfUml2SD
 *
 *  @author Bernd Hufmann
 */
public interface IUml2SdSignalValidator {
    /**
     * @return if signal is received or not
     */
    public boolean isSignalReceived();
    /**
     * Sets signal received value
     * @param received boolean value to set
     */
    public void setSignalReceived(boolean received);

    /**
     * @return whether source of signal is correct or not
     */
    public boolean isSourceError();
    /**
     * Sets the source error flag.
     * @param fIsSourceError boolean value to set
     */
    public void setSourceError(boolean fIsSourceError);

    /**
     * @return whether received current time is correct or not
     */
    public boolean isCurrentTimeError();
    /**
     * Sets the current time error flag.
     * @param fIsCurrentTimeError boolean value to set
     */
    public void setCurrentTimeError(boolean fIsCurrentTimeError);

    /**
     * @return whether received range is correct or not
     */
    public boolean isRangeError();
    /**
     * Sets the range error flag.
     * @param fIsRangeError boolean value to set
     */
    public void setRangeError(boolean fIsRangeError);

    /**
     * @return whether signal was received or not
     */
    public boolean isSignalError();
    /**
     * Sets signal error flag.
     * @param fIsSignalError boolean value to set
     */
    public void setSignalError(boolean fIsSignalError);

    /**
     * @return source of expected signal.
     */
    public Object getSource();
    /**
     * Sets source of expected signal
     * @param source expected source component
     */
    public void setSource(Object source);

    /**
     * @return the expected current time.
     */
    public TmfTimestamp getCurrentTime();
    /**
     * Sets the expected current time
     * @param currentTime Time to set
     */
    public void setCurrentTime(TmfTimestamp currentTime);

    /**
     * @return the expected current time range.
     */
    public TmfTimeRange getCurrentRange();
    /**
     * Sets the expected current time range.
     * @param currentRange the expected current time range to set
     */
    public void setCurrentRange(TmfTimeRange currentRange);

}
