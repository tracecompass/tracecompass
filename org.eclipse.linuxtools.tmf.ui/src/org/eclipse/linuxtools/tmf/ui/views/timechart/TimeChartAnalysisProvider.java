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

package org.eclipse.linuxtools.tmf.ui.views.timechart;

import java.util.Map;

import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class TimeChartAnalysisProvider extends TimeGraphProvider {

	private static final Color BOOKMARK_INNER_COLOR = new Color(Display.getDefault(), 115, 165, 224);
	private static final Color BOOKMARK_OUTER_COLOR = new Color(Display.getDefault(), 2, 70, 140);
	private static final Color SEARCH_MATCH_COLOR = new Color(Display.getDefault(), 177, 118, 14);
	
	private int lastX = Integer.MIN_VALUE;
	private int currX = Integer.MIN_VALUE;
	private int lastPriority;
	private int lastBookmarkX = Integer.MIN_VALUE;
	
    @Override
    public StateColor getEventColor(ITimeEvent event) {
        return StateColor.BLACK;
    }

    @Override
    public int getEventColorVal(ITimeEvent event) {
		int priority = ((TimeChartEvent) event).getColorSettingPriority();
		if (currX == lastX) {
			priority = Math.min(priority, lastPriority);
		}
		lastPriority = priority;
		return ColorSettingsManager.getColorSetting(priority).getTickColorIndex();
    }

	@Override
    public String getTraceClassName(ITimeGraphEntry entry) {
        return null;
    }

    @Override
    public String getEventName(ITimeEvent event, boolean upper, boolean extInfo) {
        return null;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        return null;
    }

    @Override
    public String getStateName(StateColor color) {
        return null;
    }

    @Override
    public void drawState(TimeGraphColorScheme colors, ITimeEvent event, Rectangle rect, GC gc, boolean selected, boolean rectBound, boolean timeSelected) {
	    if (! ((TimeChartEvent) event).isVisible()) {
	    	return;
	    }
    	lastX = currX;
    	currX = rect.x;
	    super.drawState(colors, event, rect, gc, selected, rectBound, timeSelected);
	    if (lastBookmarkX == rect.x || ((TimeChartEvent) event).isBookmarked()) {
	    	drawBookmark(rect, gc);
	    	lastBookmarkX = rect.x;
	    } else if (lastBookmarkX == rect.x - 1) {
	    	Rectangle r = new Rectangle(lastBookmarkX, rect.y, rect.width, rect.height);
	    	drawBookmark(r, gc);
	    } else {
	    	lastBookmarkX = Integer.MIN_VALUE;
	    }
	    if (((TimeChartEvent) event).isSearchMatch()) {
	    	drawSearchMatch(rect, gc);
	    }
    }

    private void drawBookmark(Rectangle r, GC gc) {
    	gc.setForeground(BOOKMARK_OUTER_COLOR);
    	gc.drawLine(r.x - 1, r.y - 2, r.x - 1, r.y + 2);
    	gc.drawLine(r.x + 1, r.y - 2, r.x + 1, r.y + 2);
    	gc.drawPoint(r.x, r.y - 2);
    	gc.setForeground(BOOKMARK_INNER_COLOR);
    	gc.drawLine(r.x, r.y - 1, r.x, r.y + 1);
    	gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
    	gc.drawPoint(r.x - 1, r.y + 3);
    	gc.drawPoint(r.x, r.y + 2);
    	gc.drawPoint(r.x + 1, r.y + 3);
    }
    
    private void drawSearchMatch(Rectangle r, GC gc) {
    	gc.setForeground(SEARCH_MATCH_COLOR);
    	gc.drawPoint(r.x, r.y + r.height);
    	gc.drawLine(r.x - 1, r.y + r.height + 1, r.x + 1, r.y + r.height + 1);
    	gc.drawLine(r.x - 2, r.y + r.height + 2, r.x + 2, r.y + r.height + 2);
    }
}
