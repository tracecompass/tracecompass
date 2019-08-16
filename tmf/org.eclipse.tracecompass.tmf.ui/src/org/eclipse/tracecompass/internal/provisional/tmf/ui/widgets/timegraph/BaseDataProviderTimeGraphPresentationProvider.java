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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.SymbolType;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.colors.ColorUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.ImmutableMap;

/**
 * {@link TimeGraphPresentationProvider} for views whose data provider
 * implements the {@link IOutputStyleProvider} interface. This presentation
 * provider can be used as is and does not need to be extended.
 *
 * @author Simon Delisle
 */
public class BaseDataProviderTimeGraphPresentationProvider extends TimeGraphPresentationProvider {

    private static final Map<String, String> SYMBOL_TYPES = ImmutableMap.<String, String>builder()
            .put(SymbolType.DIAMOND, IYAppearance.SymbolStyle.DIAMOND)
            .put(SymbolType.CIRCLE, IYAppearance.SymbolStyle.CIRCLE)
            .put(SymbolType.SQUARE, IYAppearance.SymbolStyle.SQUARE)
            .put(SymbolType.TRIANGLE, IYAppearance.SymbolStyle.TRIANGLE)
            .put(SymbolType.INVERTED_TRIANGLE, IYAppearance.SymbolStyle.INVERTED_TRIANGLE)
            .put(SymbolType.CROSS, IYAppearance.SymbolStyle.CROSS)
            .put(SymbolType.PLUS, IYAppearance.SymbolStyle.PLUS)
            .build();
    private final Set<ITimeGraphDataProvider<?>> fProviders = new HashSet<>();
    private Map<String, Integer> fLabelToIndex = new HashMap<>();
    private @Nullable Map<String, OutputElementStyle> fStylesMap = null;
    private @Nullable StateItem @Nullable[] fStateTable = null;

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
     */
    public void addProvider(ITimeGraphDataProvider<?> provider) {
        synchronized (fProviders) {
            fProviders.add(provider);
            fStylesMap = null;
            fStateTable = null;
        }
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
    private Map<@NonNull String, @NonNull OutputElementStyle> getStyles() {
        Map<String, OutputElementStyle> stylesMap = fStylesMap;
        if (stylesMap == null) {
            stylesMap = new LinkedHashMap<>();
            synchronized (fProviders) {
                for (ITimeGraphDataProvider<?> provider : fProviders) {
                    if (provider instanceof IOutputStyleProvider) {
                        TmfModelResponse<@NonNull OutputStyleModel> styleResponse = ((IOutputStyleProvider) provider).fetchStyle(getStyleParameters(), null);
                        OutputStyleModel styleModel = styleResponse.getModel();
                        if (styleModel != null) {
                            Map<String, OutputElementStyle> currentStyleMap = styleModel.getStyles();
                            stylesMap.putAll(currentStyleMap);
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
            Map<@NonNull String, @NonNull OutputElementStyle> styles = getStyles();
            if (styles.isEmpty()) {
                return new StateItem[0];
            }
            List<StateItem> stateItemList = new ArrayList<>();
            int tableIndex = 0;
            for (Entry<String, OutputElementStyle> styleEntry : styles.entrySet()) {
                Map<String, Object> elementStyle = styleEntry.getValue().getStyleValues();
                Object color = elementStyle.get(StyleProperties.BACKGROUND_COLOR);
                RGB rgb = new RGB(0, 0, 0);
                if (color instanceof String) {
                    RGB rgbColor = ColorUtils.fromHexColor((String) color);
                    rgb = rgbColor != null ? rgbColor : new RGB(0, 0, 0);
                }
                fLabelToIndex.put(styleEntry.getKey(), tableIndex);
                tableIndex++;
                Map<String, Object> styleMap = new HashMap<>();
                styleMap.put(ITimeEventStyleStrings.fillStyle(), ITimeEventStyleStrings.solidColorFillStyle());
                styleMap.put(ITimeEventStyleStrings.fillColor(), new RGBAColor(rgb.red, rgb.green, rgb.blue).toInt());
                styleMap.put(ITimeEventStyleStrings.label(), styleEntry.getKey());
                Object height = elementStyle.get(StyleProperties.HEIGHT);
                if (height instanceof Float) {
                    styleMap.put(ITimeEventStyleStrings.heightFactor(), height);
                }
                Object symbolType = SYMBOL_TYPES.get(elementStyle.get(StyleProperties.SYMBOL_TYPE));
                if (symbolType instanceof String) {
                    styleMap.put(ITimeEventStyleStrings.symbolStyle(), symbolType);
                }
                Object styleGroup = elementStyle.get(StyleProperties.STYLE_GROUP);
                if (styleGroup != null) {
                    styleMap.put(ITimeEventStyleStrings.group(), styleGroup);
                }
                stateItemList.add(new StateItem(styleMap));
            }
            stateTable = stateItemList.toArray(new StateItem[stateItemList.size()]);
            fStateTable = stateTable;
        }
        return stateTable;
    }

    @Override
    public int getStateTableIndex(@Nullable ITimeEvent event) {
        if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }

        if (event instanceof TimeEvent) {
            ITimeGraphState model = ((TimeEvent) event).getStateModel();
            OutputElementStyle eventStyle = model.getStyle();
            if (eventStyle == null && event.getEntry() instanceof TimeGraphEntry) {
                eventStyle = ((TimeGraphEntry) event.getEntry()).getEntryModel().getStyle();
            }
            if (eventStyle != null) {
                String styleKey = eventStyle.getParentKey();
                if (styleKey != null) {
                    Integer index = fLabelToIndex.get(styleKey);
                    return index != null ? index : Integer.MAX_VALUE;
                }
            }
        }
        return TRANSPARENT;
    }

    /**
     * Get the provider associated with this entry
     *
     * @param entry
     *            Time Graph Entry
     * @return The data provider
     */
    protected @Nullable ITimeGraphDataProvider<? extends TimeGraphEntryModel> getProvider(TimeGraphEntry entry) {
        return BaseDataProviderTimeGraphView.getProvider(entry);
    }

    @Override
    @NonNullByDefault({})
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        return getEventHoverToolTipInfo(event, event.getTime());
    }

    @Override
    @NonNullByDefault({})
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        ITimeGraphEntry entry = event.getEntry();

        if (event instanceof TimeEvent && !(event instanceof NullTimeEvent) && entry instanceof TimeGraphEntry) {
            ITmfTreeDataModel model = ((TimeGraphEntry) entry).getEntryModel();
            ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider = getProvider((TimeGraphEntry) entry);
            if (provider != null) {
                return getTooltip(provider, model.getId(), hoverTime);
            }
        }

        return Collections.emptyMap();
    }

    private static Map<String, String> getTooltip(ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider, long id, long hoverTime) {
        SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(Collections.singletonList(hoverTime), Collections.singleton(id));
        TmfModelResponse<Map<String, String>> response = provider.fetchTooltip(FetchParametersUtils.selectionTimeQueryToMap(filter), null);
        Map<String, String> tooltip = response.getModel();

        if (tooltip == null) {
            return Collections.emptyMap();
        }

        Map<String, String> retMap = new LinkedHashMap<>(tooltip);
        return retMap;
    }

    @Override
    public void refresh() {
        fStylesMap = null;
        fStateTable = null;
        super.refresh();
    }
}
