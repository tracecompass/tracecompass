/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * <p>
 * <em>Time event styles</em>, this is for reference purposes. Many values will
 * be unsupported.
 * </p>
 * <p>
 * Special care is needed when populating the map as it is untyped. The API is
 * as follows
 * </p>
 * <ul>
 * <li>{@link #label()} a <em>String</em> to show in the legend</li>
 * <li>{@link #fillStyle()} can be {@link #solidColorFillStyle()},
 * {@link #gradientColorFillStyle()} or {@link #hatchPatternFillStyle()}.</li>
 * <li>{@link #heightFactor()} a <em>Float</em> between 0 and 1.0f</li>
 * <li>{@link #fillColor()} an <em>integer</em> encoding RGBA over 4 bytes (1
 * byte red, 1 byte green, 1 byte blue, 1 byte alpha)</li>
 * <li>{@link #fillColorEnd()} an <em>integer</em> encoding RGBA over 4 bytes (1
 * byte red, 1 byte green, 1 byte blue, 1 byte alpha)</li>
 * <li>{@link #borderColor()} an <em>integer</em> encoding RGBA over 4 bytes (1
 * byte red, 1 byte green, 1 byte blue, 1 byte alpha)</li>
 * <li>{@link #borderEnable()} a <em>boolean</em></li>
 * <li>{@link #borderThickness()} an <em>integer</em></li>
 * </ul>
 *
 * @author Matthew Khouzam
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.0
 */
@NonNullByDefault
public interface ITimeEventStyleStrings {

    /**
     * The label to display in the legend
     *
     * @return the key to get the value
     */
    static String label() {
        return ".label"; //$NON-NLS-1$
    }

    /**
     * Height factor, can be between 0.0 and 1.0f.
     *
     * @return the key to get the value
     */
    static String heightFactor() {
        return ".height.factor"; //$NON-NLS-1$
    }

    /**
     * Fill style, can be {@link #solidColorFillStyle()},
     * {@link #gradientColorFillStyle()} or {@link #hatchPatternFillStyle()}
     *
     * @return the key to get the value
     */
    static String fillStyle() {
        return ".fill";//$NON-NLS-1$
    }

    /**
     * Color fill style, this is a solid color, so it should make an event that
     * is uniformly filled with a color. The color is defined in the
     * {@link #fillColor()} parameter.
     *
     * @see #fillStyle()
     *
     * @return the color fill style
     */
    static String solidColorFillStyle() {
        return "color"; //$NON-NLS-1$
    }

    /**
     * Color fill style, this is a gradient color, it should make an event that
     * transitions from {@link #fillColor()} to {@link #fillColorEnd()}.
     *
     * @see #fillStyle()
     *
     * @return the color fill style
     */
    static String gradientColorFillStyle() {
        return "gradient"; //$NON-NLS-1$
    }

    /**
     * Color fill style, this is a hatch pattern, it should make an event that
     * has a hatch pattern with {@link #fillColor()} and
     * {@link #fillColorEnd()}.
     *
     * @see #fillStyle()
     * @return the color fill style
     */
    static String hatchPatternFillStyle() {
        return "hatch"; //$NON-NLS-1$
    }

    /**
     * Fill color, used in all styles except for image.
     *
     * @return the key to get the value
     */
    static String fillColor() {
        return ".fill.color";//$NON-NLS-1$
    }

    /**
     * Second fill color, used in gradients
     *
     * @return the key to get the value
     */
    static String fillColorEnd() {
        return ".fill.color_end";//$NON-NLS-1$
    }

    /**
     * Shadow the time event
     *
     * @return the key to get the value
     */
    static String shadowEnabled() {
        return ".shadow.enable";//$NON-NLS-1$
    }

    /**
     * Border
     *
     * @return the key to get the value
     */
    static String borderEnable() {
        return ".border.enable";//$NON-NLS-1$
    }

    /**
     * Border thickness
     *
     * @return the key to get the value
     */
    static String borderThickness() {
        return ".border.weight";//$NON-NLS-1$
    }

    /**
     * Border color
     *
     * @return the key to get the value
     */
    static String borderColor() {
        return ".border.color";//$NON-NLS-1$
    }

    /**
     * Item property. Possible values are {@link ITimeEventStyleStrings#stateType()}
     * or {@link ITimeEventStyleStrings#linkType()}
     *
     * @return The key to get the item property of a state item
     * @since 3.4
     */
    static String itemTypeProperty() {
        return ".type"; //$NON-NLS-1$
    }

    /**
     * Indicate that the item type is a STATE
     *
     * @return The state item type value
     * @since 3.4
     */
    static String stateType() {
        return ".type.state"; //$NON-NLS-1$
    }

    /**
     * Indicate that the item type is a LINK
     *
     * @return The link item type value
     * @since 3.4
     */
    static String linkType() {
        return ".type.link"; //$NON-NLS-1$
    }
}
