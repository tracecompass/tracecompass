/**********************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.core.uml2sd;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

public interface ITmfSyncSequenceDiagramEvent {
    
    /**
     * @return Name of message
     */
    public String getName();
    
    /**
     * @return name of sender of message
     */
    public String getSender();
    
    /**
     * @return Name of receiver of message
     */
    public String getReceiver();

    /**
     * @return Start timestamp of message (i.e. send time)
     */
    public ITmfTimestamp getStartTime();
}
