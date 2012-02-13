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

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * <b><u>TmfEventContentStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("nls")
public class TmfEventContentStub extends TmfEventContent {

	public TmfEventContentStub(TmfEvent parent, String content) {
		super(parent, content);
	}

	public TmfEventContentStub(TmfEventContentStub other) {
		super(other);
	}

	@Override
	protected void parseContent() {
        Object value1 = new Integer(1);
        Object value2 = new Integer(-10);
        Object value3 = new Boolean(true);
        Object value4 = new String("some string");
        Object value5 = new TmfTimestamp(1, (byte) 2, 3);

        TmfEventField field1 = new TmfEventField(this, "field1", value1);
        TmfEventField field2 = new TmfEventField(this, "field2", value2);
        TmfEventField field3 = new TmfEventField(this, "field3", value3);
        TmfEventField field4 = new TmfEventField(this, "field4", value4);
        TmfEventField field5 = new TmfEventField(this, "field5", value5);

        fFields = new TmfEventField[] { field1, field2, field3, field4, field5 };
	}
	
    @Override
    public TmfEventContent clone() {
    	TmfEventContentStub content = new TmfEventContentStub(this);
    	content.fRawContent = "Some content";
    	content.fFields = null;
		return content;
    }

//    @Override
//	public String toString() {
//    	Object[] fields = getFields();
//    	StringBuilder result = new StringBuilder("[TmfEventContent(");
//    	for (int i = 0; i < fields.length; i++) {
//    		if (i > 0) result.append(",");
//    		result.append(fields[i]);
//    	}
//    	result.append(")]");
//    	return result.toString();
//    }
}
