/*******************************************************************************
 * Copyright (c) 2010, 2018 Ericsson, École Polytechnique de Montréal
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
 *   Geneviève Bastien - Moved trace type related methods to parent class
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.util.ByteBufferTracker;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.io.ResourceUtil;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.project.model.ITmfPropertiesProvider;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.NumberFormat;

/**
 * Implementation of trace model element representing a trace. It provides
 * methods to instantiate <code>ITmfTrace</code> and <code>ITmfEvent</code> as
 * well as editor ID from the trace type extension definition.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfTraceElement extends TmfCommonProjectElement implements IActionFilter, IPropertySource2 {

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
    private static final String RESOURCE_PROPERTIES_CATEGORY = Messages.TmfTraceElement_ResourceProperties;
    private static final String NAME = Messages.TmfTraceElement_Name;
    private static final String PATH = Messages.TmfTraceElement_Path;
    private static final String LOCATION = Messages.TmfTraceElement_Location;
    private static final String TRACE_TYPE = Messages.TmfTraceElement_EventType;
    private static final String TRACE_TYPE_ID = Messages.TmfTraceElement_TraceTypeId;
    private static final String IS_LINKED_PROPERTY = Messages.TmfTraceElement_IsLinked;
    private static final String SOURCE_LOCATION = Messages.TmfTraceElement_SourceLocation;
    private static final String TIME_OFFSET = Messages.TmfTraceElement_TimeOffset;
    private static final String LAST_MODIFIED = Messages.TmfTraceElement_LastModified;
    private static final String SIZE = Messages.TmfTraceElement_Size;
    private static final String TRACE_PROPERTIES_CATEGORY = Messages.TmfTraceElement_TraceProperties;

    private static final ReadOnlyTextPropertyDescriptor NAME_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(NAME, NAME);
    private static final ReadOnlyTextPropertyDescriptor PATH_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(PATH, PATH);
    private static final ReadOnlyTextPropertyDescriptor LOCATION_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(LOCATION, LOCATION);
    private static final ReadOnlyTextPropertyDescriptor TYPE_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(TRACE_TYPE, TRACE_TYPE);
    private static final ReadOnlyTextPropertyDescriptor TYPE_ID_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(TRACE_TYPE_ID, TRACE_TYPE_ID);
    private static final ReadOnlyTextPropertyDescriptor IS_LINKED_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(IS_LINKED_PROPERTY, IS_LINKED_PROPERTY);
    private static final ReadOnlyTextPropertyDescriptor SOURCE_LOCATION_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(SOURCE_LOCATION, SOURCE_LOCATION);
    private static final ReadOnlyTextPropertyDescriptor TIME_OFFSET_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(TIME_OFFSET, TIME_OFFSET);
    private static final ReadOnlyTextPropertyDescriptor LAST_MODIFIED_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(LAST_MODIFIED, LAST_MODIFIED);
    private static final ReadOnlyTextPropertyDescriptor SIZE_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(SIZE, SIZE);

    private static final IPropertyDescriptor[] sfDescriptors = { NAME_DESCRIPTOR, PATH_DESCRIPTOR, LOCATION_DESCRIPTOR,
            TYPE_DESCRIPTOR, TYPE_ID_DESCRIPTOR, IS_LINKED_DESCRIPTOR, SOURCE_LOCATION_DESCRIPTOR,
            TIME_OFFSET_DESCRIPTOR, LAST_MODIFIED_DESCRIPTOR, SIZE_DESCRIPTOR };

    static {
        NAME_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        PATH_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        LOCATION_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        TYPE_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        TYPE_ID_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        IS_LINKED_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        SOURCE_LOCATION_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        TIME_OFFSET_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        LAST_MODIFIED_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
        SIZE_DESCRIPTOR.setCategory(RESOURCE_PROPERTIES_CATEGORY);
    }

    private static final TmfTimestampFormat OFFSET_FORMAT = new TmfTimestampFormat("T.SSS SSS SSS s"); //$NON-NLS-1$

    private static final int FOLDER_MAX_COUNT = 1024;

    // ------------------------------------------------------------------------
    // Static initialization
    // ------------------------------------------------------------------------

    // The mapping of available trace type IDs to their corresponding
    // configuration element
    private static final Map<String, IConfigurationElement> TRACE_TYPE_ATTRIBUTES = new HashMap<>();
    private static final Map<String, IConfigurationElement> TRACE_TYPE_UI_ATTRIBUTES = new HashMap<>();
    private static final Map<String, IConfigurationElement> TRACE_CATEGORIES = new HashMap<>();

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
                TRACE_TYPE_ATTRIBUTES.put(traceTypeId, ce);
                break;
            case TmfTraceType.CATEGORY_ELEM:
                String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                TRACE_CATEGORIES.put(categoryId, ce);
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
                TRACE_TYPE_UI_ATTRIBUTES.put(traceType, ce);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class FileInfo {
        long lastModified;
        long size;
        int count;
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private FileInfo fFileInfo;
    private ITmfTimestamp fStartTime = null;
    private ITmfTimestamp fEndTime = null;

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
        super(name, trace, parent);
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
        super(name, trace, parent);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public @NonNull Image getIcon() {
        Image icon = super.getIcon();
        return (icon == null ? TmfProjectModelIcons.DEFAULT_TRACE_ICON : icon);
    }

    /**
     * @since 2.0
     */
    @Override
    public String getLabelText() {
        if (getParent() instanceof TmfExperimentElement) {
            return getElementPath();
        }
        return getName();
    }

    /**
     * Instantiate a <code>ITmfTrace</code> object based on the trace type and
     * the corresponding extension.
     *
     * @return the <code>ITmfTrace</code> or <code>null</code> for an error
     */
    @Override
    public ITmfTrace instantiateTrace() {
        try {

            // make sure that supplementary folder exists
            refreshSupplementaryFolder();

            String traceTypeId = getTraceType();
            if (traceTypeId != null) {
                if (CustomTxtTrace.isCustomTraceTypeId(traceTypeId)) {
                    for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                        String id = CustomTxtTrace.buildTraceTypeId(def.categoryName, def.definitionName);
                        if (traceTypeId.equals(id)) {
                            return new CustomTxtTrace(def);
                        }
                    }
                }
                if (CustomXmlTrace.isCustomTraceTypeId(traceTypeId)) {
                    for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                        String id = CustomXmlTrace.buildTraceTypeId(def.categoryName, def.definitionName);
                        if (traceTypeId.equals(id)) {
                            return new CustomXmlTrace(def);
                        }
                    }
                }
                IConfigurationElement ce = TRACE_TYPE_ATTRIBUTES.get(traceTypeId);
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
            String traceTypeId = getTraceType();
            if (traceTypeId != null) {
                if (CustomTxtTrace.isCustomTraceTypeId(traceTypeId)) {
                    for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                        String id = CustomTxtTrace.buildTraceTypeId(def.categoryName, def.definitionName);
                        if (traceTypeId.equals(id)) {
                            return new CustomTxtEvent(def);
                        }
                    }
                }
                if (CustomXmlTrace.isCustomTraceTypeId(traceTypeId)) {
                    for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                        String id = CustomXmlTrace.buildTraceTypeId(def.categoryName, def.definitionName);
                        if (traceTypeId.equals(id)) {
                            return new CustomXmlEvent(def);
                        }
                    }
                }
                IConfigurationElement ce = TRACE_TYPE_ATTRIBUTES.get(traceTypeId);
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

    @Override
    public String getEditorId() {
        String traceTypeId = getTraceType();
        if (traceTypeId != null) {
            if (CustomTxtTrace.isCustomTraceTypeId(traceTypeId) || CustomXmlTrace.isCustomTraceTypeId(traceTypeId)) {
                return TmfEventsEditor.ID;
            }

            IConfigurationElement ce = TRACE_TYPE_UI_ATTRIBUTES.get(getTraceType());
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
     * Returns the file resource used to store bookmarks. The file may not
     * exist.
     *
     * @return the bookmarks file
     */
    @Override
    public IFile getBookmarksFile() {
        IFile file = null;
        IResource resource = getResource();
        if (resource instanceof IFile) {
            file = (IFile) resource;
        } else if (resource instanceof IFolder) {
            final IFolder folder = (IFolder) resource;
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

        // If trace is under an experiment, return original trace from the traces folder
        if (getParent() instanceof TmfExperimentElement) {
            ITmfProjectModelElement parent = getProject().getTracesFolder();
            ITmfProjectModelElement element = null;
            if (parent != null) {
                for (String segment : new Path(getElementPath()).segments()) {
                    element = parent.getChild(segment);
                    if (element == null) {
                        return this;
                    }
                    parent = element;
                }
                if (element instanceof TmfTraceElement) {
                    return (TmfTraceElement) element;
                }
            }
        }
        return this;
    }

    @Override
    public String getTypeName() {
        return Messages.TmfTraceElement_TypeName;
    }

    // ------------------------------------------------------------------------
    // IActionFilter
    // ------------------------------------------------------------------------

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (name.equals(IS_LINKED)) {
            return Boolean.toString(ResourceUtil.isSymbolicLink(getElementUnderTraceFolder().getResource())).equals(value);
        }
        return false;
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
                if (getElementUnderTraceFolder().getResource().equals(singleTrace.getResource())) {
                    if (singleTrace instanceof ITmfPropertiesProvider) {
                        ITmfPropertiesProvider traceProperties = (ITmfPropertiesProvider) singleTrace;
                        return traceProperties.getProperties();
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
                descriptor.setCategory(TRACE_PROPERTIES_CATEGORY);
                propertyDescriptorArray[index] = descriptor;
                index++;
            }
            System.arraycopy(sfDescriptors, 0, propertyDescriptorArray, index, sfDescriptors.length);
            return propertyDescriptorArray;
        }
        return Arrays.copyOf(sfDescriptors, sfDescriptors.length);
    }

    @Override
    public Object getPropertyValue(Object id) {

        if (NAME.equals(id)) {
            return getName();
        }

        if (PATH.equals(id)) {
            return getPath().toString();
        }

        if (LOCATION.equals(id)) {
            URI uri = ResourceUtil.getLocationURI(getElementUnderTraceFolder().getResource());
            if (uri == null) {
                uri = getElementUnderTraceFolder().getLocation();
            }
            return URIUtil.toUnencodedString(new File(uri).toURI());
        }

        if (IS_LINKED_PROPERTY.equals(id)) {
            return Boolean.toString(ResourceUtil.isSymbolicLink(getElementUnderTraceFolder().getResource()));
        }

        if (SOURCE_LOCATION.equals(id)) {
            try {
                String sourceLocation = getElementUnderTraceFolder().getResource().getPersistentProperty(TmfCommonConstants.SOURCE_LOCATION);
                if (sourceLocation != null) {
                    return sourceLocation;
                }
            } catch (CoreException e) {
            }
            return ""; //$NON-NLS-1$
        }

        if (LAST_MODIFIED.equals(id)) {
            FileInfo fileInfo = getElementUnderTraceFolder().getFileInfo();
            if (fileInfo == null) {
                return ""; //$NON-NLS-1$
            }
            long date = fileInfo.lastModified;
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
            return format.format(new Date(date));
        }

        if (SIZE.equals(id)) {
            FileInfo fileInfo = getElementUnderTraceFolder().getFileInfo();
            if (fileInfo == null) {
                return ""; //$NON-NLS-1$
            }
            if (getElementUnderTraceFolder().getResource() instanceof IFolder) {
                if (fileInfo.count <= FOLDER_MAX_COUNT) {
                    return NLS.bind(Messages.TmfTraceElement_FolderSizeString,
                            NumberFormat.getInstance().format(fileInfo.size), fileInfo.count);
                }
                return NLS.bind(Messages.TmfTraceElement_FolderSizeOverflowString,
                        NumberFormat.getInstance().format(fileInfo.size), FOLDER_MAX_COUNT);
            }
            return NLS.bind(Messages.TmfTraceElement_FileSizeString, NumberFormat.getInstance().format(fileInfo.size));
        }

        if (TRACE_TYPE.equals(id)) {
            if (getTraceType() != null) {
                TraceTypeHelper helper = TmfTraceType.getTraceType(getTraceType());
                if (helper != null) {
                    return helper.getLabel();
                }
            }
            return ""; //$NON-NLS-1$
        }

        if (TRACE_TYPE_ID.equals(id)) {
            if (getTraceType() != null) {
                return getTraceType();
            }
            return ""; //$NON-NLS-1$
        }

        if (TIME_OFFSET.equals(id)) {
            long offset = TimestampTransformFactory.getTimestampTransform(getElementUnderTraceFolder().getResource()).transform(0);
            if (offset != 0) {
                return OFFSET_FORMAT.format(offset);
            }
            return ""; //$NON-NLS-1$
        }

        Map<String, String> traceProperties = getTraceProperties();
        if (id != null && !traceProperties.isEmpty()) {
            String key = (String) id;
            key = key.substring(this.getName().length() + 1); // remove name_
            String value = traceProperties.get(key);
            return value;
        }

        return null;
    }

    private FileInfo getFileInfo() {
        /* FileInfo is needed for both 'last modified' and 'size' properties.
         * It is freshly computed for one, and reused for the other, then
         * cleared so that the information can be refreshed the next time.
         */
        FileInfo fileInfo;
        if (fFileInfo == null) {
            try {
                fileInfo = computeFileInfo(new FileInfo(), getResource());
            } catch (CoreException e) {
                return null;
            }
            fFileInfo = fileInfo;
        } else {
            fileInfo = fFileInfo;
            fFileInfo = null;
        }
        return fileInfo;
    }

    private FileInfo computeFileInfo(FileInfo fileInfo, IResource resource) throws CoreException {
        if (fileInfo == null || fileInfo.count > FOLDER_MAX_COUNT) {
            return fileInfo;
        }
        if (resource instanceof IFolder) {
            IFolder folder = (IFolder) resource;
            for (IResource member : folder.members()) {
                computeFileInfo(fileInfo, member);
            }
            return fileInfo;
        }
        IFileInfo info = EFS.getStore(resource.getLocationURI()).fetchInfo();
        fileInfo.lastModified = Math.max(fileInfo.lastModified, info.getLastModified());
        fileInfo.size += info.getLength();
        fileInfo.count++;
        return fileInfo;
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
     * @param newName
     *            The new trace name
     * @return the new Resource object
     */
    public TmfTraceElement copy(String newName) {
        TmfTraceFolder folder = (TmfTraceFolder) getParent();
        IResource res = super.copy(newName, false);
        for (TmfTraceElement trace : folder.getTraces()) {
            if (trace.getResource().equals(res)) {
                return trace;
            }
        }
        return null;
    }

    /**
     * Close opened editors associated with this trace.
     */
    @Override
    public void closeEditors() {
        super.closeEditors();

        // Close experiments that contain the trace if open
        if (getParent() instanceof TmfTraceFolder) {
            TmfExperimentFolder experimentsFolder = getProject().getExperimentsFolder();
            if (experimentsFolder != null) {
                for (TmfExperimentElement experiment : experimentsFolder.getExperiments()) {
                    for (TmfTraceElement trace : experiment.getTraces()) {
                        if (trace.getElementPath().equals(getElementPath())) {
                            experiment.closeEditors();
                            break;
                        }
                    }
                }
            }
        } else if (getParent() instanceof TmfExperimentElement) {
            TmfExperimentElement experiment = (TmfExperimentElement) getParent();
            experiment.closeEditors();
        }

        /*
         * We will be deleting a trace shortly. Invoke GC to release
         * MappedByteBuffer objects, which some trace types, like CTF, use.
         * (see Java bug JDK-4724038)
         */
        if (ByteBufferTracker.getAndReset()) {
            System.gc();
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
     */
    public void delete(IProgressMonitor progressMonitor) throws CoreException {
        delete(progressMonitor, false);
    }

    /**
     * Delete the trace resource, and optionally remove it from experiments and
     * delete its supplementary files.
     *
     * @param progressMonitor
     *            a progress monitor, or null if progress reporting is not desired
     * @param overwriting
     *            if true, keep the trace in experiments and only delete non-hidden
     *            supplementary files (keeping the properties sub-folder), otherwise
     *            remove the trace from experiments and delete the supplementary
     *            folder completely
     *
     * @throws CoreException
     *             thrown when IResource.delete fails
     * @since 3.1
     */
    public void delete(IProgressMonitor progressMonitor, boolean overwriting) throws CoreException {
        delete(progressMonitor, overwriting, true);
    }

    /**
     * Delete the trace resource, and optionally remove it from experiments and
     * delete its supplementary files. Editors are first closed if requested.
     *
     * @param progressMonitor
     *            a progress monitor, or null if progress reporting is not desired
     * @param overwriting
     *            if true, keep the trace in experiments and only delete non-hidden
     *            supplementary files (keeping the properties sub-folder), otherwise
     *            remove the trace from experiments and delete the supplementary
     *            folder completely
     * @param closeEditors
     *            if true, editors associated with this trace are first closed
     *            before proceeding, otherwise it is the responsibility of the
     *            caller to first close editors before calling the method
     *
     * @throws CoreException
     *             thrown when IResource.delete fails
     * @since 3.4
     */
    public void delete(IProgressMonitor progressMonitor, boolean overwriting, boolean closeEditors) throws CoreException {
        // Close editors in UI Thread
        if (closeEditors) {
            Display.getDefault().syncExec(this::closeEditors);
        }

        IResource resourceToDelete = getResource();
        if (resourceToDelete == null) {
            return;
        }
        IPath path = resourceToDelete.getLocation();
        if (path != null) {
            if (getParent() instanceof TmfTraceFolder) {
                TmfExperimentFolder experimentFolder = getProject().getExperimentsFolder();

                // Propagate the removal to experiments
                if (experimentFolder != null && !overwriting) {
                    for (TmfExperimentElement experiment : experimentFolder.getExperiments()) {
                        List<TmfTraceElement> toRemove = new LinkedList<>();
                        for (TmfTraceElement trace : experiment.getTraces()) {
                            if (trace.getElementPath().equals(getElementPath())) {
                                toRemove.add(trace);
                            }
                        }
                        for (TmfTraceElement child : toRemove) {
                            experiment.removeTrace(child, false);
                        }
                        if (!toRemove.isEmpty() && experiment.getTraces().isEmpty()) {
                            // If experiment becomes empty, delete it
                            experiment.deleteSupplementaryFolder();
                            experiment.getResource().delete(true, progressMonitor);
                        }
                    }
                }
                // Delete supplementary files
                if (overwriting) {
                    deleteSupplementaryResources();
                } else {
                    deleteSupplementaryFolder();
                }

            } else if (getParent() instanceof TmfExperimentElement) {
                TmfExperimentElement experimentElement = (TmfExperimentElement) getParent();
                experimentElement.removeTrace(this, false);
            }
        }

        // Finally, delete the trace
        ResourceUtil.deleteResource(resourceToDelete, progressMonitor);
    }

    /**
     * Update the trace's start time
     *
     * @param startTime
     *            updated start time for this trace
     * @since 3.0
     */
    public void setStartTime(ITmfTimestamp startTime) {
        fStartTime = startTime;
    }

    /**
     * Getter for the trace start time
     *
     * @return the start time from the trace if available, or from self when
     *         read in advance from supplementary files or from fast trace read.
     *         Return null if completely unknown.
     * @since 3.0
     */
    public ITmfTimestamp getStartTime() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            setStartTime(trace.getStartTime());
        }
        return fStartTime;
    }

    /**
     * Update the trace's end time
     *
     * @param end
     *            updated end time for this trace
     * @since 3.0
     */
    public void setEndTime(@NonNull ITmfTimestamp end) {
        if (fEndTime == null || end.compareTo(fEndTime) > 0) {
            fEndTime = end;
        }
    }

    /**
     * Getter for the trace end time
     *
     * @return the end time from the trace if available, or from self when read
     *         in advance from supplementary files or from fast trace read.
     *         Return null if completely unknown.
     * @since 3.0
     */
    public ITmfTimestamp getEndTime() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            setEndTime(trace.getEndTime());
        }
        return fEndTime;
    }

    @Override
    public void deleteSupplementaryResources(IResource[] resources) {
        /* Invalidate the cached trace bounds */
        fStartTime = null;
        fEndTime = null;

        super.deleteSupplementaryResources(resources);
    }

    /**
     * Deletes all supplementary resources in the supplementary directory. Also
     * delete the supplementary resources of experiments that contain this trace.
     */
    @Override
    public void deleteSupplementaryResources() {
        super.deleteSupplementaryResources();

        // Propagate the deletion to experiments
        TmfExperimentFolder experimentFolder = getProject().getExperimentsFolder();
        if (experimentFolder != null) {
            for (TmfExperimentElement experiment : experimentFolder.getExperiments()) {
                for (TmfTraceElement trace : experiment.getTraces()) {
                    if (trace.getElementPath().equals(getElementPath())) {
                        experiment.deleteSupplementaryResources();
                        break;
                    }
                }
            }
        }
    }
}
