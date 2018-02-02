/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.component;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class with constants used in data providers
 *
 * @author Bernd Hufmann
 * @since 4.3
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
