/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface that contains the list of possible properties for a timegraph item
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 *
 */
@NonNullByDefault
public interface IFilterProperty {

    /**
     * get the dimmed property key string
     *
     * @return The dimmed property key string
     */
    public static String isDimmed() {
        return "dimmed"; //$NON-NLS-1$
    }

    /**
     * get the draw bound property key string
     *
     * @return The draw bound property key string
     */
    public static String drawBound() {
        return "bound"; //$NON-NLS-1$
    }

    /**
     * Get the exclude property key string
     *
     * @return The exclude property key string
     */
    public static String exclude() {
        return "exclude"; //$NON-NLS-1$
    }
}
