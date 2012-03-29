package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

public class CtfIterator extends CTFTraceReader implements ITmfContext,
        Comparable<CtfIterator> {

    private final CtfTmfTrace ctfTmfTrace;

    private CtfLocation curLocation;
    private final long curRank;

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
        this.curLocation = (new CtfLocation(this.getCurrentEvent()
                .getTimestampValue()));
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
        long offsetTimestamp = timestamp - this.getCtfTmfTrace().getCTFTrace().getOffset();
        if( offsetTimestamp < 0 ) {
            ret = super.seek(timestamp);
        } else {
            ret = super.seek(offsetTimestamp);
        }

        if (ret) {
            curLocation.setLocation(getCurrentEvent().getTimestampValue());
        }
        return ret;
    }

    public boolean seekRank(long rank) {
        boolean ret = false;
        ret = super.seekIndex(rank);

        if (ret) {
            curLocation.setLocation(getCurrentEvent().getTimestampValue());
        }
        return ret;
    }

    @Override
    public long getRank() {
        return super.getIndex();
    }

    @Override
    public void setRank(long rank) {
        seekRank(rank);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfContext#clone()
     */
    @Override
    public CtfIterator clone() {
        CtfIterator clone = null;
        clone = new CtfIterator(ctfTmfTrace, this.getCurrentEvent()
                .getTimestampValue(), curRank);
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
        seek(((CtfLocation)location).getLocation());
    }

    @Override
    public CtfLocation getLocation() {
        return curLocation;
    }

    @Override
    public void updateRank(int rank) {
        // not needed I think
    }

    @Override
    public boolean isValidRank() {
        return (getRank() > -1);
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