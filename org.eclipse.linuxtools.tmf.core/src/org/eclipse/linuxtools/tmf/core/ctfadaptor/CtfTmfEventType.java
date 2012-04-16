package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

public class CtfTmfEventType extends TmfEventType {

    public CtfTmfEventType(String contextId, String eventName,
            ITmfEventField content) {
        super(contextId, eventName, content);
    }

    @Override
    public String toString()
    {
        return this.getName();
    }
}
