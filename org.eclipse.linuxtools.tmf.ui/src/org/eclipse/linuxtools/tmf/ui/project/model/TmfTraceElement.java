/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson, École Polytechnique de Montréal
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

package org.eclipse.linuxtools.tmf.ui.project.model;

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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceProperties;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
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
    private static final String sfResourcePropertiesCategory = Messages.TmfTraceElement_ResourceProperties;
    private static final String sfName = Messages.TmfTraceElement_Name;
    private static final String sfPath = Messages.TmfTraceElement_Path;
    private static final String sfLocation = Messages.TmfTraceElement_Location;
    private static final String sfTraceType = Messages.TmfTraceElement_EventType;
    private static final String sfIsLinked = Messages.TmfTraceElement_IsLinked;
    private static final String sfSourceLocation = Messages.TmfTraceElement_SourceLocation;
    private static final String sfTimeOffset = Messages.TmfTraceElement_TimeOffset;
    private static final String sfLastModified = Messages.TmfTraceElement_LastModified;
    private static final String sfSize = Messages.TmfTraceElement_Size;
    private static final String sfTracePropertiesCategory = Messages.TmfTraceElement_TraceProperties;

    private static final ReadOnlyTextPropertyDescriptor sfNameDescriptor = new ReadOnlyTextPropertyDescriptor(sfName, sfName);
    private static final ReadOnlyTextPropertyDescriptor sfPathDescriptor = new ReadOnlyTextPropertyDescriptor(sfPath, sfPath);
    private static final ReadOnlyTextPropertyDescriptor sfLocationDescriptor = new ReadOnlyTextPropertyDescriptor(sfLocation, sfLocation);
    private static final ReadOnlyTextPropertyDescriptor sfTypeDescriptor = new ReadOnlyTextPropertyDescriptor(sfTraceType, sfTraceType);
    private static final ReadOnlyTextPropertyDescriptor sfIsLinkedDescriptor = new ReadOnlyTextPropertyDescriptor(sfIsLinked, sfIsLinked);
    private static final ReadOnlyTextPropertyDescriptor sfSourceLocationDescriptor = new ReadOnlyTextPropertyDescriptor(sfSourceLocation, sfSourceLocation);
    private static final ReadOnlyTextPropertyDescriptor sfTimeOffsetDescriptor = new ReadOnlyTextPropertyDescriptor(sfTimeOffset, sfTimeOffset);
    private static final ReadOnlyTextPropertyDescriptor sfLastModifiedDescriptor = new ReadOnlyTextPropertyDescriptor(sfLastModified, sfLastModified);
    private static final ReadOnlyTextPropertyDescriptor sfSizeDescriptor = new ReadOnlyTextPropertyDescriptor(sfSize, sfSize);

    private static final IPropertyDescriptor[] sfDescriptors = { sfNameDescriptor, sfPathDescriptor, sfLocationDescriptor,
            sfTypeDescriptor, sfIsLinkedDescriptor, sfSourceLocationDescriptor,
            sfTimeOffsetDescriptor, sfLastModifiedDescriptor, sfSizeDescriptor };

    static {
        sfNameDescriptor.setCategory(sfResourcePropertiesCategory);
        sfPathDescriptor.setCategory(sfResourcePropertiesCategory);
        sfLocationDescriptor.setCategory(sfResourcePropertiesCategory);
        sfTypeDescriptor.setCategory(sfResourcePropertiesCategory);
        sfIsLinkedDescriptor.setCategory(sfResourcePropertiesCategory);
        sfSourceLocationDescriptor.setCategory(sfResourcePropertiesCategory);
        sfTimeOffsetDescriptor.setCategory(sfResourcePropertiesCategory);
        sfLastModifiedDescriptor.setCategory(sfResourcePropertiesCategory);
        sfSizeDescriptor.setCategory(sfResourcePropertiesCategory);
    }

    private static final TmfTimestampFormat OFFSET_FORMAT = new TmfTimestampFormat("T.SSS SSS SSS s"); //$NON-NLS-1$

    private static final int FOLDER_MAX_COUNT = 1024;

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

            if (getTraceType() != null) {
                if (getTraceType().startsWith(CustomTxtTrace.class.getCanonicalName())) {
                    for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                        if (getTraceType().equals(CustomTxtTrace.class.getCanonicalName() + ':' + def.categoryName+ ':' + def.definitionName)) {
                            return new CustomTxtTrace(def);
                        }
                    }
                }
                if (getTraceType().startsWith(CustomXmlTrace.class.getCanonicalName())) {
                    for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                        if (getTraceType().equals(CustomXmlTrace.class.getCanonicalName() + ':' + def.categoryName+ ':' + def.definitionName)) {
                            return new CustomXmlTrace(def);
                        }
                    }
                }
                IConfigurationElement ce = sfTraceTypeAttributes.get(getTraceType());
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
            if (getTraceType() != null) {
                if (getTraceType().startsWith(CustomTxtTrace.class.getCanonicalName())) {
                    for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                        if (getTraceType().equals(CustomTxtTrace.class.getCanonicalName() + ':' + def.categoryName+ ':' + def.definitionName)) {
                            return new CustomTxtEvent(def);
                        }
                    }
                }
                if (getTraceType().startsWith(CustomXmlTrace.class.getCanonicalName())) {
                    for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                        if (getTraceType().equals(CustomXmlTrace.class.getCanonicalName() + ':' + def.categoryName+ ':' + def.definitionName)) {
                            return new CustomXmlEvent(def);
                        }
                    }
                }
                IConfigurationElement ce = sfTraceTypeAttributes.get(getTraceType());
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
        if (getTraceType() != null) {
            if (getTraceType().startsWith(CustomTxtTrace.class.getCanonicalName())) {
                return TmfEventsEditor.ID;
            }
            if (getTraceType().startsWith(CustomXmlTrace.class.getCanonicalName())) {
                return TmfEventsEditor.ID;
            }
            IConfigurationElement ce = sfTraceTypeUIAttributes.get(getTraceType());
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
    @Override
    public IFile createBookmarksFile() throws CoreException {
        IFile file = getBookmarksFile();
        if (fResource instanceof IFolder) {
            return createBookmarksFile(getProject().getTracesFolder().getResource(), TmfTrace.class.getCanonicalName());
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
    @Override
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
                if (aTrace.getElementPath().equals(getElementPath())) {
                    return aTrace;
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
            boolean isLinked = getResource().isLinked();
            return Boolean.toString(isLinked).equals(value);
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
            return URIUtil.toUnencodedString(getLocation());
        }

        if (sfIsLinked.equals(id)) {
            return Boolean.valueOf(getResource().isLinked()).toString();
        }

        if (sfSourceLocation.equals(id)) {
            try {
                String sourceLocation = getElementUnderTraceFolder().getResource().getPersistentProperty(TmfCommonConstants.SOURCE_LOCATION);
                if (sourceLocation != null) {
                    return sourceLocation;
                }
            } catch (CoreException e) {
            }
            return ""; //$NON-NLS-1$
        }

        if (sfLastModified.equals(id)) {
            FileInfo fileInfo = getFileInfo();
            if (fileInfo == null) {
                return ""; //$NON-NLS-1$
            }
            long date = fileInfo.lastModified;
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
            return format.format(new Date(date));
        }

        if (sfSize.equals(id)) {
            FileInfo fileInfo = getFileInfo();
            if (fileInfo == null) {
                return ""; //$NON-NLS-1$
            }
            if (getResource() instanceof IFolder) {
                if (fileInfo.count <= FOLDER_MAX_COUNT) {
                    return NLS.bind(Messages.TmfTraceElement_FolderSizeString,
                            NumberFormat.getInstance().format(fileInfo.size), fileInfo.count);
                }
                return NLS.bind(Messages.TmfTraceElement_FolderSizeOverflowString,
                        NumberFormat.getInstance().format(fileInfo.size), FOLDER_MAX_COUNT);
            }
            return NLS.bind(Messages.TmfTraceElement_FileSizeString, NumberFormat.getInstance().format(fileInfo.size));
        }

        if (sfTraceType.equals(id)) {
            if (getTraceType() != null) {
                TraceTypeHelper helper = TmfTraceType.getTraceType(getTraceType());
                if (helper != null) {
                    return helper.getCategoryName() + " : " + helper.getName(); //$NON-NLS-1$
                }
            }
            return ""; //$NON-NLS-1$
        }

        if (sfTimeOffset.equals(id)) {
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
     * @since 2.0
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
     *
     * @since 2.0
     */
    @Override
    public void closeEditors() {
        super.closeEditors();

        // Close experiments that contain the trace if open
        if (getParent() instanceof TmfTraceFolder) {
            TmfExperimentFolder experimentsFolder = getProject().getExperimentsFolder();
            for (TmfExperimentElement experiment : experimentsFolder.getExperiments()) {
                for (TmfTraceElement trace : experiment.getTraces()) {
                    if (trace.getElementPath().equals(getElementPath())) {
                        experiment.closeEditors();
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
        // Close editors in UI Thread
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                closeEditors();
            }
        });

        IPath path = fResource.getLocation();
        if (path != null) {
            if (getParent() instanceof TmfTraceFolder) {
                TmfExperimentFolder experimentFolder = getProject().getExperimentsFolder();

                // Propagate the removal to traces
                for (TmfExperimentElement experiment : experimentFolder.getExperiments()) {
                    List<TmfTraceElement> toRemove = new LinkedList<>();
                    for (TmfTraceElement trace : experiment.getTraces()) {
                        if (trace.getElementPath().equals(getElementPath())) {
                            toRemove.add(trace);
                        }
                    }
                    for (TmfTraceElement child : toRemove) {
                        experiment.removeTrace(child);
                    }
                }

                // Delete supplementary files
                deleteSupplementaryFolder();

            } else if (getParent() instanceof TmfExperimentElement) {
                TmfExperimentElement experimentElement = (TmfExperimentElement) getParent();
                experimentElement.removeTrace(this);
            }
        }

        // Finally, delete the trace
        fResource.delete(true, progressMonitor);
    }

}
