/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.event;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

/**
 * <b><u>TmfEventTypeStub</u></b>
 */
@SuppressWarnings("javadoc")
public class TmfEventTypeStub extends TmfEventType {

    private static final ITmfEventField FIELD_1 = new TmfEventField("Field1", null, null);
    private static final ITmfEventField FIELD_2 = new TmfEventField("Field2", null, null);
    private static final ITmfEventField FIELD_3 = new TmfEventField("Field3", null, null);
    private static final ITmfEventField FIELD_4 = new TmfEventField("Field4", null, null);
    private static final ITmfEventField FIELD_5 = new TmfEventField("Field5", null, null);

    private static final ITmfEventField[] FIELDS = new ITmfEventField[] {
        FIELD_1, FIELD_2, FIELD_3, FIELD_4, FIELD_5 };

    private static ITmfEventField ROOT = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null,  FIELDS);

    public TmfEventTypeStub() {
        super("UnitTest", "TmfEventTypeStub", ROOT);
    }
}
