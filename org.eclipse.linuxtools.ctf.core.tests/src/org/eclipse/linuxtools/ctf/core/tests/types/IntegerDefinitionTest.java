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

package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>IntegerDefinitionTest</code> contains tests for the class
 * <code>{@link IntegerDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class IntegerDefinitionTest {

    private IntegerDefinition fixture;
    @NonNull private static final String NAME = "testInt";
    @NonNull private static final String clockName = "clock";

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     *             won't happen
     */
    @Before
    public void setUp() throws CTFReaderException {
        IntegerDeclaration id = IntegerDeclaration.createDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
        ByteBuffer byb = ByteBuffer.allocate(128);
        byb.mark();
        byb.putInt(1);
        byb.reset();
        BitBuffer bb = new BitBuffer(byb);
        fixture = id.createDefinition(null, NAME, bb);
    }

    /**
     * Run the IntegerDefinition(IntegerDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testIntegerDefinition() {
        IntegerDeclaration declaration = IntegerDeclaration.createDeclaration(1, false, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8);
        IDefinitionScope definitionScope = null;
        String fieldName = "";

        IntegerDefinition result = new IntegerDefinition(declaration,
                definitionScope, fieldName, 1);
        assertNotNull(result);
    }

    /**
     * Run the IntegerDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        IntegerDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the long getValue() method test.
     */
    @Test
    public void testGetValue() {
        long result = fixture.getValue();
        assertEquals(0L, result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertEquals("0", result);
    }

    /**
     * Run the IntegerDefinition formatNumber(Long, int, boolean) method test
     * for unsigned values.
     */
    @Test
    public void testFormatNumber_unsignedLong() {

        long unsignedLongValue = -64;
        String result = IntegerDefinition.formatNumber(unsignedLongValue, 10, false);
        // -64 + 2^64 = 18446744073709551552
        assertEquals("18446744073709551552", result);

        unsignedLongValue = -131940199973272L;
        result = IntegerDefinition.formatNumber(unsignedLongValue, 10, false);
        // -131940199973272l + 2^64 = 18446612133509578344
        assertEquals("18446612133509578344", result);

        unsignedLongValue = 123456789L;
        result = IntegerDefinition.formatNumber(unsignedLongValue, 10, false);
        assertEquals("123456789", result);
    }

    /**
     * Run the IntegerDefinition formatNumber(Long, int, boolean) method test
     * for signed values.
     */
    @Test
    public void testFormatNumber_signedLong() {
        long signedValue = -64L;
        String result = IntegerDefinition.formatNumber(signedValue, 10, true);
        assertEquals("-64", result);

        signedValue = -131940199973272L;
        result = IntegerDefinition.formatNumber(signedValue, 10, true);
        assertEquals("-131940199973272", result);

        signedValue = 123456789L;
        result = IntegerDefinition.formatNumber(signedValue, 10, true);
        assertEquals("123456789", result);
    }
}
