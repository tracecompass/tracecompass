/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <b><u>TmfEventTypeTest</u></b>
 * <p>
 * JUnit test suite for the TmfEventType class.
 */
public class TmfEventTypeTest {

    // ========================================================================
    // Constructor
    // ========================================================================

    @Test
    public void testEmptyConstructor() {
        TmfEventType type = new TmfEventType("", null);
        assertEquals("getValue", "", type.getTypeId());
        assertEquals("getFormat", null, type.getFormat());
   }

    @Test
    public void testNormalConstructor() {
        TmfEventType type = new TmfEventType("Type", new TmfEventFormat(new String[] { "field1", "field2" }));
        assertEquals("getValue", "Type", type.getTypeId());
        assertEquals("getFormat", "field1", type.getFormat().getLabels()[0]);
        assertEquals("getFormat", "field2", type.getFormat().getLabels()[1]);
   }

}
