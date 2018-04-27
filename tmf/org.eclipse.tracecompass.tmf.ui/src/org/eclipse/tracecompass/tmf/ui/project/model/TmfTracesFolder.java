/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.core.resources.IFolder;

/**
 * Implementation of model element representing the unique "Traces" folder in
 * the project.
 */
public class TmfTracesFolder extends TmfTraceFolder {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    static final String TRACES_RESOURCE_NAME = "Traces"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     * Creates folder model element under the project.
     * @param name The name of trace folder.
     * @param resource The folder resource.
     * @param parent The parent element (project).
     */
    public TmfTracesFolder(String name, IFolder resource, TmfProjectElement parent) {
        super(name, resource, parent);
    }

    /**
     * @since 2.0
     */
    @Override
    public String getLabelText() {
        return getName() + " [" + getTraces().size() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
