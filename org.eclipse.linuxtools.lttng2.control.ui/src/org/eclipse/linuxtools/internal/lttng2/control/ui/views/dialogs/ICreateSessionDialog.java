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
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;

/**
 * <p>
 * Interface for create session dialog.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ICreateSessionDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get the parameters necessary for the creation of a LTTng session
     *
     * @return the parameters
     */
    ISessionInfo getParameters();


    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Initializes the dialog box.
     * @param group - the session group
     */
    void initialize(TraceSessionGroup group);

    /**
     * @return the open return value
     */
    int open();
}
