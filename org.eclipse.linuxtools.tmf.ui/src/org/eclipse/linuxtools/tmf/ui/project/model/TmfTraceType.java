/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
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
     * Returns a list of "category : tracetype , ..."
     *
     * @return returns a list of "category : tracetype , ..."
     * @since 2.0
     */
    public String[] getAvailableTraceTypes() {

        // Generate the list of Category:TraceType to populate the ComboBox
        List<String> traceTypes = new ArrayList<String>();

        List<String> customTypes = getCustomTraceTypes();
        for (String key : this.fTraceTypes.keySet()) {
            TraceTypeHelper tt = this.fTraceTypes.get(key);
            traceTypes.add(tt.getCategoryName() + " : " + tt.getName()); //$NON-NLS-1$
        }
        traceTypes.addAll(customTypes);

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
            if (fTraceTypes.get(key).getCategoryName().equals(CUSTOM_TXT_CATEGORY) || fTraceTypes.get(key).getCategoryName().equals(CUSTOM_XML_CATEGORY)) {
                fTraceTypes.remove(key);
            }
        }

        // add the custom trace types
        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
            String traceTypeName = CUSTOM_TXT_CATEGORY + " : " + def.definitionName; //$NON-NLS-1$
            TraceTypeHelper tt = new TraceTypeHelper(traceTypeName, CUSTOM_TXT_CATEGORY, def.definitionName, null);
            fTraceTypes.put(traceTypeName, tt);
            traceTypes.add(traceTypeName);
        }
        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
            String traceTypeName = CUSTOM_XML_CATEGORY + " : " + def.definitionName; //$NON-NLS-1$
            TraceTypeHelper tt = new TraceTypeHelper(traceTypeName, CUSTOM_TXT_CATEGORY, def.definitionName, null);
            fTraceTypes.put(traceTypeName, tt);
            traceTypes.add(traceTypeName);
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
     * Get the trace types
     *
     * @param category
     *            the category to lookup
     * @return the trace types
     * @since 2.0
     */

    public List<TraceTypeHelper> getTraceTypes(String category) {
        List<TraceTypeHelper> traceNames = new ArrayList<TraceTypeHelper>();
        for (String key : fTraceTypes.keySet()) {
            final String categoryName = fTraceTypes.get(key).getCategoryName();
            if (categoryName.equals(category)) {
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
        if (traceTypeName != null && !"".equals(traceTypeName) && //$NON-NLS-1$
                !traceTypeName.startsWith(TmfTraceType.CUSTOM_TXT_CATEGORY) && !traceTypeName.startsWith(TmfTraceType.CUSTOM_XML_CATEGORY)) {
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
                validate(traceTypeName, trace.getAbsolutePath());
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
}
