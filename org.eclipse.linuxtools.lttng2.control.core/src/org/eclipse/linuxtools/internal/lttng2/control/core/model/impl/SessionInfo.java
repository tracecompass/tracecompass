/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.core.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISnapshotInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;

/**
 * <p>
 * Implementation of the trace session interface (ISessionInfo) to store session
 * related data.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class SessionInfo extends TraceInfo implements ISessionInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The trace session state.
     */
    private TraceSessionState fState = TraceSessionState.INACTIVE;
    /**
     * The trace session path for storing traces.
     */
    private String fSessionPath = ""; //$NON-NLS-1$
    /**
     * The domains information of this session.
     */
    private final List<IDomainInfo> fDomains = new ArrayList<>();
    /**
     * Flag to indicate whether trace is streamed over network or not.
     */
    private boolean fIsStreamedTrace = false;
    /**
     * Flag to indicate whether the session is a snapshot session or not.
     */
    private boolean fIsSnapshot = false;
    /**
     * The snapshot information of the session
     */
    private ISnapshotInfo fSnapshotInfo = null;
    /**
     * The network URL for the session (-U)
     */
    private String fNetworkUrl = null;
    /**
     * The control URL for the session (-C)
     */
    private String fControlUrl = null;
    /**
     * The data URL for the session (-D)
     */
    private String fDataUrl = null;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of base event
     */
    public SessionInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public SessionInfo(SessionInfo other) {
        super(other);
        fState = other.fState;
        fSessionPath = other.fSessionPath;
        fIsStreamedTrace = other.fIsStreamedTrace;
        fIsSnapshot = other.fIsSnapshot;
        fSnapshotInfo = other.fSnapshotInfo;
        fNetworkUrl = other.fNetworkUrl;
        fControlUrl = other.fControlUrl;
        fDataUrl = other.fDataUrl;

        for (Iterator<IDomainInfo> iterator = other.fDomains.iterator(); iterator.hasNext();) {
            IDomainInfo domain = iterator.next();
            if (domain instanceof DomainInfo) {
                fDomains.add(new DomainInfo((DomainInfo)domain));
            } else {
                fDomains.add(domain);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public TraceSessionState getSessionState() {
        return fState;
    }

    @Override
    public void setSessionState(TraceSessionState state) {
        fState = state;
    }

    @Override
    public void setSessionState(String stateName) {
        if (TraceSessionState.INACTIVE.getInName().equals(stateName)) {
            fState = TraceSessionState.INACTIVE;
        } else if (TraceSessionState.ACTIVE.getInName().equals(stateName)) {
            fState = TraceSessionState.ACTIVE;
        }
    }

    @Override
    public String getSessionPath() {
        if (isSnapshotSession() && fSnapshotInfo != null) {
            return fSnapshotInfo.getSnapshotPath();
        }
        return fSessionPath;
    }

    @Override
    public void setSessionPath(String path) {
        fSessionPath = path;
    }

    @Override
    public IDomainInfo[] getDomains() {
        return fDomains.toArray(new IDomainInfo[fDomains.size()]);
    }

    @Override
    public void setDomains(List<IDomainInfo> domains) {
        fDomains.clear();
        for (Iterator<IDomainInfo> iterator = domains.iterator(); iterator.hasNext();) {
            IDomainInfo domainInfo = iterator.next();
            fDomains.add(domainInfo);
        }
    }

    @Override
    public boolean isStreamedTrace() {
        return fIsStreamedTrace;
    }

    @Override
    public void setStreamedTrace(boolean isStreamedTrace) {
        fIsStreamedTrace = isStreamedTrace;
    }

    @Override
    public boolean isSnapshotSession() {
        return fIsSnapshot || fSnapshotInfo != null;
    }

    @Override
    public void setSnapshot(boolean isSnapshot) {
        fIsSnapshot = isSnapshot;
    }

    @Override
    public ISnapshotInfo getSnapshotInfo() {
        return fSnapshotInfo;
    }

    @Override
    public void setSnapshotInfo(ISnapshotInfo info) {
        fSnapshotInfo = info;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void addDomain(IDomainInfo domainInfo) {
        fDomains.add(domainInfo);
    }


    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[SessionInfo(");
            output.append(super.toString());
            output.append(",Path=");
            output.append(getSessionPath());
            output.append(",State=");
            output.append(fState);
            output.append(",isStreamedTrace=");
            output.append(fIsStreamedTrace);
            output.append(",isSnapshot=");
            output.append(fIsSnapshot);

            if (fSnapshotInfo != null) {
                output.append(",snapshotInfo=");
                output.append(fSnapshotInfo.toString());
            }
            output.append(",Domains=");
            for (Iterator<IDomainInfo> iterator = fDomains.iterator(); iterator.hasNext();) {
                IDomainInfo domain = iterator.next();
                output.append(domain.toString());
            }

            output.append(",NetworkUrl=");
            output.append(getNetworkUrl());
            output.append(",ControlUrl=");
            output.append(getControlUrl());
            output.append(",DataUrl=");
            output.append(getDataUrl());

            output.append(")]");
            return output.toString();
    }

    @Override
    public String getNetworkUrl() {
        return fNetworkUrl;
    }

    @Override
    public void setNetworkUrl(String networkUrl) {
        fNetworkUrl = networkUrl;
    }

    @Override
    public String getControlUrl() {
        return fControlUrl;
    }

    @Override
    public void setControlUrl(String controlUrl) {
        fControlUrl = controlUrl;
    }

    @Override
    public void setDataUrl(String datalUrl) {
        fDataUrl = datalUrl;
    }

    @Override
    public String getDataUrl() {
        return fDataUrl;
    }
}
