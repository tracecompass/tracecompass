/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
