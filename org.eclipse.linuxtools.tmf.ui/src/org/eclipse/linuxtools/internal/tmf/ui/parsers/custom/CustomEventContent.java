package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

public class CustomEventContent extends TmfEventField {

    public CustomEventContent(CustomEvent parent, StringBuffer content) {
        super(ITmfEventField.ROOT_FIELD_ID, content);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CustomEventContent)) {
            return false;
        }
        return true;
    }

    public void setFields(ITmfEventField[] fields) {
        super.setValue(getValue(), fields);
    }

}
