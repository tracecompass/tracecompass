/**********************************************************************
 * Copyright (c) 2016, 2017 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.views;

import static org.junit.Assert.*;
import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Arrays;

import org.eclipse.tracecompass.tmf.ui.tests.stubs.views.TmfViewStub;
import org.eclipse.tracecompass.tmf.ui.views.TmfViewFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * Test the {@link org.eclipse.tracecompass.tmf.ui.views.TmfViewFactory} class
 * and its utility method.
 *
 * @author Jonathan Rajotte Julien
 */
public class TmfViewFactoryTest {

    /* The internal separator */
    private static final String SEPARATOR = TmfViewFactory.INTERNAL_SECONDARY_ID_SEPARATOR;
    private static final String UUID = "90148656-2bd7-4f3e-8513-4def0ac76b1f";

    BaseSecIdTestCase[] baseSecIdTestCases = {
            new BaseSecIdTestCase(null, null),
            new BaseSecIdTestCase(UUID, null),
            new BaseSecIdTestCase(SEPARATOR + UUID, ""),
            new BaseSecIdTestCase(SEPARATOR + "1-1-1-1-1", SEPARATOR + "1-1-1-1-1"),
            new BaseSecIdTestCase("sec_id", "sec_id"),
            new BaseSecIdTestCase("sec_id" + SEPARATOR, "sec_id" + SEPARATOR),
            new BaseSecIdTestCase("sec_id" + SEPARATOR + UUID, "sec_id"),
            new BaseSecIdTestCase("sec_id" + SEPARATOR + SEPARATOR + UUID, "sec_id" + SEPARATOR),
            new BaseSecIdTestCase("sec_id" + SEPARATOR + "third_id", "sec_id" + SEPARATOR + "third_id"),
            new BaseSecIdTestCase("sec_id" + SEPARATOR + "third_id" + SEPARATOR + UUID, "sec_id" + SEPARATOR + "third_id"),
            new BaseSecIdTestCase("sec_id" + SEPARATOR + "third_id" + SEPARATOR + "fourth_id", "sec_id" + SEPARATOR + "third_id" + SEPARATOR + "fourth_id"),
    };

    private static class BaseSecIdTestCase {
        private String input;
        private String output;

        public BaseSecIdTestCase(String input, String secondaryId) {
            this.input = input;
            this.output = secondaryId;
        }

        public String getInput() {
            return input;
        }

        public String getOutput() {
            return output;
        }
    }

    /**
     * Test method for
     * {@link org.eclipse.tracecompass.tmf.ui.views.TmfViewFactory#getBaseSecId(java.lang.String)}.
     */
    @Test
    public void testGetBaseSecId() {
        for (BaseSecIdTestCase testCase : baseSecIdTestCases) {
            String input = testCase.getInput();
            String expect = testCase.getOutput();
            String result = TmfViewFactory.getBaseSecId(input);
            String message = String.format("Input:%s Output: %s Expected: %s", input, result, expect);

            assertEquals(message, expect, result);
        }
    }

    /**
     * Test method for
     * {@link org.eclipse.tracecompass.tmf.ui.views.TmfViewFactory#newView(java.lang.String, boolean)}.
     */
    @Test
    public void testNewView() {
        IViewPart firstView = TmfViewFactory.newView(checkNotNull(TmfViewStub.TMF_VIEW_STUB_ID), false);
        IViewPart sameAsFirstView = TmfViewFactory.newView(checkNotNull(TmfViewStub.TMF_VIEW_STUB_ID), false);
        IViewPart secondView = TmfViewFactory.newView(checkNotNull(TmfViewStub.TMF_VIEW_STUB_ID), true);
        IViewPart failView1 = TmfViewFactory.newView("this.is.a.failing.view.id", false);
        IViewPart failView2 = TmfViewFactory.newView("this.is.a.failing.view.id", true);

        assertNotNull("Failed to spawn first view", firstView);
        assertEquals("Same id returned different instance", sameAsFirstView, firstView);
        assertNotNull("Failed to open second view with suffix", secondView);
        assertNull("Expected to fail on dummy view id", failView1);
        assertNull("Expected to fail on dummy view id with suffix", failView2);

        /** Test for new view from a duplicate view */
        /* Fetch duplicate view complete id */
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = win.getActivePage();
        IViewReference[] viewRefs = page.getViewReferences();

        String fullId = null;
        for (IViewReference view : viewRefs) {
            if (view.getSecondaryId() != null && view.getId().equals(TmfViewStub.TMF_VIEW_STUB_ID)) {
                assertTrue("Instanceof a TmfViewStub", view.getView(false) instanceof TmfViewStub);
                fullId = ((TmfViewStub) view.getView(false)).getViewId();
                break;
            }
        }
        assertNotNull(fullId);
        IViewPart thirdView = TmfViewFactory.newView(fullId, true);
        assertNotNull("Creation from a view id with suffix failed", fullId);
        assertFalse("New view from view id with suffix was not created", Arrays.asList(viewRefs).contains(thirdView));
    }
}
