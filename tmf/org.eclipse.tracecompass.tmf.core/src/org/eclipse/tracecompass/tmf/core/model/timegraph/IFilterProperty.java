/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

/**
 * Interface that contains the list of possible properties for a timegraph item.
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 *
 */
public interface IFilterProperty {

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
}
