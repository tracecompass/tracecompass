/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TimeEvent.java,v 1.2 2006/09/20 20:56:25 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * @author sveyrier
 * 
 */
public class SDTimeEvent {

    protected ITmfTimestamp time;
    protected int event;
    protected ITimeRange node;

    public SDTimeEvent(ITmfTimestamp _time, int _event, ITimeRange _node) {
        time = _time;
        event = _event;
        node = _node;
    }

    public ITmfTimestamp getTime() {
        return time;
    }

    public int getEvent() {
        return event;
    }

    public ITimeRange getGraphNode() {
        return node;
    }

}
