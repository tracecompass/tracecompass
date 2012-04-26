package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

public class CtfTmfContent extends TmfEventField {

    public CtfTmfContent(String name, ITmfEventField[] fields) {
        super(name, fields);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder();
        for( ITmfEventField field : getFields())        {
            retVal.append(field.toString());
            retVal.append('\t');
        }
        return retVal.toString();
    }
}
