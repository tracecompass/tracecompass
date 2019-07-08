/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

/**
 * String constants to be used for the metadata of {@link IElementResolver}.
 *
 * @since 5.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMetadataStrings {

    /**
     * The key for the label field
     */
    static final String LABEL_KEY = "label"; //$NON-NLS-1$

    /**
     * The key for the entry name field
     */
    static final String ENTRY_NAME_KEY = "entry"; //$NON-NLS-1$

}
