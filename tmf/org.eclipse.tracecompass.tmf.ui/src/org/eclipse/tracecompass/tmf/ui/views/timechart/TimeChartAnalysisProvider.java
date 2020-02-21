/*******************************************************************************
 * Copyright (c) 2010, 2017 Ericsson
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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timechart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorSetting;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.tracecompass.tmf.ui.views.colors.IColorSettingsListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;

/**
 * Provider for a time chart analysis view
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeChartAnalysisProvider extends TimeGraphPresentationProvider implements IColorSettingsListener {

    private static final Color BOOKMARK_INNER_COLOR = new Color(Display.getDefault(), 115, 165, 224);
    private static final Color BOOKMARK_OUTER_COLOR = new Color(Display.getDefault(), 2, 70, 140);
    private static final Color SEARCH_MATCH_COLOR = new Color(Display.getDefault(), 177, 118, 14);

    private StateItem[] fStateTable;

    private int lastBookmarkX = Integer.MIN_VALUE;

    /**
     * Constructor
     */
    public TimeChartAnalysisProvider() {
        colorSettingsChanged(ColorSettingsManager.getColorSettings());
    }

    @Override
    public StateItem[] getStateTable() {
        return fStateTable;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (! ((TimeChartEvent) event).isVisible()) {
            return ITimeGraphPresentationProvider.INVISIBLE;
        }
        return ((TimeChartEvent) event).getColorSettingPriority();
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle rect, GC gc) {
        if (! ((TimeChartEvent) event).isVisible()) {
            return;
        }
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

    private static void drawBookmark(Rectangle r, GC gc) {
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

    private static void drawSearchMatch(Rectangle r, GC gc) {
        gc.setForeground(SEARCH_MATCH_COLOR);
        gc.drawPoint(r.x, r.y + r.height);
        gc.drawLine(r.x - 1, r.y + r.height + 1, r.x + 1, r.y + r.height + 1);
        gc.drawLine(r.x - 2, r.y + r.height + 2, r.x + 2, r.y + r.height + 2);
    }

    @Override
    public void colorSettingsChanged(ColorSetting[] settings) {
        StateItem[] stateItems = new StateItem[settings.length];
        for (int i = 0; i < settings.length; i++) {
            stateItems[i] = new StateItem(settings[i].getTickColorRGB());
        }
        fStateTable = stateItems;
    }
}
