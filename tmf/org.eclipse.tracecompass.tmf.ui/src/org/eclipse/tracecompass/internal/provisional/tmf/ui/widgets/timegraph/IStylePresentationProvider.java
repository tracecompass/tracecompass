/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.model.StyleManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;

/**
 * Interface for time graph presentation providers that support styles.
 *
 * @author Patrick Tasse
 */
@NonNullByDefault
public interface IStylePresentationProvider {

    /**
     * Get the element style for the specified event.
     *
     * @param event
     *            the event
     * @return the element style, or null to ignore event
     */
    @Nullable OutputElementStyle getElementStyle(ITimeEvent event);

    /**
     * Get the style manager for this presentation provider.
     *
     * @return the style manager
     */
    StyleManager getStyleManager();

    /**
     * Get the style property value for the specified element style. The style
     * hierarchy is traversed until a value is found.
     *
     * @param elementStyle
     *            the style
     * @param property
     *            the style property
     * @return the style value, or null
     */
    @Nullable Object getStyle(OutputElementStyle elementStyle, String property);

    /**
     * Get the style property float value for the specified element style. The
     * style hierarchy is completely traversed, and the returned value is the
     * multiplication of every float value that is found.
     *
     * @param elementStyle
     *            the style
     * @param property
     *            the style property
     * @return the style float value, or null
     */
    @Nullable Float getFloatStyle(OutputElementStyle elementStyle, String property);

    /**
     * Get the style property color value for the specified element style. The
     * style hierarchy is completely traversed, and the returned value is the
     * blended color of every color value that is found.
     *
     * @param elementStyle
     *            the style
     * @param property
     *            the style property
     * @return the style value, or null
     */
    @Nullable RGBAColor getColorStyle(OutputElementStyle elementStyle, String property);

}
