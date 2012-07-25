/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis (mathieu.denis@polymtl.ca)  - Initial design and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.util;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.util.TmfFixedArray;

/**
 * <b><u>TmfFixedArrayTest</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfFixedArrayTest extends TestCase {

    // ------------------------------------------------------------------------
    // Field(s)
    // ------------------------------------------------------------------------
    TmfFixedArray<String> fFixedArray1 = null;
    TmfFixedArray<String> fFixedArray2 = null;
    String fString1, fString2, fString3, fString4, fString5;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    public TmfFixedArrayTest() {
        fString1 = "First String";
        fString2 = "Second String";
        fString3 = "Third String";
        fString4 = "Fourth String";
        fString5 = "Fifth String";
        fFixedArray1 = new TmfFixedArray<String>(fString1, fString2, fString3);
        fFixedArray2 = new TmfFixedArray<String>(); // Empty array at the beginning
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Get
    // ------------------------------------------------------------------------

    public void testGet() {
        assertTrue("get", fString1.equals(fFixedArray1.get(0)));
        assertTrue("get", fString2.equals(fFixedArray1.get(1)));
        assertTrue("get", fString3.equals(fFixedArray1.get(2)));

        try {
            fFixedArray2.get(0);
            fail();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // Success
        }
    }

    public void testGetArray() {
        String[] stringArray = fFixedArray1.getArray();
        assertNotNull("getArray", stringArray);
        assertTrue("getArray", fString1.equals(stringArray[0]));
        assertTrue("getArray", fString2.equals(stringArray[1]));
        assertTrue("getArray", fString3.equals(stringArray[2]));
    }

    // ------------------------------------------------------------------------
    // Equals
    // ------------------------------------------------------------------------

    public void testEqualsReflexivity() {
        assertTrue("equals", fFixedArray1.equals(fFixedArray1));
        assertTrue("equals", fFixedArray2.equals(fFixedArray2));

        assertTrue("equals", !fFixedArray1.equals(fFixedArray2));
        assertTrue("equals", !fFixedArray2.equals(fFixedArray1));
    }

    public void testEqualsSymmetry() {
        TmfFixedArray<String> fixedArray1 = (TmfFixedArray<String>) fFixedArray1.clone();
        TmfFixedArray<String> fixedArray2 = (TmfFixedArray<String>) fFixedArray2.clone();

        assertTrue("equals", fixedArray1.equals(fFixedArray1));
        assertTrue("equals", fFixedArray1.equals(fixedArray1));

        assertTrue("equals", fixedArray2.equals(fFixedArray2));
        assertTrue("equals", fFixedArray2.equals(fixedArray2));
    }

    public void testEqualsTransivity() {
        TmfFixedArray<String> fixedArray1 = (TmfFixedArray<String>) fFixedArray1.clone();
        TmfFixedArray<String> fixedArray2 = (TmfFixedArray<String>) fFixedArray1.clone();
        TmfFixedArray<String> fixedArray3 = (TmfFixedArray<String>) fFixedArray1.clone();

        assertTrue("equals", fixedArray1.equals(fixedArray2));
        assertTrue("equals", fixedArray2.equals(fixedArray3));
        assertTrue("equals", fixedArray1.equals(fixedArray3));
    }

    public void testEqualsNull() {
        assertTrue("equals", !fFixedArray1.equals(null));
        assertTrue("equals", !fFixedArray2.equals(null));
    }

    // ------------------------------------------------------------------------
    // Append
    // ------------------------------------------------------------------------

    public void testAppend() {
        TmfFixedArray<String> fixedArray = new TmfFixedArray<String>();

        fixedArray = fixedArray.append(fString1, fString2, fString3);
        assertEquals("append", 3, fixedArray.size());
        assertTrue("append", fString1.equals(fixedArray.get(0)));
        assertTrue("append", fString2.equals(fixedArray.get(1)));
        assertTrue("append", fString3.equals(fixedArray.get(2)));

        fixedArray = fixedArray.append(fString4);
        assertEquals("append", 4, fixedArray.size());
        assertTrue("append", fString4.equals(fixedArray.get(3)));
    }


    @SuppressWarnings("unchecked")
    public void testAppendFixedArray() {
        TmfFixedArray<String> fixedArrayToAppend1 = new TmfFixedArray<String>(fString4);
        TmfFixedArray<String> fixedArrayToAppend2 = new TmfFixedArray<String>(fString5);
        TmfFixedArray<String> fixedArray         = new TmfFixedArray<String>();

        fixedArray = fixedArray.append(fFixedArray1, fixedArrayToAppend1);
        assertEquals("append", 4, fixedArray.size());
        assertTrue("append", fString1.equals(fixedArray.get(0)));
        assertTrue("append", fString2.equals(fixedArray.get(1)));
        assertTrue("append", fString3.equals(fixedArray.get(2)));
        assertTrue("append", fString4.equals(fixedArray.get(3)));

        fixedArray = fixedArray.append(fixedArrayToAppend2);
        assertEquals("append", 5, fixedArray.size());
        assertTrue("append", fString5.equals(fixedArray.get(4)));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() {
        TmfFixedArray<String> fixedArray1 = (TmfFixedArray<String>) fFixedArray1.clone();
        TmfFixedArray<String> fixedArray2 = (TmfFixedArray<String>) fFixedArray2.clone();

        assertTrue("hashCode", fixedArray1.hashCode() == fFixedArray1.hashCode());
        assertTrue("hashCode", fFixedArray2.hashCode() == fixedArray2.hashCode());

        assertTrue("hashCode", fFixedArray1.hashCode() != fixedArray2.hashCode());
        assertTrue("hashCode", fFixedArray2.hashCode() != fixedArray1.hashCode());
    }

    // ------------------------------------------------------------------------
    // toArray
    // ------------------------------------------------------------------------

    public void testToArray() {
        String[] expected1 = {fString1, fString2, fString3};
        assertTrue("toArray", Arrays.equals(expected1, fFixedArray1.toArray()));

        String[] expected2 = {};
        assertTrue("toArray", Arrays.equals(expected2, fFixedArray2.toArray()));
    }

    public void testToArrayArg() {
        String[] stringArray = new String[3];
        fFixedArray1.toArray(stringArray);
        assertTrue("toArrayArg", stringArray[0].equals(fFixedArray1.get(0)));
        assertTrue("toArrayArg", stringArray[1].equals(fFixedArray1.get(1)));
        assertTrue("toArrayArg", stringArray[2].equals(fFixedArray1.get(2)));

        String[] stringBigArray = new String[10];
        fFixedArray1.toArray(stringBigArray);
        assertNull("toArrayArg", stringBigArray[3]);

        TmfFixedArray<Object> fFixedArrayObject  = new TmfFixedArray<Object>(fString1);
        stringArray = fFixedArrayObject.toArray(new String[0]);
        assertTrue("toArrayArg", stringArray[0].equals(fString1));
    }

    // ------------------------------------------------------------------------
    // Size
    // ------------------------------------------------------------------------

    public void testSize() {
        assertEquals("toArray", 3, fFixedArray1.size());

        assertEquals("toArray", 0, fFixedArray2.size());
    }

    // ------------------------------------------------------------------------
    // SubArray
    // ------------------------------------------------------------------------

    public void testSubArray() {
        TmfFixedArray<String> subArray = fFixedArray1.subArray(1);

        assertEquals("SubArray", 2, subArray.size());
        assertTrue("SubArray", fString2.equals(subArray.get(0)));
        assertTrue("SubArray", fString3.equals(subArray.get(1)));
    }

    public void testSubArray2() {
        TmfFixedArray<String> subArray = fFixedArray1.subArray(1, 2);

        assertEquals("SubArray", 2, subArray.size());
        assertTrue("SubArray", fString2.equals(subArray.get(0)));
        assertTrue("SubArray", fString3.equals(subArray.get(1)));
    }

    // ------------------------------------------------------------------------
    // Set
    // ------------------------------------------------------------------------

    public void testSet() {
        String[] newString = {"new FirstString", "new SecondString", "new ThirdString"};

        fFixedArray1.set(0, newString[0]);
        assertTrue("getArray", newString[0].equals(newString[0]));

        fFixedArray1.set(1, newString[1]);
        assertTrue("getArray", newString[1].equals(newString[1]));

        fFixedArray1.set(2, newString[2]);
        assertTrue("getArray", newString[2].equals(newString[2]));

        try {
            fFixedArray2.set(0, "newString");
            fail();
        } catch (Exception e) {
            // Success
        }
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    public void testToString() {
        String expected1 = Arrays.asList(fString1, fString2, fString3).toString();
        assertEquals("toString", expected1, fFixedArray1.toString());

        String expected2 = Arrays.asList().toString();
        assertEquals("toString", expected2, fFixedArray2.toString());
    }
}
