/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.tests.project.model;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectModelPreferences;
import org.eclipse.tracecompass.tmf.ui.tests.TmfUITestPlugin;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Test suite for the {@link TmfProjectModelPreferences} class
 *
 * @author Bernd Hufmann
 *
 */
public class TmfProjectModelPreferencesTests {

    private static final String DEFAULT_ELEMENT_LABEL = "Trace Compass";
    private static final String DEFAULT_ICON_BUNDLE_SYMBOLIC_NAME = "org.eclipse.tracecompass.tmf.ui";
    private static final String DEFAULT_ICON_PATH = "icons/obj16/tc_icon.png";

    private static final @NonNull String TEST_ELEMENT_LABEL = "Test Label";
    private static final @NonNull String TEST_ICON_BUNDLE_SYMBOLIC_NAME = "org.eclipse.tracecompass.tmf.ui.tests";
    private static final @NonNull String TEST_ICON_PATH = "icons/node_obj.gif";
    private static final @NonNull String TEST_INVALID_ICON_PATH = "icons/invalid.gif";
    private static final @NonNull String TEST_INVALID_SYMBOLIC_NAME = "invalid.bundle.symbolic.name";

    private static Image fDefaulIcon = null;
    private static Image fTestIcon = null;
    private static Image fMissingIcon = null;

    /**
     * Test class initialization
     */
    @BeforeClass
    public static void init() {
        // Call getProjectModelIcon() so that all icons are loaded in the image registry
        TmfProjectModelPreferences.getProjectModelIcon();

        Bundle bundle = Platform.getBundle(DEFAULT_ICON_BUNDLE_SYMBOLIC_NAME);
        fDefaulIcon = Activator.getDefault().getImageRegistry().get(bundle.getSymbolicName() + '/' + DEFAULT_ICON_PATH);

        bundle = Platform.getBundle(TEST_ICON_BUNDLE_SYMBOLIC_NAME);
        fTestIcon = TmfUITestPlugin.loadIcon(bundle, TEST_ICON_PATH);
        fMissingIcon = TmfUITestPlugin.loadIcon(bundle, TEST_INVALID_ICON_PATH);
    }

    /**
     * Tests Constructor (default values)
     */
    @Test
    public void testInit() {
        assertEquals(DEFAULT_ELEMENT_LABEL, TmfProjectModelPreferences.getProjectModelLabel());
        assertEquals(fDefaulIcon, TmfProjectModelPreferences.getProjectModelIcon());
    }

    /**
     * Tests the setters and getters
     */
    @Test
    public void testLabelSetterGetters() {
        /**
         *  If invalid bundle symbolic name is stored then use default label.
         *  This tests the case if defining bundle has been uninstalled.
         */
        TmfProjectModelPreferences.setProjectModelLabel(TEST_INVALID_SYMBOLIC_NAME, TEST_ELEMENT_LABEL);
        assertEquals(DEFAULT_ELEMENT_LABEL, TmfProjectModelPreferences.getProjectModelLabel());

        TmfProjectModelPreferences.setProjectModelLabel(TEST_ICON_BUNDLE_SYMBOLIC_NAME, TEST_ELEMENT_LABEL);
        assertEquals(TEST_ELEMENT_LABEL, TmfProjectModelPreferences.getProjectModelLabel());
    }

    /**
     * Tests the setters and getters
     */
    @Test
    public void testIconSetterGetters() {
        /**
         *  If invalid bundle symbolic name is stored then use default icon.
         *  This tests the case if defining bundle has been uninstalled.
         */
        TmfProjectModelPreferences.setProjectModelIcon(TEST_INVALID_SYMBOLIC_NAME, TEST_ICON_PATH);
        assertEquals(fDefaulIcon, TmfProjectModelPreferences.getProjectModelIcon());

        TmfProjectModelPreferences.setProjectModelIcon(TEST_ICON_BUNDLE_SYMBOLIC_NAME, TEST_INVALID_ICON_PATH);
        assertEquals(fMissingIcon, TmfProjectModelPreferences.getProjectModelIcon());

        TmfProjectModelPreferences.setProjectModelIcon(TEST_ICON_BUNDLE_SYMBOLIC_NAME, TEST_ICON_PATH);
        assertEquals(fTestIcon, TmfProjectModelPreferences.getProjectModelIcon());
    }
}
