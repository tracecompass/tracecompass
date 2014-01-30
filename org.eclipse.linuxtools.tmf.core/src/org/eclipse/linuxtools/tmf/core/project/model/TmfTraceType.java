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

package org.eclipse.linuxtools.tmf.core.project.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Utility class for accessing TMF trace type extensions from the platform's
 * extensions registry.
 *
 * @version 1.0
 * @author Patrick Tasse
 * @author Matthew Khouzam
 * @since 3.0
 */
public final class TmfTraceType {

    private static final char SEPARATOR = ':';

    /** Extension point ID */
    public static final String TMF_TRACE_TYPE_ID = "org.eclipse.linuxtools.tmf.ui.tracetype"; //$NON-NLS-1$

    /** Extension point element 'Category' */
    public static final String CATEGORY_ELEM = "category"; //$NON-NLS-1$

    /** Extension point element 'Type' */
    public static final String TYPE_ELEM = "type"; //$NON-NLS-1$

    /** Extension point element 'Default editor' */
    public static final String DEFAULT_EDITOR_ELEM = "defaultEditor"; //$NON-NLS-1$

    /** Extension point element 'Events table type' */
    public static final String EVENTS_TABLE_TYPE_ELEM = "eventsTableType"; //$NON-NLS-1$

    /** Extension point element 'Statistics viewer type' */
    public static final String STATISTICS_VIEWER_ELEM = "statisticsViewerType"; //$NON-NLS-1$

    /** Extension point attribute 'ID' */
    public static final String ID_ATTR = "id"; //$NON-NLS-1$

    /** Extension point attribute 'name' */
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$

    /** Extension point attribute 'category' */
    public static final String CATEGORY_ATTR = "category"; //$NON-NLS-1$

    /** Extension point attribute 'trace_type' */
    public static final String TRACE_TYPE_ATTR = "trace_type"; //$NON-NLS-1$

    /** Extension point attribute 'event_type' */
    public static final String EVENT_TYPE_ATTR = "event_type"; //$NON-NLS-1$

    /** Extension point attribute 'icon' */
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$

    /** Extension point attribute 'class' */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    /**
     * Custom text label used internally and therefore should not be
     * externalized
     */
    public static final String CUSTOM_TXT_CATEGORY = "Custom Text"; //$NON-NLS-1$

    /**
     * Custom XML label used internally and therefore should not be externalized
     */
    public static final String CUSTOM_XML_CATEGORY = "Custom XML"; //$NON-NLS-1$

    // The mapping of available trace type IDs to their corresponding
    // configuration element
    private final Map<String, IConfigurationElement> fTraceTypeAttributes = new HashMap<>();
    private final Map<String, IConfigurationElement> fTraceCategories = new HashMap<>();
    private final Map<String, TraceTypeHelper> fTraceTypes = new LinkedHashMap<>();

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
            Activator.logError("Error creating the element from the resource", e); //$NON-NLS-1$
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
        List<IConfigurationElement> typeElements = new LinkedList<>();
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
     * Retrieve the TraceTypeHelper for a given trace type ID
     *
     * @param id
     *            The trace type ID
     * @return The corresponding TraceTypeHelper, or null if there is none for
     *         the specified ID
     */
    public TraceTypeHelper getTraceTypeHelper(String id) {
        return fTraceTypes.get(id);
    }

    /**
     * Get an iterable view of the existing trace type IDs.
     *
     * @return The currently registered trace type IDs
     */
    public Iterable<String> getTraceTypeIDs() {
        return fTraceTypes.keySet();
    }

    /**
     * Returns a list of "category:tracetype , ..."
     *
     * @return returns a list of "category:tracetype , ..."
     */
    public String[] getAvailableTraceTypes() {

        // Generate the list of Category:TraceType to populate the ComboBox
        List<String> traceTypes = new ArrayList<>();

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
     */
    public static List<String> getCustomTraceTypes(String type) {
        List<String> traceTypes = new ArrayList<>();
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
     */
    public static List<String> getCustomTraceTypes() {

        List<String> traceTypes = new ArrayList<>();
        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
            String traceTypeName = def.definitionName;
            traceTypes.add(traceTypeName);
        }
        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
            String traceTypeName = def.definitionName;
            traceTypes.add(traceTypeName);
        }
        return traceTypes;
    }

    private void populateCustomTraceTypes() {
        // add the custom trace types
        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
            String traceTypeId = CustomTxtTrace.class.getCanonicalName() + SEPARATOR + def.definitionName;
            ITmfTrace trace = new CustomTxtTrace(def);
            TraceTypeHelper tt = new TraceTypeHelper(traceTypeId, CUSTOM_TXT_CATEGORY, def.definitionName, trace);
            fTraceTypes.put(traceTypeId, tt);
            // Deregister trace as signal handler because it is only used for validation
            TmfSignalManager.deregister(trace);
        }
        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
            String traceTypeId = CustomXmlTrace.class.getCanonicalName() + SEPARATOR + def.definitionName;
            ITmfTrace trace = new CustomXmlTrace(def);
            TraceTypeHelper tt = new TraceTypeHelper(traceTypeId, CUSTOM_XML_CATEGORY, def.definitionName, trace);
            fTraceTypes.put(traceTypeId, tt);
            // Deregister trace as signal handler because it is only used for validation
            TmfSignalManager.deregister(trace);
        }
    }

    /**
     * Add or replace a custom trace type
     *
     * @param category
     *            The custom parser category
     * @param definitionName
     *            The custom parser definition name to add or replace
     */
    public void addCustomTraceType(String category, String definitionName) {
        String traceTypeId = null;
        ITmfTrace trace = null;

        if (category.equals(CUSTOM_TXT_CATEGORY)) {
            traceTypeId = CustomTxtTrace.class.getCanonicalName() + SEPARATOR + definitionName;
            CustomTxtTraceDefinition def = CustomTxtTraceDefinition.load(definitionName);
            if (def != null) {
                trace = new CustomTxtTrace(def);
            }
        } else if (category.equals(CUSTOM_XML_CATEGORY)) {
            traceTypeId = CustomXmlTrace.class.getCanonicalName() + SEPARATOR + definitionName;
            CustomXmlTraceDefinition def = CustomXmlTraceDefinition.load(definitionName);
            if (def != null) {
                trace = new CustomXmlTrace(def);
            }
        }

        if (traceTypeId != null && trace != null) {
            TraceTypeHelper helper = fTraceTypes.get(traceTypeId);
            if (helper != null) {
                helper.getTrace().dispose();
            }
            TraceTypeHelper tt = new TraceTypeHelper(traceTypeId, category, definitionName, trace);
            fTraceTypes.put(traceTypeId, tt);
            // Deregister trace as signal handler because it is only used for validation
            TmfSignalManager.deregister(trace);
        }
    }

    /**
     * Remove a custom trace type
     *
     * @param category
     *            The custom parser category
     * @param definitionName
     *            The custom parser definition name to add or replace
     */
    public void removeCustomTraceType(String category, String definitionName) {
        if (category.equals(CUSTOM_TXT_CATEGORY)) {
            String traceTypeId = CustomTxtTrace.class.getCanonicalName() + SEPARATOR + definitionName;
            TraceTypeHelper helper = fTraceTypes.remove(traceTypeId);
            if (helper != null) {
                helper.getTrace().dispose();
            }
        } else if (category.equals(CUSTOM_XML_CATEGORY)) {
            String traceTypeId = CustomXmlTrace.class.getCanonicalName() + SEPARATOR + definitionName;
            TraceTypeHelper helper = fTraceTypes.remove(traceTypeId);
            if (helper != null) {
                helper.getTrace().dispose();
            }
        }
    }

    /**
     * Gets a trace type for a given canonical id
     *
     * @param id
     *            the ID of the trace
     * @return the return type
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
                    // Deregister trace as signal handler because it is only used for validation
                    TmfSignalManager.deregister(trace);
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
     */
    public List<String> getTraceCategories() {
        List<String> categoryNames = new ArrayList<>();
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
     */
    public List<TraceTypeHelper> getTraceTypes(String categoryName) {
        List<TraceTypeHelper> traceNames = new ArrayList<>();
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
        populateCustomTraceTypes();

    }

    /**
     * Validate a trace type
     *
     * @param traceTypeName
     *            the trace category (canonical name)
     * @param fileName
     *            the file name (and path)
     * @return true if the trace is of a valid type
     */
    public boolean validate(String traceTypeName, String fileName) {
        if (traceTypeName != null && !traceTypeName.isEmpty()) {
            final TraceTypeHelper traceTypeHelper = fTraceTypes.get(traceTypeName);
            if (!traceTypeHelper.validate(fileName)) {
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
     */
    public boolean validate(TraceValidationHelper traceToValidate) {
        return validate(traceToValidate.getTraceType(), traceToValidate.getTraceToScan());
    }

    /**
     * Validate a list of files with a tracetype
     *
     * @param traceTypeName
     *            the trace category (canonical name)
     * @param traces
     *            the list of files to check if they are trace
     * @return true if all the traces are valid
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
     * Gets the custom trace type ID from the custom trace name
     *
     * @param traceType
     *            The trace type in human form (category:name)
     * @return the trace type ID or null if the trace is not a custom one
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

    /**
     * Is the trace a custom (user-defined) trace type. These are the traces
     * like : text and xml defined by the custom trace wizard.
     *
     * @param traceType
     *            the trace type in human form (category:name)
     * @return true if the trace is a custom type
     */
    public static boolean isCustomTrace(String traceType) {
        final boolean startsWithTxt = traceType.startsWith(TmfTraceType.CUSTOM_TXT_CATEGORY);
        final boolean startsWithXML = traceType.startsWith(TmfTraceType.CUSTOM_XML_CATEGORY);
        return (startsWithTxt || startsWithXML);
    }
}
