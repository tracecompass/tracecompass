/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated to use RGB for the tick color
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.colors;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.ui.themes.ColorUtil;

/**
 * Class for storing color settings of a TMF filter.
 *
 * Application code must explicitly invoke the ColorSetting.dispose() method to
 * release the operating system resources managed by each instance when those
 * instances are no longer required.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class ColorSetting {

    private @Nullable RGB fForegroundRGB;
    private @Nullable RGB fBackgroundRGB;
    private @NonNull RGB fTickColorRGB;
    private @Nullable Color fForegroundColor;
    private @Nullable Color fBackgroundColor;
    private @Nullable Color fDimmedForegroundColor;
    private @Nullable Color fDimmedBackgroundColor;
    private @NonNull Color fTickColor;
    private @Nullable ITmfFilterTreeNode fFilter;

    /**
     * Constructor
     *
     * You must dispose the color setting when it is no longer required.
     *
     * @param foreground
     *            The foreground color, or null to use the default system color
     * @param background
     *            The background color, or null to use the default system color
     * @param tickColorRGB
     *            The color for the time graph ticks, or null to use the default system color
     * @param filter
     *            The filter tree node, or null
     */
    public ColorSetting(@Nullable RGB foreground, @Nullable RGB background, @Nullable RGB tickColorRGB, @Nullable ITmfFilterTreeNode filter) {
        fForegroundRGB = foreground;
        fBackgroundRGB = background;
        fTickColorRGB = (tickColorRGB != null) ? tickColorRGB : checkNotNull(Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB());
        fFilter = filter;
        Display display = Display.getDefault();
        fForegroundColor = (fForegroundRGB != null) ? new Color(display, fForegroundRGB) : null;
        fBackgroundColor = (fBackgroundRGB != null) ? new Color(display, fBackgroundRGB) : null;
        fDimmedForegroundColor = new Color(display, ColorUtil.blend(
                (fForegroundRGB != null) ? fForegroundRGB : display.getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB(),
                (fBackgroundRGB != null) ? fBackgroundRGB : display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB()));
        fDimmedBackgroundColor = (fBackgroundRGB == null) ? null : new Color(display, ColorUtil.blend(
                fBackgroundRGB, display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB()));
        fTickColor = new Color(display, fTickColorRGB);
    }

    /**
     * Dispose the color setting resources
     */
    public void dispose() {
        if (fForegroundColor != null) {
            fForegroundColor.dispose();
        }
        if (fBackgroundColor != null) {
            fBackgroundColor.dispose();
        }
        if (fDimmedForegroundColor != null) {
            fDimmedForegroundColor.dispose();
        }
        if (fDimmedBackgroundColor != null) {
            fDimmedBackgroundColor.dispose();
        }
        fTickColor.dispose();
    }

    /**
     * Returns foreground RGB value, or null if the default system color is
     * used.
     *
     * @return the foreground RGB, or null
     */
    public @Nullable RGB getForegroundRGB() {
        return fForegroundRGB;
    }

    /**
     * Sets the foreground RGB value. If the argument is null the default system
     * color will be used.
     *
     * @param foreground
     *            the foreground to set, or null
     */
    public void setForegroundRGB(@Nullable RGB foreground) {
        fForegroundRGB = foreground;
        if (fForegroundColor != null) {
            fForegroundColor.dispose();
        }
        if (fDimmedForegroundColor != null) {
            fDimmedForegroundColor.dispose();
        }
        Display display = Display.getDefault();
        fForegroundColor = (fForegroundRGB != null) ? new Color(display, fForegroundRGB) : null;
        fDimmedForegroundColor = new Color(display, ColorUtil.blend(
                (fForegroundRGB != null) ? fForegroundRGB : display.getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB(),
                (fBackgroundRGB != null) ? fBackgroundRGB : display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB()));
    }

    /**
     * Returns the background RGB value, or null if the default system color is
     * used.
     *
     * @return the background RGB, or null
     */
    public @Nullable RGB getBackgroundRGB() {
        return fBackgroundRGB;
    }

    /**
     * Sets the background RGB value. If the argument is null the default system
     * color will be used.
     *
     * @param background
     *            the background to set, or null
     */
    public void setBackgroundRGB(@Nullable RGB background) {
        fBackgroundRGB = background;
        if (fBackgroundColor != null) {
            fBackgroundColor.dispose();
        }
        if (fDimmedBackgroundColor != null) {
            fDimmedBackgroundColor.dispose();
        }
        if (fDimmedForegroundColor != null) {
            fDimmedForegroundColor.dispose();
        }
        Display display = Display.getDefault();
        fBackgroundColor = (fBackgroundRGB != null) ? new Color(display, fBackgroundRGB) : null;
        fDimmedBackgroundColor = (fBackgroundRGB == null) ? null : new Color(display, ColorUtil.blend(
                fBackgroundRGB, display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB()));
        fDimmedForegroundColor = new Color(display, ColorUtil.blend(
                (fForegroundRGB != null) ? fForegroundRGB : display.getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB(),
                (fBackgroundRGB != null) ? fBackgroundRGB : display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB()));
    }

    /**
     * Returns the RGB of the tick color
     *
     * @return the RGB of the tick color
     */
    public @NonNull RGB getTickColorRGB() {
        return fTickColorRGB;
    }

    /**
     * Sets the RGB of the tick color
     *
     * @param tickColorRGB
     *            the tick color TGB
     */
    public void setTickColorRGB(@NonNull RGB tickColorRGB) {
        fTickColorRGB = tickColorRGB;
        fTickColor.dispose();
        Display display = Display.getDefault();
        fTickColor = new Color(display, fTickColorRGB);
    }

    /**
     * Returns the filter implementation.
     *
     * @return the filter, or null
     */
    public @Nullable ITmfFilterTreeNode getFilter() {
        return fFilter;
    }

    /**
     * Sets the filter implementation.
     *
     * @param filter
     *            the filter to set, or null
     */
    public void setFilter(@Nullable ITmfFilterTreeNode filter) {
        fFilter = filter;
    }

    /**
     * Returns the foreground color, or null if the default system color is
     * used.
     *
     * @return the foreground color, or null
     */
    public @Nullable Color getForegroundColor() {
        return fForegroundColor;
    }

    /**
     * Returns the background color, or null if the default system color is
     * used.
     *
     * @return the background color, or null
     */
    public @Nullable Color getBackgroundColor() {
        return fBackgroundColor;
    }

    /**
     * Returns the dimmed foreground color, or null if the default system color
     * is used.
     *
     * @return the dimmed foreground color, or null
     */
    public @Nullable Color getDimmedForegroundColor() {
        return fDimmedForegroundColor;
    }

    /**
     * Returns the dimmed background color, or null if the default system color
     * is used.
     *
     * @return the dimmed background color, or null
     */
    public @Nullable Color getDimmedBackgroundColor() {
        return fDimmedBackgroundColor;
    }

    /**
     * Returns the tick color.
     *
     * @return the tick color
     */
    public Color getTickColor() {
        return fTickColor;
    }
}
