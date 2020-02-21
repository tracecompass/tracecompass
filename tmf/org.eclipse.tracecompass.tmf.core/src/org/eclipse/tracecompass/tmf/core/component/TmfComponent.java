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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;

/**
 * This is the base class of the TMF components.
 * <p>
 * Currently, it only addresses the inter-component signaling.
 *
 * @author Francois Chouinard
 */
public abstract class TmfComponent implements ITmfComponent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private @NonNull String fName = ""; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor. To be used in conjunction with init()
     */
    public TmfComponent() {
        this(""); //$NON-NLS-1$
    }

    /**
     * Perform component initialization and register it as a signal listener.
     * Need to be called when the default constructor was used.
     *
     * @param name
     *            the component name
     */
    public void init(String name) {
        String cmpName = name;
        if (cmpName == null) {
            cmpName = ""; //$NON-NLS-1$
        }
        TmfCoreTracer.traceComponent(cmpName, "created"); //$NON-NLS-1$
        fName = cmpName;
        TmfSignalManager.register(this);
    }

    /**
     * The standard constructor
     *
     * @param name
     *            the component name
     */
    public TmfComponent(String name) {
        init(name);
    }

    /**
     * The copy constructor
     *
     * @param other
     *            the other component
     */
    public TmfComponent(TmfComponent other) {
        init(other.fName);
    }

    // ------------------------------------------------------------------------
    // Getters/setters
    // ------------------------------------------------------------------------

    @Override
    public @NonNull String getName() {
        return fName;
    }

    /**
     * @param name
     *            the new component name
     */
    protected void setName(@NonNull String name) {
        fName = name;
    }

    // ------------------------------------------------------------------------
    // ITmfComponent
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
        TmfCoreTracer.traceComponent(fName, "disposed"); //$NON-NLS-1$
    }

    @Override
    public void broadcast(TmfSignal signal) {
        TmfSignalManager.dispatchSignal(signal);
    }

    @Override
    public void broadcastAsync(TmfSignal signal) {
        TmfSignalManager.dispatchSignalAsync(signal);
    }
}
