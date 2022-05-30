/*****************************************************************************
 * Copyright (c) 2018, 2020 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.util;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;

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
        String bgColorKey = getPreferenceName(presentationProvider, stateItem, StyleProperties.BACKGROUND_COLOR);
        String colorKey = getPreferenceName(presentationProvider, stateItem, StyleProperties.COLOR);
        String heightFactorKey = getPreferenceName(presentationProvider, stateItem, StyleProperties.HEIGHT);
        String widthKey = getPreferenceName(presentationProvider, stateItem, StyleProperties.WIDTH);
        Map<String, Object> styleMap = stateItem.getStyleMap();

        String prefBgColor = store.getString(bgColorKey);
        if (!prefBgColor.isEmpty()) {
            styleMap.put(StyleProperties.BACKGROUND_COLOR, prefBgColor);
        }
        String prefColor = store.getString(colorKey);
        if (!prefColor.isEmpty()) {
            styleMap.put(StyleProperties.COLOR, prefColor);
        }

        store.setDefault(heightFactorKey, -1.0f);
        float prefHeightFactor = store.getFloat(heightFactorKey);
        if (prefHeightFactor != -1.0f) {
            styleMap.put(StyleProperties.HEIGHT, prefHeightFactor);
        }

        store.setDefault(widthKey, -1);
        int prefWidth = store.getInt(widthKey);
        if (prefWidth != -1) {
            styleMap.put(StyleProperties.WIDTH, prefWidth);
        }
    }

    private static @Nullable Object getItemProperty(StateItem stateItem) {
        return stateItem.getStyleMap().get(StyleProperties.itemTypeProperty());
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
     *
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
