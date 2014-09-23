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
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

/**
 * <p>
 * Interface for dialog box for updating file import information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IImportConfirmationDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @param name old trace name.
     */
    void setTraceName(String name);

    /**
     * @return the new trace name if not overwrite.
     */
    String getNewTraceName();

    /**
     *
     * @return true to overwrite existing trace.
     */
    boolean isOverwrite();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the open return value
     */
    int open();
}
