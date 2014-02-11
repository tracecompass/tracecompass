/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.util.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Utils class for the UI-specific parts of @link {@link TmfTraceType}.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public final class TmfTraceTypeUIUtils {

    private static final String DEFAULT_TRACE_ICON_PATH = "icons" + File.separator + "elcl16" + File.separator + "trace.gif"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private static final char SEPARATOR = ':';

    private TmfTraceTypeUIUtils() {}

    private static List<Pair<Integer, TraceTypeHelper>> reduce(List<Pair<Integer, TraceTypeHelper>> candidates) {
        List<Pair<Integer, TraceTypeHelper>> retVal = new ArrayList<>();

        // get all the tracetypes that are unique in that stage
        for (Pair<Integer, TraceTypeHelper> candidatePair : candidates) {
            TraceTypeHelper candidate = candidatePair.getSecond();
            if (isUnique(candidate, candidates)) {
                retVal.add(candidatePair);
            }
        }
        return retVal;
    }

    /*
     * Only return the leaves of the trace types. Ignore custom trace types.
     */
    private static boolean isUnique(TraceTypeHelper trace, List<Pair<Integer, TraceTypeHelper>> set) {
        if (isCustomTraceId(trace.getCanonicalName())) {
            return true;
        }
        // check if the trace type is the leaf. we make an instance of the trace
        // type and if it is only an instance of itself, it is a leaf
        final ITmfTrace tmfTrace = trace.getTrace();
        int count = -1;
        for (Pair<Integer, TraceTypeHelper> child : set) {
            final ITmfTrace traceCandidate = child.getSecond().getTrace();
            if (tmfTrace.getClass().isInstance(traceCandidate)) {
                count++;
            }
        }
        return count == 0;
    }


    /**
     * Is the trace type id a custom (user-defined) trace type. These are the
     * traces like : text and xml defined by the custom trace wizard.
     *
     * @param traceTypeId
     *            the trace type id
     * @return true if the trace is a custom type
     */
    private static boolean isCustomTraceId(String traceTypeId) {
        TraceTypeHelper traceType = TmfTraceType.getInstance().getTraceType(traceTypeId);
        if (traceType != null) {
            return TmfTraceType.isCustomTrace(traceType.getCategoryName() + SEPARATOR + traceType.getName());
        }
        return false;
    }

    private static TraceTypeHelper getTraceTypeToSet(TmfTraceType type, List<Pair<Integer, TraceTypeHelper>> candidates, Shell shell) {
        final Map<String, String> names = new HashMap<>();
        Shell shellToShow = new Shell(shell);
        shellToShow.setText(Messages.TmfTraceType_SelectTraceType);
        final String candidatesToSet[] = new String[1];
        for (Pair<Integer, TraceTypeHelper> candidatePair : candidates) {
            TraceTypeHelper candidate = candidatePair.getSecond();
            Button b = new Button(shellToShow, SWT.RADIO);
            final String displayName = candidate.getCategoryName() + ':' + candidate.getName();
            b.setText(displayName);
            names.put(displayName, candidate.getCanonicalName());

            b.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    final Button source = (Button) e.getSource();
                    candidatesToSet[0] = (names.get(source.getText()));
                    source.getParent().dispose();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {

                }
            });
        }
        shellToShow.setLayout(new RowLayout(SWT.VERTICAL));
        shellToShow.pack();
        shellToShow.open();

        Display display = shellToShow.getDisplay();
        while (!shellToShow.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return type.getTraceTypeHelper(candidatesToSet[0]);
    }

    /**
     * This member figures out the trace type of a given file. It will prompt
     * the user if it needs more information to properly pick the trace type.
     *
     * @param path
     *            The path of file to import
     * @param shell
     *            a shell to display the message to. If it is null, it is
     *            assumed to be cancelled.
     * @param traceTypeHint
     *            the ID of a trace (like "o.e.l.specifictrace" )
     * @return null if the request is cancelled or a TraceTypeHelper if it
     *         passes.
     * @throws TmfTraceImportException
     *             if the traces don't match or there are errors in the trace
     *             file
     */
    public static TraceTypeHelper selectTraceType(String path, Shell shell, String traceTypeHint) throws TmfTraceImportException {
        TmfTraceType type = TmfTraceType.getInstance();

        Comparator<Pair<Integer, TraceTypeHelper>> comparator = new Comparator<Pair<Integer, TraceTypeHelper>>() {
            @Override
            public int compare(Pair<Integer, TraceTypeHelper> o1, Pair<Integer, TraceTypeHelper> o2) {
                int res = -o1.getFirst().compareTo(o2.getFirst()); // invert so that highest confidence is first
                if (res == 0) {
                    res = o1.getSecond().getName().compareTo(o2.getSecond().getName());
                }
                return res;
            }
        };
        TreeSet<Pair<Integer, TraceTypeHelper>> validCandidates = new TreeSet<>(comparator);
        final Iterable<TraceTypeHelper> traceTypeHelpers = type.getTraceTypeHelpers();
        for (TraceTypeHelper traceTypeHelper : traceTypeHelpers) {
            int confidence = traceTypeHelper.validateWithConfidence(path);
            if (confidence >= 0) {
                // insert in the tree map, ordered by confidence (highest confidence first) then name
                Pair<Integer, TraceTypeHelper> element = new Pair<>(confidence, traceTypeHelper);
                validCandidates.add(element);
            }
        }

        TraceTypeHelper traceTypeToSet = null;
        if (validCandidates.isEmpty()) {
            final String errorMsg = Messages.TmfOpenTraceHelper_NoTraceTypeMatch + path;
            throw new TmfTraceImportException(errorMsg);
        } else if (validCandidates.size() != 1) {
            List<Pair<Integer, TraceTypeHelper>> candidates = new ArrayList<>(validCandidates);
            List<Pair<Integer, TraceTypeHelper>> reducedCandidates = reduce(candidates);
            for (Pair<Integer, TraceTypeHelper> candidatePair : reducedCandidates) {
                TraceTypeHelper candidate = candidatePair.getSecond();
                if (candidate.getCanonicalName().equals(traceTypeHint)) {
                    traceTypeToSet = candidate;
                    break;
                }
            }
            if (traceTypeToSet == null) {
                if (reducedCandidates.size() == 0) {
                    throw new TmfTraceImportException(Messages.TmfOpenTraceHelper_ReduceError);
                } else if (reducedCandidates.size() == 1) {
                    traceTypeToSet = reducedCandidates.get(0).getSecond();
                } else if (shell == null) {
                    Pair<Integer, TraceTypeHelper> candidate = reducedCandidates.get(0);
                    // if the best match has lowest confidence, don't select it
                    if (candidate.getFirst() > 0) {
                        traceTypeToSet = candidate.getSecond();
                    }
                } else {
                    traceTypeToSet = getTraceTypeToSet(type, reducedCandidates, shell);
                }
            }
        } else {
            traceTypeToSet = validCandidates.first().getSecond();
        }
        return traceTypeToSet;
    }


    /**
     * Set the trace type of a {@Link TraceTypeHelper}. Should only be
     * used internally by this project.
     *
     * @param path
     *            the {@link IPath} path of the resource to set
     * @param traceType
     *            the {@link TraceTypeHelper} to set the trace type to.
     * @return Status.OK_Status if successful, error is otherwise.
     * @throws CoreException
     *             An exception caused by accessing eclipse project items.
     */
    public static IStatus setTraceType(IPath path, TraceTypeHelper traceType) throws CoreException {
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        String traceBundle = null, traceTypeId = traceType.getCanonicalName(), traceIcon = null;
        if (isCustomTraceId(traceTypeId)) {
            traceBundle = Activator.getDefault().getBundle().getSymbolicName();
            traceIcon = DEFAULT_TRACE_ICON_PATH;
        } else {
            IConfigurationElement ce = TmfTraceType.getInstance().getTraceAttributes(traceTypeId);
            traceBundle = ce.getContributor().getName();
            traceIcon = ce.getAttribute(TmfTraceType.ICON_ATTR);
        }

        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, traceBundle);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceTypeId);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, traceIcon);

        TmfProjectElement tmfProject = TmfProjectRegistry.getProject(resource.getProject(), true);
        final TmfTraceFolder tracesFolder = tmfProject.getTracesFolder();
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        for (TmfTraceElement traceElement : traces) {
            if (traceElement.getName().equals(resource.getName())) {
                traceElement.refreshTraceType();
                break;
            }
        }
        tmfProject.refresh();
        return Status.OK_STATUS;
    }
}
