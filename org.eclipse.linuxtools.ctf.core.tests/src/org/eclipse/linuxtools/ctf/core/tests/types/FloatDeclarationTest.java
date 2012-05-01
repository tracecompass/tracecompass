package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDeclaration;
import org.junit.Test;


public class FloatDeclarationTest {
    private FloatDeclaration fixture;


    @Test
    public void ctorTest() {
        for( int i = 1; i < 20; i++) {
            fixture = new FloatDeclaration(i, 32-i, ByteOrder.nativeOrder(), Encoding.NONE, 0);
            assertNotNull(fixture);
        }
    }

    @Test
    public void getterTest() {
        fixture = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), Encoding.NONE, 0);
        assertEquals( fixture.getAlignment(), 0);
        assertEquals( fixture.getByteOrder(), ByteOrder.nativeOrder());
        assertEquals( fixture.getEncoding(), Encoding.NONE);
        assertEquals( fixture.getExponent(), 8);
        assertEquals( fixture.getMantissa(), 24);
    }

    @Test
    public void toStringTest() {
        fixture = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), Encoding.NONE, 0);
        assertTrue(fixture.toString().contains("float")); //$NON-NLS-1$
    }
}
