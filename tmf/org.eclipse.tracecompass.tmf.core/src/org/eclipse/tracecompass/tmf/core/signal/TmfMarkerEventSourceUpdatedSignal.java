/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

/**
 * A marker event source has been updated
 *
 * @since 2.1
 */
public class TmfMarkerEventSourceUpdatedSignal extends TmfSignal {

    /**
     * @param source the signal source
     */
    public TmfMarkerEventSourceUpdatedSignal(Object source) {
        super(source);
    }

}
