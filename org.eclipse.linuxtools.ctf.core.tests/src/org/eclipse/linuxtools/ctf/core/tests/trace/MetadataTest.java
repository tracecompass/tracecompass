/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.Metadata;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>MetadataTest</code> contains tests for the class
 * <code>{@link Metadata}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class MetadataTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private Metadata fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        assumeTrue(testTrace.exists());
        fixture = new Metadata(testTrace.getTrace());
    }

    /**
     * Run the Metadata(CTFTrace) constructor test.
     */
    @Test
    public void testMetadata() {
        assertNotNull(fixture);
    }

    /**
     * Run the ByteOrder getDetectedByteOrder() method test.
     */
    @Test
    public void testGetDetectedByteOrder() {
        ByteOrder result = fixture.getDetectedByteOrder();
        assertNull(result);
    }

    /**
     * Test toString
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertNotNull(result);
    }

    /**
     * Run the void parse() method test.
     *
     * @throws CTFReaderException
     */
    @Test(expected = org.eclipse.linuxtools.ctf.core.trace.CTFReaderException.class)
    public void testParse() throws CTFReaderException {
        fixture.parse();
    }
}
