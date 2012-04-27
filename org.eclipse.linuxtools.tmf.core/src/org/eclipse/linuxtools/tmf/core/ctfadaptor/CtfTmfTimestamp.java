package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

public class CtfTmfTimestamp extends TmfTimestamp {

    final private CtfTmfTrace fTrace;

    public CtfTmfTimestamp(long timestamp, CtfTmfTrace trace) {
        fTrace = trace;
        setValue(timestamp, -9, 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((fTrace == null) ? 0 : fTrace.hashCode());
        return result;
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
        if (!(obj instanceof CtfTmfTimestamp)) {
            return false;
        }
        CtfTmfTimestamp other = (CtfTmfTimestamp) obj;
        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!fTrace.equals(other.fTrace)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final long timestamp = getValue();
        final Date d = new Date(timestamp / 1000000);
        final DateFormat df = new SimpleDateFormat("HH:mm:ss."); //$NON-NLS-1$
        final long nanos = (timestamp % 1000000000);
        StringBuilder output = new StringBuilder();
        output.append(df.format(d));
        output.append(String.format("%09d", nanos)); //$NON-NLS-1$
        return output.toString();
    }

    public String toFullDateString(){
        final long timestamp = getValue();
        final Date d = new Date(timestamp / 1000000);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss."); //$NON-NLS-1$
        final long nanos = (timestamp % 1000000000);
        StringBuilder output = new StringBuilder();
        output.append(df.format(d));
        output.append(String.format("%09d", nanos)); //$NON-NLS-1$
        return output.toString();
    }

}
