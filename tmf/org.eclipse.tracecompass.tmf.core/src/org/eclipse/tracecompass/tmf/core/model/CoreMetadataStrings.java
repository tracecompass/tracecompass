/**********************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

/**
 * String constants to be used for the metadata of {@link ICoreElementResolver}.
 *
 * @since 7.0
 */
public final class CoreMetadataStrings {

    private CoreMetadataStrings() {
        // Private constructor
    }

    /**
     * The key for the label field
     */
    public static final String LABEL_KEY = "label"; //$NON-NLS-1$

    /**
     * The key for the entry name field
     */
    public static final String ENTRY_NAME_KEY = "entry"; //$NON-NLS-1$

}
