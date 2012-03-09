package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

public class CustomEventContent extends TmfEventField {

    CustomEvent fParent;
    
    public CustomEventContent(CustomEvent parent, String content) {
        super(ITmfEventField.ROOT_FIELD_ID, content);
        fParent = parent;
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
