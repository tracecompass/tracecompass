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

import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.ColorUtil;

public class TimeChartAnalysisProvider extends TimeGraphPresentationProvider {

    private static final Color BOOKMARK_INNER_COLOR = new Color(Display.getDefault(), 115, 165, 224);
    private static final Color BOOKMARK_OUTER_COLOR = new Color(Display.getDefault(), 2, 70, 140);
    private static final Color SEARCH_MATCH_COLOR = new Color(Display.getDefault(), 177, 118, 14);

    private int lastX = Integer.MIN_VALUE;
    private int currX = Integer.MIN_VALUE;
    private int lastPriority;
    private int lastBookmarkX = Integer.MIN_VALUE;

    @Override
    public StateItem[] getStateTable() {
        return new StateItem[] {
                new StateItem(new RGB(100, 100, 100)), 
                new StateItem(new RGB(174, 200, 124)),
                new StateItem(ColorUtil.blend(Display.getDefault().getSystemColor(SWT.COLOR_BLUE).getRGB(), Display.getDefault().getSystemColor(SWT.COLOR_GRAY).getRGB())),
                new StateItem(new RGB(210, 150, 60)),
                new StateItem(new RGB(242, 225, 168)),
                new StateItem(ColorUtil.blend(Display.getDefault().getSystemColor(SWT.COLOR_RED).getRGB(), Display.getDefault().getSystemColor(SWT.COLOR_GRAY).getRGB())),
                new StateItem(new RGB(200, 200, 200)),
                new StateItem(new RGB(35, 107, 42)),
                new StateItem(new RGB(205,205,0)),
                new StateItem(new RGB(205, 0, 205)),
                new StateItem(new RGB(171, 130, 255)),
                new StateItem(new RGB(255, 181, 197)),
                new StateItem(new RGB(112, 219, 147)),
                new StateItem(new RGB(198, 226, 255)),
                new StateItem(new RGB(95, 158, 160)),
                new StateItem(new RGB(107, 142, 35))
        };
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        int priority = ((TimeChartEvent) event).getColorSettingPriority();
        if (currX == lastX) {
            priority = Math.min(priority, lastPriority);
        }
        lastPriority = priority;
        return ColorSettingsManager.getColorSetting(priority).getTickColorIndex();
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle rect, GC gc) {
        if (! ((TimeChartEvent) event).isVisible()) {
            return;
        }
        lastX = currX;
        currX = rect.x;
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
