package org.eclipse.linuxtools.lttng.jni;

import org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniMarkerFieldException;


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
 *
 * <b>NOTE</b><p>
 * This class is ABSTRACT, you need to extends it to support your specific LTTng version.<p>
 *
 */
public abstract class JniMarkerField extends Jni_C_Common
{
    // Internal C pointer of the JniEvent used in LTT
    private Jni_C_Pointer_And_Library_Id thisMarkerFieldPtr = new Jni_C_Pointer_And_Library_Id();

    private String field = ""; //$NON-NLS-1$
    private String format = ""; //$NON-NLS-1$

    // Native access method
    protected native String ltt_getField(int libId, long markerFieldPtr);
    protected native int ltt_getType(int libId, long markerFieldPtr);
    protected native long ltt_getOffset(int libId, long markerFieldPtr);
    protected native long ltt_getSize(int libId, long markerFieldPtr);
    protected native long ltt_getAlignment(int libId, long markerFieldPtr);
    protected native long ltt_getAttributes(int libId, long markerFieldPtr);
    protected native int ltt_getStatic_offset(int libId, long markerFieldPtr);
    protected native String ltt_getFormat(int libId, long markerFieldPtr);

    // Debug native function, ask LTT to print marker structure
    protected native void ltt_printMarkerField(int libId, long markerFieldPtr);

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
     * Constructor, using pointer.
     * <p>
     *
     * @param newMarkerFieldPtr
     *            Pointer to a C marker_field structure
     * @exception JniException
     *                If the JNI call fails
     *
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public JniMarkerField(Jni_C_Pointer_And_Library_Id newMarkerFieldPtr) throws JniException {
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
                    "Pointer is NULL, trace closed? (populateMarkerInformation)"); //$NON-NLS-1$
        }
        field = ltt_getField(thisMarkerFieldPtr.getLibraryId(), thisMarkerFieldPtr.getPointer());
        format = ltt_getFormat(thisMarkerFieldPtr.getLibraryId(), thisMarkerFieldPtr.getPointer());
    }

    /**
     * Get the field of this marker
     *
     * @return The field name
     */
    public String getField() {
        return field;
    }

    /**
     * Get the format of this marker
     *
     * @return The format, as a String
     */
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
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public Jni_C_Pointer_And_Library_Id getMarkerFieldPtr() {
        return thisMarkerFieldPtr;
    }

    /**
     * Print information for this event. <u>Intended to debug</u><br>
     *
     * This function will call Ltt to print, so information printed will be the one from
     * the C structure, not the one populated in java.<p>
     */
    public void printMarkerFieldInformation() {
        ltt_printMarkerField(thisMarkerFieldPtr.getLibraryId(), thisMarkerFieldPtr.getPointer());
    }

    /**
     * toString() method.
     * <u>Intended to debug</u><br>
     *
     * @return Attributes of the object concatenated in String
     */
	@Override
    @SuppressWarnings("nls")
    public String toString() {
        String returnData = "";
        returnData += "field                   : " + field + "\n";
        returnData += "format                  : " + format + "\n";

        return returnData;
    }

}
