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

import junit.framework.TestCase;

/**
 * <b><u>TmfEventSourceTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventSourceTest extends TestCase {

	private final Object fSource = new String("Some source");

	// ========================================================================
	// Housekeeping
	// ========================================================================

	public TmfEventSourceTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ========================================================================
	// Constructors
	// ========================================================================

	public void testTmfEventSourceDefault() {
		TmfEventSource source = new TmfEventSource();
		assertEquals("getSourceId", null, source.getSourceId());
	}

	public void testTmfEventSource() {
		TmfEventSource source = new TmfEventSource(fSource);
		assertSame("getSourceId", fSource, source.getSourceId());
	}

	public void testTmfEventSourceCopy() {
		TmfEventSource original = new TmfEventSource(fSource);
		TmfEventSource source = new TmfEventSource(original);
		assertSame("getSourceId", fSource, source.getSourceId());
	}

	public void testCloneShallowCopy() {
		TmfEventSource original = new TmfEventSource(fSource);
		TmfEventSource source = original.clone();
		assertSame("getSourceId", fSource, source.getSourceId());
	}

//	public void testCloneDeepCopy() {
//		TmfEventSource original = new TmfEventSource(fSource);
//		TmfEventSource source = original.clone();
//		assertNotSame("getSourceId", fSource, source.getSourceId());
//		assertEquals ("getSourceId", fSource, source.getSourceId());
//	}

	// ========================================================================
	// Operators
	// ========================================================================

	public void testToString() {
		String expected1 = "[TmfEventSource(" + "null" + ")]";
		TmfEventSource source1 = new TmfEventSource();
		assertEquals("toString", expected1, source1.toString());

		String expected2 = "[TmfEventSource(" + fSource.toString() + ")]";
		TmfEventSource source2 = new TmfEventSource(fSource);
		assertEquals("toString", expected2, source2.toString());
	}

}
