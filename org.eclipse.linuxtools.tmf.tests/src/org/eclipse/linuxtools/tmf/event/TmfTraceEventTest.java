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
 * <b><u>TmfTraceEventTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceEventTest extends TestCase {

	private final String   fTypeId = "Some type";
	private final String   fLabel0 = "label1";
	private final String   fLabel1 = "label2";
	private final String[] fLabels = new String[] { fLabel0, fLabel1 };

	private final TmfTimestamp      fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
	private final TmfTimestamp      fTimestamp2 = new TmfTimestamp(12350, (byte) 2, 5);
	private final TmfEventSource    fSource     = new TmfEventSource("Source");
	private final TmfEventType      fType       = new TmfEventType(fTypeId, fLabels);
	private final TmfEventReference fReference  = new TmfEventReference("Some reference");

	private final TmfTraceEvent fEvent1;
	private final TmfTraceEvent fEvent2;

	private final TmfEventContent fContent1;
	private final TmfEventContent fContent2;

	private final String fPath = "/some/path/";
	private final String fFile = "filename";
	private final int    fLine = 10;

	// ========================================================================
	// Housekeeping
	// ========================================================================

	public TmfTraceEventTest(String name) {
		super(name);

		fEvent1 = new TmfTraceEvent(fTimestamp1, fSource, fType, fReference, fPath, fFile, fLine);
		fContent1 = new TmfEventContent(fEvent1, "Some content");
		fEvent1.setContent(fContent1);

		fEvent2 = new TmfTraceEvent(fTimestamp1, fTimestamp2, fSource, fType, fReference, fPath, fFile, fLine);
		fContent2 = new TmfEventContent(fEvent2, "Some other content");
		fEvent2.setContent(fContent2);
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

    public void testTmfTraceEvent() throws Exception {
		assertEquals("getTimestamp",         fTimestamp1, fEvent1.getTimestamp());
		assertEquals("getOriginalTimestamp", fTimestamp1, fEvent1.getOriginalTimestamp());
		assertEquals("getSource",            fSource,     fEvent1.getSource());
		assertEquals("getType",              fType,       fEvent1.getType());
		assertEquals("getContent",           fContent1,   fEvent1.getContent());
		assertEquals("getReference",         fReference,  fEvent1.getReference());
		assertEquals("getSourcePath",        fPath,       fEvent1.getSourcePath());
		assertEquals("getFileName",          fFile,       fEvent1.getFileName());
		assertEquals("getLineNumber",        fLine,       fEvent1.getLineNumber());
    }

    public void testTmfTraceEvent2() throws Exception {
		assertEquals("getTimestamp",         fTimestamp2, fEvent2.getTimestamp());
		assertEquals("getOriginalTimestamp", fTimestamp1, fEvent2.getOriginalTimestamp());
		assertEquals("getSource",            fSource,     fEvent2.getSource());
		assertEquals("getType",              fType,       fEvent2.getType());
		assertEquals("getContent",           fContent2,   fEvent2.getContent());
		assertEquals("getReference",         fReference,  fEvent2.getReference());
		assertEquals("getSourcePath",        fPath,       fEvent2.getSourcePath());
		assertEquals("getFileName",          fFile,       fEvent2.getFileName());
		assertEquals("getLineNumber",        fLine,       fEvent2.getLineNumber());
    }

    public void testTmfTraceEventCopy() throws Exception {
    	TmfTraceEvent event = new TmfTraceEvent(fEvent2);
		assertEquals("getTimestamp",         fTimestamp2, event.getTimestamp());
		assertEquals("getOriginalTimestamp", fTimestamp1, event.getOriginalTimestamp());
		assertEquals("getSource",            fSource,     event.getSource());
		assertEquals("getType",              fType,       event.getType());
		assertEquals("getContent",           fContent2,   event.getContent());
		assertEquals("getReference",         fReference,  event.getReference());
		assertEquals("getSourcePath",        fPath,       event.getSourcePath());
		assertEquals("getFileName",          fFile,       event.getFileName());
		assertEquals("getLineNumber",        fLine,       event.getLineNumber());
    }

}

