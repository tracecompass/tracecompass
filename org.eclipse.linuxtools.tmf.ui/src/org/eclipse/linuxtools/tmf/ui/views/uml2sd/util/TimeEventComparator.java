/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TimeEventComparator.java,v 1.2 2006/09/20 20:56:27 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.util;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SDTimeEvent;

/**
 * Time event comparator
 * 
 * @author sveyrier
 * 
 */
public class TimeEventComparator implements Comparator<SDTimeEvent>, Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5885497718872575669L;

    /**
     * Compares two time events. 
     * 
     * @return 1 if arg0 is greater, 0 if equal, -1 otherwise
     */
    @Override
    public int compare(SDTimeEvent arg0, SDTimeEvent arg1) {
        SDTimeEvent t1 = (SDTimeEvent) arg0;
        SDTimeEvent t2 = (SDTimeEvent) arg1;
        if (t1.getEvent() > t2.getEvent())
            return 1;
        else if (t1.getEvent() == t2.getEvent())
            return 0;
        else
            return -1;
    }

}
