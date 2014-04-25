/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Add interface for broadcasting signals asynchronously
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.editors;

import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.ui.part.EditorPart;

/**
 * The main editor abstract class for use in TMF.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public abstract class TmfEditor extends EditorPart implements ITmfComponent {

    private final String fName;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEditor() {
        super();
        fName = "TmfEditor"; //$NON-NLS-1$
        TmfSignalManager.register(this);
    }

    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfComponent
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
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
