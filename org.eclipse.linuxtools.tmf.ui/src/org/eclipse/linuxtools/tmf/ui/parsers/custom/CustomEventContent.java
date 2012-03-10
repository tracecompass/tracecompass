package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

public class CustomEventContent extends TmfEventField {

//    CustomEvent fParent;
    
    public CustomEventContent(CustomEvent parent, String content) {
        super(ITmfEventField.ROOT_FIELD_ID, content);
//        fParent = parent;
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

//    @Override
//    protected void parseContent() {
//        CustomEvent event = (CustomEvent) fParentEvent;
//        fFields = event.extractItemFields();
//    }
//
//    @Override
//    public String toString() {
//        return Arrays.toString(getFields());
//    }

}
