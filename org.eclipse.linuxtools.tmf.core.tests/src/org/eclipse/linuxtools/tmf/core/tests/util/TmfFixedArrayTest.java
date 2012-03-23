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
        fString1 = "First String"; //$NON-NLS-1$
        fString2 = "Second String"; //$NON-NLS-1$
        fString3 = "Third String"; //$NON-NLS-1$
        fString4 = "Fourth String"; //$NON-NLS-1$
        fString5 = "Fifth String"; //$NON-NLS-1$
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
    
    public void testGet() throws Exception {
        assertTrue("get", fString1.equals(fFixedArray1.get(0))); //$NON-NLS-1$
        assertTrue("get", fString2.equals(fFixedArray1.get(1))); //$NON-NLS-1$
        assertTrue("get", fString3.equals(fFixedArray1.get(2))); //$NON-NLS-1$
        
        try {
            fFixedArray2.get(0);
            fail();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // Success
        }
    }
    
    public void testGetArray() throws Exception {
        String[] stringArray = fFixedArray1.getArray();
        assertNotNull("getArray", stringArray); //$NON-NLS-1$
        assertTrue("getArray", fString1.equals(stringArray[0])); //$NON-NLS-1$
        assertTrue("getArray", fString2.equals(stringArray[1])); //$NON-NLS-1$
        assertTrue("getArray", fString3.equals(stringArray[2])); //$NON-NLS-1$
    }
    
    // ------------------------------------------------------------------------
    // Equals
    // ------------------------------------------------------------------------
    
    public void testEqualsReflexivity() throws Exception {
        assertTrue("equals", fFixedArray1.equals(fFixedArray1)); //$NON-NLS-1$
        assertTrue("equals", fFixedArray2.equals(fFixedArray2)); //$NON-NLS-1$
        
        assertTrue("equals", !fFixedArray1.equals(fFixedArray2)); //$NON-NLS-1$
        assertTrue("equals", !fFixedArray2.equals(fFixedArray1)); //$NON-NLS-1$
    }
    
    @SuppressWarnings("unchecked")
    public void testEqualsSymmetry() throws Exception {
        TmfFixedArray<String> fixedArray1 = (TmfFixedArray<String>) fFixedArray1.clone();
        TmfFixedArray<String> fixedArray2 = (TmfFixedArray<String>) fFixedArray2.clone();

        assertTrue("equals", fixedArray1.equals(fFixedArray1)); //$NON-NLS-1$
        assertTrue("equals", fFixedArray1.equals(fixedArray1)); //$NON-NLS-1$

        assertTrue("equals", fixedArray2.equals(fFixedArray2)); //$NON-NLS-1$
        assertTrue("equals", fFixedArray2.equals(fixedArray2)); //$NON-NLS-1$
    }
    
    @SuppressWarnings("unchecked")
    public void testEqualsTransivity() throws Exception {
        TmfFixedArray<String> fixedArray1 = (TmfFixedArray<String>) fFixedArray1.clone();
        TmfFixedArray<String> fixedArray2 = (TmfFixedArray<String>) fFixedArray1.clone();
        TmfFixedArray<String> fixedArray3 = (TmfFixedArray<String>) fFixedArray1.clone();

        assertTrue("equals", fixedArray1.equals(fixedArray2)); //$NON-NLS-1$
        assertTrue("equals", fixedArray2.equals(fixedArray3)); //$NON-NLS-1$
        assertTrue("equals", fixedArray1.equals(fixedArray3)); //$NON-NLS-1$
    }
    
    public void testEqualsNull() throws Exception {
        assertTrue("equals", !fFixedArray1.equals(null)); //$NON-NLS-1$
        assertTrue("equals", !fFixedArray2.equals(null)); //$NON-NLS-1$
    }
    
    // ------------------------------------------------------------------------
    // Append
    // ------------------------------------------------------------------------

    public void testAppend() {
        TmfFixedArray<String> fixedArray         = new TmfFixedArray<String>();
        
        fixedArray = fixedArray.append(fString1, fString2, fString3);
        assertEquals("append", 3, fixedArray.size()); //$NON-NLS-1$
        assertTrue("append", fString1.equals(fixedArray.get(0))); //$NON-NLS-1$
        assertTrue("append", fString2.equals(fixedArray.get(1))); //$NON-NLS-1$
        assertTrue("append", fString3.equals(fixedArray.get(2))); //$NON-NLS-1$

        fixedArray = fixedArray.append(fString4);
        assertEquals("append", 4, fixedArray.size()); //$NON-NLS-1$
        assertTrue("append", fString4.equals(fixedArray.get(3))); //$NON-NLS-1$
    }
    
   
    @SuppressWarnings("unchecked")
    public void testAppendFixedArray() throws Exception {
        TmfFixedArray<String> fixedArrayToAppend1 = new TmfFixedArray<String>(fString4);
        TmfFixedArray<String> fixedArrayToAppend2 = new TmfFixedArray<String>(fString5);
        TmfFixedArray<String> fixedArray         = new TmfFixedArray<String>();
        
        fixedArray = fixedArray.append(fFixedArray1, fixedArrayToAppend1);
        assertEquals("append", 4, fixedArray.size()); //$NON-NLS-1$
        assertTrue("append", fString1.equals(fixedArray.get(0))); //$NON-NLS-1$
        assertTrue("append", fString2.equals(fixedArray.get(1))); //$NON-NLS-1$
        assertTrue("append", fString3.equals(fixedArray.get(2))); //$NON-NLS-1$
        assertTrue("append", fString4.equals(fixedArray.get(3))); //$NON-NLS-1$
        
        fixedArray = fixedArray.append(fixedArrayToAppend2);
        assertEquals("append", 5, fixedArray.size()); //$NON-NLS-1$
        assertTrue("append", fString5.equals(fixedArray.get(4))); //$NON-NLS-1$
    }
    
    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    public void testHashCode() throws Exception {
        TmfFixedArray<String> fixedArray1 = (TmfFixedArray<String>) fFixedArray1.clone();
        TmfFixedArray<String> fixedArray2 = (TmfFixedArray<String>) fFixedArray2.clone();

        assertTrue("hashCode", fixedArray1.hashCode() == fFixedArray1.hashCode()); //$NON-NLS-1$
        assertTrue("hashCode", fFixedArray2.hashCode() == fixedArray2.hashCode()); //$NON-NLS-1$

        assertTrue("hashCode", fFixedArray1.hashCode() != fixedArray2.hashCode()); //$NON-NLS-1$
        assertTrue("hashCode", fFixedArray2.hashCode() != fixedArray1.hashCode()); //$NON-NLS-1$
    }
    
    // ------------------------------------------------------------------------
    // toArray
    // ------------------------------------------------------------------------

    public void testToArray() {
        String[] expected1 = {fString1, fString2, fString3};
        assertTrue("toArray", Arrays.equals(expected1, fFixedArray1.toArray())); //$NON-NLS-1$
        
        String[] expected2 = {};
        assertTrue("toArray", Arrays.equals(expected2, fFixedArray2.toArray())); //$NON-NLS-1$
    }

    public void testToArrayArg() throws Exception {
        String[] stringArray = new String[3];
        fFixedArray1.toArray(stringArray);
        assertTrue("toArrayArg", stringArray[0].equals(fFixedArray1.get(0))); //$NON-NLS-1$
        assertTrue("toArrayArg", stringArray[1].equals(fFixedArray1.get(1))); //$NON-NLS-1$
        assertTrue("toArrayArg", stringArray[2].equals(fFixedArray1.get(2))); //$NON-NLS-1$
        
        String[] stringBigArray = new String[10];
        fFixedArray1.toArray(stringBigArray);
        assertNull("toArrayArg", stringBigArray[3]); //$NON-NLS-1$
        
        TmfFixedArray<Object> fFixedArrayObject  = new TmfFixedArray<Object>(fString1);
        stringArray = fFixedArrayObject.toArray(new String[0]);
        assertTrue("toArrayArg", stringArray[0].equals(fString1)); //$NON-NLS-1$
    }
    
    // ------------------------------------------------------------------------
    // Size
    // ------------------------------------------------------------------------

    public void testSize() {
        assertEquals("toArray", 3, fFixedArray1.size()); //$NON-NLS-1$
        
        assertEquals("toArray", 0, fFixedArray2.size()); //$NON-NLS-1$
    }
    
    // ------------------------------------------------------------------------
    // SubArray
    // ------------------------------------------------------------------------

    public void testSubArray() throws Exception {
        TmfFixedArray<String> subArray = fFixedArray1.subArray(1);
        
        assertEquals("SubArray", 2, subArray.size()); //$NON-NLS-1$
        assertTrue("SubArray", fString2.equals(subArray.get(0))); //$NON-NLS-1$
        assertTrue("SubArray", fString3.equals(subArray.get(1))); //$NON-NLS-1$
    }
    
    public void testSubArray2() {
        TmfFixedArray<String> subArray = fFixedArray1.subArray(1, 2);
        
        assertEquals("SubArray", 2, subArray.size()); //$NON-NLS-1$
        assertTrue("SubArray", fString2.equals(subArray.get(0))); //$NON-NLS-1$
        assertTrue("SubArray", fString3.equals(subArray.get(1))); //$NON-NLS-1$
    }
    
    // ------------------------------------------------------------------------
    // Set
    // ------------------------------------------------------------------------
    
    public void testSet() throws Exception {
        String[] newString = {"new FirstString", "new SecondString", "new ThirdString"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        fFixedArray1.set(0, newString[0]);
        assertTrue("getArray", newString[0].equals(newString[0])); //$NON-NLS-1$
        
        fFixedArray1.set(1, newString[1]);
        assertTrue("getArray", newString[1].equals(newString[1])); //$NON-NLS-1$
        
        fFixedArray1.set(2, newString[2]);
        assertTrue("getArray", newString[2].equals(newString[2])); //$NON-NLS-1$
        
        try {
            fFixedArray2.set(0, "newString"); //$NON-NLS-1$
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
        assertEquals("toString", expected1, fFixedArray1.toString()); //$NON-NLS-1$

        String expected2 = Arrays.asList().toString();
        assertEquals("toString", expected2, fFixedArray2.toString()); //$NON-NLS-1$
    }
}
