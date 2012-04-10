package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

public class CtfTmfTimestamp extends TmfTimestamp implements ITmfTimestamp {

    final private CtfTmfTrace fTrace;

    public CtfTmfTimestamp(long timestamp, CtfTmfTrace trace) {
        fTrace = trace;
        fValue = timestamp;
        fScale = (byte) -9;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final long timestamp = fValue;
        final Date d = new Date(timestamp / 1000000);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss."); //$NON-NLS-1$
        final long nanos = (timestamp % 1000000000);
        StringBuilder output = new StringBuilder();
        output.append(df.format(d));
        output.append(String.format("%09d", nanos)); //$NON-NLS-1$
        return output.toString();
    }

}
