/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.ui.viewers.xychart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.model.IStylePresentationProvider;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.presentation.XYPresentationProvider;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.model.StyleManager;

import com.google.common.collect.Maps;

/**
 * Base presentation provider for XY models. It supports data providers
 *
 * @author Geneviève Bastien
 */
public class BaseXYPresentationProvider extends XYPresentationProvider
        implements IStylePresentationProvider {

    private final List<ITmfTreeXYDataProvider<?>> fProviders = new ArrayList<>();
    private StyleManager fStyleManager;
    private @Nullable Map<String, OutputElementStyle> fStylesMap = null;

    /**
     * Constructor
     */
    public BaseXYPresentationProvider() {
        fStyleManager = StyleManager.empty();
    }

    /**
     * Add a data provider to this presentation provider. You can add multiple
     * providers and the styles will be put in a common map. This is the
     * responsibility of the view to set providers.
     *
     * @param provider
     *            Data provider to add to this presentation provider
     */
    public void addProvider(ITmfTreeXYDataProvider<?> provider) {
        synchronized (fProviders) {
            fProviders.add(provider);
        }
        Display.getDefault().asyncExec(() -> refresh());
    }

    private void refresh() {
        fStylesMap = null;
        fStyleManager = new StyleManager(fetchStyles());
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
                for (ITmfTreeXYDataProvider<?> provider : fProviders) {
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
    public StyleManager getStyleManager() {
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

    /**
     * Get the style property value for the specified element style. The style
     * hierarchy is traversed until a value is found.
     *
     * @param elementStyle
     *            the style
     * @param property
     *            the style property
     * @param defaultValue
     *            The default value to return in case the property does not
     *            exist in the style
     * @return the style value or default, if property not available
     */
    public Object getStyleOrDefault(OutputElementStyle elementStyle, String property, Object defaultValue) {
        Object style = fStyleManager.getStyle(elementStyle, property);
        return style == null ? defaultValue : style;
    }

    /**
     * Get the style property float value for the specified element style. The
     * style hierarchy is completely traversed, and the returned value is the
     * multiplication of every float value that is found.
     *
     * @param elementStyle
     *            the style
     * @param property
     *            the style property
     * @param defaultValue
     *            The default value to return in case the property does not
     *            exist in the style
     * @return the style float value or default, if property not available
     */
    public Float getFloatStyleOrDefault(OutputElementStyle elementStyle, String property, Float defaultValue) {
        Float style = fStyleManager.getFactorStyle(elementStyle, property);
        return style == null ? defaultValue : style;
    }

    /**
     * Get the style property color value for the specified element style. The
     * style hierarchy is completely traversed, and the returned value is the
     * blended color of every color value that is found.
     *
     * @param elementStyle
     *            the style
     * @param property
     *            the style property
     * @param defaultValue
     *            The default value to return in case the property does not
     *            exist in the style
     * @return the style value or default, if property not available
     */
    public RGBAColor getColorStyleOrDefault(OutputElementStyle elementStyle, String property, RGBAColor defaultValue) {
        RGBAColor style = fStyleManager.getColorStyle(elementStyle, property);
        return style == null ? defaultValue : style;
    }

}
