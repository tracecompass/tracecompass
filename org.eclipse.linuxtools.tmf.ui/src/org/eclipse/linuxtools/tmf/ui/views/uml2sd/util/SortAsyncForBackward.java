/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SortAsyncForBackward.java,v 1.2 2006/09/20 20:56:27 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.util;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.AsyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;

/**
 * Asynchronous message comparator 
 * 
 * Compares two asyncMessages only taking into account the event occurrence when their
 * appear.<br>
 * 
 * Used to order the AsyncMessage list insuring that the previous node has both of his ends smaller than the current node
 * 
 * @author sveyrier
 * 
 */
public class SortAsyncForBackward implements Comparator<GraphNode>, Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 603959931263853359L;

    /**
     * Compares two asynchronous messages Returns 0 (equal) if one of the message is not asynchronous
     * 
     * @return 1 if arg0 is greater, 0 if equal, -1 otherwise
     */
    @Override
    public int compare(GraphNode arg0, GraphNode arg1) {
        if (arg0 instanceof AsyncMessage && arg1 instanceof AsyncMessage) {
            AsyncMessage m1 = (AsyncMessage) arg0;
            AsyncMessage m2 = (AsyncMessage) arg1;
            int m1Max, m2Max;
            // AsyncMessage has two ends which may have different event occurrences
            // Search for the greater event occurrence for each messages
            if (m1.getStartOccurrence() > m1.getEndOccurrence())
                m1Max = m1.getStartOccurrence();
            else
                m1Max = m1.getEndOccurrence();
            if (m2.getStartOccurrence() > m2.getEndOccurrence())
                m2Max = m2.getStartOccurrence();
            else
                m2Max = m2.getEndOccurrence();

            int m1Min, m2Min;
            // Search for the smaller event occurrence for each messages
            if (m1.getStartOccurrence() > m1.getEndOccurrence())
                m1Min = m1.getEndOccurrence();
            else
                m1Min = m1.getStartOccurrence();
            if (m2.getStartOccurrence() > m2.getEndOccurrence())
                m2Min = m2.getEndOccurrence();
            else
                m2Min = m2.getStartOccurrence();

            if (m1Max > m2Max)
                return 1;
            else if (m1Max == m2Max)
                if (m1Min == m2Min)
                    return 0;
                else if (m1Min > m2Min)
                    return -1;
                else
                    return 1;
            else
                return -1;
        } else
            return 0;
    }

}
