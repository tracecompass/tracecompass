package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

public class CtfLocation implements ITmfLocation<Long> {

    public static final Long INVALID_LOCATION = -1L;

    public CtfLocation(Long location) {
        setLocation(location);
    }

    public CtfLocation(ITmfTimestamp timestamp) {
        setLocation(timestamp.getValue());
    }

    private Long fTimestamp;

//    @Override
    public void setLocation(Long location) {
        this.fTimestamp = location;
    }

    @Override
    public Long getLocation() {
        return this.fTimestamp;
    }

    @Override
    public CtfLocation clone() {
        return new CtfLocation(getLocation());
    }

}
