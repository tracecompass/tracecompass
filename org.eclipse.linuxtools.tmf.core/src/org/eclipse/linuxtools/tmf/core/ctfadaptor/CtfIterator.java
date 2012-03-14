package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

public class CtfIterator extends CTFTraceReader implements ITmfContext,
        Comparable<CtfIterator> {

    private final CtfTmfTrace ctfTmfTrace;

    private CtfLocation curLocation;
    private long curRank;

    /**
     * Create a new CTF trace iterator, which initially points at the first
     * event in the trace.
     *
     * @param trace
     */
    public CtfIterator(CtfTmfTrace trace) {
        super(trace.getCTFTrace());
        this.ctfTmfTrace = trace;

        // FIXME put the real stuff here...
        this.curLocation = new CtfLocation(trace.getStartTime());
        this.curRank = 0;
    }

    public CtfIterator(CtfTmfTrace trace, long timestampValue, long rank) {
        super(trace.getCTFTrace());
        this.ctfTmfTrace = trace;
        this.curLocation = (new CtfLocation(
                this.getCurrentEvent().getTimestampValue()));
        if (this.getCurrentEvent().getTimestampValue() != timestampValue) {
            this.seek(timestampValue);
        }

        this.curRank = rank;
    }

    public CtfTmfTrace getCtfTmfTrace() {
        return ctfTmfTrace;
    }

    public CtfTmfEvent getCurrentEvent() {
        StreamInputReader top = super.prio.peek();
        if (top != null) {
            return new CtfTmfEvent(top.getCurrentEvent(), top, ctfTmfTrace);
        }
        return null;
    }

    @Override
    public boolean seek(long timestamp) {
        boolean ret = false;
        ret = super.seek(timestamp);

        if (ret) {
            curLocation.setLocation(getCurrentEvent().getTimestampValue());
        }
        return ret;
    }

    @Override
    public long getRank() {
        final CtfTmfEvent current = getCurrentEvent();
        if (current != null) {
            return getCurrentEvent().getRank();
        }
        return 0;
    }

    @Override
    public void setRank(long rank) {
        // FIXME NYI
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfContext#clone()
     */
    @Override
    public CtfIterator clone() {
        CtfIterator clone = null;
        clone = new CtfIterator(ctfTmfTrace,
                this.getCurrentEvent().getTimestampValue(), curRank);
        return clone;
    }

    @Override
    public void dispose() {
        // FIXME add dispose() stuff to CTFTrace and call it here...

    }

    @Override
    public void setLocation(ITmfLocation<?> location) {
        // FIXME alex: isn't there a cleaner way than a cast here?
        this.curLocation = (CtfLocation) location;
    }

    @Override
    public CtfLocation getLocation() {
        return curLocation;
    }

    @Override
    public void updateRank(int rank) {
        curRank = rank;
    }

    @Override
    public boolean isValidRank() {
        return true;
    }

    @Override
    public boolean advance() {
        return super.advance();
    }

    @Override
    public int compareTo(CtfIterator o) {
        if (this.getRank() < o.getRank()) {
            return -1;
        } else if (this.getRank() > o.getRank()) {
            return 1;
        }
        return 0;
    }

}