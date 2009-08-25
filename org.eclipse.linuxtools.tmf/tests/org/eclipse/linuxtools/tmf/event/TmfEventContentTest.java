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
 * <b><u>TmfEventContentTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventContentTest {

	// ========================================================================
	// Constructors
	// ========================================================================

	@Test
	public void testTmfEventContent() {
		TmfEventContent content = new TmfEventContent("Some content",
				new TmfEventFormat());
		assertEquals("getFormat", 1, content.getFormat().getLabels().length);
		assertEquals("getLabels", "Content", content.getFormat().getLabels()[0]);
		assertEquals("getContent", "Some content", content.getContent());
	}

	// ========================================================================
	// getField
	// ========================================================================

	@Test
	public void testBasicGetField() {
		TmfEventContent content = new TmfEventContent("Some content",
				new TmfEventFormat());
		assertEquals("getField", 1, content.getFields().length);
		assertEquals("getField", "Some content", content.getField(0).toString());
	}

	@Test
	public void testExtendedGetField() {
		TmfEventContent content = new TmfEventContent("",
				new TmfEventFormatStub());
		assertEquals("getField", 5, content.getFields().length);
		assertEquals("getField", "1", content.getField(0).toString());
		assertEquals("getField", "-10", content.getField(1).toString());
		assertEquals("getField", "true", content.getField(2).toString());
		assertEquals("getField", "some string", content.getField(3).toString());
		assertEquals("getField", "[TmfTimestamp:1,2,3]", content.getField(4)
				.toString());
	}

}
