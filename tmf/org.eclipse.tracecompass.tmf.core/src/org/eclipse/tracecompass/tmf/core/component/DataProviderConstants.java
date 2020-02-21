/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.component;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class with constants used in data providers
 *
 * @author Bernd Hufmann
 * @since 5.0
 */
@NonNullByDefault
public class DataProviderConstants {
    /**
     * Separator between data provider ID and secondary ID
     */
    public static final String ID_SEPARATOR = ":"; //$NON-NLS-1$

    private DataProviderConstants() {
        // Empty constructor
    }
}
