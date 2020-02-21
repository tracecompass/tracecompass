/**********************************************************************
 * Copyright (c) 2016 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.stubs.views;

import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Simple TmfView stub
 *
 * @author Jonathan Rajotte Julien
 */
public class TmfViewStub extends TmfView {

    /** The stub view id */
    public static String TMF_VIEW_STUB_ID = "org.eclipse.tracecompass.tmf.ui.tests.stubs.views.TmfViewStub";

    /** Stub view constructor */
    public TmfViewStub() {
        super(TMF_VIEW_STUB_ID);
    }

    /**
     * Override the normally protected getViewId to allow access for test
     */
    @Override
    public String getViewId() {
        return super.getViewId();
    }

    @Override
    public void setFocus() {
        /** This is a stub no need to do thing on setFocus */
    }
}
