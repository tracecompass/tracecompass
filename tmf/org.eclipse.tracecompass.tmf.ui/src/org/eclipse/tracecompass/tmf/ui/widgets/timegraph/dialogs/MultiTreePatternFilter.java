/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * A filter extending the
 * <code>org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TreePatternFilter<code>.
 *
 * The user can now enter multiple filters (separated by '/'). The filter also
 * detects a match if a subsequence of a tree element matches the pattern.
 *
 * @author Mikael Ferland
 * @since 3.2
 */
public class MultiTreePatternFilter extends TreePatternFilter {

    /**
     * List of Predicates to test the column texts, ordered from leaf to root.
     */
    private final List<Predicate<String>> fPredicates = new ArrayList<>();

    @Override
    public void setPattern(String patternString) {
        fPredicates.clear();
        super.setPattern(patternString);
        if (patternString == null || patternString.isEmpty()) {
            return;
        }

        /*
         * The asterisk (*) wildcard needs to be converted to a period (.) for it to be
         * recognized by the Pattern class during regex compilation
         */
        String[] split = patternString.replace('*', '.').split("/"); //$NON-NLS-1$
        for (int i = split.length - 1; i >= 0; i--) {
            String filter = split[i];
            Predicate<String> predicate;
            try {
                Pattern pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
                predicate = s -> pattern.matcher(s).find();
            } catch (PatternSyntaxException e) {
                predicate = s -> s.contains(filter);
            }
            fPredicates.add(predicate);
        }
    }

    @Override
    protected boolean isLeafMatch(Viewer viewer, Object element) {
        Object node = element;
        StructuredViewer structuredViewer = (StructuredViewer) viewer;
        ITableLabelProvider labelProvider = (ITableLabelProvider) structuredViewer.getLabelProvider();
        ITreeContentProvider treeContentProvider = (ITreeContentProvider) structuredViewer.getContentProvider();

        // Ensure the tree element and its parent(s) match the filter text
        for (Predicate<String> p : fPredicates) {
            // Retrieve tree element text and make verification. Text is at column 0
            String labelText = labelProvider.getColumnText(node, 0);
            if (labelText == null || !p.test(labelText)) {
                return false;
            }

            // Retrieve parent element
            node = treeContentProvider.getParent(node);
        }
        return true;
    }
}
