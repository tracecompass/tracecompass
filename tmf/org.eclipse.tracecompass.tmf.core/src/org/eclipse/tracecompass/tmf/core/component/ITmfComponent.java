/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Add interface for broadcasting signals asynchronously
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.component;

import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;

/**
 * This is the basic interface of all the TMF components.
 * <p>
 * Currently, it only addresses the inter-component signalling.
 *
 * @author Francois Chouinard
 *
 * @see TmfComponent
 */
public interface ITmfComponent {

    /**
     * @return the component ID (display name)
     */
    String getName();

    /**
     * Dispose of the component
     */
    void dispose();

    /**
     * Propagate a signal to all the interested listeners in
     * the same thread of execution.
     *
     * @param signal the signal to broadcast
     */
    void broadcast(TmfSignal signal);

    /**
     * Propagate a signal to all the interested listeners
     * in a separate thread.
     *
     * @param signal the signal to broadcast
     */
    void broadcastAsync(TmfSignal signal);
}
