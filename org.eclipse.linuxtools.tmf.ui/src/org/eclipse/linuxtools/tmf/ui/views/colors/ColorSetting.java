/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated to use RGB for the tick color
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.colors;

import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.ColorUtil;

/**
 * Class for storing color settings of a TMF filter.
 *
 * Application code must explicitly invoke the ColorSetting.dispose() method to release the operating system
 * resources managed by each instance when those instances are no longer required.
 *
 *  @version 1.0
 *  @author Patrick Tasse
 */
public class ColorSetting {

    private RGB fForegroundRGB;
    private RGB fBackgroundRGB;
    private RGB fTickColorRGB;
    private Color fForegroundColor;
    private Color fBackgroundColor;
    private Color fDimmedForegroundColor;
    private Color fDimmedBackgroundColor;
    private Color fTickColor;
    private ITmfFilterTreeNode fFilter;

        /**
     * Constructor
     *
     * You must dispose the color setting when it is no longer required.
     *
     * @param foreground
     *            The foreground color
     * @param background
     *            The background color
     * @param tickColorRGB
     *            The color for the checkbox ticks
     * @param filter
     *            The filter tree node
     */
    public ColorSetting(RGB foreground, RGB background, RGB tickColorRGB, ITmfFilterTreeNode filter) {
        fForegroundRGB = foreground;
        fBackgroundRGB = background;
        fTickColorRGB = tickColorRGB;
        fFilter = filter;
        Display display = Display.getDefault();
        fForegroundColor = new Color(display, fForegroundRGB);
        fBackgroundColor = new Color(display, fBackgroundRGB);
        fDimmedForegroundColor = new Color(display, ColorUtil.blend(
                fForegroundRGB, fBackgroundRGB));
        fDimmedBackgroundColor = new Color(display, ColorUtil.blend(
                fBackgroundRGB, display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB()));
        fTickColor = new Color(display, fTickColorRGB);
    }

    /**
     * Dispose the color setting resources
     */
    public void dispose() {
        fForegroundColor.dispose();
        fBackgroundColor.dispose();
        fDimmedForegroundColor.dispose();
        fDimmedBackgroundColor.dispose();
        fTickColor.dispose();
    }

    /**
     * Returns foreground RGB value.
     *
     * @return the foreground RGB
     */
    public RGB getForegroundRGB() {
        return fForegroundRGB;
    }

    /**
     * Sets the foreground RGB value
     *
     * @param foreground the foreground to set
     */
    public void setForegroundRGB(RGB foreground) {
        fForegroundRGB = foreground;
        fForegroundColor.dispose();
        fDimmedForegroundColor.dispose();
        Display display = Display.getDefault();
        fForegroundColor = new Color(display, fForegroundRGB);
        fDimmedForegroundColor = new Color(display, ColorUtil.blend(
                fForegroundRGB, fBackgroundRGB));
    }

    /**
     * Returns the background RGB value.
     *
     * @return the background RGB
     */
    public RGB getBackgroundRGB() {
        return fBackgroundRGB;
    }

    /**
     * Sets the background RGB value.
     *
     * @param background the background to set
     */
    public void setBackgroundRGB(RGB background) {
        fBackgroundRGB = background;
        fBackgroundColor.dispose();
        fDimmedBackgroundColor.dispose();
        Display display = Display.getDefault();
        fBackgroundColor = new Color(display, fBackgroundRGB);
        fDimmedBackgroundColor = new Color(display, ColorUtil.blend(
                fBackgroundRGB, display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB()));
    }

    /**
     * Returns the RGB of the tick color
     *
     * @return the RGB of the tick color
     */
    public RGB getTickColorRGB() {
        return fTickColorRGB;
    }

    /**
     * Sets the RGB of the tick color
     *
     * @param tickColorRGB the tick color TGB
     */
    public void setTickColorRGB(RGB tickColorRGB) {
           fTickColorRGB = tickColorRGB;
           fTickColor.dispose();
           Display display = Display.getDefault();
           fTickColor = new Color(display, fTickColorRGB);
    }

    /**
     * Returns the filter implementation.
     * @return the filter
     */
    public ITmfFilterTreeNode getFilter() {
        return fFilter;
    }

    /**
     * Sets the filter implementation.
     *
     * @param filter the filter to set
     */
    public void setFilter(ITmfFilterTreeNode filter) {
        fFilter = filter;
    }

    /**
     * Returns the foreground color.
     *
     * @return the foreground color
     */
    public Color getForegroundColor() {
        return fForegroundColor;
    }

    /**
     * Returns the background color.
     *
     * @return the background color
     */
    public Color getBackgroundColor() {
        return fBackgroundColor;
    }

    /**
     * Returns the dimmed foreground color.
     *
     * @return the dimmed foreground color
     */
    public Color getDimmedForegroundColor() {
        return fDimmedForegroundColor;
    }

    /**
     * Returns the dimmed background color.
     *
     * @return the dimmed background color
     */
    public Color getDimmedBackgroundColor() {
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
