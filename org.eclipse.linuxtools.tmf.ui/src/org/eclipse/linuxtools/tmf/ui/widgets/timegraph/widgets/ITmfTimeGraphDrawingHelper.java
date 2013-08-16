/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

/**
 * This interface provides functions to convert a model element to a drawing
 * element and vice versa.
 *
 * Views who implement this interface allow access to some model-to-canvas and
 * vice-versa functions without having to expose their full functionnalities.
 *
 * @author gbastien
 * @since 2.1
 */
public interface ITmfTimeGraphDrawingHelper {

    /**
     * Return the x coordinate corresponding to a time
     *
     * @param time
     *            the time
     * @return the x coordinate corresponding to the time
     */
    int getXForTime(long time);

    /**
     * Return the time corresponding to an x coordinate
     *
     * @param x
     *            the x coordinate
     * @return the time corresponding to the x coordinate
     */
    long getTimeAtX(int x);
}
