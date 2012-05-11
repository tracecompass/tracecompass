/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

/**
 * CtfTmfcontent is a wrapper to allow pretty printing of the fields in a CtfTmfEvent
 */
public class CtfTmfContent extends TmfEventField {

    /**
     * Constructor for CtfTmfContent.
     * @param name String
     * @param fields ITmfEventField[]
     */
    public CtfTmfContent(String name, ITmfEventField[] fields) {
        super(name, fields);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder();
        for( int i=0; i < getFields().length;i++){
            ITmfEventField field = getFields()[i];
            if(i != 0) {
                retVal.append(", ");//$NON-NLS-1$
            }
            retVal.append(field.toString());
        }
        return retVal.toString();
    }
}
