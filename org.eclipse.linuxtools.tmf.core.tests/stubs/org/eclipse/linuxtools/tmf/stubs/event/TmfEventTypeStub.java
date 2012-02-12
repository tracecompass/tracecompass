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

package org.eclipse.linuxtools.tmf.stubs.event;

import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

/**
 * <b><u>TmfEventTypeStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("nls")
public class TmfEventTypeStub extends TmfEventType {

    private static final String FIELD_1 = "Field1"; 
    private static final String FIELD_2 = "Field2"; 
    private static final String FIELD_3 = "Field3"; 
    private static final String FIELD_4 = "Field4"; 
    private static final String FIELD_5 = "Field5"; 
    private static final String[] FIELDS = new String[] { FIELD_1, FIELD_2, FIELD_3, FIELD_4, FIELD_5 };

   public TmfEventTypeStub() {
       super("UnitTest", "TmfEventTypeStub", FIELDS);
    }
}
