/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.model.IOutputElement;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.colors.ColorUtils;
import org.eclipse.tracecompass.tmf.ui.model.StyleManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * {@link TimeGraphPresentationProvider} for views whose data provider
 * implements the {@link IOutputStyleProvider} interface. This presentation
 * provider can be used as is and does not need to be extended.
 *
 * @author Simon Delisle
 */
public class BaseDataProviderTimeGraphPresentationProvider extends TimeGraphPresentationProvider implements IStylePresentationProvider {

    private static final OutputElementStyle TRANSPARENT_STYLE = new OutputElementStyle(null, ImmutableMap.of());

    private final Map<ITimeGraphDataProvider<?>, BiFunction<ITimeEvent, Long, Map<String, String>>> fProviders = new LinkedHashMap<>();
    private boolean fShowTooltipTimes = true;
    private Map<String, Integer> fKeyToIndex = new HashMap<>();
    private @Nullable Map<String, OutputElementStyle> fStylesMap = null;
    private @Nullable StateItem @Nullable[] fStateTable = null;
    private StyleManager fStyleManager = new StyleManager(fetchStyles());

    /**
     * Constructor
     */
    public BaseDataProviderTimeGraphPresentationProvider() {
        super();
    }

    /**
     * Add a data provider to this presentation provider. You can add multiple
     * providers and the styles will be put in a common map. This is the
     * responsibility of the view to set providers.
     *
     * @param provider
     *            Data provider to add to this presentation provider
     * @param tooltipResolver
     *            Tooltip resolver for this data provider
     */
    public void addProvider(ITimeGraphDataProvider<?> provider, BiFunction<ITimeEvent, Long, Map<String, String>> tooltipResolver) {
        synchronized (fProviders) {
            fProviders.put(provider, tooltipResolver);
        }
        Display.getDefault().asyncExec(() -> refresh());
    }

    /**
     * Use the
     * {@link IOutputStyleProvider#fetchStyle(Map, org.eclipse.core.runtime.IProgressMonitor)}
     * to fetch the appropriate style for a specific provider ID given by
     * getProviderId. Everything is stored in a map of styles where the keys are
     * string that will be used in states and the value are
     * {@link OutputElementStyle}
     *
     * @return The style map
     */
    private Map<@NonNull String, @NonNull OutputElementStyle> fetchStyles() {
        Map<String, OutputElementStyle> stylesMap = fStylesMap;
        if (stylesMap == null) {
            stylesMap = new LinkedHashMap<>();
            synchronized (fProviders) {
                for (ITimeGraphDataProvider<?> provider : fProviders.keySet()) {
                    if (provider instanceof IOutputStyleProvider) {
                        TmfModelResponse<@NonNull OutputStyleModel> styleResponse = ((IOutputStyleProvider) provider).fetchStyle(getStyleParameters(), null);
                        OutputStyleModel styleModel = styleResponse.getModel();
                        if (styleModel != null) {
                            for (Entry<String, OutputElementStyle> entry : styleModel.getStyles().entrySet()) {
                                OutputElementStyle style = entry.getValue();
                                // Make sure the style values map is mutable
                                stylesMap.put(entry.getKey(),
                                        new OutputElementStyle(style.getParentKey(), Maps.newHashMap(style.getStyleValues())));
                            }
                        }
                    }
                }
            }
            fStylesMap = stylesMap;
        }
        return stylesMap;
    }

    /**
     * Get the style parameters to pass to a fetchStyle call
     *
     * @return Map of parameters for fetchStyle
     */
    protected Map<String, Object> getStyleParameters() {
        return Collections.emptyMap();
    }

    @Override
    public StateItem[] getStateTable() {
        @Nullable StateItem[] stateTable = fStateTable;
        if (stateTable == null) {
            Map<@NonNull String, @NonNull OutputElementStyle> styles = fetchStyles();
            if (styles.isEmpty()) {
                stateTable = new StateItem[0];
                fStateTable = stateTable;
                return stateTable;
            }
            List<StateItem> stateItemList = new ArrayList<>();
            int tableIndex = 0;
            for (Entry<@NonNull String, @NonNull OutputElementStyle> styleEntry : styles.entrySet()) {
                String styleKey = styleEntry.getKey();
                fKeyToIndex.put(styleKey, tableIndex++);
                OutputElementStyle elementStyle = styleEntry.getValue();
                Map<String, Object> styleMap = new HashMap<>();
                RGBAColor rgba = getColorStyle(elementStyle, StyleProperties.BACKGROUND_COLOR);
                RGB rgb = (rgba != null) ?  new RGB(rgba.getRed(), rgba.getGreen(), rgba.getBlue()) : new RGB(0, 0, 0);
                styleMap.put(StyleProperties.BACKGROUND_COLOR, ColorUtils.toHexColor(rgb));
                Object styleName = getStyle(elementStyle, StyleProperties.STYLE_NAME);
                if (styleName instanceof String) {
                    styleMap.put(StyleProperties.STYLE_NAME, styleName);
                } else {
                    styleMap.put(StyleProperties.STYLE_NAME, styleEntry.getKey());
                }
                Float height = getFloatStyle(elementStyle, StyleProperties.HEIGHT);
                if (height != null) {
                    styleMap.put(StyleProperties.HEIGHT, height);
                }
                Object symbolType = getStyle(elementStyle, StyleProperties.SYMBOL_TYPE);
                if (symbolType instanceof String) {
                    styleMap.put(StyleProperties.SYMBOL_TYPE, symbolType);
                }
                Object styleGroup = getStyle(elementStyle, StyleProperties.STYLE_GROUP);
                if (styleGroup != null) {
                    styleMap.put(StyleProperties.STYLE_GROUP, styleGroup);
                }
                stateItemList.add(new StateItem(styleMap));
            }
            stateTable = stateItemList.toArray(new StateItem[stateItemList.size()]);
            fStateTable = stateTable;
        }
        return stateTable;
    }

    /**
     * @deprecated Use {@link #getEventStyle(ITimeEvent)} instead.
     */
    @Deprecated
    @Override
    public int getStateTableIndex(@Nullable ITimeEvent event) {
        return INVISIBLE;
    }

    @Override
    @NonNullByDefault({})
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        return getEventHoverToolTipInfo(event, event.getTime());
    }

    @Override
    @NonNullByDefault({})
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        if (event != null && !(event instanceof NullTimeEvent)) {
            Map<String, String> tooltipInfo = new LinkedHashMap<>();
            synchronized (fProviders) {
                fProviders.values().forEach(tooltipResolver -> tooltipInfo.putAll(tooltipResolver.apply(event, hoverTime)));
            }
            return tooltipInfo;
        }
        return Collections.emptyMap();
    }

    @Override
    public @Nullable OutputElementStyle getElementStyle(ITimeEvent event) {
        if (event instanceof NullTimeEvent) {
            return null;
        }

        if (event instanceof TimeEvent) {
            IOutputElement model = ((TimeEvent) event).getModel();
            OutputElementStyle eventStyle = model.getStyle();
            if (eventStyle == null && event.getEntry() instanceof TimeGraphEntry) {
                eventStyle = ((TimeGraphEntry) event.getEntry()).getEntryModel().getStyle();
            }
            if (eventStyle != null) {
                return eventStyle;
            }
        }
        return TRANSPARENT_STYLE;
    }

    @Override
    public @NonNull StyleManager getStyleManager() {
        return fStyleManager;
    }

    @Override
    public @Nullable Object getStyle(OutputElementStyle elementStyle, String property) {
        return fStyleManager.getStyle(elementStyle, property);
    }

    @Override
    public @Nullable Float getFloatStyle(OutputElementStyle elementStyle, String property) {
        return fStyleManager.getFactorStyle(elementStyle, property);
    }

    @Override
    public @Nullable RGBAColor getColorStyle(OutputElementStyle elementStyle, String property) {
        return fStyleManager.getColorStyle(elementStyle, property);
    }

    @Override
    public void refresh() {
        fStylesMap = null;
        fStateTable = null;
        fStyleManager = new StyleManager(fetchStyles());
        super.refresh();
        updateStyles();
    }

    @Override
    public boolean displayTimesInTooltip() {
        return fShowTooltipTimes;
    }

    /**
     * Set whether to show times (start, end, duration) in the tooltips
     *
     * @param showTimes
     *            Whether to display start/end/duration times in the tooltips
     */
    public void setShowTimesInTooltip(boolean showTimes) {
        fShowTooltipTimes = showTimes;
    }

    private void updateStyles() {
        Map<String, OutputElementStyle> stylesMap = fStylesMap;
        StateItem[] stateTable = fStateTable;
        if (stylesMap == null || stateTable == null) {
            return;
        }
        for (Entry<String, Integer> entry : fKeyToIndex.entrySet()) {
            OutputElementStyle elementStyle = stylesMap.get(entry.getKey());
            Integer index = entry.getValue();
            if (elementStyle == null || index >= stateTable.length) {
                continue;
            }
            StateItem stateItem = stateTable[index];

            RGB rgb = stateItem.getStateColor();
            Map<@NonNull String, @NonNull Object> styleValues = elementStyle.getStyleValues();
            RGBAColor rgba = getColorStyle(elementStyle, StyleProperties.BACKGROUND_COLOR);
            if (rgba == null || !new RGB(rgba.getRed(), rgba.getGreen(), rgba.getBlue()).equals(rgb)) {
                String hexColor = ColorUtils.toHexColor(rgb);
                styleValues.put(StyleProperties.BACKGROUND_COLOR, hexColor);
                styleValues.put(StyleProperties.COLOR, hexColor);
                styleValues.put(StyleProperties.OPACITY, 1.0f);
            }

            float heightFactor = stateItem.getStateHeightFactor();
            Float prevHeightFactor = getFloatStyle(elementStyle, StyleProperties.HEIGHT);
            if (!Float.valueOf(heightFactor).equals(prevHeightFactor)) {
                if (prevHeightFactor == null) {
                    styleValues.put(StyleProperties.HEIGHT, heightFactor);
                } else {
                    Object height = elementStyle.getStyleValues().getOrDefault(StyleProperties.HEIGHT, 1.0f);
                    height = height instanceof Float ? height : 1.0f;
                    styleValues.put(StyleProperties.HEIGHT, (float) height * heightFactor / prevHeightFactor);
                }
            }
        }
    }
}
