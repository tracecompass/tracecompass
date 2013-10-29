/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Matthew Khouzam - Added import functionalities
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
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
 * Utility class for accessing TMF trace type extensions from the platform's
 * extensions registry.
 *
 * @version 1.0
 * @author Patrick Tasse
 * @author Matthew Khouzam
 */
public final class TmfTraceType {

    private static final String DEFAULT_TRACE_ICON_PATH = "icons" + File.separator + "elcl16" + File.separator + "trace.gif"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private static final char SEPARATOR = ':';

    /**
     * Extension point ID
     */
    public static final String TMF_TRACE_TYPE_ID = "org.eclipse.linuxtools.tmf.ui.tracetype"; //$NON-NLS-1$

    /**
     * Extension point element 'Category'
     */
    public static final String CATEGORY_ELEM = "category"; //$NON-NLS-1$
    /**
     * Extension point element 'Type'
     */
    public static final String TYPE_ELEM = "type"; //$NON-NLS-1$
    /**
     * Extension point element 'Default editor'
     */
    public static final String DEFAULT_EDITOR_ELEM = "defaultEditor"; //$NON-NLS-1$
    /**
     * Extension point element 'Events table type'
     */
    public static final String EVENTS_TABLE_TYPE_ELEM = "eventsTableType"; //$NON-NLS-1$
    /**
     * Extension point element 'Statistics viewer type'
     *
     * @since 2.0
     */
    public static final String STATISTICS_VIEWER_ELEM = "statisticsViewerType"; //$NON-NLS-1$

    /**
     * Extension point attribute 'ID'
     */
    public static final String ID_ATTR = "id"; //$NON-NLS-1$
    /**
     * Extension point attribute 'name'
     */
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$
    /**
     * Extension point attribute 'category'
     */
    public static final String CATEGORY_ATTR = "category"; //$NON-NLS-1$
    /**
     * Extension point attribute 'trace_type'
     */
    public static final String TRACE_TYPE_ATTR = "trace_type"; //$NON-NLS-1$
    /**
     * Extension point attribute 'event_type'
     */
    public static final String EVENT_TYPE_ATTR = "event_type"; //$NON-NLS-1$
    /**
     * Extension point attribute 'icon'
     */
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$
    /**
     * Extension point attribute 'class'
     */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    /**
     * Custom text label used internally and therefore should not be
     * externalized
     *
     * @since 2.0
     */
    public static final String CUSTOM_TXT_CATEGORY = "Custom Text"; //$NON-NLS-1$
    /**
     * Custom XML label used internally and therefore should not be externalized
     *
     * @since 2.0
     */
    public static final String CUSTOM_XML_CATEGORY = "Custom XML"; //$NON-NLS-1$

    // The mapping of available trace type IDs to their corresponding
    // configuration element
    private final Map<String, IConfigurationElement> fTraceTypeAttributes = new HashMap<String, IConfigurationElement>();
    private final Map<String, IConfigurationElement> fTraceCategories = new HashMap<String, IConfigurationElement>();
    private final Map<String, TraceTypeHelper> fTraceTypes = new LinkedHashMap<String, TraceTypeHelper>();

    private static TmfTraceType fInstance = null;

    /**
     * Retrieves the category name from the platform extension registry based on
     * the category ID
     *
     * @param categoryId
     *            The category ID
     * @return the category name or empty string if not found
     */
    public static String getCategoryName(String categoryId) {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(TMF_TRACE_TYPE_ID);
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(CATEGORY_ELEM) && categoryId.equals(element.getAttribute(ID_ATTR))) {
                return element.getAttribute(NAME_ATTR);
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Retrieves and instantiates an element's object based on his plug-in
     * definition for a specific trace type.
     *
     * The element's object is instantiated using its 0-argument constructor.
     *
     * @param resource
     *            The resource where to find the information about the trace
     *            properties
     * @param element
     *            The name of the element to find under the trace type
     *            definition
     * @return a new Object based on his definition in plugin.xml, or null if no
     *         definition was found
     * @since 2.0
     */
    public static Object getTraceTypeElement(IResource resource, String element) {
        try {
            if (resource != null) {
                String traceType = resource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                /*
                 * Search in the configuration if there is any viewer specified
                 * for this kind of trace type.
                 */
                for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                    if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(traceType)) {
                        IConfigurationElement[] viewerCE = ce.getChildren(element);
                        if (viewerCE.length != 1) {
                            break;
                        }
                        return viewerCE[0].createExecutableExtension(TmfTraceType.CLASS_ATTR);
                    }
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating the element from the resource", e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Retrieves all configuration elements from the platform extension registry
     * for the trace type extension.
     *
     * @return an array of trace type configuration elements
     */
    public static IConfigurationElement[] getTypeElements() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(TMF_TRACE_TYPE_ID);
        List<IConfigurationElement> typeElements = new LinkedList<IConfigurationElement>();
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(TYPE_ELEM)) {
                typeElements.add(element);
            }
        }
        return typeElements.toArray(new IConfigurationElement[typeElements.size()]);
    }

    private TmfTraceType() {
        init();
    }

    /**
     * The import utils instance
     *
     * @return the import utils instance
     * @since 2.0
     */
    public static TmfTraceType getInstance() {
        if (fInstance == null) {
            fInstance = new TmfTraceType();
        }
        return fInstance;
    }

    // ------------------------------------------------------------------
    // Get trace types
    // ------------------------------------------------------------------

    /**
     * Returns a list of "category:tracetype , ..."
     *
     * @return returns a list of "category:tracetype , ..."
     * @since 2.0
     */
    public String[] getAvailableTraceTypes() {

        // Generate the list of Category:TraceType to populate the ComboBox
        List<String> traceTypes = new ArrayList<String>();

        // re-populate custom trace types
        getCustomTraceTypes();
        for (String key : this.fTraceTypes.keySet()) {
            TraceTypeHelper tt = this.fTraceTypes.get(key);
            traceTypes.add(tt.getCategoryName() + SEPARATOR + tt.getName());
        }

        // Format result
        return traceTypes.toArray(new String[traceTypes.size()]);
    }

    /**
     * Gets the custom trace types (custom text and friends)
     *
     * @param type
     *            the type to get (Text, xml or other...)
     * @return the list of custom trace types
     * @since 2.0
     */
    public static List<String> getCustomTraceTypes(String type) {
        List<String> traceTypes = new ArrayList<String>();
        if (type.equals(CUSTOM_TXT_CATEGORY)) {
            for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                String traceTypeName = def.definitionName;
                traceTypes.add(traceTypeName);
            }
        }
        if (type.equals(CUSTOM_XML_CATEGORY)) {
            for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                String traceTypeName = def.definitionName;
                traceTypes.add(traceTypeName);
            }
        }
        return traceTypes;
    }

    /**
     * Gets all the custom trace types
     *
     * @return the list of custom trace types
     * @since 2.0
     */
    public List<String> getCustomTraceTypes() {
        List<String> traceTypes = new ArrayList<String>();
        // remove the customTraceTypes
        final String[] keySet = fTraceTypes.keySet().toArray(new String[0]);
        for (String key : keySet) {
            TraceTypeHelper helper = fTraceTypes.get(key);
            if (helper.getCategoryName().equals(CUSTOM_TXT_CATEGORY) || helper.getCategoryName().equals(CUSTOM_XML_CATEGORY)) {
                helper.getTrace().dispose();
                fTraceTypes.remove(key);
            }
        }

        // add the custom trace types
        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
            String traceTypeId = CustomTxtTrace.class.getCanonicalName() + SEPARATOR + def.definitionName;
            TraceTypeHelper tt = new TraceTypeHelper(traceTypeId, CUSTOM_TXT_CATEGORY, def.definitionName, new CustomTxtTrace(def));
            fTraceTypes.put(traceTypeId, tt);
            traceTypes.add(traceTypeId);
        }
        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
            String traceTypeId = CustomXmlTrace.class.getCanonicalName() + SEPARATOR + def.definitionName;
            TraceTypeHelper tt = new TraceTypeHelper(traceTypeId, CUSTOM_XML_CATEGORY, def.definitionName, new CustomXmlTrace(def));
            fTraceTypes.put(traceTypeId, tt);
            traceTypes.add(traceTypeId);
        }
        return traceTypes;
    }

    /**
     * Gets a trace type for a given canonical id
     *
     * @param id
     *            the ID of the trace
     * @return the return type
     * @since 2.0
     */
    public TraceTypeHelper getTraceType(String id) {
        init();
        return fTraceTypes.get(id);
    }

    private void populateCategoriesAndTraceTypes() {
        if (fTraceTypes.isEmpty()) {
            // Populate the Categories and Trace Types
            IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceType.TMF_TRACE_TYPE_ID);
            for (IConfigurationElement ce : config) {
                String elementName = ce.getName();
                if (elementName.equals(TmfTraceType.TYPE_ELEM)) {
                    String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    fTraceTypeAttributes.put(traceTypeId, ce);
                } else if (elementName.equals(TmfTraceType.CATEGORY_ELEM)) {
                    String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    fTraceCategories.put(categoryId, ce);
                }
            }
            // create the trace types
            for (String typeId : fTraceTypeAttributes.keySet()) {
                IConfigurationElement ce = fTraceTypeAttributes.get(typeId);
                final String category = getCategory(ce);
                final String attribute = ce.getAttribute(TmfTraceType.NAME_ATTR);
                ITmfTrace trace = null;
                try {
                    trace = (ITmfTrace) ce.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
                } catch (CoreException e) {
                }
                TraceTypeHelper tt = new TraceTypeHelper(typeId, category, attribute, trace);
                fTraceTypes.put(typeId, tt);
            }
        }
    }

    private String getCategory(IConfigurationElement ce) {
        final String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
        if (categoryId != null) {
            IConfigurationElement category = fTraceCategories.get(categoryId);
            if (category != null && !category.getName().equals("")) { //$NON-NLS-1$
                return category.getAttribute(TmfTraceType.NAME_ATTR);
            }
        }
        return "[no category]"; //$NON-NLS-1$
    }

    /**
     * Returns the list of trace categories
     *
     * @return the list of trace categories
     * @since 2.0
     */
    public List<String> getTraceCategories() {
        List<String> categoryNames = new ArrayList<String>();
        for (String key : fTraceTypes.keySet()) {
            final String categoryName = fTraceTypes.get(key).getCategoryName();
            if (!categoryNames.contains(categoryName)) {
                categoryNames.add(categoryName);
            }
        }
        return categoryNames;
    }

    /**
     * Get the trace type helper classes from category name
     *
     * @param categoryName
     *            the categoryName to lookup
     * @return a list of trace type helper classes {@link TraceTypeHelper}
     * @since 2.0
     */

    public List<TraceTypeHelper> getTraceTypes(String categoryName) {
        init();
        List<TraceTypeHelper> traceNames = new ArrayList<TraceTypeHelper>();
        for (String key : fTraceTypes.keySet()) {
            final String storedCategoryName = fTraceTypes.get(key).getCategoryName();
            if (storedCategoryName.equals(categoryName)) {
                traceNames.add(fTraceTypes.get(key));
            }
        }
        return traceNames;
    }

    private void init() {
        populateCategoriesAndTraceTypes();
        getCustomTraceTypes();

    }

    private static List<File> isolateTraces(List<FileSystemElement> selectedResources) {

        List<File> traces = new ArrayList<File>();

        // Get the selection
        Iterator<FileSystemElement> resources = selectedResources.iterator();

        // Get the sorted list of unique entries
        Map<String, File> fileSystemObjects = new HashMap<String, File>();
        while (resources.hasNext()) {
            File resource = (File) resources.next().getFileSystemObject();
            String key = resource.getAbsolutePath();
            fileSystemObjects.put(key, resource);
        }
        List<String> files = new ArrayList<String>(fileSystemObjects.keySet());
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

    /**
     * Validate a trace type
     *
     * @param traceTypeName
     *            the trace category (canonical name)
     * @param fileName
     *            the file name (and path)
     * @return true if the trace is of a valid type
     * @since 2.0
     */
    public boolean validate(String traceTypeName, String fileName) {
        if (traceTypeName != null && !traceTypeName.isEmpty()) {
            if (!fTraceTypes.get(traceTypeName).validate(fileName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate a trace
     *
     * @param traceToValidate
     *            the trace category (canonical name)
     * @return true if the trace is of a valid type
     * @since 2.0
     */
    public boolean validate(TraceValidationHelper traceToValidate) {
        return validate(traceToValidate.getTraceType(), traceToValidate.getTraceToScan());
    }

    /**
     * validate list of traces with a tracetype
     *
     * @param traceTypeName
     *            the trace category (canonical name)
     * @param selectedResources
     *            List of traces to validate
     * @return true if all the traces are valid
     * @since 2.0
     */
    public boolean validateTrace(String traceTypeName, List<FileSystemElement> selectedResources) {
        List<File> traces = isolateTraces(selectedResources);
        return validateTraceFiles(traceTypeName, traces);
    }

    /**
     * Validate a list of files with a tracetype
     *
     * @param traceTypeName
     *            the trace category (canonical name)
     * @param traces
     *            the list of files to check if they are trace
     * @return true if all the traces are valid
     * @since 2.0
     */
    public boolean validateTraceFiles(String traceTypeName, List<File> traces) {
        if (traceTypeName != null && !"".equals(traceTypeName) && //$NON-NLS-1$
                !traceTypeName.startsWith(TmfTraceType.CUSTOM_TXT_CATEGORY) && !traceTypeName.startsWith(TmfTraceType.CUSTOM_XML_CATEGORY)) {
            for (File trace : traces) {
                if (!validate(traceTypeName, trace.getAbsolutePath())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get a configuration element for a given name
     *
     * @param traceType
     *            the name canonical
     * @return the configuration element, can be null
     * @since 2.0
     */
    public IConfigurationElement getTraceAttributes(String traceType) {
        return fTraceTypeAttributes.get(traceType);
    }

    /**
     * Find the id of a trace type by its parameters
     *
     * @param category
     *            like "ctf" or "custom text"
     * @param traceType
     *            like "kernel"
     * @return an id like "org.eclipse.linuxtools.blabla...
     * @since 2.0
     */
    public String getTraceTypeId(String category, String traceType) {
        for (String key : fTraceTypes.keySet()) {
            if (fTraceTypes.get(key).getCategoryName().equals(category.trim()) && fTraceTypes.get(key).getName().equals(traceType.trim())) {
                return key;
            }
        }
        return null;
    }

    /**
     * Is the trace a custom (user-defined) trace type. These are the traces
     * like : text and xml defined by the custom trace wizard.
     *
     * @param traceType
     *            the trace type in human form (category:name)
     * @return true if the trace is a custom type
     * @since 2.1
     */
    public static boolean isCustomTrace(String traceType) {
        final boolean startsWithTxt = traceType.startsWith(TmfTraceType.CUSTOM_TXT_CATEGORY);
        final boolean startsWithXML = traceType.startsWith(TmfTraceType.CUSTOM_XML_CATEGORY);
        return (startsWithTxt || startsWithXML);
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
        TraceTypeHelper traceType = getInstance().getTraceType(traceTypeId);
        if (traceType != null) {
            return isCustomTrace(traceType.getCategoryName() + SEPARATOR + traceType.getName());
        }

        return false;
    }

    /**
     * Gets the custom trace type ID from the custom trace name
     *
     * @param traceType
     *            The trace type in human form (category:name)
     * @return the trace type ID or null if the trace is not a custom one
     * @since 2.1
     */
    public static String getCustomTraceTypeId(String traceType) {
        String traceTypeId = null;

        // do custom trace stuff here
        String traceTypeToken[] = traceType.split(":", 2); //$NON-NLS-1$
        if (traceTypeToken.length == 2) {
            final boolean startsWithTxt = traceType.startsWith(TmfTraceType.CUSTOM_TXT_CATEGORY);
            final boolean startsWithXML = traceType.startsWith(TmfTraceType.CUSTOM_XML_CATEGORY);
            if (startsWithTxt) {
                traceTypeId = CustomTxtTrace.class.getCanonicalName() + SEPARATOR + traceTypeToken[1];
            } else if (startsWithXML) {
                traceTypeId = CustomXmlTrace.class.getCanonicalName() + SEPARATOR + traceTypeToken[1];
            }
        }
        return traceTypeId;
    }

    TraceTypeHelper selectTraceType(String path, Shell shell) throws TmfTraceImportException {
        return selectTraceType(path, shell, null);
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
     * @param traceTypeHint the ID of a trace (like "o.e.l.specifictrace" )
     * @return null if the request is cancelled or a TraceTypeHelper if it passes.
     * @throws TmfTraceImportException
     *             if the traces don't match or there are errors in the trace
     *             file
     */
    TraceTypeHelper selectTraceType(String path, Shell shell, String traceTypeHint) throws TmfTraceImportException {
        List<TraceTypeHelper> validCandidates = new ArrayList<TraceTypeHelper>();
        getCustomTraceTypes();
        final Set<String> traceTypes = fTraceTypes.keySet();
        for (String traceType : traceTypes) {
            if (validate(traceType, path)) {
                validCandidates.add(fTraceTypes.get(traceType));
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
                    traceTypeToSet = getTraceTypeToSet(reducedCandidates, shell);
                }
            }
        } else {
            traceTypeToSet = validCandidates.get(0);
        }
        return traceTypeToSet;
    }

    private static List<TraceTypeHelper> reduce(List<TraceTypeHelper> candidates) {
        List<TraceTypeHelper> retVal = new ArrayList<TraceTypeHelper>();

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
        if (TmfTraceType.isCustomTraceId(trace.getCanonicalName())) {
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

    private TraceTypeHelper getTraceTypeToSet(List<TraceTypeHelper> candidates, Shell shell) {
        final Map<String, String> names = new HashMap<String, String>();
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
        return fTraceTypes.get(candidatesToSet[0]);
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
     * @since 2.1
     */
    public static IStatus setTraceType(IPath path, TraceTypeHelper traceType) throws CoreException {
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        String TRACE_NAME = path.lastSegment();
        String traceBundle = null, traceTypeId = traceType.getCanonicalName(), traceIcon = null;
        if (TmfTraceType.isCustomTraceId(traceTypeId)) {
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

        TmfProjectElement tmfProject = TmfProjectRegistry.getProject(resource.getProject());
        if (tmfProject != null) {
            final TmfTraceFolder tracesFolder = tmfProject.getTracesFolder();
            tracesFolder.refresh();

            List<TmfTraceElement> traces = tracesFolder.getTraces();
            boolean found = false;
            for (TmfTraceElement traceElement : traces) {
                if (traceElement.getName().equals(resource.getName())) {
                    traceElement.refreshTraceType();
                    found = true;
                    break;
                }
            }
            if (!found) {
                TmfTraceElement te = new TmfTraceElement(TRACE_NAME, resource, tracesFolder);
                te.refreshTraceType();
                traces = tracesFolder.getTraces();
                for (TmfTraceElement traceElement : traces) {
                    traceElement.refreshTraceType();
                }
            }
        }
        return Status.OK_STATUS;
    }

}
