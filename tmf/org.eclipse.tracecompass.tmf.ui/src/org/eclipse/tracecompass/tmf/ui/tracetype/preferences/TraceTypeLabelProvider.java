/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.tracetype.preferences;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypePreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

/**
 * Trace type element label provider
 *
 * @author Jean-Christian Kouame
 * @since 3.0
 *
 */
public class TraceTypeLabelProvider implements ITableLabelProvider, ILabelProvider {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    @Override
    public void addListener(ILabelProviderListener listener) {
        // do nothing
    }

    @Override
    public void dispose() {
        // do nothing
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // do nothing
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (columnIndex == 0) {
            return getText(element);
        } else if (columnIndex == 1) {
            if (element instanceof TraceTypeHelper) {
                TraceTypeHelper helper = (TraceTypeHelper) element;
                String traceTypeId = helper.getTraceTypeId();
                long initialTimeRange = TraceTypePreferences.getInitialTimeRange(traceTypeId, helper.getTrace().getInitialRangeOffset().toNanos());
                return TmfTimestamp.fromNanos(initialTimeRange).toString(TmfTimestampFormat.getDefaulIntervalFormat());
            }
        }

        return EMPTY_STRING;
    }

    @Override
    public Image getImage(Object element) {
        return null;
    }

    @Override
    public String getText(Object element) {
        return element instanceof TraceTypeHelper ? ((TraceTypeHelper) element).getName() : element.toString();
    }
}