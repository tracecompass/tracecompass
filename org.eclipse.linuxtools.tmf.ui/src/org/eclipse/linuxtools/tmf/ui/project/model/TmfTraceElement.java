/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added supplementary files handling
 *   Geneviève Bastien - Moved supplementary files handling to parent class,
 *                       added code to copy trace
 *   Patrick Tasse - Close editors to release resources
 *   Jean-Christian Kouame - added trace properties to be shown into
 *                           the properties view
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceProperties;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

/**
 * Implementation of trace model element representing a trace. It provides
 * methods to instantiate <code>ITmfTrace</code> and <code>ITmfEvent</code> as
 * well as editor ID from the trace type extension definition.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfTraceElement extends TmfWithFolderElement implements IActionFilter, IPropertySource2 {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Other attributes
    /**
     * Bundle attribute name
     */
    public static final String BUNDLE = "bundle"; //$NON-NLS-1$
    /**
     * IsLinked attribute name.
     */
    public static final String IS_LINKED = "isLinked"; //$NON-NLS-1$

    // Property View stuff
    private static final String sfResourcePropertiesCategory = Messages.TmfTraceElement_ResourceProperties;
    private static final String sfName = Messages.TmfTraceElement_Name;
    private static final String sfPath = Messages.TmfTraceElement_Path;
    private static final String sfLocation = Messages.TmfTraceElement_Location;
    private static final String sfEventType = Messages.TmfTraceElement_EventType;
    private static final String sfIsLinked = Messages.TmfTraceElement_IsLinked;
    private static final String sfTracePropertiesCategory = Messages.TmfTraceElement_TraceProperties;

    private static final ReadOnlyTextPropertyDescriptor sfNameDescriptor = new ReadOnlyTextPropertyDescriptor(sfName, sfName);
    private static final ReadOnlyTextPropertyDescriptor sfPathDescriptor = new ReadOnlyTextPropertyDescriptor(sfPath, sfPath);
    private static final ReadOnlyTextPropertyDescriptor sfLocationDescriptor = new ReadOnlyTextPropertyDescriptor(sfLocation, sfLocation);
    private static final ReadOnlyTextPropertyDescriptor sfTypeDescriptor = new ReadOnlyTextPropertyDescriptor(sfEventType, sfEventType);
    private static final ReadOnlyTextPropertyDescriptor sfIsLinkedDescriptor = new ReadOnlyTextPropertyDescriptor(sfIsLinked, sfIsLinked);

    private static final IPropertyDescriptor[] sfDescriptors = { sfNameDescriptor, sfPathDescriptor, sfLocationDescriptor,
            sfTypeDescriptor, sfIsLinkedDescriptor };

    static {
        sfNameDescriptor.setCategory(sfResourcePropertiesCategory);
        sfPathDescriptor.setCategory(sfResourcePropertiesCategory);
        sfLocationDescriptor.setCategory(sfResourcePropertiesCategory);
        sfTypeDescriptor.setCategory(sfResourcePropertiesCategory);
        sfIsLinkedDescriptor.setCategory(sfResourcePropertiesCategory);
    }

    private static final String BOOKMARKS_HIDDEN_FILE = ".bookmarks"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // This trace type ID as defined in plugin.xml
    private String fTraceTypeId = null;

    // ------------------------------------------------------------------------
    // Static initialization
    // ------------------------------------------------------------------------

    // The mapping of available trace type IDs to their corresponding
    // configuration element
    private static final Map<String, IConfigurationElement> sfTraceTypeAttributes = new HashMap<>();
    private static final Map<String, IConfigurationElement> sfTraceTypeUIAttributes = new HashMap<>();
    private static final Map<String, IConfigurationElement> sfTraceCategories = new HashMap<>();

    /**
     * Initialize statically at startup by getting extensions from the platform
     * extension registry.
     */
    public static void init() {
        /* Read the tmf.core "tracetype" extension point */
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceType.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            switch (ce.getName()) {
            case TmfTraceType.TYPE_ELEM:
                String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                sfTraceTypeAttributes.put(traceTypeId, ce);
                break;
            case TmfTraceType.CATEGORY_ELEM:
                String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                sfTraceCategories.put(categoryId, ce);
                break;
            default:
            }
        }

        /*
         * Read the corresponding tmf.ui "tracetypeui" extension point for this
         * trace type, if it exists.
         */
        config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceTypeUIUtils.TMF_TRACE_TYPE_UI_ID);
        for (IConfigurationElement ce : config) {
            String elemName = ce.getName();
            if (TmfTraceTypeUIUtils.TYPE_ELEM.equals(elemName)) {
                String traceType = ce.getAttribute(TmfTraceTypeUIUtils.TRACETYPE_ATTR);
                sfTraceTypeUIAttributes.put(traceType, ce);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor. Creates trace model element under the trace folder.
     *
     * @param name
     *            The name of trace
     * @param trace
     *            The trace resource.
     * @param parent
     *            The parent element (trace folder)
     */
    public TmfTraceElement(String name, IResource trace, TmfTraceFolder parent) {
        this(name, trace, (TmfProjectModelElement) parent);
    }

    /**
     * Constructor. Creates trace model element under the experiment folder.
     *
     * @param name
     *            The name of trace
     * @param trace
     *            The trace resource.
     * @param parent
     *            The parent element (experiment folder)
     */
    public TmfTraceElement(String name, IResource trace, TmfExperimentElement parent) {
        this(name, trace, (TmfProjectModelElement) parent);
    }

    private TmfTraceElement(String name, IResource trace, TmfProjectModelElement parent) {
        super(name, trace, parent);
        parent.addChild(this);
        refreshTraceType();
        TmfSignalManager.register(this);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    void refreshChildren() {

        /* Refreshes the analysis under this trace */
        Map<String, TmfAnalysisElement> childrenMap = new HashMap<>();
        for (TmfAnalysisElement analysis : getAvailableAnalysis()) {
            childrenMap.put(analysis.getAnalysisId(), analysis);
        }

        TraceTypeHelper helper = TmfTraceType.getInstance().getTraceType(getTraceType());

        Class<? extends ITmfTrace> traceClass = null;

        if (helper == null && fTraceTypeId != null) {
            if (fTraceTypeId.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                    if (fTraceTypeId.equals(CustomTxtTrace.class.getCanonicalName() + ":" + def.definitionName)) { //$NON-NLS-1$
                        traceClass = CustomTxtTrace.class;
                    }
                }
            }
            if (fTraceTypeId.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                    if (fTraceTypeId.equals(CustomXmlTrace.class.getCanonicalName() + ":" + def.definitionName)) { //$NON-NLS-1$
                        traceClass = CustomTxtTrace.class;
                    }
                }
            }
        } else if (helper != null) {
            traceClass = helper.getTraceClass();
        }

        /* Remove all analysis and return */
        if (traceClass == null) {
            for (TmfAnalysisElement analysis : childrenMap.values()) {
                removeChild(analysis);
            }
            return;
        }

        /** Get the base path to put the resource to */
        IPath path = fResource.getFullPath();

        /* Add all new analysis modules or refresh outputs of existing ones */
        for (IAnalysisModuleHelper module : TmfAnalysisManager.getAnalysisModules(traceClass).values()) {

            /* If the analysis is not a child of the trace, create it */
            TmfAnalysisElement analysis = childrenMap.remove(module.getId());
            if (analysis == null) {
                /**
                 * No need for the resource to exist, nothing will be done with
                 * it
                 */
                IFolder newresource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path.append(module.getId()));
                analysis = new TmfAnalysisElement(module.getName(), newresource, this, module.getId());
            }
            analysis.refreshChildren();
        }

        /* Remove analysis that are not children of this trace anymore */
        for (TmfAnalysisElement analysis : childrenMap.values()) {
            removeChild(analysis);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Returns the trace type ID.
     *
     * @return trace type ID.
     */
    public String getTraceType() {
        return fTraceTypeId;
    }

    /**
     * Refreshes the trace type filed by reading the trace type persistent
     * property of the resource referenece.
     */
    public void refreshTraceType() {
        try {
            fTraceTypeId = getResource().getPersistentProperty(TmfCommonConstants.TRACETYPE);
        } catch (CoreException e) {
            Activator.getDefault().logError("Error refreshing trace type pesistent property for trace " + getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * Instantiate a <code>ITmfTrace</code> object based on the trace type and
     * the corresponding extension.
     *
     * @return the <code>ITmfTrace</code> or <code>null</code> for an error
     */
    public ITmfTrace instantiateTrace() {
        try {

            // make sure that supplementary folder exists
            refreshSupplementaryFolder();

            if (fTraceTypeId != null) {
                if (fTraceTypeId.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                    for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                        if (fTraceTypeId.equals(CustomTxtTrace.class.getCanonicalName() + ":" + def.definitionName)) { //$NON-NLS-1$
                            return new CustomTxtTrace(def);
                        }
                    }
                }
                if (fTraceTypeId.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                    for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                        if (fTraceTypeId.equals(CustomXmlTrace.class.getCanonicalName() + ":" + def.definitionName)) { //$NON-NLS-1$
                            return new CustomXmlTrace(def);
                        }
                    }
                }
                IConfigurationElement ce = sfTraceTypeAttributes.get(fTraceTypeId);
                if (ce == null) {
                    return null;
                }
                ITmfTrace trace = (ITmfTrace) ce.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
                return trace;
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error instantiating ITmfTrace object for trace " + getName(), e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Instantiate a <code>ITmfEvent</code> object based on the trace type and
     * the corresponding extension.
     *
     * @return the <code>ITmfEvent</code> or <code>null</code> for an error
     */
    public ITmfEvent instantiateEvent() {
        try {
            if (fTraceTypeId != null) {
                if (fTraceTypeId.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                    for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                        if (fTraceTypeId.equals(CustomTxtTrace.class.getCanonicalName() + ":" + def.definitionName)) { //$NON-NLS-1$
                            return new CustomTxtEvent(def);
                        }
                    }
                }
                if (fTraceTypeId.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                    for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                        if (fTraceTypeId.equals(CustomXmlTrace.class.getCanonicalName() + ":" + def.definitionName)) { //$NON-NLS-1$
                            return new CustomXmlEvent(def);
                        }
                    }
                }
                IConfigurationElement ce = sfTraceTypeAttributes.get(fTraceTypeId);
                if (ce == null) {
                    return null;
                }
                ITmfEvent event = (ITmfEvent) ce.createExecutableExtension(TmfTraceType.EVENT_TYPE_ATTR);
                return event;
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error instantiating ITmfEvent object for trace " + getName(), e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Returns the optional editor ID from the trace type extension.
     *
     * @return the editor ID or <code>null</code> if not defined.
     */
    public String getEditorId() {
        if (fTraceTypeId != null) {
            if (fTraceTypeId.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                return TmfEventsEditor.ID;
            }
            if (fTraceTypeId.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                return TmfEventsEditor.ID;
            }
            IConfigurationElement ce = sfTraceTypeUIAttributes.get(fTraceTypeId);
            if (ce == null) {
                /* This trace type does not define UI attributes */
                return null;
            }
            IConfigurationElement[] defaultEditorCE = ce.getChildren(TmfTraceTypeUIUtils.DEFAULT_EDITOR_ELEM);
            if (defaultEditorCE.length == 1) {
                return defaultEditorCE[0].getAttribute(TmfTraceType.ID_ATTR);
            }
        }
        return null;
    }

    /**
     * Returns the file resource used to store bookmarks after creating it if
     * necessary. If the trace resource is a file, it is returned directly. If
     * the trace resource is a folder, a linked file is returned. The file will
     * be created if it does not exist.
     *
     * @return the bookmarks file
     * @throws CoreException
     *             if the bookmarks file cannot be created
     * @since 2.0
     */
    public IFile createBookmarksFile() throws CoreException {
        IFile file = getBookmarksFile();
        if (fResource instanceof IFolder) {
            if (!file.exists()) {
                final IFile bookmarksFile = getProject().getTracesFolder().getResource().getFile(BOOKMARKS_HIDDEN_FILE);
                if (!bookmarksFile.exists()) {
                    final InputStream source = new ByteArrayInputStream(new byte[0]);
                    bookmarksFile.create(source, true, null);
                }
                bookmarksFile.setHidden(true);
                file.createLink(bookmarksFile.getLocation(), IResource.REPLACE, null);
                file.setHidden(true);
                file.setPersistentProperty(TmfCommonConstants.TRACETYPE, TmfTrace.class.getCanonicalName());
            }
        }
        return file;
    }

    /**
     * Returns the file resource used to store bookmarks. The file may not
     * exist.
     *
     * @return the bookmarks file
     * @since 2.0
     */
    public IFile getBookmarksFile() {
        IFile file = null;
        if (fResource instanceof IFile) {
            file = (IFile) fResource;
        } else if (fResource instanceof IFolder) {
            final IFolder folder = (IFolder) fResource;
            file = folder.getFile(getName() + '_');
        }
        return file;
    }

    /**
     * Returns the <code>TmfTraceElement</code> located under the
     * <code>TmfTracesFolder</code>.
     *
     * @return <code>this</code> if this element is under the
     *         <code>TmfTracesFolder</code> else the corresponding
     *         <code>TmfTraceElement</code> if this element is under
     *         <code>TmfExperimentElement</code>.
     */
    public TmfTraceElement getElementUnderTraceFolder() {

        // If trace is under an experiment, return original trace from the
        // traces folder
        if (getParent() instanceof TmfExperimentElement) {
            for (TmfTraceElement aTrace : getProject().getTracesFolder().getTraces()) {
                if (aTrace.getName().equals(getName())) {
                    return aTrace;
                }
            }
        }
        return this;
    }

    // ------------------------------------------------------------------------
    // IActionFilter
    // ------------------------------------------------------------------------

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (name.equals(IS_LINKED)) {
            boolean isLinked = getResource().isLinked();
            return Boolean.toString(isLinked).equals(value);
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // TmfTraceElement
    // ------------------------------------------------------------------------

    @Override
    public TmfProjectElement getProject() {
        if (getParent() instanceof TmfTraceFolder) {
            TmfTraceFolder folder = (TmfTraceFolder) getParent();
            TmfProjectElement project = (TmfProjectElement) folder.getParent();
            return project;
        }
        if (getParent() instanceof TmfExperimentElement) {
            TmfExperimentElement experiment = (TmfExperimentElement) getParent();
            TmfExperimentFolder folder = (TmfExperimentFolder) experiment.getParent();
            TmfProjectElement project = (TmfProjectElement) folder.getParent();
            return project;
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // IPropertySource2
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    /**
     * Get the trace properties of this traceElement if the corresponding trace
     * is opened in an editor
     *
     * @return a map with the names and values of the trace properties
     *         respectively as keys and values
     */
    private Map<String, String> getTraceProperties() {
        for (ITmfTrace openedTrace : TmfTraceManager.getInstance().getOpenedTraces()) {
            for (ITmfTrace singleTrace : TmfTraceManager.getTraceSet(openedTrace)) {
                if (this.getLocation().toString().endsWith(singleTrace.getPath())) {
                    if (singleTrace instanceof ITmfTraceProperties) {
                        ITmfTraceProperties traceProperties = (ITmfTraceProperties) singleTrace;
                        return traceProperties.getTraceProperties();
                    }
                }
            }
        }
        return new HashMap<>();
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        Map<String, String> traceProperties = getTraceProperties();
        if (!traceProperties.isEmpty()) {
            IPropertyDescriptor[] propertyDescriptorArray = new IPropertyDescriptor[traceProperties.size() + sfDescriptors.length];
            int index = 0;
            for (Map.Entry<String, String> varName : traceProperties.entrySet()) {
                ReadOnlyTextPropertyDescriptor descriptor = new ReadOnlyTextPropertyDescriptor(this.getName() + "_" + varName.getKey(), varName.getKey()); //$NON-NLS-1$
                descriptor.setCategory(sfTracePropertiesCategory);
                propertyDescriptorArray[index] = descriptor;
                index++;
            }
            for (int i = 0; i < sfDescriptors.length; i++) {
                propertyDescriptorArray[index] = sfDescriptors[i];
                index++;
            }
            return propertyDescriptorArray;
        }
        return Arrays.copyOf(sfDescriptors, sfDescriptors.length);
    }

    @Override
    public Object getPropertyValue(Object id) {

        if (sfName.equals(id)) {
            return getName();
        }

        if (sfPath.equals(id)) {
            return getPath().toString();
        }

        if (sfLocation.equals(id)) {
            return getLocation().toString();
        }

        if (sfIsLinked.equals(id)) {
            return Boolean.valueOf(getResource().isLinked()).toString();
        }

        if (sfEventType.equals(id)) {
            if (fTraceTypeId != null) {
                IConfigurationElement ce = sfTraceTypeAttributes.get(fTraceTypeId);
                return (ce != null) ? (getCategory(ce) + " : " + ce.getAttribute(TmfTraceType.NAME_ATTR)) : ""; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        Map<String, String> traceProperties = getTraceProperties();
        if (id != null && !traceProperties.isEmpty()) {
            String key = (String) id;
            key = key.replaceFirst(this.getName() + "_", ""); //$NON-NLS-1$ //$NON-NLS-2$
            String value = traceProperties.get(key);
            return value;
        }

        return null;
    }

    private static String getCategory(IConfigurationElement ce) {
        String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
        if (categoryId != null) {
            IConfigurationElement category = sfTraceCategories.get(categoryId);
            if (category != null) {
                return category.getAttribute(TmfTraceType.NAME_ATTR);
            }
        }
        return "[no category]"; //$NON-NLS-1$
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

    @Override
    public boolean isPropertyResettable(Object id) {
        return false;
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    /**
     * Copy this trace in the trace folder. No other parameters are mentioned so
     * the trace is copied in this element's project trace folder
     *
     * @param string
     *            The new trace name
     * @return the new Resource object
     * @since 2.0
     */
    public TmfTraceElement copy(String string) {
        TmfTraceFolder folder = this.getProject().getTracesFolder();
        IResource res = super.copy(string, false);
        return new TmfTraceElement(string, res, folder);
    }

    /**
     * Close opened editors associated with this trace.
     *
     * @since 2.0
     */
    public void closeEditors() {
        // Close the trace if open
        IFile file = getBookmarksFile();
        FileEditorInput input = new FileEditorInput(file);
        IWorkbench wb = PlatformUI.getWorkbench();
        for (IWorkbenchWindow wbWindow : wb.getWorkbenchWindows()) {
            for (IWorkbenchPage wbPage : wbWindow.getPages()) {
                for (IEditorReference editorReference : wbPage.getEditorReferences()) {
                    try {
                        if (editorReference.getEditorInput().equals(input)) {
                            wbPage.closeEditor(editorReference.getEditor(false), false);
                        }
                    } catch (PartInitException e) {
                        Activator.getDefault().logError("Error closing editor for trace " + getName(), e); //$NON-NLS-1$
                    }
                }
            }
        }

        // Close experiments that contain the trace if open
        if (getParent() instanceof TmfTraceFolder) {
            TmfExperimentFolder experimentFolder = getProject().getExperimentsFolder();
            for (ITmfProjectModelElement experiment : experimentFolder.getChildren()) {
                for (ITmfProjectModelElement child : experiment.getChildren()) {
                    if (child.getName().equals(getName())) {
                        ((TmfExperimentElement) experiment).closeEditors();
                        break;
                    }
                }
            }
        } else if (getParent() instanceof TmfExperimentElement) {
            TmfExperimentElement experiment = (TmfExperimentElement) getParent();
            experiment.closeEditors();
        }
    }

    /**
     * Delete the trace resource, remove it from experiments and delete its
     * supplementary files
     *
     * @param progressMonitor
     *            a progress monitor, or null if progress reporting is not
     *            desired
     *
     * @throws CoreException
     *             thrown when IResource.delete fails
     * @since 2.2
     */
    public void delete(IProgressMonitor progressMonitor) throws CoreException {
        closeEditors();

        IPath path = fResource.getLocation();
        if (path != null && (getParent() instanceof TmfTraceFolder)) {
            TmfExperimentFolder experimentFolder = getProject().getExperimentsFolder();

            // Propagate the removal to traces
            for (ITmfProjectModelElement experiment : experimentFolder.getChildren()) {
                List<ITmfProjectModelElement> toRemove = new LinkedList<>();
                for (ITmfProjectModelElement child : experiment.getChildren()) {
                    if (child.getName().equals(getName())) {
                        toRemove.add(child);
                    }
                }
                for (ITmfProjectModelElement child : toRemove) {
                    ((TmfExperimentElement) experiment).removeTrace((TmfTraceElement) child);
                }
            }

            // Delete supplementary files
            deleteSupplementaryFolder();
        }

        // Finally, delete the trace
        fResource.delete(true, progressMonitor);
    }

    /**
     * Get the instantiated trace associated with this element.
     *
     * @return The instantiated trace or null if trace is not (yet) available
     * @since 2.1
     */
    public ITmfTrace getTrace() {
        for (ITmfTrace trace : TmfTraceManager.getInstance().getOpenedTraces()) {
            if (trace.getResource().equals(getResource())) {
                return trace;
            }
        }
        return null;
    }

    /**
     * Get the list of analysis model elements under this trace
     *
     * @return Array of analysis elements
     * @since 3.0
     */
    public List<TmfAnalysisElement> getAvailableAnalysis() {
        List<ITmfProjectModelElement> children = getChildren();
        List<TmfAnalysisElement> analysis = new ArrayList<>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfAnalysisElement) {
                analysis.add((TmfAnalysisElement) child);
            }
        }
        return analysis;
    }

    /**
     * Handler for the Trace Opened signal
     *
     * @param signal
     *            The incoming signal
     * @since 3.0
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        IResource resource = signal.getTrace().getResource();
        if ((resource == null) || !resource.equals(getResource())) {
            return;
        }

        getParent().refresh();
    }
}
