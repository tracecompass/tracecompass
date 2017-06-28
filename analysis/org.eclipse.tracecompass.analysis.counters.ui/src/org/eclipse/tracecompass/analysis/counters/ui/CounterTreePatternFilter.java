/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TreePatternFilter;

import com.google.common.base.Splitter;

/**
 * A filter extending the
 * <code>org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TreePatternFilter<code>.
 *
 * The user can now enter multiple filters (separated by '/'). The filter also
 * detects a match if a subsequence of a tree element matches the pattern.
 *
 * @author Mikael Ferland
 */
public class CounterTreePatternFilter extends TreePatternFilter {

    /**
     * List of compiled regular expressions for filtering the tree elements
     */
    private final List<Pattern> fPatterns = new ArrayList<>();

    /**
     * Cached matchers used for comparing regexes to Strings
     */
    private final Map<String, Matcher> fMatchers = new HashMap<>();

    @Override
    public void setPattern(String patternString) {
        fPatterns.clear();
        fMatchers.clear();
        super.setPattern(patternString);

        /*
         * The asterisk (*) wildcard needs to be converted to a period (.) for it to be
         * recognized by the Pattern class during regex compilation
         */
        Iterable<String> filters = Splitter.on('/').split(patternString.replace('*', '.'));
        filters.forEach(filter -> fPatterns.add(Pattern.compile(filter, Pattern.CASE_INSENSITIVE)));
    }

    @Override
    protected boolean isLeafMatch(Viewer viewer, Object element) {
        Object node = element;
        StructuredViewer structuredViewer = (StructuredViewer) viewer;
        ILabelProvider labelProvider = (ILabelProvider) structuredViewer.getLabelProvider();

        // Ensure the tree element and its parent(s) match the filter text
        ListIterator<Pattern> iter = fPatterns.listIterator(fPatterns.size());
        while (iter.hasPrevious()) {
            // Retrieve tree element text and make verification
            String labelText = labelProvider.getText(node);
            if (!wordMatches(labelText, iter.previous())) {
                return false;
            }

            // Retrieve parent element
            IContentProvider contentProvider = ((StructuredViewer) viewer).getContentProvider();
            node = ((ITreeContentProvider) contentProvider).getParent(node);
        }
        return true;
    }

    private boolean wordMatches(String text, Pattern pattern) {
        Matcher matcher = fMatchers.containsKey(text) ? fMatchers.get(text) : pattern.matcher(text);
        return (text != null) && (matcher != null) && (matcher.find());
    }
}
