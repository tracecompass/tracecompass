/*****************************************************************************
 * Copyright (c) 2008, 2012 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Ruslan A. Scherbakov, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon - Updated for TMF
 *   Patrick Tasse - Refactoring
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Color theme used by the timegraph view
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TimeGraphColorScheme {

    // elements color indices
    public static final int BLACK_STATE = 0;
    public static final int GREEN_STATE = 1;
    public static final int DARK_BLUE_STATE = 2;
    public static final int ORANGE_STATE = 3;
    public static final int GOLD_STATE = 4;
    public static final int RED_STATE = 5;
    public static final int GRAY_STATE = 6;
    public static final int DARK_GREEN_STATE = 7;
    public static final int DARK_YELLOW_STATE = 8;
    public static final int MAGENTA3_STATE = 9;
    public static final int PURPLE1_STATE = 10;
    public static final int PINK1_STATE = 11;
    public static final int AQUAMARINE_STATE = 12;
    public static final int LIGHT_BLUE_STATE = 13;
    public static final int CADET_BLUE_STATE = 14;
    public static final int OLIVE_STATE = 15;

    public static final int STATES0 = 0;
    public static final int STATES1 = 15;

    // selected state elements color indices
    public static final int BLACK_STATE_SEL = 16;
    public static final int GREEN_STATE_SEL = 17;
    public static final int DARK_BLUE_STATE_SEL = 18;
    public static final int ORANGE_STATE_SEL = 19;
    public static final int GOLD_STATE_SEL = 20;
    public static final int RED_STATE_SEL = 21;
    public static final int GRAY_STATE_SEL = 22;
    public static final int DARK_GREEN_STATE_SEL = 23;
    public static final int DARK_YELLOW_STATE_SEL = 24;
    public static final int MAGENTA3_STATE_SEL = 25;
    public static final int PURPLE1_STATE_SEL = 26;
    public static final int PINK1_STATE_SEL = 27;
    public static final int AQUAMARINE_STATE_SEL = 28;
    public static final int LIGHT_BLUE_STATE_SEL = 29;
    public static final int CADET_BLUE_STATE_SEL = 30;
    public static final int OLIVE_STATE_SEL = 31;

    public static final int STATES_SEL0 = 16;
    public static final int STATES_SEL1 = 31;

    // colors indices for viewer controls
    public static final int BACKGROUND = 32;
    public static final int FOREGROUND = 33;
    public static final int BACKGROUND_SEL = 34;
    public static final int FOREGROUND_SEL = 35;
    public static final int BACKGROUND_SEL_NOFOCUS = 36;
    public static final int FOREGROUND_SEL_NOFOCUS = 37;
    public static final int TOOL_BACKGROUND = 38;
    public static final int TOOL_FOREGROUND = 39;

    // misc colors
    public static final int FIX_COLOR = 40;
    public static final int WHITE = 41;
    public static final int GRAY = 42;
    public static final int BLACK = 43;
    public static final int DARK_GRAY = 44;

    // selected border color indices
    public static final int BLACK_BORDER = 45;
    public static final int GREEN_BORDER = 46;
    public static final int DARK_BLUE_BORDER = 47;
    public static final int ORANGE_BORDER = 48;
    public static final int GOLD_BORDER = 49;
    public static final int RED_BORDER = 50;
    public static final int GRAY_BORDER = 51;
    public static final int DARK_GREEN_BORDER1 = 52;
    public static final int DARK_YELLOW_BORDER1 = 53;
    public static final int MAGENTA3_BORDER1 = 54;
    public static final int PURPLE1_BORDER1 = 55;
    public static final int PINK1_BORDER1 = 56;
    public static final int AQUAMARINE_BORDER1 = 57;
    public static final int LIGHT_BLUE_BORDER1 = 58;
    public static final int CADET_BLUE_STATE_BORDER = 59;
    public static final int OLIVE_BORDER2 = 60;

    public static final int STATES_BORDER0 = 45;
    public static final int STATES_BORDER1 = 60;

    public static final int MID_LINE = 61;
    public static final int RED = 62;
    public static final int GREEN = 63;
    public static final int BLUE = 64;
    public static final int YELLOW = 65;
    public static final int CYAN = 66;
    public static final int MAGENTA = 67;

    public static final int SELECTED_TIME = 68;
    public static final int LEGEND_BACKGROUND = 69;
    public static final int LEGEND_FOREGROUND = 70;

    // group items' colors
    public static final int GR_BACKGROUND = 71;
    public static final int GR_FOREGROUND = 72;
    public static final int GR_BACKGROUND_SEL = 73;
    public static final int GR_FOREGROUND_SEL = 74;
    public static final int GR_BACKGROUND_SEL_NOFOCUS = 75;
    public static final int GR_FOREGROUND_SEL_NOFOCUS = 76;

    public static final int LIGHT_LINE = 77;
    public static final int BACKGROUND_NAME = 78;
    public static final int BACKGROUND_NAME_SEL = 79;
    public static final int BACKGROUND_NAME_SEL_NOFOCUS = 80;

    // Interraction's colors
    public static final int TI_START_THREAD = BLACK;
    public static final int TI_HANDOFF_LOCK = BLUE;
    public static final int TI_NOTIFY_ALL = GREEN;
    public static final int TI_NOTIFY = GREEN;
    public static final int TI_NOTIFY_JOINED = DARK_GRAY;
    public static final int TI_INTERRUPT = RED;
    public static final int TI_WAIT_EXCEEDED = BLUE;

    interface IColorProvider {
        Color get();
    }

    static class SysCol implements IColorProvider {
        private int syscol;

        SysCol(int syscol) {
            this.syscol = syscol;
        }

        @Override
        public Color get() {
            return Utils.getSysColor(syscol);
        }
    }

    static class RGB implements IColorProvider {
        private int r;
        private int g;
        private int b;

        RGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public Color get() {
            return new Color(null, r, g, b);
        }
    }

    static class Mix implements IColorProvider {
        private IColorProvider cp1;
        private IColorProvider cp2;
        private int w1;
        private int w2;

        Mix(IColorProvider cp1, IColorProvider cp2, int w1, int w2) {
            this.cp1 = cp1;
            this.cp2 = cp2;
            this.w1 = w1;
            this.w2 = w2;
        }

        Mix(IColorProvider cp1, IColorProvider cp2) {
            this.cp1 = cp1;
            this.cp2 = cp2;
            this.w1 = 1;
            this.w2 = 1;
        }

        @Override
        public Color get() {
            Color col1 = cp1.get();
            Color col2 = cp2.get();
            return Utils.mixColors(col1, col2, w1, w2);
        }
    }

    private static final IColorProvider PROVIDERS_MAP[] = {
        //
        new RGB(100, 100, 100), // UNKNOWN
        new RGB(174, 200, 124), // RUNNING
        new Mix(new SysCol(SWT.COLOR_BLUE), new SysCol(SWT.COLOR_GRAY), 1, 3), // SLEEPING
        new RGB(210, 150, 60), // WAITING
        new RGB(242, 225, 168), // BLOCKED
        new Mix(new SysCol(SWT.COLOR_RED), new SysCol(SWT.COLOR_GRAY), 1, 3), // DEADLOCK
        new RGB(200, 200, 200), // STOPPED
        new RGB(35, 107, 42), // STEEL BLUE
        new RGB(205,205,0), // DARK YELLOW
        new RGB(205, 0, 205), // MAGENTA
        new RGB(171, 130, 255), // PURPLE
        new RGB(255, 181, 197), // PINK
        new RGB(112, 219, 147), // AQUAMARINE
        new RGB(198, 226, 255), // SLATEGRAY
        new RGB(95, 158, 160), // CADET BLUE
        new RGB(107, 142, 35), // OLIVE


        //TODO: Does not seem to be used, check during clean-up
        new SysCol(SWT.COLOR_WHITE), // UNKNOWN_SEL
        new SysCol(SWT.COLOR_GREEN), // RUNNING_SEL
        new SysCol(SWT.COLOR_BLUE), // SLEEPING_SEL
        new SysCol(SWT.COLOR_CYAN), // WAITING_SEL
        new SysCol(SWT.COLOR_YELLOW), // BLOCKED_SEL
        new SysCol(SWT.COLOR_RED), // DEADLOCK_SEL
        new SysCol(SWT.COLOR_DARK_GRAY), // STOPPED_SEL
        new SysCol(SWT.COLOR_WHITE),
        new SysCol(SWT.COLOR_GREEN),
        new SysCol(SWT.COLOR_BLUE),
        new SysCol(SWT.COLOR_CYAN),
        new SysCol(SWT.COLOR_YELLOW),
        new SysCol(SWT.COLOR_RED),
        new SysCol(SWT.COLOR_DARK_GRAY),
        new SysCol(SWT.COLOR_WHITE),
        new SysCol(SWT.COLOR_GREEN),


        new SysCol(SWT.COLOR_LIST_BACKGROUND), // BACKGROUND
        new SysCol(SWT.COLOR_LIST_FOREGROUND), // FOREGROUND
        new RGB(232, 242, 254), // BACKGROUND_SEL
        new SysCol(SWT.COLOR_LIST_FOREGROUND), // FOREGROUND_SEL
        new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // BACKGROUND_SEL_NOFOCUS
        new SysCol(SWT.COLOR_WIDGET_FOREGROUND), // FOREGROUND_SEL_NOFOCUS
        new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // TOOL_BACKGROUND
        new SysCol(SWT.COLOR_WIDGET_DARK_SHADOW), // TOOL_FOREGROUND

        new SysCol(SWT.COLOR_GRAY), // FIX_COLOR
        new SysCol(SWT.COLOR_WHITE), // WHITE
        new SysCol(SWT.COLOR_GRAY), // GRAY
        new SysCol(SWT.COLOR_BLACK), // BLACK
        new SysCol(SWT.COLOR_DARK_GRAY), // DARK_GRAY

        new SysCol(SWT.COLOR_DARK_GRAY), // BLACK_BORDER
        new RGB(75, 115, 120), // GREEN_BORDER
        new SysCol(SWT.COLOR_DARK_BLUE), // DARK_BLUE_BORDER
        new RGB(242, 225, 168), // ORANGE_BORDER
        new RGB(210, 150, 60), // GOLD_BORDER
        new SysCol(SWT.COLOR_DARK_RED), // RED_BORDER
        new SysCol(SWT.COLOR_BLACK), // GRAY_BORDER
        new SysCol(SWT.COLOR_DARK_GRAY), // DARK_GREEN_BORDER
        new RGB(75, 115, 120), // DARK_YELLOW_BORDER
        new SysCol(SWT.COLOR_DARK_BLUE), // MAGENTA3_BORDER
        new RGB(242, 225, 168), // PURPLE1_BORDER
        new RGB(210, 150, 60), // PINK1_BORDER
        new SysCol(SWT.COLOR_DARK_RED), // AQUAMARINE_BORDER
        new SysCol(SWT.COLOR_BLACK), // LIGHT_BLUE_BORDER
        new SysCol(SWT.COLOR_DARK_GRAY), // BLUE_BORDER
        new RGB(75, 115, 120), // OLIVE_BORDER


        new SysCol(SWT.COLOR_GRAY), // MID_LINE
        new SysCol(SWT.COLOR_RED), // RED
        new SysCol(SWT.COLOR_GREEN), // GREEN
        new SysCol(SWT.COLOR_BLUE), // BLUE
        new SysCol(SWT.COLOR_YELLOW), // YELLOW
        new SysCol(SWT.COLOR_CYAN), // CYAN
        new SysCol(SWT.COLOR_MAGENTA), // MAGENTA

        new SysCol(SWT.COLOR_BLUE), // SELECTED_TIME
        new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // LEGEND_BACKGROUND
        new SysCol(SWT.COLOR_WIDGET_DARK_SHADOW), // LEGEND_FOREGROUND

        new Mix(new RGB(150, 200, 240), new SysCol(SWT.COLOR_LIST_BACKGROUND)),     // GR_BACKGROUND
        new RGB(0, 0, 50),                                                          // GR_FOREGROUND
        new Mix(new RGB(150, 200, 240), new SysCol(SWT.COLOR_WHITE), 6, 1),         // GR_BACKGROUND_SEL
        new RGB(0, 0, 50),                                                          // GR_FOREGROUND_SEL
        new Mix(new RGB(150, 200, 240), new SysCol(SWT.COLOR_WHITE), 6, 1),         // GR_BACKGROUND_SEL_NOFOCUS
        new RGB(0, 0, 50),                                                          // GR_FOREGROUND_SEL_NOFOCUS

        new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(SWT.COLOR_LIST_BACKGROUND), 1, 3), // LIGHT_LINE

        new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(SWT.COLOR_LIST_BACKGROUND), 1, 6),   // BACKGROUND_NAME
        new Mix(new SysCol(SWT.COLOR_GRAY), new RGB(232, 242, 254), 1, 6),                  // BACKGROUND_NAME_SEL
        new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(SWT.COLOR_WIDGET_BACKGROUND), 1, 6), // BACKGROUND_NAME_SEL_NOFOCUS
    };

    private final Color fColors[];

    /**
     * Default constructor
     */
    public TimeGraphColorScheme() {
        fColors = new Color[PROVIDERS_MAP.length];
    }

    /**
     * Dispose this color scheme
     */
    public void dispose() {
        for (int i = 0; i < fColors.length; i++) {
            Utils.dispose(fColors[i]);
            fColors[i] = null;
        }
    }

    /**
     * Get the color matching the given index
     *
     * @param idx
     *            The index
     * @return The matching color
     */
    public Color getColor(int idx) {
        if (null == fColors[idx]) {
            if (idx >= STATES_SEL0 && idx <= STATES_SEL1) {
                Color col1 = getColor(idx - STATES_SEL0);
                Color col2 = getColor(BACKGROUND_SEL);
                fColors[idx] = Utils.mixColors(col1, col2, 3, 1);
            } else {
                fColors[idx] = PROVIDERS_MAP[idx].get();
            }
        }
        return fColors[idx];
    }

    /**
     * Get an entry's background color based on its status.
     *
     * @param selected
     *            If the entry is selected
     * @param focused
     *            If the entry is focused
     * @param name
     *            Get the color of the name column (false for other columns)
     * @return The matching color
     */
    public Color getBkColor(boolean selected, boolean focused, boolean name) {
        if (name) {
            if (selected && focused) {
                return getColor(BACKGROUND_NAME_SEL);
            }
            if (selected) {
                return getColor(BACKGROUND_NAME_SEL_NOFOCUS);
            }
            return getColor(BACKGROUND_NAME);
        }
        if (selected && focused) {
            return getColor(BACKGROUND_SEL);
        }
        if (selected) {
            return getColor(BACKGROUND_SEL_NOFOCUS);
        }
        return getColor(BACKGROUND);
    }

    /**
     * Get the correct foreground color
     *
     * @param selected
     *            Is the entry selected
     * @param focused
     *            Is the entry focused
     * @return The matching color
     */
    public Color getFgColor(boolean selected, boolean focused) {
        if (selected && focused) {
            return getColor(FOREGROUND_SEL);
        }
        if (selected) {
            return getColor(FOREGROUND_SEL_NOFOCUS);
        }
        return getColor(FOREGROUND);
    }

    /**
     * Get the correct background color group
     *
     * @param selected
     *            Is the entry selected
     * @param focused
     *            Is the entry focused
     * @return The matching color
     */
    public Color getBkColorGroup(boolean selected, boolean focused) {
        if (selected && focused) {
            return getColor(GR_BACKGROUND_SEL);
        }
        if (selected) {
            return getColor(GR_BACKGROUND_SEL_NOFOCUS);
        }
        return getColor(GR_BACKGROUND);
    }

    /**
     * Get the correct foreground color group
     *
     * @param selected
     *            Is the entry selected
     * @param focused
     *            Is the entry focused
     * @return The matching color
     */
    public Color getFgColorGroup(boolean selected, boolean focused) {
        if (selected && focused) {
            return getColor(GR_FOREGROUND_SEL);
        }
        if (selected) {
            return getColor(GR_FOREGROUND_SEL_NOFOCUS);
        }
        return getColor(GR_FOREGROUND);
    }
}
