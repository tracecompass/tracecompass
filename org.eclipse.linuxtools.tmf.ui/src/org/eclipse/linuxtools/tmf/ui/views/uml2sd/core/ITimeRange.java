/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * A interface for handling time ranges.
 * 
 * @version 1.0
 * @author sveyrier
 * 
 */
public interface ITimeRange {

    /**
     * Returns the time when the message began.
     * @return the time when the message began
     */
    public ITmfTimestamp getStartTime();

    /**
     * Returns the time when the message ended.
     * 
     * @return the time when the message ended
     */
    public ITmfTimestamp getEndTime();

    /**
     * Returns flag to indicate whether time information is available or not.
     * 
     * @return flag to indicate whether time information is available or not  
     */
    public boolean hasTimeInfo();
}
