/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler.ToolTipString;
import org.junit.Test;

/**
 * Test ToolTipString
 *
 * @author Matthew Khouzam
 */
public class ToolTipStringTest {

    /**
     * Test fromDecimal
     */
    @Test
    public void testDecimal() {
        ToolTipString fromFloat = ToolTipString.fromDecimal(1.0f);
        ToolTipString fromDouble = ToolTipString.fromDecimal(1.0);
        ToolTipString fromInt = ToolTipString.fromDecimal(1);
        ToolTipString fromLong = ToolTipString.fromDecimal(1L);

        assertEquals("1", fromFloat.toString());
        assertEquals("1", fromDouble.toString());
        assertEquals("1", fromInt.toString());
        assertEquals("1", fromLong.toString());
        assertEquals("1", fromFloat.toHtmlString());
        assertEquals("1", fromDouble.toHtmlString());
        assertEquals("1", fromInt.toHtmlString());
        assertEquals("1", fromLong.toHtmlString());
    }

    /**
     * Test fromHtml
     */
    @Test
    public void testHtml() {
        String simple = "<head></head>";
        String normal = "<a href=\"www.tracecompass.org\">Trace Compass</a>";
        String complexValid = "<body id=\"useless tag\">"+
                "<div attribute=someattr> <a>\tSkip to main page  <!-- Sign Up to our Newsletter --></a>  "+
                "</body>";
        String broken1 = "<head><head>";
        String broken2 = "<head></body>";
        String broken3 = "Vince wants his name in the unit tests";

        ToolTipString simpleHtml = ToolTipString.fromHtml(simple);
        ToolTipString normalHtml = ToolTipString.fromHtml(normal);
        ToolTipString complexHtml = ToolTipString.fromHtml(complexValid);
        ToolTipString brokenHtml1 = ToolTipString.fromHtml(broken1);
        ToolTipString brokenHtml2 = ToolTipString.fromHtml(broken2);
        ToolTipString brokenHtml3 = ToolTipString.fromHtml(broken3);

        assertEquals("", simpleHtml.toString());
        assertEquals("Trace Compass", normalHtml.toString());
        assertEquals(" \tSkip to main page    ", complexHtml.toString());
        assertEquals("", brokenHtml1.toString());
        assertEquals("", brokenHtml2.toString());
        assertEquals(broken3, brokenHtml3.toString());

        assertEquals(simple, simpleHtml.toHtmlString());
        assertEquals(normal, normalHtml.toHtmlString());
        assertEquals(complexValid, complexHtml.toHtmlString());
        assertEquals(broken1, brokenHtml1.toHtmlString());
        assertEquals(broken2, brokenHtml2.toHtmlString());
        assertEquals(broken3, brokenHtml3.toHtmlString());
    }

    /**
     * Test fromString
     */
    @Test
    public void testString() {
        ToolTipString empty = ToolTipString.fromString("");
        ToolTipString complicatedString = ToolTipString.fromString("¯\\_(ツ)_/¯");

        assertEquals("", empty.toString());
        assertEquals("¯\\_(ツ)_/¯", complicatedString.toString());

        assertEquals("", empty.toHtmlString());
        assertEquals("&macr;\\_(ツ)_/&macr;", complicatedString.toHtmlString());
    }

    /**
     * Test fromTimestamp
     */
    @Test
    public void testTimestamp() {
        ToolTipString ts0 = ToolTipString.fromTimestamp("", -1);
        ToolTipString ts1 = ToolTipString.fromTimestamp("bob", -1);
        ToolTipString ts2 = ToolTipString.fromTimestamp("", 1);
        ToolTipString ts3 = ToolTipString.fromTimestamp("bob", 1);

        assertEquals("", ts0.toString());
        assertEquals("bob", ts1.toString());
        assertEquals("", ts2.toString());
        assertEquals("bob", ts3.toString());
        assertEquals("<a href=time://-1></a>", ts0.toHtmlString());
        assertEquals("<a href=time://-1>bob</a>", ts1.toHtmlString());
        assertEquals("<a href=time://1></a>", ts2.toHtmlString());
        assertEquals("<a href=time://1>bob</a>", ts3.toHtmlString());
    }

    /**
     * Test equals
     */
    @Test
    public void testEgality() {
        ToolTipString fromHtml = ToolTipString.fromHtml("<div>1</div>");
        ToolTipString fromHtmlString = ToolTipString.fromString("<div>1</div>");
        ToolTipString fromString = ToolTipString.fromString("1");
        ToolTipString fromInt = ToolTipString.fromDecimal(1);
        ToolTipString fromLong = ToolTipString.fromDecimal(1L);
        assertNotEquals(fromInt, null);
        assertNotEquals(fromInt, new Object());
        assertEquals(fromInt, fromInt);
        assertEquals(fromInt.hashCode(), fromLong.hashCode());
        assertEquals(fromInt, fromLong);
        assertNotEquals(fromInt, fromHtml);
        assertNotEquals(fromHtmlString, fromHtml);
        assertNotEquals(fromHtml, ToolTipString.fromHtml("<a>1</a>"));
        assertEquals(fromString.hashCode(), fromLong.hashCode());
        assertEquals(fromString, fromLong);
    }
}
