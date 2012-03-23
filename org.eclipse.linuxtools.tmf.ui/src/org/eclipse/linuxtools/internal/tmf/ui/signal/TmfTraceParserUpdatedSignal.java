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

package org.eclipse.linuxtools.internal.tmf.ui.signal;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;

/**
 * <b><u>TmfTraceParserUpdatedSignal</u></b>
 */
public class TmfTraceParserUpdatedSignal extends TmfSignal {

    private final IResource fTraceResource;
    
    public TmfTraceParserUpdatedSignal(Object source, IResource traceResource) {
        super(source);
        fTraceResource = traceResource;
    }

    public IResource getTraceResource() {
        return fTraceResource;
    }

    @Override
    public String toString() {
        return "[TmfTraceParserUpdatedSignal (" + fTraceResource.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
