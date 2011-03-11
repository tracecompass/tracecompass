package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.event.TmfEventType;

public abstract class CustomEventType extends TmfEventType {
    
    public CustomEventType(CustomTraceDefinition definition) {
        super(definition.definitionName, getLabels(definition));
    }

    private static String[] getLabels(CustomTraceDefinition definition) {
        String[] labels = new String[definition.outputs.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = definition.outputs.get(i).name;
        }
        return labels;
    }

}
