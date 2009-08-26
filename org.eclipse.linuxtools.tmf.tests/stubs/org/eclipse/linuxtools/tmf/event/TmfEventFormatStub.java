/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

/**
 * <b><u>TmfEventFormatStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventFormatStub extends TmfEventFormat {

    // ========================================================================
    // Attributes
    // ========================================================================

    // ========================================================================
    // Constructors
    // ========================================================================

   public TmfEventFormatStub() {
        super(new String[] { "Field1", "Field2", "Field3", "Field4", "Field5" });
    }

   // ========================================================================
   // Accessors
   // ========================================================================

   // ========================================================================
   // Operators
   // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.event.TmfEventFormat#parse(java.lang.Object)
     */
    @Override
    public TmfEventField[] parse(Object content) {
        TmfEventField field1 = new TmfEventField(1);
        TmfEventField field2 = new TmfEventField(-10);
        TmfEventField field3 = new TmfEventField(true);
        TmfEventField field4 = new TmfEventField("some string");
        TmfEventField field5 = new TmfEventField(new TmfTimestamp(1, (byte) 2, 3));
        return new TmfEventField[] { field1, field2, field3, field4, field5 };
    }

    // ========================================================================
    // Helper functions
    // ========================================================================

}
