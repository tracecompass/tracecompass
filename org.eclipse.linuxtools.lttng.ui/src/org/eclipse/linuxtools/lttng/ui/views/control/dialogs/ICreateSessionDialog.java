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
package org.eclipse.linuxtools.lttng.ui.views.control.dialogs;

import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceSessionGroup;

/**
 * <b><u>ICreateSessionDialog</u></b>
 * <p>
 * Interface for create session dialog.
 * </p>
 */
public interface ICreateSessionDialog {
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the session name.
     */
    public String getSessionName();

    /**
     * @return the session path (null for default path)
     */
    public String getSessionPath();
    
    /**
     * @return true for default location else false
     */
    public boolean isDefaultSessionPath();
    
    /**
     * Set trace session group.
     * @param group - the session group
     */
    public void setTraceSessionGroup(TraceSessionGroup group);

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the open return value
     */
    int open();
}
