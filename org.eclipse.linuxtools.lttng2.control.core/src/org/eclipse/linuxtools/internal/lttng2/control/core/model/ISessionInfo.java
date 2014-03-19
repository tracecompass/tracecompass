/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 *   Marc-Andre Laperle - Support for creating a live session
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.core.model;

import java.util.List;

/**
 * <p>
 * Interface for retrieval of trace session information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ISessionInfo extends ITraceInfo {

    /**
     * @return the session state state (active or inactive).
     */
    TraceSessionState getSessionState();
    /**
     * Sets the session state  to the given value.
     * @param state - state to set.
     */
    void setSessionState(TraceSessionState state);

    /**
     * Sets the event state to the value specified by the given name.
     * @param stateName - state to set.
     */
    void setSessionState(String stateName);

    /**
     * @return path string where session is located.
     */
    String getSessionPath();

    /**
     * Sets the path string (where session is located) to the given value.
     * @param path - session path to set.
     */
    void setSessionPath(String path);

    /**
     * @return all domain information as array.
     */
    IDomainInfo[] getDomains();

    /**
     * Sets all domain information specified by given list.
     * @param domains - all domain information to set.
     */
    void setDomains(List<IDomainInfo> domains);

    /**
     * Adds a single domain information.
     * @param domainInfo domain information to add.
     */
    void addDomain(IDomainInfo domainInfo);

    /**
     * Returns if session is streamed over network
     * @return <code>true</code> if streamed over network else <code>false</code>
     */
    boolean isStreamedTrace();

    /**
     * Sets whether the trace is streamed or not
     * @param isStreamedTrace <code>true</code> if streamed over network else <code>false</code>
     */
    void setStreamedTrace(boolean isStreamedTrace);

    /**
     * Returns whether the session is snapshot session or not
     * @return <code>true</code> if it is snapshot session else <code>false</code>
     */
    boolean isSnapshotSession();

    /**
     * Set whether or not the session should be in snapshot mode
     *
     * @param isSnapshot
     *            true for snapshot mode, false otherwise
     */
    void setSnapshot(boolean isSnapshot);

    /**
     * Gets the snapshot information the session or null if it is not a
     * snapshot session.
     * @return snapshot information
     */
    ISnapshotInfo getSnapshotInfo();

    /**
     * Sets the snapshot information of the session
     * @param setSnapshotInfo - the snapshot data to set.
     */
    void setSnapshotInfo(ISnapshotInfo setSnapshotInfo);

    /**
     * Get whether or not the session should be in Live mode
     *
     * @return <code>true</code> if is a live session else <code>false</code>
     */
    public boolean isLive();

    /**
     * Set whether or not the session should be in Live mode
     *
     * @param isLive
     *            true for Live mode, false otherwise
     */
    public void setLive(boolean isLive);

    /**
     * Get the live delay which is the delay in micro seconds before the data is
     * flushed and streamed.
     *
     * @return the live delay or -1 if the default value should be used
     */
    public int getLiveDelay();

    /**
     * Set the live delay which is the delay in micro seconds before the data is
     * flushed and streamed.
     *
     * @param liveDelay
     *            the live delay
     */
    public void setLiveDelay(int liveDelay);

    /**
     * Get the network URL in case control and data is configured together
     * otherwise null If it returns a non-null value, getControlUrl() and
     * getDataUrl() have to return null.
     *
     * @return The network URL or null.
     */
    String getNetworkUrl();

    /**
     * Set the network URL
     *
     * @param networkUrl
     *            the network URL
     */
    void setNetworkUrl(String networkUrl);

    /**
     * Get the control URL in case control and data is configured separately. If
     * it returns a non-null value, getDataUrl() has to return a valid value too
     * and getNetworkUrl() has to return null.
     *
     * @return The control URL or null.
     */
    String getControlUrl();

    /**
     * Set the control URL
     *
     * @param controlUrl
     *            the control URL
     */
    void setControlUrl(String controlUrl);

    /**
     * Get the data URL in case control and data is configured separately. If it
     * returns a non-null value, getControlUrl() has to return a valid value too
     * and getNetworkUrl() has to return null.
     *
     * @return The data URL or null.
     */
    String getDataUrl();

    /**
     * Set the data URL
     *
     * @param datalUrl
     *            the data URL
     */
    void setDataUrl(String datalUrl);
}
