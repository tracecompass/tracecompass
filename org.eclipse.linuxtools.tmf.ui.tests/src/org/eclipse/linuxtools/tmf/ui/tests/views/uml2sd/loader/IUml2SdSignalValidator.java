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

import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 *  Interface for testing signal handling within TmfUml2SD 
 */
public interface IUml2SdSignalValidator {
    public boolean isSignalReceived();
    public void setSignalReceived(boolean received);
    
    public boolean isSourceError();
    public void setSourceError(boolean fIsSourceError);

    public boolean isCurrentTimeError();
    public void setCurrentTimeError(boolean fIsCurrentTimeError);

    public boolean isRangeError();
    public void setRangeError(boolean fIsRangeError);
    
    public boolean isSignalError();
    public void setSignalError(boolean fIsSignalError);
    
    public Object getSource();
    public void setSource(Object source);
    
    public TmfTimestamp getCurrentTime();
    public void setCurrentTime(TmfTimestamp currentTime);
    
    public TmfTimeRange getCurrentRange();
    public void setCurrentRange(TmfTimeRange currentRange);
    
}
