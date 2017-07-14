/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.presentation;

import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.RGBColor;

/**
 * This is a base implementation of {@link IYAppearance}
 *
 * @author Yonni Chen
 * @since 3.1
 */
public class YAppearance implements IYAppearance {

    private final String fName;
    private final String fStyle;
    private final String fType;
    private final RGBColor fColor;

    /**
     * Constructor
     *
     * @param name
     *            The Y series name
     * @param type
     *            The Y series type
     * @param style
     *            The Y series style
     * @param color
     *            The Y series color
     */
    public YAppearance(String name, String type, String style, RGBColor color) {
        fName = name;
        fStyle = style;
        fType = type;
        fColor = color;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public String getStyle() {
        return fStyle;
    }

    @Override
    public RGBColor getColor() {
        return fColor;
    }

    @Override
    public String getType() {
        return fType;
    }
}
