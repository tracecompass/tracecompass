/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.presentation;

/**
 * This is an interface responsible to retrieve presentation information for XY
 * charts. XY Viewers must use this provider, in order to apply style and color
 * to XY models computed by data providers.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface IXYPresentationProvider {

    /**
     * Returns the {@link IYAppearance} to which the specified series name is
     * mapped. If no appearance is found, a new one will be created with given
     * paramters and added to this presentation provider
     *
     * @param seriesName
     *            The name of the series
     * @param type
     *            The series type
     * @param width
     *            The series width
     * @return The {@link IYAppearance} instance of the Y series.
     */
    IYAppearance getAppearance(String seriesName, String type, int width);

    /**
     * Remove all {@link IYAppearance}
     */
    void clear();
}
