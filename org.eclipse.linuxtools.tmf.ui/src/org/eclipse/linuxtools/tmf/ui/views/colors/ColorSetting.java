/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.colors;

import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.ColorUtil;

/**
 * Application code must explicitly invoke the ColorSetting.dispose() method to release the operating system
 * resources managed by each instance when those instances are no longer required. 
 */

public class ColorSetting {

	private RGB fForegroundRGB;
	private RGB fBackgroundRGB;
	private Color fForegroundColor;
	private Color fBackgroundColor;
	private Color fDimmedForegroundColor;
	private Color fDimmedBackgroundColor;
	private int fTickColorIndex;
	private ITmfFilterTreeNode fFilter;
	
	/**
	 * You must dispose the color setting when it is no longer required.
	 */
	public ColorSetting(RGB foreground, RGB background, int tickColorIndex, ITmfFilterTreeNode filter) {
		fForegroundRGB = foreground;
		fBackgroundRGB = background;
		fTickColorIndex = tickColorIndex;
		fFilter = filter;
		Display display = Display.getDefault();
		fForegroundColor = new Color(display, fForegroundRGB);
		fBackgroundColor = new Color(display, fBackgroundRGB);
		fDimmedForegroundColor = new Color(display, ColorUtil.blend(
				fForegroundRGB, fBackgroundRGB));
		fDimmedBackgroundColor = new Color(display, ColorUtil.blend(
				fBackgroundRGB, display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB()));
	}
	
	/**
	 * Dispose the color setting resources
	 */
	public void dispose() {
		fForegroundColor.dispose();
		fBackgroundColor.dispose();
		fDimmedForegroundColor.dispose();
		fDimmedBackgroundColor.dispose();
	}
	
	/**
	 * @return the foreground
	 */
	public RGB getForegroundRGB() {
		return fForegroundRGB;
	}
	
	/**
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
	 * @return the background
	 */
	public RGB getBackgroundRGB() {
		return fBackgroundRGB;
	}
	
	/**
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
	 * @return the tick color index (0-15)
	 * @see org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.TraceColorScheme
	 */
	public int getTickColorIndex() {
		return fTickColorIndex;
	}
	
	/**
	 * @param tickColorIndex the tick color index to set (0-15)
	 * @see org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.TraceColorScheme
	 */
	public void setTickColorIndex(int tickColorIndex) {
		fTickColorIndex = tickColorIndex;
	}
	
	/**
	 * @return the filter
	 */
	public ITmfFilterTreeNode getFilter() {
		return fFilter;
	}
	
	/**
	 * @param filter the filter to set
	 */
	public void setFilter(ITmfFilterTreeNode filter) {
		fFilter = filter;
	}

	/**
	 * @return the foreground color
	 */
	public Color getForegroundColor() {
		return fForegroundColor;
	}

	/**
	 * @return the background color
	 */
	public Color getBackgroundColor() {
		return fBackgroundColor;
	}

	/**
	 * @return the dimmed foreground color
	 */
	public Color getDimmedForegroundColor() {
		return fDimmedForegroundColor;
	}

	/**
	 * @return the dimmed background color
	 */
	public Color getDimmedBackgroundColor() {
		return fDimmedBackgroundColor;
	}
	
}
