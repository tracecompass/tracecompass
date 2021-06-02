/**********************************************************************
 * Copyright (c) 2018, 2021 Ericsson
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
 * Class with constants that contains the list of possible properties for a model item,
 * for example timegraph item.
 *
 * @author Jean-Christian Kouame
 * @author Bernd Hufmann
 * @since 7.0
 *
 */
public final class CoreFilterProperty {

    private CoreFilterProperty () {
        // Empty private constructor
    }

    /**
     * The dimmed property mask
     */
    public static final int DIMMED = 1 << 0;

    /**
     * The draw bound property mask
     */
    public static final int BOUND = 1 << 1;

    /**
     * The exclude property mask
     */
    public static final int EXCLUDE = 1 << 2;

    /**
     * The highlight property mask
     */
    public static final int HIGHLIGHT = 1 << 3;

}
