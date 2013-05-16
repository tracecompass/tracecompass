/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;

/**
 * A helper class to show the trace types and files and names. it contains the
 * model which can be defined as follows : {tracetype -> { file1, file2, ... }
 * }+
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
class ImportTraceContentProvider implements ITreeContentProvider {

    private final Map<String, String> fTraceTypes = new HashMap<String, String>();
    private final Map<String, Set<FileAndName>> fTraceFiles = new HashMap<String, Set<FileAndName>>();
    private final List<String> fTraceTypesToScan;
    private final Set<String> fParentFilesToScan;

    public ImportTraceContentProvider(List<String> traceTypesToScan, Set<String> parentFilesToScan) {
        fTraceTypesToScan = traceTypesToScan;
        fParentFilesToScan = parentFilesToScan;
    }

    /**
     * Add a trace candidate to display
     *
     * @param category
     *            the category of the trace
     * @param traceToOpen
     *            the trace file.
     */
    public synchronized void addCandidate(String category, File traceToOpen) {
        fTraceTypes.put(TmfTraceType.getInstance().getTraceType(category).getName(), category);
        if (!fTraceFiles.containsKey(category)) {
            fTraceFiles.put(category, new TreeSet<FileAndName>());
        }
        final FileAndName traceFile = new FileAndName(traceToOpen, traceToOpen.getName());
        traceFile.setTraceTypeId(category);
        final Set<FileAndName> categorySet = fTraceFiles.get(category);
        categorySet.add(traceFile);
    }

    /**
     * Reset all the candidates
     */
    public synchronized void clearCandidates() {
        fTraceTypes.clear();
        fTraceFiles.clear();
    }

    @Override
    public void dispose() {
        fTraceFiles.clear();
        fTraceTypes.clear();

    }

    @Override
    public synchronized void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (oldInput != newInput && newInput != null) {
            ImportTraceContentProvider input = (ImportTraceContentProvider) newInput;
            clearCandidates();
            fTraceTypes.putAll(input.fTraceTypes);
            fTraceFiles.putAll(fTraceFiles);
        }
    }

    @Override
    public synchronized Object[] getElements(Object inputElement) {
        List<String> candidates = new ArrayList<String>();

        for (String candidate : fTraceTypesToScan) {
            for (Entry<String, String> entry : fTraceTypes.entrySet()) {
                if (entry.getValue().equals(candidate)) {
                    candidates.add(entry.getKey());
                    break;
                }
            }

        }
        return candidates.toArray(new String[candidates.size()]);
    }

    @Override
    public synchronized Object[] getChildren(Object parentElement) {
        if (parentElement instanceof String) {
            final Set<FileAndName> children = fTraceFiles.get(fTraceTypes.get(parentElement));
            if (children != null) {
                Set<FileAndName> candidates = new TreeSet<FileAndName>();
                for (FileAndName child : children) {
                    for (String parent : fParentFilesToScan) {
                        // this is going to be slow, but less slow than UI
                        // display and should not be done for more than 10k
                        // elements.
                        if (child.getFile().getAbsolutePath().startsWith(parent)) {
                            candidates.add(child);
                        }
                    }
                }
                return candidates.toArray(new FileAndName[0]);
            }
        }
        return null;
    }

    /**
     * Gets the brothers and systems of a file element
     *
     * @param element
     *            the child leaf
     * @return the siblings of an element, including itself. Should never be
     *         null
     */
    public synchronized FileAndName[] getSiblings(FileAndName element) {
        String key = (String) getParent(element);
        return (FileAndName[]) getChildren(key);

    }

    @Override
    public synchronized Object getParent(Object element) {
        if (element instanceof FileAndName) {
            for (String key : fTraceFiles.keySet()) {
                Set<FileAndName> fanSet = fTraceFiles.get(key);
                if (fanSet.contains(element)) {
                    return key;
                }
            }
        }
        return null;
    }

    @Override
    public synchronized boolean hasChildren(Object element) {
        if (element instanceof String) {
            String key = (String) element;
            return fTraceFiles.containsKey(fTraceTypes.get(key));
        }
        return false;
    }

    /**
     * Gets the number of traces to import
     *
     * @return the number of traces to import
     */
    public synchronized int getSize() {
        int tot = 0;
        for (String s : fTraceFiles.keySet()) {
            tot += fTraceFiles.get(s).size();
        }
        return tot;
    }
}