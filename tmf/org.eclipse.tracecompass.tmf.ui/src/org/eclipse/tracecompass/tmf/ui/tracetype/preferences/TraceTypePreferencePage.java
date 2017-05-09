/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.tracetype.preferences;

import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypePreferences;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * This class implements a preference page for the trace type
 *
 * @author Jean-Christian Kouame
 * @since 3.0
 *
 */
public class TraceTypePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private TraceTypePreferencePageViewer fViewer;

    @Override
    public void init(IWorkbench workbench) {
        fViewer = new TraceTypePreferencePageViewer(Iterables.filter(TmfTraceType.getTraceTypeHelpers(), helper -> !helper.isExperimentType()));
        fViewer.setComparator(new ViewerComparator());
    }

    @Override
    protected Control createContents(Composite parent) {
        return fViewer.create(parent);
    }

    @Override
    public boolean performOk() {
        List<TraceTypeHelper> unchecked = fViewer.getUncheckedElements();
        TraceTypePreferences.setPreferenceValue(Lists.newArrayList(Iterables.transform(unchecked, helper -> helper.getTraceTypeId())));
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        fViewer.performDefaults();
    }
}