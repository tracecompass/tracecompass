/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Simple presentation provider to test the time graph with palettes
 *
 * @author Matthew Khouzam
 */
public abstract class PalettedPresentationProvider extends StubPresentationProvider {

    @Override
    public String getPreferenceKey() {
        return "Paletted";
    }

    /**
     * Constructor
     */
    public PalettedPresentationProvider() {
        StateItem[] states = getStateTable();
        for(int i = 0; i < states.length; i++) {
            states[i].getStyleMap().put(ITimeEventStyleStrings.fillColor(), getColor(new TimeEvent(null, 0, 0, i)));
        }
    }

    /**
     * Get the palette provider
     *
     * @return the palette provider
     */
    public abstract IPaletteProvider getPalette();

    private int getColor(TimeEvent event) {
        List<@NonNull RGBAColor> list = getPalette().get();
        int floor = Long.valueOf(Math.floorMod((event.getValue() & ((1L << Integer.SIZE) - 1)), list.size())).intValue();
        return list.get(floor).toInt();
    }

    @Override
    public Map<String, Object> getEventStyle(@Nullable ITimeEvent event) {
        Map<String, Object> style = new HashMap<>(super.getEventStyle(event));
        if (event instanceof TimeEvent) {
            style.put(ITimeEventStyleStrings.fillColor(), getColor((TimeEvent) event));
        }
        return style;
    }

}
