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
import static org.junit.Assert.assertTrue;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.FloatDeclaration;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class FloatDeclarationTest {
    private FloatDeclaration fixture;


    @Test
    public void ctorTest() {
        for( int i = 1; i < 20; i++) {
            fixture = new FloatDeclaration(i, 32-i, ByteOrder.nativeOrder(),  0);
            assertNotNull(fixture);
        }
    }

    @Test
    public void getterTest() {
        fixture = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), 1);
        assertEquals( fixture.getAlignment(), 1);
        assertEquals( fixture.getByteOrder(), ByteOrder.nativeOrder());
        assertEquals( fixture.getExponent(), 8);
        assertEquals( fixture.getMantissa(), 24);
    }

    @Test
    public void toStringTest() {
        fixture = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), 0);
        assertTrue(fixture.toString().contains("float"));
    }
}
