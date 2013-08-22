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
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;

/**
 * <p>
 * Interface for import traces dialog.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IImportDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return a list of trace paths to import.
     */
    List<ImportFileInfo> getTracePathes();

    /**
     * @return the project to import the traces to
     */
    IProject getProject();

    /**
     * Sets the session containing the traces to import
     * @param session The trace session
     */
    void setSession(TraceSessionComponent session);

    /**
     * Sets the default project name to use
     *
     * @param defaultProject
     *            the name of the default project to use
     */
    void setDefaultProject(String defaultProject);


    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the open return value
     */
    int open();
}
