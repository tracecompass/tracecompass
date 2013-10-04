/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.core.control.model;


/**
 * <p>
 * Interface for retrieval of snapshot information of a session.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ISnapshotInfo extends ITraceInfo {

    /**
     * @return path string where snapshot is located.
     */
    String getSnapshotPath();

    /**
     * Sets the path string (where snapshot is located) to the given value.
     * @param path - session path to set.
     */
    void setSnapshotPath(String path);

    /**
     * @return the snapshot ID.
     */
    int getId();

    /**
     * Sets the snapshot ID.
     * @param id - the ID to set.
     */
    void setId(int id);

    /**
     * Sets whether snapshot is streamed over the network or stored locally
     * at the tracers host.
     *
     * @param isStreamed - <code>true</code> if streamed else <code>false</code>
     */
    void setStreamedSnapshot(boolean isStreamed);

    /**
     * Gets whether snapshot is streamed over the network or stored locally
     * at the tracers host.
     *
     * @return <code>true</code> if streamed else <code>false</code>
     */
    boolean isStreamedSnapshot();

}
