/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;

/**
 * Trace type content provider, a helper for showing trace types
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class TraceTypeContentProvider implements ITreeContentProvider {

    private final List<String> fTraceCategory = new ArrayList<>();
    private final Map<String, List<TraceTypeHelper>> fTraceType = new HashMap<>();

    /**
     * Default Constructor
     */
    public TraceTypeContentProvider() {
        fTraceType.clear();
        fTraceCategory.clear();

        for (String category : TmfTraceType.getTraceCategories()) {
            List<TraceTypeHelper> value = TmfTraceType.getTraceTypes(category);
            if (!value.isEmpty()) {
                fTraceCategory.add(category);
                fTraceType.put(category, value);
            }
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Do nothing
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return fTraceCategory.toArray(new String[0]);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof String) {
            final List<TraceTypeHelper> children = fTraceType.get(parentElement);
            if (children != null) {
                return children.toArray(new TraceTypeHelper[0]);
            }
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof TraceTypeHelper) {
            for (String key : fTraceCategory) {
                List<TraceTypeHelper> traceSet = fTraceType.get(key);
                if (traceSet != null) {
                    if (traceSet.contains(element)) {
                        return key;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof String) {
            String key = (String) element;
            return fTraceType.containsKey(key);
        }
        return false;
    }

}
