/**********************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.stubs.dialogs;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateSessionDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionGroup;

/**
 * Create session dialog stub implementation.
 */
@SuppressWarnings("javadoc")
public class CreateSessionDialogStub implements ICreateSessionDialog {

    public String fName = "mysession"; //$NON-NLS-1$
    public String fPath = null;


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
    public void setTraceSessionGroup(TraceSessionGroup group) {

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
}
