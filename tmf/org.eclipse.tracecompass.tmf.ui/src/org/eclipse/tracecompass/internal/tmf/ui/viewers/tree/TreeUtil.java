/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.tree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;

/**
 * Tree Utils, good for finding equivalent elements
 *
 * @author Matthew Khouzam
 */
public final class TreeUtil {

    private TreeUtil() {
        // do nothing
    }

    /**
     * Find new selected item
     *
     * @param selection
     *            the selected element
     * @param rootEntry
     *            the element to search through recursively
     * @return the new selected element
     */
    public static ISelection getNewSelection(ISelection selection, @NonNull ITmfTreeViewerEntry rootEntry) {
        if (selection instanceof StructuredSelection && !selection.isEmpty()) {
            StructuredSelection structuredSelection = (StructuredSelection) selection;
            Object element = structuredSelection.getFirstElement();
            if (element instanceof ITmfTreeViewerEntry) {
                ITmfTreeViewerEntry newSelection = findEquivalent(rootEntry, (ITmfTreeViewerEntry) element);
                return newSelection != null ? new StructuredSelection(newSelection) : StructuredSelection.EMPTY;
            }
        }
        return selection;
    }

    /**
     * Recursively add all element paths of items of a tree to a collection
     *
     * @param collection
     *            the collection of strings to add to
     * @param entry
     *            the root entry
     */
    public static void add(Collection<String> collection, @NonNull ITmfTreeViewerEntry entry) {
        collection.add(getPath(entry).toString());
        for (ITmfTreeViewerEntry child : entry.getChildren()) {
            add(collection, child);
        }
    }

    /**
     * Add an tree entry to the collection if a condition is met
     *
     * @param toAdd
     *            the collection to add to
     * @param parent
     *            the parent node
     * @param condition
     *            the condition to add to
     */
    public static void addIf(Collection<@NonNull ITmfTreeViewerEntry> toAdd, @NonNull ITmfTreeViewerEntry parent, Predicate<@NonNull ITmfTreeViewerEntry> condition) {
        if (condition.test(parent)) {
            toAdd.add(parent);
        }
        for (ITmfTreeViewerEntry child : parent.getChildren()) {
            addIf(toAdd, child, condition);
        }
    }

    /**
     * Gets the path of an entry
     *
     * @param entry
     *            the entry (e.g. /root/foo/bar)
     * @return the path as a collection (e.g. ["root", "foo", "bar"])
     */
    public static Collection<String> getPath(@NonNull ITmfTreeViewerEntry entry) {
        Deque<String> retVal = new ArrayDeque<>();
        ITmfTreeViewerEntry current = entry;
        while (current.getParent() != null) {
            retVal.addFirst(current.getName());
            current = current.getParent();
        }
        return retVal;
    }

    /**
     * Find an element in a new tree with the same path as the item to search
     * for. e.g. if there is a tree
     * <ul>
     * <li>root
     * <ul>
     * <li>foo
     * <ul>
     * <li>baz</li>
     * </ul>
     * </li>
     * <li>baz</li>
     * </ul>
     * </ul>
     *
     * and we search for <strong>/foo/bar</strong>
     *
     * then it will return the bolded entry
     *
     * <ul>
     * <li>root
     * <ul>
     * <li>foo
     * <ul>
     * <li><strong>baz</strong></li>
     * </ul>
     * </li>
     * <li>baz</li>
     * </ul>
     * </ul>
     *
     * @param entriesToSearch
     *            the root of the entries to search
     * @param selectedItem
     *            the item to search
     * @return the equivalent item in the new tree or null if not found
     */
    private static @Nullable ITmfTreeViewerEntry findEquivalent(@NonNull ITmfTreeViewerEntry entriesToSearch, @NonNull ITmfTreeViewerEntry selectedItem) {
        Collection<String> path = getPath(selectedItem);
        Iterator<String> iter = path.iterator();
        ITmfTreeViewerEntry currentEntry = entriesToSearch;
        while (iter.hasNext()) {
            String current = iter.next();
            boolean found = false;
            for (ITmfTreeViewerEntry child : currentEntry.getChildren()) {
                if (Objects.equals(child.getName(), current)) {
                    found = true;
                    currentEntry = child;
                    break;
                }
            }
            if (!found) {
                return null;
            }

        }
        return currentEntry;
    }

}
