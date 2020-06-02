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

package org.eclipse.tracecompass.tmf.ui.tests.widgets.timegraph.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.BorderStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.SymbolType;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.TextDirection;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.model.StyleManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Test the {@link StyleManager} class
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
@NonNullByDefault
public class StyleManagerTest {

    private static final String STYLE1 = "style1";
    private static final String STYLE2 = "style2";
    private static final String RED_COLOR = Objects.requireNonNull(X11ColorUtils.toHexColor("red"));
    private static final String BLUE_COLOR = Objects.requireNonNull(X11ColorUtils.toHexColor("blue"));
    private static final String BLACK_COLOR = Objects.requireNonNull(X11ColorUtils.toHexColor("black"));

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "disjoint styles", DISJOINT_STYLES },
                { "styles with map", STYLES_WITH_MAP },
                { "style inheritance", STYLE_INHERITANCE },
                { "default values", DEFAULT_VALUES },
                { "multiple inheritance", MULTIPLE_INHERITANCE },
        });
    }

    private static class StyleTestCase {

        private Map<String, OutputElementStyle> fStyleMap;

        public StyleTestCase(Map<String, OutputElementStyle> styleMap) {
            fStyleMap = styleMap;
        }

        public Map<String, OutputElementStyle> getStyleMap() {
            return fStyleMap;
        }

        public List<StyleProperty> getStylePropertiesToTest() {
            return Collections.emptyList();
        }

        public List<StyleProperty> getFactorStylePropertiesToTest() {
            return Collections.emptyList();
        }

        public List<StyleProperty> getColorStylePropertiesToTest() {
            return Collections.emptyList();
        }

    }

    private static class StyleProperty {
        private OutputElementStyle fStyle;
        private String fPropertyName;
        private @Nullable Object fResult;

        public StyleProperty(OutputElementStyle style, String propertyName, @Nullable Object result) {
            fPropertyName = propertyName;
            fResult = result;
            fStyle = style;
        }
    }

    /**
     * Test the values returned by a normal map of disjoint styles, where each
     * styles have the same properties
     */
    private static StyleTestCase DISJOINT_STYLES = new StyleTestCase(ImmutableMap.of(
            STYLE1, new OutputElementStyle(null, ImmutableMap.of(
                    StyleProperties.STYLE_NAME, STYLE1,
                    StyleProperties.BACKGROUND_COLOR, RED_COLOR,
                    StyleProperties.HEIGHT, 1.0f,
                    StyleProperties.OPACITY, 1.0f)),
            STYLE2, new OutputElementStyle(null, ImmutableMap.of(
                    StyleProperties.STYLE_NAME, STYLE2,
                    StyleProperties.BACKGROUND_COLOR, BLUE_COLOR,
                    StyleProperties.HEIGHT, 0.5f,
                    StyleProperties.OPACITY, 0.5f)))) {

        @Override
        public List<StyleProperty> getStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(new OutputElementStyle(STYLE1), StyleProperties.STYLE_NAME, STYLE1),
                    new StyleProperty(new OutputElementStyle(STYLE2), StyleProperties.STYLE_NAME, STYLE2));
        }

        @Override
        public List<StyleProperty> getFactorStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(new OutputElementStyle(STYLE1), StyleProperties.HEIGHT, 1.0f),
                    new StyleProperty(new OutputElementStyle(STYLE2), StyleProperties.HEIGHT, 0.5f));
        }

        @Override
        public List<StyleProperty> getColorStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(new OutputElementStyle(STYLE1), StyleProperties.BACKGROUND_COLOR, RGBAColor.fromString(RED_COLOR, 255)),
                    new StyleProperty(new OutputElementStyle(STYLE2), StyleProperties.BACKGROUND_COLOR, RGBAColor.fromString(BLUE_COLOR, (int) (0.5 * 255))));
        }

    };

    /**
     * Test the values of style elements with contain a map that overrides some
     * properties.
     *
     * The static style below inherits the base style but overrides some
     * properties in a map
     */
    private static final OutputElementStyle STYLE_WITH_MAP = new OutputElementStyle(STYLE1, ImmutableMap.of(
            StyleProperties.STYLE_NAME, STYLE2,
            StyleProperties.BACKGROUND_COLOR, BLUE_COLOR,
            StyleProperties.HEIGHT, 0.5f,
            StyleProperties.COLOR, RED_COLOR));
    private static StyleTestCase STYLES_WITH_MAP = new StyleTestCase(ImmutableMap.of(
            STYLE1, new OutputElementStyle(null, ImmutableMap.of(
                    StyleProperties.STYLE_NAME, STYLE1,
                    StyleProperties.BACKGROUND_COLOR, RED_COLOR,
                    StyleProperties.HEIGHT, 0.75f,
                    StyleProperties.OPACITY, 0.5f)))) {

        @Override
        public List<StyleProperty> getStylePropertiesToTest() {
            return ImmutableList.of(new StyleProperty(STYLE_WITH_MAP, StyleProperties.STYLE_NAME, STYLE2));
        }

        @Override
        public List<StyleProperty> getFactorStylePropertiesToTest() {
            return ImmutableList.of(new StyleProperty(STYLE_WITH_MAP, StyleProperties.HEIGHT, 0.5f));
        }

        @Override
        public List<StyleProperty> getColorStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(STYLE_WITH_MAP, StyleProperties.BACKGROUND_COLOR, RGBAColor.fromString(BLUE_COLOR, (int) (0.5 * 255))),
                    new StyleProperty(STYLE_WITH_MAP, StyleProperties.COLOR, RGBAColor.fromString(RED_COLOR, (int) (0.5 * 255))));
        }
    };

    /**
     * Test simple style inheritance. Base style has 6 properties, 2 of each
     * type: simple, factor, and color and one of each type will be overridden
     * by inherited style
     */
    private static final Map<String, Object> STYLE1_PROPERTIES;
    private static final OutputElementStyle INHERITING_STYLE = new OutputElementStyle(STYLE2);
    static {
        ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();
        /*
         * ADD STATE MAPPING HERE
         */
        builder.put(StyleProperties.BORDER_STYLE, BorderStyle.DASHED);
        builder.put(StyleProperties.TEXT_DIRECTION, TextDirection.LTR);
        builder.put(StyleProperties.HEIGHT, 0.75f);
        builder.put(StyleProperties.OPACITY, 0.75f);
        builder.put(StyleProperties.BACKGROUND_COLOR, RED_COLOR);
        builder.put(StyleProperties.COLOR, BLUE_COLOR);
        STYLE1_PROPERTIES = builder.build();
    }
    private static final OutputElementStyle BASE_STYLE = new OutputElementStyle(STYLE1);

    private static StyleTestCase STYLE_INHERITANCE = new StyleTestCase(ImmutableMap.of(
            STYLE1, new OutputElementStyle(null, STYLE1_PROPERTIES),
            STYLE2, new OutputElementStyle(STYLE1, ImmutableMap.of(
                    StyleProperties.TEXT_DIRECTION, TextDirection.RTL,
                    StyleProperties.COLOR, RED_COLOR,
                    StyleProperties.OPACITY, 0.5f,
                    StyleProperties.HEIGHT + StyleProperties.FACTOR, 0.5f,
                    StyleProperties.BACKGROUND_COLOR + StyleProperties.BLEND, BLUE_COLOR + String.format("%02X", (int) (0.5 * 255)))))) {
        @Override
        public List<StyleProperty> getStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(INHERITING_STYLE, StyleProperties.BORDER_STYLE, BorderStyle.DASHED),
                    new StyleProperty(INHERITING_STYLE, StyleProperties.TEXT_DIRECTION, TextDirection.RTL));
        }

        @Override
        public List<StyleProperty> getFactorStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(INHERITING_STYLE, StyleProperties.HEIGHT, 0.75f * 0.5f),
                    new StyleProperty(INHERITING_STYLE, StyleProperties.OPACITY, 0.5f));
        }

        @Override
        public List<StyleProperty> getColorStylePropertiesToTest() {
            return ImmutableList.of(
                    // Verify the blend
                    new StyleProperty(INHERITING_STYLE, StyleProperties.BACKGROUND_COLOR, new RGBAColor(85, 0, 169, 191)),
                    new StyleProperty(BASE_STYLE, StyleProperties.BACKGROUND_COLOR, RGBAColor.fromString(RED_COLOR, (int) (0.75 * 255))),
                    new StyleProperty(INHERITING_STYLE, StyleProperties.COLOR, RGBAColor.fromString(RED_COLOR, (int) (0.5 * 255))));
        }
    };

    /**
     * Test the default property values when styles do not specify values for
     * some properties. There are no styles defined
     */
    private static final OutputElementStyle EMPTY_STYLE = new OutputElementStyle(null, Collections.emptyMap());
    private static StyleTestCase DEFAULT_VALUES = new StyleTestCase(Collections.emptyMap()) {
        @Override
        public List<StyleProperty> getStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(EMPTY_STYLE, StyleProperties.LINEAR_GRADIENT, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.LINEAR_GRADIENT_DIRECTION, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.TEXT_DIRECTION, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.TEXT_ALIGN, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.FONT_STYLE, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.FONT_WEIGHT, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.SYMBOL_TYPE, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.BORDER_STYLE, null));
        }

        @Override
        public List<StyleProperty> getFactorStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(EMPTY_STYLE, StyleProperties.HEIGHT, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.WIDTH, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.BORDER_RADIUS, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.OPACITY, null));
        }

        @Override
        public List<StyleProperty> getColorStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(EMPTY_STYLE, StyleProperties.BACKGROUND_COLOR, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.LINEAR_GRADIENT_COLOR_END, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.BORDER_COLOR, null),
                    new StyleProperty(EMPTY_STYLE, StyleProperties.COLOR, null));
        }
    };

    /**
     * Test multiple style inheritance, where parent style is a comma-separated
     * list of styles. Right-most style and its ancestors should have precedence
     * over left most. The "A1" style also has a parent that is not present in
     * the styles.
     */
    private static final OutputElementStyle A_THEN_B_STYLE = new OutputElementStyle("B2,A2");
    private static final OutputElementStyle B_THEN_A_STYLE = new OutputElementStyle("A2,B2");
    private static final OutputElementStyle B_THEN_A_STYLE_THEN_MAP = new OutputElementStyle("A2,B2",
            ImmutableMap.of(
                    StyleProperties.SYMBOL_TYPE, SymbolType.PLUS,
                    StyleProperties.BORDER_RADIUS, 0.25f,
                    StyleProperties.COLOR, BLACK_COLOR));
    private static StyleTestCase MULTIPLE_INHERITANCE = new StyleTestCase(ImmutableMap.of(
            "A1", new OutputElementStyle("A", ImmutableMap.of(
                    StyleProperties.BACKGROUND_COLOR, RED_COLOR,
                    StyleProperties.OPACITY, 0.5f,
                    StyleProperties.SYMBOL_TYPE, SymbolType.CIRCLE)),
            "A2", new OutputElementStyle("A1", ImmutableMap.of(
                    StyleProperties.BACKGROUND_COLOR, BLUE_COLOR,
                    StyleProperties.BORDER_RADIUS, 0.75f,
                    StyleProperties.COLOR, RED_COLOR)),
            "B1", new OutputElementStyle(null, ImmutableMap.of(
                    StyleProperties.HEIGHT, 0.75f,
                    StyleProperties.WIDTH, 2,
                    StyleProperties.SYMBOL_TYPE, SymbolType.DIAMOND,
                    StyleProperties.COLOR, BLUE_COLOR)),
            "B2", new OutputElementStyle("B1", ImmutableMap.of(
                    StyleProperties.HEIGHT, 0.5f,
                    StyleProperties.BORDER_RADIUS, 0.5f)))) {
        @Override
        public List<StyleProperty> getStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(A_THEN_B_STYLE, StyleProperties.SYMBOL_TYPE, SymbolType.CIRCLE),
                    new StyleProperty(B_THEN_A_STYLE, StyleProperties.SYMBOL_TYPE, SymbolType.DIAMOND),
                    new StyleProperty(B_THEN_A_STYLE_THEN_MAP, StyleProperties.SYMBOL_TYPE, SymbolType.PLUS));
        }

        @Override
        public List<StyleProperty> getFactorStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(A_THEN_B_STYLE, StyleProperties.HEIGHT, 0.5f),
                    new StyleProperty(B_THEN_A_STYLE, StyleProperties.HEIGHT, 0.5f),
                    new StyleProperty(A_THEN_B_STYLE, StyleProperties.WIDTH, 2.0f),
                    new StyleProperty(B_THEN_A_STYLE, StyleProperties.WIDTH, 2.0f),
                    new StyleProperty(A_THEN_B_STYLE, StyleProperties.BORDER_RADIUS, 0.75f),
                    new StyleProperty(B_THEN_A_STYLE, StyleProperties.BORDER_RADIUS, 0.5f),
                    new StyleProperty(B_THEN_A_STYLE_THEN_MAP, StyleProperties.BORDER_RADIUS, 0.25f));
        }

        @Override
        public List<StyleProperty> getColorStylePropertiesToTest() {
            return ImmutableList.of(
                    new StyleProperty(A_THEN_B_STYLE, StyleProperties.BACKGROUND_COLOR, RGBAColor.fromString(BLUE_COLOR, (int) (0.5 * 255))),
                    new StyleProperty(B_THEN_A_STYLE, StyleProperties.BACKGROUND_COLOR, RGBAColor.fromString(BLUE_COLOR, (int) (0.5 * 255))),
                    new StyleProperty(A_THEN_B_STYLE, StyleProperties.COLOR, RGBAColor.fromString(RED_COLOR, (int) (0.5 * 255))),
                    new StyleProperty(B_THEN_A_STYLE, StyleProperties.COLOR, RGBAColor.fromString(BLUE_COLOR, (int) (0.5 * 255))),
                    new StyleProperty(B_THEN_A_STYLE_THEN_MAP, StyleProperties.COLOR, RGBAColor.fromString(BLACK_COLOR, (int) (0.5 * 255))));
        }
    };

    private StyleTestCase fTestCase;
    private StyleManager fManager;

    /**
     * @param testCaseName
     *            The name of the test case
     * @param testCase
     *            The test case
     */
    public StyleManagerTest(String testCaseName, StyleTestCase testCase) {
        fTestCase = testCase;
        fManager = new StyleManager(fTestCase.getStyleMap());
    }

    /**
     * Test the properties and styles returned by the style manager
     */
    @Test
    public void testStyleProperties() {
        for (StyleProperty property : fTestCase.getStylePropertiesToTest()) {
            Object value = fManager.getStyle(property.fStyle, property.fPropertyName);
            if (property.fResult == null) {
                assertNull(property.fStyle.getParentKey() + "," + property.fPropertyName, value);
            } else {
                assertEquals(property.fStyle.getParentKey() + "," + property.fPropertyName, property.fResult, value);
            }
        }

    }

    /**
     * Test the properties and styles returned by the style manager
     */
    @Test
    public void testFactorStyleProperties() {
        for (StyleProperty property : fTestCase.getFactorStylePropertiesToTest()) {
            Float value = fManager.getFactorStyle(property.fStyle, property.fPropertyName);
            if (property.fResult == null) {
                assertNull(property.fStyle.getParentKey() + "," + property.fPropertyName, value);
            } else {
                assertEquals(property.fStyle.getParentKey() + "," + property.fPropertyName, property.fResult, value);
            }
        }
    }

    /**
     * Test the properties and styles returned by the style manager
     */
    @Test
    public void testColorStyleProperties() {
        for (StyleProperty property : fTestCase.getColorStylePropertiesToTest()) {
            RGBAColor value = fManager.getColorStyle(property.fStyle, property.fPropertyName);
            if (property.fResult == null) {
                assertNull(property.fStyle.getParentKey() + "," + property.fPropertyName, value);
            } else {
                assertEquals(property.fStyle.getParentKey() + "," + property.fPropertyName, property.fResult, value);
            }
        }
    }

}
