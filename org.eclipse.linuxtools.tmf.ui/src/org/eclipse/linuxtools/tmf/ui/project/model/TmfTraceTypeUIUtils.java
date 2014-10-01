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
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomEventTableColumns;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType.TraceElementType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.util.Pair;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

/**
 * Utils class for the UI-specific parts of @link {@link TmfTraceType}.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
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

    /** Extension point element 'Event Table Columns'
     * @since 3.2*/
    public static final String EVENT_TABLE_COLUMNS = "eventTableColumns"; //$NON-NLS-1$

    /** Extension point attribute 'tracetype' */
    public static final String TRACETYPE_ATTR = "tracetype"; //$NON-NLS-1$

    /** Extension point attribute 'icon' */
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$

    /** Extension point attribute 'class' (attribute of other elements) */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    private TmfTraceTypeUIUtils() {
    }

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
        if (trace.getTraceClass().equals(CustomTxtTrace.class) ||
                trace.getTraceClass().equals(CustomXmlTrace.class)) {
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

    private static TraceTypeHelper getTraceTypeToSet(List<Pair<Integer, TraceTypeHelper>> candidates, Shell shell) {
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
        return TmfTraceType.getTraceType(candidatesToSet[0]);
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
        final Iterable<TraceTypeHelper> traceTypeHelpers = TmfTraceType.getTraceTypeHelpers();
        for (TraceTypeHelper traceTypeHelper : traceTypeHelpers) {
            if (traceTypeHelper.isExperimentType()) {
                continue;
            }
            int confidence = traceTypeHelper.validateWithConfidence(path);
            if (confidence >= 0) {
                // insert in the tree map, ordered by confidence (highest confidence first) then name
                Pair<Integer, TraceTypeHelper> element = new Pair<>(confidence, traceTypeHelper);
                validCandidates.add(element);
            }
        }

        TraceTypeHelper traceTypeToSet = null;
        if (validCandidates.isEmpty()) {
            final String errorMsg = NLS.bind(Messages.TmfOpenTraceHelper_NoTraceTypeMatch, path);
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
                    traceTypeToSet = getTraceTypeToSet(reducedCandidates, shell);
                }
            }
        } else {
            traceTypeToSet = validCandidates.first().getSecond();
        }
        return traceTypeToSet;
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
         * @since 3.1
         */
    public static IStatus setTraceType(IResource resource, TraceTypeHelper traceType, boolean refresh) throws CoreException {
        String traceTypeId = traceType.getCanonicalName();

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
     * @since 3.2
     */
    public static @Nullable TmfEventsTable getEventTable(ITmfTrace trace, Composite parent, int cacheSize) {
        final String traceType = getTraceType(trace);
        if (traceType == null) {
            return null;
        }

        for (final IConfigurationElement ce : TmfTraceTypeUIUtils.getTypeUIElements(TraceElementType.TRACE)) {
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
     * Get the Event Table columns specified by the trace type's extension
     * point, if there are any.
     *
     * @param trace
     *            The trace for which we want the columns.
     * @return The corresponding event table columns, or 'null' if this trace
     *         type did not specify any.
     * @since 3.2
     */
    public static @Nullable Collection<? extends TmfEventTableColumn> getEventTableColumns(ITmfTrace trace) {
        final String traceType = getTraceType(trace);
        if (traceType == null) {
            return null;
        }

        /*
         * Custom traces are a special case : the columns are defined by the
         * trace definition.
         */
        if (traceType.startsWith(CustomTxtTrace.class.getCanonicalName())) {
            return CustomEventTableColumns.generateColumns(((CustomTxtTrace) trace).getDefinition());
        }
        if (traceType.startsWith(CustomXmlTrace.class.getCanonicalName())) {
            return CustomEventTableColumns.generateColumns(((CustomXmlTrace) trace).getDefinition());
        }

        /* For all other trace types, we will go look into the extension point */
        for (final IConfigurationElement ce : TmfTraceTypeUIUtils.getTypeUIElements(TraceElementType.TRACE)) {
            if (ce.getAttribute(TmfTraceTypeUIUtils.TRACETYPE_ATTR).equals(traceType)) {
                final IConfigurationElement[] eventTableColumnsCE = ce.getChildren(TmfTraceTypeUIUtils.EVENT_TABLE_COLUMNS);

                if (eventTableColumnsCE.length != 1) {
                    break;
                }
                final String eventTableColumnsClass = eventTableColumnsCE[0].getAttribute(TmfTraceTypeUIUtils.CLASS_ATTR);
                if ((eventTableColumnsClass == null) || (eventTableColumnsClass.isEmpty())) {
                    break;
                }
                try {
                    final Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    final Class<?> c = bundle.loadClass(eventTableColumnsClass);
                    final Constructor<?> ctor = c.getConstructor();
                    ITmfEventTableColumns cols = (ITmfEventTableColumns) ctor.newInstance();
                    return cols.getEventTableColumns();

                } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException |
                        IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    return null;
                }
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
