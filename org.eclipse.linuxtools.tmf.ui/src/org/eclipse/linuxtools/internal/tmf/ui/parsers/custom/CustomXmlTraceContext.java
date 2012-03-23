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

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.io.IOException;

import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((raFile == null) ? 0 : raFile.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CustomXmlTraceContext)) {
            return false;
        }
        CustomXmlTraceContext other = (CustomXmlTraceContext) obj;
        if (raFile == null) {
            if (other.raFile != null) {
                return false;
            }
        } else if (!raFile.equals(other.raFile)) {
            return false;
        }
        return true;
    }

}