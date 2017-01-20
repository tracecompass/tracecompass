/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *   Bernd Hufmann - Update trace type auto-detection
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType.TraceElementType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTable;
import org.osgi.framework.Bundle;

/**
 * Utils class for the UI-specific parts of @link {@link TmfTraceType}.
 *
 * @author Alexandre Montplaisir
 */
public final class TmfTraceTypeUIUtils {

    /** Extension point ID */
    public static final String TMF_TRACE_TYPE_UI_ID = "org.eclipse.linuxtools.tmf.ui.tracetypeui"; //$NON-NLS-1$

    /** Extension point element 'type' (should match the type in TmfTraceType) */
    public static final String TYPE_ELEM = "type"; //$NON-NLS-1$

    /**
     * Extension point element 'experiment' (should match the type in
     * TmfTraceType)
     */
    public static final String EXPERIMENT_ELEM = "experiment"; //$NON-NLS-1$

    /** Extension point element 'Default editor' */
    public static final String DEFAULT_EDITOR_ELEM = "defaultEditor"; //$NON-NLS-1$

    /** Extension point element 'Events table type' */
    public static final String EVENTS_TABLE_TYPE_ELEM = "eventsTableType"; //$NON-NLS-1$

    /** Extension point element 'Event Table Columns' */
    public static final String EVENT_TABLE_COLUMNS = "eventTableColumns"; //$NON-NLS-1$

    /** Extension point element 'perspective'
     * @since 2.3*/
    public static final String PERSPECTIVE_ELEM = "perspective"; //$NON-NLS-1$

    /** Extension point attribute 'id'
     * @since 2.3*/
    public static final String ID_ATTR = "id"; //$NON-NLS-1$

    /** Extension point attribute 'tracetype' */
    public static final String TRACETYPE_ATTR = "tracetype"; //$NON-NLS-1$

    /** Extension point attribute 'icon' */
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$

    /** Extension point attribute 'class' (attribute of other elements) */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    private TmfTraceTypeUIUtils() {
    }

    private static TraceTypeHelper getTraceTypeToSet(List<TraceTypeHelper> candidates, Shell shell) {
        final Map<String, String> names = new HashMap<>();
        Shell shellToShow = new Shell(shell);
        shellToShow.setText(Messages.TmfTraceType_SelectTraceType);
        final String candidatesToSet[] = new String[1];
        for (TraceTypeHelper candidate : candidates) {
            Button b = new Button(shellToShow, SWT.RADIO);
            final String displayName = candidate.getCategoryName() + ':' + candidate.getName();
            b.setText(displayName);
            names.put(displayName, candidate.getTraceTypeId());

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
        return TmfTraceType.getTraceType(candidatesToSet[0]);
    }

    /**
     * This member figures out the trace type of a given trace. It will prompt
     * the user if it needs more information to properly pick the trace type.
     *
     * @param path
     *            The path of trace to import (file or directory for directory
     *            traces)
     * @param shell
     *            a shell to query user in case of multiple valid trace types.
     *            If it is null, than the first one alphabetically is selected.
     * @param traceTypeHint
     *            the ID of a trace (like "o.e.l.specifictrace" )
     * @return {@link TraceTypeHelper} for valid trace type or null if no valid
     *         trace type was found in case of single file trace
     * @throws TmfTraceImportException
     *             if there are errors in the trace file or no trace type found
     *             for a directory trace
     */
    public static @Nullable TraceTypeHelper selectTraceType(String path, Shell shell, String traceTypeHint) throws TmfTraceImportException {
        List<TraceTypeHelper> candidates = TmfTraceType.selectTraceType(path, traceTypeHint);

        if (candidates.isEmpty()) {
            return null;
        }

        if ((candidates.size() == 1) || (shell == null)) {
            return candidates.get(0);
        }

        return getTraceTypeToSet(candidates, shell);
    }

    /**
     * Set the trace type of a {@link TraceTypeHelper}. Should only be
     * used internally by this project.
     *
     * @param resource
     *            the resource to set
     * @param traceType
     *            the {@link TraceTypeHelper} to set the trace type to.
     * @return Status.OK_Status if successful, error is otherwise.
     * @throws CoreException
     *             An exception caused by accessing eclipse project items.
     */
    public static IStatus setTraceType(IResource resource, TraceTypeHelper traceType) throws CoreException {
        return setTraceType(resource, traceType, true);
    }

    /**
     * Set the trace type of a {@link TraceTypeHelper}. Should only be
     * used internally by this project.
     *
     * @param resource
     *            the resource to set
     * @param traceType
     *            the {@link TraceTypeHelper} to set the trace type to.
     * @param refresh
     *            Flag for refreshing the project
     * @return Status.OK_Status if successful, error is otherwise.
     * @throws CoreException
     *             An exception caused by accessing eclipse project items.
     */
    public static IStatus setTraceType(IResource resource, TraceTypeHelper traceType, boolean refresh) throws CoreException {
        String traceTypeId = traceType.getTraceTypeId();

        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceTypeId);

        TmfProjectElement tmfProject = TmfProjectRegistry.getProject(resource.getProject(), true);
        if (tmfProject.getTracesFolder().getPath().isPrefixOf(resource.getFullPath())) {
            String elementPath = resource.getFullPath().makeRelativeTo(tmfProject.getTracesFolder().getPath()).toString();
            refreshTraceElement(tmfProject.getTracesFolder().getTraces(), elementPath);
        } else if (resource.getParent().equals(tmfProject.getExperimentsFolder().getResource())) {
            /* The trace type to set is for an experiment */
            for (TmfExperimentElement experimentElement : tmfProject.getExperimentsFolder().getExperiments()) {
                if (resource.equals(experimentElement.getResource())) {
                    experimentElement.refreshTraceType();
                    break;
                }
            }
        } else {
            for (TmfExperimentElement experimentElement : tmfProject.getExperimentsFolder().getExperiments()) {
                if (experimentElement.getPath().isPrefixOf(resource.getFullPath())) {
                    String elementPath = resource.getFullPath().makeRelativeTo(experimentElement.getPath()).toString();
                    refreshTraceElement(experimentElement.getTraces(), elementPath);
                    break;
                }
            }
        }
        if (refresh) {
            tmfProject.refresh();
        }
        return Status.OK_STATUS;
    }

    private static void refreshTraceElement(List<TmfTraceElement> traceElements, String elementPath) {
        for (TmfTraceElement traceElement : traceElements) {
            if (traceElement.getElementPath().equals(elementPath)) {
                traceElement.refreshTraceType();
                break;
            }
        }
    }

    /**
     * Retrieves all configuration elements from the platform extension registry
     * for the trace type UI extension.
     *
     * @param elType
     *            The type of trace type requested, either TRACE or EXPERIMENT
     * @return An array of trace type configuration elements
     */
    public static IConfigurationElement[] getTypeUIElements(TraceElementType elType) {
        String elementName = TYPE_ELEM;
        if (elType == TraceElementType.EXPERIMENT) {
            elementName = EXPERIMENT_ELEM;
        }
        IConfigurationElement[] elements =
                Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_TRACE_TYPE_UI_ID);
        List<IConfigurationElement> typeElements = new LinkedList<>();
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(elementName)) {
                typeElements.add(element);
            }
        }
        return typeElements.toArray(new IConfigurationElement[typeElements.size()]);
    }

    /**
     * Get the UI elements for the given trace type
     *
     * @param traceType
     *            The tracetype ID
     * @param elType
     *            The type of trace type requested, either TRACE or EXPERIMENT
     * @return The top-level configuration element (access its children with
     *         .getChildren()). Or null if there is no such element.
     */
    @Nullable
    public static IConfigurationElement getTraceUIAttributes(String traceType, TraceElementType elType) {
        IConfigurationElement[] elements = getTypeUIElements(elType);
        for (IConfigurationElement ce : elements) {
            if (traceType.equals(ce.getAttribute(TRACETYPE_ATTR))) {
                return ce;
            }
        }
        return null;
    }

    /**
     * Get the Event Table type specified by the trace type's extension point,
     * if there is one.
     *
     * @param trace
     *            The trace for which we want the events table.
     * @param parent
     *            The parent composite that the event table will have
     * @param cacheSize
     *            The cache size to use with this event table. Should be defined
     *            by the trace type.
     * @return The corresponding Event Table, or 'null' if this trace type did
     *         not specify any.
     */
    public static @Nullable TmfEventsTable getEventTable(ITmfTrace trace, Composite parent, int cacheSize) {
        final String traceType = getTraceType(trace);
        if (traceType == null) {
            return null;
        }

        TraceElementType elType = (trace instanceof TmfExperiment) ? TraceElementType.EXPERIMENT : TraceElementType.TRACE;
        for (final IConfigurationElement ce : TmfTraceTypeUIUtils.getTypeUIElements(elType)) {
            if (ce.getAttribute(TmfTraceTypeUIUtils.TRACETYPE_ATTR).equals(traceType)) {
                final IConfigurationElement[] eventsTableTypeCE = ce.getChildren(TmfTraceTypeUIUtils.EVENTS_TABLE_TYPE_ELEM);

                if (eventsTableTypeCE.length != 1) {
                    break;
                }
                final String eventsTableType = eventsTableTypeCE[0].getAttribute(TmfTraceTypeUIUtils.CLASS_ATTR);
                if (eventsTableType.isEmpty()) {
                    break;
                }
                try {
                    final Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    final Class<?> c = bundle.loadClass(eventsTableType);
                    final Class<?>[] constructorArgs = new Class[] { Composite.class, int.class };
                    final Constructor<?> constructor = c.getConstructor(constructorArgs);
                    final Object[] args = new Object[] { parent, cacheSize };
                    return (TmfEventsTable) constructor.newInstance(args);

                } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException |
                        IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Get the perspective id specified by the trace type's extension point, if
     * there is one.
     *
     * @param trace
     *            The trace for which we want the perspective id.
     * @return The corresponding perspective id, or 'null' if this trace type
     *         did not specify any.
     * @since 2.3
     */
    public static @Nullable String getPerspectiveId(ITmfTrace trace) {
        final String traceType = getTraceType(trace);
        if (traceType == null) {
            return null;
        }

        TraceElementType elType = (trace instanceof TmfExperiment) ? TraceElementType.EXPERIMENT : TraceElementType.TRACE;
        for (final IConfigurationElement ce : TmfTraceTypeUIUtils.getTypeUIElements(elType)) {
            if (ce.getAttribute(TRACETYPE_ATTR).equals(traceType)) {
                final IConfigurationElement[] perspectiveCE = ce.getChildren(PERSPECTIVE_ELEM);

                if (perspectiveCE.length != 1) {
                    break;
                }
                final String perspectiveId = perspectiveCE[0].getAttribute(ID_ATTR);
                if (!perspectiveId.isEmpty()) {
                    return perspectiveId;
                }
                break;
            }
        }
        return null;
    }

    /**
     * Get the trace type (as a String) for the given trace
     *
     * @param trace
     *            The trace object
     * @return The String representing the trace type, or 'null' if this trace
     *         does not advertise it.
     */
    private static @Nullable String getTraceType(ITmfTrace trace) {
        IResource res = trace.getResource();
        if (res == null) {
            return null;
        }
        try {
            String traceType = res.getPersistentProperty(TmfCommonConstants.TRACETYPE);
            /* May be null here too */
            return traceType;

        } catch (CoreException e) {
            return null;
        }
    }
}
