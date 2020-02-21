/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

/**
 * String constants to be used for the metadata of {@link IElementResolver}.
 *
 * @since 5.2
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
