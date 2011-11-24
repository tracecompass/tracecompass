package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.util.Arrays;

import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;

public class CustomEventContent extends TmfEventContent {

    public CustomEventContent(CustomEvent parent, Object content) {
        super(parent, content);
    }

    @Override
    protected void parseContent() {
        CustomEvent event = (CustomEvent) fParentEvent;
        fFields = event.extractItemFields();
    }

    @Override
    public String toString() {
        return Arrays.toString(getFields());
    }

}
