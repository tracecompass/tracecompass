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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <b><u>TmfEventFormatTest</u></b>
 * <p>
 * JUnit test suite for the TmfEventFormat class.
 */
public class TmfEventFormatTest {

    // ========================================================================
    // Constructors
    // ========================================================================

    @Test
    public void testBasicTmfEventFormat() {
        TmfEventFormat format = new TmfEventFormat();
        assertEquals("getLabels", 1, format.getLabels().length);
        assertEquals("getValue", "Content", format.getLabels()[0]);
    }

    @Test
    public void testEmptyConstructor() {
        TmfEventFormat format = new TmfEventFormat(new String[] {});
        assertEquals("getLabels", 0, format.getLabels().length);
    }

    @Test
    public void testNormalConstructor() {
        TmfEventFormat format = new TmfEventFormat(new String[] { "field1", "field2", "field3" });
        assertEquals("getLabels", 3, format.getLabels().length);
        assertEquals("getLabels", "field1", format.getLabels()[0]);
        assertEquals("getLabels", "field2", format.getLabels()[1]);
        assertEquals("getLabels", "field3", format.getLabels()[2]);
    }

    @Test
    public void testExtendedConstructor() {
        TmfEventFormatStub format = new TmfEventFormatStub();
        assertEquals("getLabels", 5, format.getLabels().length);
        assertEquals("getLabels", "Field1", format.getLabels()[0]);
        assertEquals("getLabels", "Field2", format.getLabels()[1]);
        assertEquals("getLabels", "Field3", format.getLabels()[2]);
        assertEquals("getLabels", "Field4", format.getLabels()[3]);
        assertEquals("getLabels", "Field5", format.getLabels()[4]);
    }

    // ========================================================================
    // parse
    // ========================================================================

    @Test
    public void testBasicParse() {
        TmfEventFormat format = new TmfEventFormat();
        TmfEventField[] content = format.parse(new TmfTimestamp());
        assertEquals("length", 1, content.length);
        assertEquals("getValue", "[TmfTimestamp:0,0,0]", content[0].toString());
    }

    @Test
    public void testExtendedParse() {
        TmfEventFormatStub format = new TmfEventFormatStub();
        TmfEventField[] content = format.parse(new TmfTimestamp());
        assertEquals("length", 5, content.length);
        assertEquals("getValue", "1",                    content[0].toString());
        assertEquals("getValue", "-10",                  content[1].toString());
        assertEquals("getValue", "true",                 content[2].toString());
        assertEquals("getValue", "some string",          content[3].toString());
        assertEquals("getValue", "[TmfTimestamp:1,2,3]", content[4].toString());
    }

}
