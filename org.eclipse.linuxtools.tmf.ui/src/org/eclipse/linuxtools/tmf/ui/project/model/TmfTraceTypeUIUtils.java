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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FileSystemElement;

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

    private static List<File> isolateTraces(List<FileSystemElement> selectedResources) {
        List<File> traces = new ArrayList<>();

        // Get the selection
        Iterator<FileSystemElement> resources = selectedResources.iterator();

        // Get the sorted list of unique entries
        Map<String, File> fileSystemObjects = new HashMap<>();
        while (resources.hasNext()) {
            File resource = (File) resources.next().getFileSystemObject();
            String key = resource.getAbsolutePath();
            fileSystemObjects.put(key, resource);
        }
        List<String> files = new ArrayList<>(fileSystemObjects.keySet());
        Collections.sort(files);

        // After sorting, traces correspond to the unique prefixes
        String prefix = null;
        for (int i = 0; i < files.size(); i++) {
            File file = fileSystemObjects.get(files.get(i));
            String name = file.getAbsolutePath();
            if (prefix == null || !name.startsWith(prefix)) {
                prefix = name; // new prefix
                traces.add(file);
            }
        }

        return traces;
    }

    private static List<TraceTypeHelper> reduce(List<TraceTypeHelper> candidates) {
        List<TraceTypeHelper> retVal = new ArrayList<>();

        // get all the tracetypes that are unique in that stage
        for (TraceTypeHelper trace : candidates) {
            if (isUnique(trace, candidates)) {
                retVal.add(trace);
            }
        }
        return retVal;
    }

    /*
     * Only return the leaves of the trace types. Ignore custom trace types.
     */
    private static boolean isUnique(TraceTypeHelper trace, List<TraceTypeHelper> set) {
        if (isCustomTraceId(trace.getCanonicalName())) {
            return true;
        }
        // check if the trace type is the leaf. we make an instance of the trace
        // type and if it is only an instance of itself, it is a leaf
        final ITmfTrace tmfTrace = trace.getTrace();
        int count = -1;
        for (TraceTypeHelper child : set) {
            final ITmfTrace traceCandidate = child.getTrace();
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

    private static TraceTypeHelper getTraceTypeToSet(TmfTraceType type, List<TraceTypeHelper> candidates, Shell shell) {
        final Map<String, String> names = new HashMap<>();
        Shell shellToShow = new Shell(shell);
        shellToShow.setText(Messages.TmfTraceType_SelectTraceType);
        final String candidatesToSet[] = new String[1];
        for (TraceTypeHelper candidate : candidates) {
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
     * validate list of traces with a tracetype
     *
     * @param type
     *            The TmfTraceType instance
     * @param traceTypeName
     *            the trace category (canonical name)
     * @param selectedResources
     *            List of traces to validate
     * @return true if all the traces are valid
     */
    public static boolean validateTrace(TmfTraceType type, String traceTypeName, List<FileSystemElement> selectedResources) {
        List<File> traces = isolateTraces(selectedResources);
        return type.validateTraceFiles(traceTypeName, traces);
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
        List<TraceTypeHelper> validCandidates = new ArrayList<>();
        final Iterable<String> traceTypes = type.getTraceTypeIDs();
        for (String traceType : traceTypes) {
            if (type.validate(traceType, path)) {
                validCandidates.add(type.getTraceTypeHelper(traceType));
            }
        }

        TraceTypeHelper traceTypeToSet = null;
        if (validCandidates.isEmpty()) {
            final String errorMsg = Messages.TmfOpenTraceHelper_NoTraceTypeMatch + path;
            throw new TmfTraceImportException(errorMsg);
        } else if (validCandidates.size() != 1) {
            List<TraceTypeHelper> reducedCandidates = reduce(validCandidates);
            for (TraceTypeHelper tth : reducedCandidates) {
                if (tth.getCanonicalName().equals(traceTypeHint)) {
                    traceTypeToSet = tth;
                }
            }
            if (traceTypeToSet == null) {
                if (reducedCandidates.size() == 0) {
                    throw new TmfTraceImportException(Messages.TmfOpenTraceHelper_ReduceError);
                } else if (reducedCandidates.size() == 1) {
                    traceTypeToSet = reducedCandidates.get(0);
                } else {
                    if (shell == null) {
                        return null;
                    }
                    traceTypeToSet = getTraceTypeToSet(type, reducedCandidates, shell);
                }
            }
        } else {
            traceTypeToSet = validCandidates.get(0);
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
