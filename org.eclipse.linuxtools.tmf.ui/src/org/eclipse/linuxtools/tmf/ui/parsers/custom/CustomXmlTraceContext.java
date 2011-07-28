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

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.io.IOException;

import org.eclipse.linuxtools.tmf.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.TmfContext;

public class CustomXmlTraceContext extends TmfContext {
    public BufferedRandomAccessFile raFile;

    public CustomXmlTraceContext(ITmfLocation<?> location, long rank) {
        super(location, rank);
    }

    @Override
    public void dispose() {
        if (raFile != null) {
            try {
                raFile.close();
            } catch (IOException e) {
            }
        }
        super.dispose();
    }

}