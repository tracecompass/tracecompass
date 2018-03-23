/*****************************************************************************
 * Copyright (c) 2018 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.util;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;

import com.google.common.base.Joiner;

/**
 * Utils to help load styles for timegraphs
 *
 * @author Matthew Khouzam
 */
public final class TimeGraphStyleUtil {

    private static final char SEPARATOR = '.';
    private static final String PREFIX = "view" + SEPARATOR; //$NON-NLS-1$

    /**
     * Default Constructor
     */
    private TimeGraphStyleUtil() {
        // do nothing
    }

    /**
     * Load default values into the state item from a preference store
     *
     * @param presentationProvider
     *            the presentation provider
     * @param stateItem
     *            the state item
     */
    public static void loadValue(ITimeGraphPresentationProvider presentationProvider, StateItem stateItem) {
        IPreferenceStore store = getStore();
        String fillColorKey = getPreferenceName(presentationProvider, stateItem, ITimeEventStyleStrings.fillColor());
        String heightFactorKey = getPreferenceName(presentationProvider, stateItem, ITimeEventStyleStrings.heightFactor());

        Map<String, Object> styleMap = stateItem.getStyleMap();
        RGB defaultRgb = stateItem.getStateColor();

        String defaultString = store.getDefaultString(fillColorKey);
        if (defaultString.isEmpty()) {
            store.setDefault(fillColorKey, new RGBAColor(defaultRgb.red, defaultRgb.green, defaultRgb.blue, 255).toString());
        }
        String rgbColor = store.getString(fillColorKey);

        if (store.getDefaultFloat(heightFactorKey) == 0.0f) {
            float defaultHeightFactor = isLink(stateItem) ? TimeGraphControl.DEFAULT_LINK_WIDTH : TimeGraphControl.DEFAULT_STATE_WIDTH;
            defaultHeightFactor = (float) styleMap.getOrDefault(ITimeEventStyleStrings.heightFactor(), defaultHeightFactor);
            store.setDefault(heightFactorKey, defaultHeightFactor);
        }
        float heightFactor = store.getFloat(heightFactorKey);

        RGBAColor rgba = RGBAColor.fromString(rgbColor);
        if (rgba != null) {
            styleMap.put(ITimeEventStyleStrings.fillColor(), rgba.toInt());
            styleMap.put(ITimeEventStyleStrings.heightFactor(), heightFactor);
        }
    }

    private static boolean isLink(StateItem stateItem) {
        return ITimeEventStyleStrings.linkType().equals(getItemProperty(stateItem));
    }

    private static @Nullable Object getItemProperty(StateItem stateItem) {
        return stateItem.getStyleMap().get(ITimeEventStyleStrings.itemTypeProperty());
    }

    /**
     * Get the preference name for a style key
     *
     * @param presentationProvider
     *            the presentation provider being queried
     * @param stateItem
     *            the state item
     * @param styleKey
     *            the key of the state item
     * @return a path to lookup the value
     */
    public static String getPreferenceName(ITimeGraphPresentationProvider presentationProvider, StateItem stateItem, String styleKey) {
        return Joiner
                .on(SEPARATOR)
                .skipNulls()
                .join(
                        PREFIX + String.valueOf(presentationProvider.getPreferenceKey()),
                        getItemProperty(stateItem),
                        stateItem.getStateString(),
                        styleKey);
    }

    /**
     * Load default values into the state items from a preference store
     * @param presentationProvider
     *            the presentation provider
     */
    public static void loadValues(ITimeGraphPresentationProvider presentationProvider) {
        for (StateItem stateItem : presentationProvider.getStateTable()) {
            loadValue(presentationProvider, stateItem);
        }
    }

    /**
     * Get the store that contains the style
     *
     * @return the store
     */
    public static IPreferenceStore getStore() {
        return Activator.getDefault().getPreferenceStore();
    }
}
