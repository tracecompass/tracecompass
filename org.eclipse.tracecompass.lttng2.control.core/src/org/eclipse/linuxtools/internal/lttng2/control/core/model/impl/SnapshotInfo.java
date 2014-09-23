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
package org.eclipse.linuxtools.internal.lttng2.control.core.model.impl;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISnapshotInfo;

/**
 * <p>
 * Implementation of the snapshot interface (ISnapshotInfo) to store snapshot
 * related data.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class SnapshotInfo extends TraceInfo implements ISnapshotInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** The snapshot path for storing traces. */
    private String fPath = ""; //$NON-NLS-1$
    /** The snapshot ID */
    private int fId = -1;
    /** Flag whether snapshot is stored over the network or locally */
    private boolean fIsStreamed = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of base event
     */
    public SnapshotInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public SnapshotInfo(SnapshotInfo other) {
        super(other);
        fPath = other.fPath;
        fId = other.fId;
        fIsStreamed = other.fIsStreamed;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public String getSnapshotPath() {
        return fPath;
    }

    @Override
    public void setSnapshotPath(String path) {
        fPath = path;
    }

    @Override
    public int getId() {
        return fId;
    }

    @Override
    public void setId(int id) {
        fId = id;
    }

    @Override
    public void setStreamedSnapshot(boolean isStreamed) {
        fIsStreamed = isStreamed;
    }

    @Override
    public boolean isStreamedSnapshot() {
        return fIsStreamed;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + fId;
        result = prime * result + (fIsStreamed ? 1231 : 1237);
        result = prime * result + ((fPath == null) ? 0 : fPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SnapshotInfo other = (SnapshotInfo) obj;
        if (fId != other.fId) {
            return false;
        }
        if (fIsStreamed != other.fIsStreamed) {
            return false;
        }
        if (fPath == null) {
            if (other.fPath != null) {
                return false;
            }
        } else if (!fPath.equals(other.fPath)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[SnapshotInfo(");
            output.append(super.toString());
            output.append(",snapshotPath=");
            output.append(fPath);
            output.append(",ID=");
            output.append(fId);
            output.append(",isStreamedSnapshot=");
            output.append(fIsStreamed);
            output.append(")]");
            return output.toString();
    }

}
