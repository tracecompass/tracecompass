/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.editors;

import org.eclipse.linuxtools.tmf.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.ui.part.MultiPageEditorPart;

public abstract class TmfMultiPageEditorPart extends MultiPageEditorPart implements ITmfComponent {

    private final String fName;
    
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public TmfMultiPageEditorPart() {
        super();
        fName = "TmfMultiPageEditorPart"; //$NON-NLS-1$
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
}
