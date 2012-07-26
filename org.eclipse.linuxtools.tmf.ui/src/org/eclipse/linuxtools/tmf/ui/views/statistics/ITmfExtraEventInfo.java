/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial design and implementation
 *   Bernd Hufmann - Changed interface and class name
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

/**
 * This interface provides an extension for updating the data model and to pass
 * along more information beside events.
 *
 * @version 1.0
 * @author Mathieu Denis
 */
public interface ITmfExtraEventInfo {

    /**
     * Returns the trace name.
     *
     * @return the name of the trace.
     */
    public String getTraceName();
}
