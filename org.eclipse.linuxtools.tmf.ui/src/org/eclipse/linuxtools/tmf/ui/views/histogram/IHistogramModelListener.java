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
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

/**
 * Listener interface for receiving histogram data model notifications.
 * 
 * @version 1.0
 * @author Bernd Hufmann
 */
public interface IHistogramModelListener {
    /**
     * Method to implement to receive notification about model updates. 
     */
    public void modelUpdated();
}
