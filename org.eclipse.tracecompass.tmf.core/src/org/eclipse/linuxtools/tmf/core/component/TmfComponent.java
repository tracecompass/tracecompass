/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Add interface for broadcasting signals asynchronously
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.component;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;

/**
 * This is the base class of the TMF components.
 * <p>
 * Currently, it only addresses the inter-component signaling.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfComponent implements ITmfComponent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private String fName;

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
        TmfCoreTracer.traceComponent(this, "created"); //$NON-NLS-1$
        fName = name;
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
    public String getName() {
        return fName;
    }

    /**
     * @param name
     *            the new component name
     */
    protected void setName(String name) {
        fName = name;
    }

    // ------------------------------------------------------------------------
    // ITmfComponent
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
        TmfCoreTracer.traceComponent(this, "disposed"); //$NON-NLS-1$
    }

    @Override
    public void broadcast(TmfSignal signal) {
        TmfSignalManager.dispatchSignal(signal);
    }

    /**
     * @since 3.0
     */
    @Override
    public void broadcastAsync(TmfSignal signal) {
        TmfSignalManager.dispatchSignalAsync(signal);
    }
}
