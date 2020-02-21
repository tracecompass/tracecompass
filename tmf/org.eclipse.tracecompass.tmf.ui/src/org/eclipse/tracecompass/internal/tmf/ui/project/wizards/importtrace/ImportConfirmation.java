/*******************************************************************************
 * Copyright (c) 2014 Ericsson.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann  - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

/**
 * Enumeration for import conflict dialog
 *
 * @author Bernd Hufmann
 */
public enum ImportConfirmation {

    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /** Single rename */
    RENAME(Messages.ImportTraceWizard_ImportConfigurationRename),
    /** Rename all */
    RENAME_ALL(Messages.ImportTraceWizard_ImportConfigurationRenameAll),
    /** Single overwrite */
    OVERWRITE(Messages.ImportTraceWizard_ImportConfigurationOverwrite),
    /** Overwrite all */
    OVERWRITE_ALL(Messages.ImportTraceWizard_ImportConfigurationOverwriteAll),
    /** Single skip */
    SKIP(Messages.ImportTraceWizard_ImportConfigurationSkip),
    /** Skip all*/
    SKIP_ALL(Messages.ImportTraceWizard_ImportConfigurationSkipAll),
    /** Default value*/
    CONTINUE("CONTINUE"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Name of enum
     */
    private final String fInName;

    // ------------------------------------------------------------------------
    // Constuctor
    // ------------------------------------------------------------------------
    /**
     * Private constructor
     *
     * @param name
     *            the name of state
     */
    private ImportConfirmation(String name) {
        fInName = name;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return state name
     */
    public String getInName() {
        return fInName;
    }
}
