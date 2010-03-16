package org.eclipse.linuxtools.lttng.jni;

import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniMarkerFieldException;


/**
 * <b><u>JniMarkerField</u></b> <p>
 * A JniMarkerField is one of the field of the unparsed content (payload) of an event. <p>
 * 
 * Provides access to the marker_field C structure (from LTT) in java.<p>
 * 
 * Most important attributes in the JniMarkerField are :
 * <ul>
 * <li> the name (field) of in String
 * <li> the marker field format (in C style printf format)
 * </ul>
 */
public abstract class JniMarkerField extends Jni_C_Common
{
    // Internal C pointer of the JniEvent used in LTT
    private Jni_C_Pointer thisMarkerFieldPtr = new Jni_C_Pointer();

    private String field = "";
    private String format = "";
    
    // Native access method
    protected native String ltt_getField(long markerFieldPtr);
    protected native int ltt_getType(long markerFieldPtr);
    protected native long ltt_getOffset(long markerFieldPtr);
    protected native long ltt_getSize(long markerFieldPtr);
    protected native long ltt_getAlignment(long markerFieldPtr);
    protected native long ltt_getAttributes(long markerFieldPtr);
    protected native int ltt_getStatic_offset(long markerFieldPtr);
    protected native String ltt_getFormat(long markerFieldPtr);

    // Debug native function, ask LTT to print marker structure
    protected native void ltt_printMarkerField(long markerFieldPtr);

    /*
     * Default constructor is forbidden
     */
    protected JniMarkerField() {
    }

    /**
     * Copy constructor.<p>
     * 
     * @param oldMarkerField Reference to the JniMarkerField you want to copy. 
     */
    public JniMarkerField(JniMarkerField oldMarkerField) {
        thisMarkerFieldPtr = oldMarkerField.getMarkerFieldPtr();
        field = oldMarkerField.getField();
        format = oldMarkerField.getFormat();
    }

    /**
     * Constructor, using pointer.<p>
     * 
     * @param newMarkerFieldPtr  Pointer to a C marker_field structure
     * 
     * @exception JniException
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer
     */
    public JniMarkerField(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
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
     * Pointer to the marker_field C structure.<p>
     * 
     * The pointer should only be used <u>INTERNALY</u>, do not use these unless you
     * know what you are doing.<p>
     * 
     * @return The actual (long converted) pointer or NULL
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer
     */
    public Jni_C_Pointer getMarkerFieldPtr() {
        return thisMarkerFieldPtr;
    }
    
    /**
     * Print information for this event. <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be the one from 
     * the C structure, not the one populated in java.<p>
     * 
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
    
    /**
     * toString() method. 
     * <u>Intended to debug</u><br>
     * 
     * @return Attributes of the object concatenated in String
     */
    @Override
    public String toString() {
        String returnData = "";
        returnData += "field                   : " + field + "\n";
        returnData += "format                  : " + format + "\n";
        
        return returnData;
    }
    
}
