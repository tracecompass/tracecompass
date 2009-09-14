/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.jni;

/**
 * <b><u>JniMarkerField</u></b>
 * <p>
 * A JniMarkerField is one of the field of the unparsed content (payload) of an event
 * <p>
 * Most important attributes in the JniMarkerField are :
 * <ul>
 * <li> the name (field) of in String
 * <li> the marker field format (in C style printf format)
 * </ul>
 */
public final class JniMarkerField extends Jni_C_Common
{
    // Internal C pointer of the JniEvent used in LTT
    private C_Pointer thisMarkerFieldPtr = new C_Pointer();

    private String field = "";
    private String format = "";

    // Native access method
    private native String ltt_getField(long markerFieldPtr);
    @SuppressWarnings("unused")
    private native int ltt_getType(long markerFieldPtr);
    @SuppressWarnings("unused")
    private native long ltt_getOffset(long markerFieldPtr);
    @SuppressWarnings("unused")
    private native long ltt_getSize(long markerFieldPtr);
    @SuppressWarnings("unused")
    private native long ltt_getAlignment(long markerFieldPtr);
    @SuppressWarnings("unused")
    private native long ltt_getAttributes(long markerFieldPtr);
    @SuppressWarnings("unused")
    private native int ltt_getStatic_offset(long markerFieldPtr);
    private native String ltt_getFormat(long markerFieldPtr);

    // Debug native function, ask LTT to print marker structure
    private native void ltt_printMarkerField(long markerFieldPtr);

    static {
        System.loadLibrary("lttvtraceread");
    }

    /**
     * Default constructor is forbidden
     */
    @SuppressWarnings("unused")
    private JniMarkerField() {
    }

    /**
     * Copy constructor.
     * 
     * @param oldMarkerField
     *            A reference to the JniMarkerField you want to copy. 
     */
    public JniMarkerField(JniMarkerField oldMarkerField) {
        thisMarkerFieldPtr = oldMarkerField.getMarkerFieldPtr();
        field = oldMarkerField.getField();
        format = oldMarkerField.getFormat();
    }

    /**
     * Copy constructor, using pointer.
     * 
     * @param newMarkerFieldPtr  Pointer to a C marker_field structure
     * 
     * @exception JniException
     */
    public JniMarkerField(C_Pointer newMarkerFieldPtr) throws JniException {
        thisMarkerFieldPtr = newMarkerFieldPtr;

        // Populate the marker field
        populateMarkerFieldInformation();
    }

    /* 
     * This function populates the marker field data with data from LTT
     * 
     */
    private void populateMarkerFieldInformation() throws JniException {
        if (thisMarkerFieldPtr.getPointer() == NULL) {
            throw new JniMarkerFieldException(
                    "Pointer is NULL, trace closed? (populateMarkerInformation)");
        } else {
            field = ltt_getField(thisMarkerFieldPtr.getPointer());
            format = ltt_getFormat(thisMarkerFieldPtr.getPointer());
        }
    }

    public String getField() {
        return field;
    }

    public String getFormat() {
        return format;
    }

    /**
     * Pointer to the marker_field C structure<br>
     * <br>
     * The pointer should only be used INTERNALY, do not use these unless you
     * know what you are doing.
     * 
     * @return The actual (long converted) pointer or NULL
     */
    public C_Pointer getMarkerFieldPtr() {
        return thisMarkerFieldPtr;
    }

    /**
     * toString() method. <u>Intended to debug</u><br>
     * 
     * @return String Attributes of the object concatenated in String
     */
    public String toString() {
        String returnData = "";
        returnData += "field                   : " + field + "\n";
        returnData += "format                  : " + format + "\n";
        return returnData;
    }

    /**
     * Print information for this event. <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be the one from the C structure<br>
     * <br>
     * This function will not throw but will complain loudly if pointer is NULL
     */
    public void printMarkerFieldInformation() {

        // If null pointer, print a warning!
        if (thisMarkerFieldPtr.getPointer() == NULL) {
            printlnC("Pointer is NULL, cannot print. (printMarkerFieldInformation)");
        } else {
            ltt_printMarkerField(thisMarkerFieldPtr.getPointer());
        }
    }
}
