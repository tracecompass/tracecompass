/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.request;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;

/**
 * <b><u>ITmfEventRequest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public interface ITmfEventRequest<T extends TmfEvent> extends ITmfDataRequest<T> {

    /**
     * @return the requested time range
     */
    public TmfTimeRange getRange();

}
