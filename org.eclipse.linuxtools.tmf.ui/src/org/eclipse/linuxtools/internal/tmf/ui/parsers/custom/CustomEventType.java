package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

public abstract class CustomEventType extends TmfEventType {
    
    private static String CONTEXT_ID = "CustomEventType"; //$NON-NLS-1$
    
    public CustomEventType(CustomTraceDefinition definition) {
        super(CONTEXT_ID, definition.definitionName, getRootField(definition));
    }

    private static ITmfEventField getRootField(CustomTraceDefinition definition) {
        ITmfEventField[] fields = new ITmfEventField[definition.outputs.size()];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TmfEventField(definition.outputs.get(i).name, null);
        }
        ITmfEventField rootField = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, fields);
        return rootField;
    }

}
