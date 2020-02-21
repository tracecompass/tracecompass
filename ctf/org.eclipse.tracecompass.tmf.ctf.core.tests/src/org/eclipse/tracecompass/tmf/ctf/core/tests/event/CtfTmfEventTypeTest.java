/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventType;
import org.junit.Test;

/**
 * The class <code>CtfTmfEventTypeTest</code> contains tests for the class
 * <code>{@link CtfTmfEventType}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfEventTypeTest {

    /**
     * Run the CtfTmfEventType(String,String,ITmfEventField) constructor test.
     */
    @Test
    public void testCtfTmfEventType() {
        String eventName = "";
        ITmfEventField content = new TmfEventField("", null, new ITmfEventField[] {});
        CtfTmfEventType result = new CtfTmfEventType(eventName, content);

        assertNotNull(result);
        assertEquals("", result.toString());
        assertEquals("", result.getName());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        ITmfEventField emptyField = new TmfEventField("", null, new ITmfEventField[] {});
        CtfTmfEventType fixture = new CtfTmfEventType("" , emptyField);

        String result = fixture.toString();

        assertEquals("", result);
    }
}
