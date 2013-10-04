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
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.stubs.dialogs;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateSessionDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionGroup;

/**
 * Create session dialog stub implementation.
 */
@SuppressWarnings("javadoc")
public class CreateSessionDialogStub implements ICreateSessionDialog {

    public String fName = "mysession";
    public String fPath = null;
    private boolean fIsStreamedTrace = false;
    private String fNetworkUrl = null;
    private String fControlUrl = null;
    private String fDataUrl = null;
    private boolean fIsSnapshot;

    @Override
    public String getSessionName() {
        return fName;
    }

    @Override
    public String getSessionPath() {
        return fPath;
    }

    @Override
    public boolean isDefaultSessionPath() {
        return fPath == null;
    }

    @Override
    public void initialize(TraceSessionGroup group) {
    }

    @Override
    public int open() {
        return 0;
    }

    public void setSessionPath(String path) {
        fPath = path;
    }

    public void setSessionName(String name) {
        fName = name;
    }

    @Override
    public boolean isStreamedTrace() {
        return fIsStreamedTrace;
    }

    public void setStreamedTrace(boolean isStreamedTrace) {
        fIsStreamedTrace = isStreamedTrace;
    }

    @Override
    public String getNetworkUrl() {
        return fNetworkUrl;
    }

    public void setNetworkUrl(String fNetworkUrl) {
        this.fNetworkUrl = fNetworkUrl;
    }


    @Override
    public String getControlUrl() {
        return fControlUrl;
    }

    public void setControlUrl(String fControlUrl) {
        this.fControlUrl = fControlUrl;
    }

    @Override
    public String getDataUrl() {
        return fDataUrl;
    }

    public void setDataUrl(String fDataUrl) {
        this.fDataUrl = fDataUrl;
    }

    @Override
    public boolean isSnapshot() {
        return fIsSnapshot;
    }

    public void setSnapshot(boolean isSnapshot) {
        fIsSnapshot = isSnapshot;
    }
}
