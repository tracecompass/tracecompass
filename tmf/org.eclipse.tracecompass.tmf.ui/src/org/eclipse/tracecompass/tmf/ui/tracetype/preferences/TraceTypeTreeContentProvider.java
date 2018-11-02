/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.tracetype.preferences;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;

import com.google.common.collect.Iterables;

/**
 * This class implements a trace type tree content provider
 *
 * @author Jean-Christian Kouame
 * @since 3.0
 *
 */
public class TraceTypeTreeContentProvider implements ITreeContentProvider {

    Iterable<@NonNull TraceTypeHelper> fInput;
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Iterable) {
            Set<Object> input = new HashSet<>();

            ((Iterable<TraceTypeHelper>) inputElement).forEach(helper -> {
                String categoryName = helper.getCategoryName();
                if (!categoryName.isEmpty()) {
                    input.add(categoryName);
                } else {
                    input.add(helper);
                }
            });

            return input.toArray();

        }

        return new TraceTypeHelper[0];
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof String ? true : false;
    }

    @Override
    public TraceTypeHelper[] getChildren(Object parentElement) {
        return (parentElement instanceof String && fInput != null) ?
                Iterables.toArray(Iterables.filter(fInput, helper -> helper.getCategoryName().equals(parentElement)), TraceTypeHelper.class)
                : new TraceTypeHelper[0];
    }

    @Override
    public String getParent(Object element) {
        return (element instanceof TraceTypeHelper) ?
            ((TraceTypeHelper) element).getCategoryName()
            : null;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof Iterable) {
            fInput = (Iterable<@NonNull TraceTypeHelper>) newInput;
        }
    }

    @Override
    public void dispose() {
        // do nothing
    }
}
