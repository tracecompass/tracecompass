/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Geneviève Bastien - Copied code to add/remove traces in this class
 *   Patrick Tasse - Close editors to release resources
 *   Geneviève Bastien - Experiment instantiated with trace type
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.editors.ITmfEventsEditorConstants;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

/**
 * Implementation of TMF Experiment Model Element.
 * <p>
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 */
public class TmfExperimentElement extends TmfCommonProjectElement implements IPropertySource2 {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Property View stuff
    private static final String INFO_CATEGORY = "Info"; //$NON-NLS-1$
    private static final String NAME = "name"; //$NON-NLS-1$
    private static final String PATH = "path"; //$NON-NLS-1$
    private static final String LOCATION = "location"; //$NON-NLS-1$
    private static final String FOLDER_SUFFIX = "_exp"; //$NON-NLS-1$
    private static final String EXPERIMENT_TYPE = "type"; //$NON-NLS-1$

    private static final ReadOnlyTextPropertyDescriptor NAME_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(NAME, NAME);
    private static final ReadOnlyTextPropertyDescriptor PATH_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(PATH, PATH);
    private static final ReadOnlyTextPropertyDescriptor LOCATION_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(LOCATION,
            LOCATION);
    private static final ReadOnlyTextPropertyDescriptor TYPE_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(EXPERIMENT_TYPE, EXPERIMENT_TYPE);

    private static final IPropertyDescriptor[] DESCRIPTORS = { NAME_DESCRIPTOR, PATH_DESCRIPTOR,
            LOCATION_DESCRIPTOR, TYPE_DESCRIPTOR };

    static {
        NAME_DESCRIPTOR.setCategory(INFO_CATEGORY);
        PATH_DESCRIPTOR.setCategory(INFO_CATEGORY);
        LOCATION_DESCRIPTOR.setCategory(INFO_CATEGORY);
        TYPE_DESCRIPTOR.setCategory(INFO_CATEGORY);
    }

    // The mapping of available trace type IDs to their corresponding
    // configuration element
    private static final Map<String, IConfigurationElement> TRACE_TYPE_ATTRIBUTES = new HashMap<>();
    private static final Map<String, IConfigurationElement> TRACE_TYPE_UI_ATTRIBUTES = new HashMap<>();
    private static final Map<String, IConfigurationElement> TRACE_CATEGORIES = new HashMap<>();

    // ------------------------------------------------------------------------
    // Static initialization
    // ------------------------------------------------------------------------

    /**
     * Initialize statically at startup by getting extensions from the platform
     * extension registry.
     */
    public static void init() {
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceType.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            String elementName = ce.getName();
            if (elementName.equals(TmfTraceType.EXPERIMENT_ELEM)) {
                String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                TRACE_TYPE_ATTRIBUTES.put(traceTypeId, ce);
            } else if (elementName.equals(TmfTraceType.CATEGORY_ELEM)) {
                String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                TRACE_CATEGORIES.put(categoryId, ce);
            }
        }

        /*
         * Read the corresponding tmf.ui "tracetypeui" extension point for this
         * trace type, if it exists.
         */
        config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceTypeUIUtils.TMF_TRACE_TYPE_UI_ID);
        for (IConfigurationElement ce : config) {
            String elemName = ce.getName();
            if (TmfTraceTypeUIUtils.EXPERIMENT_ELEM.equals(elemName)) {
                String traceType = ce.getAttribute(TmfTraceTypeUIUtils.TRACETYPE_ATTR);
                TRACE_TYPE_UI_ATTRIBUTES.put(traceType, ce);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param name
     *            The name of the experiment
     * @param folder
     *            The folder reference
     * @param parent
     *            The experiment folder reference.
     */
    public TmfExperimentElement(String name, IFolder folder, TmfExperimentFolder parent) {
        super(name, folder, parent);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public IFolder getResource() {
        return (IFolder) super.getResource();
    }

    /**
     * @since 2.0
     */
    @Override
    protected void refreshChildren() {
        IFolder folder = getResource();

        /* Update the trace children of this experiment */
        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<>();
        for (TmfTraceElement trace : getTraces()) {
            childrenMap.put(trace.getElementPath(), trace);
        }

        List<IResource> members = getTraceResources();
        for (IResource resource : members) {
            String name = resource.getName();
            String elementPath = resource.getFullPath().makeRelativeTo(folder.getFullPath()).toString();
            ITmfProjectModelElement element = childrenMap.get(elementPath);
            if (element instanceof TmfTraceElement) {
                childrenMap.remove(elementPath);
            } else {
                element = new TmfTraceElement(name, resource, this);
                addChild(element);
            }
        }

        // Cleanup dangling children from the model
        for (ITmfProjectModelElement danglingChild : childrenMap.values()) {
            removeChild(danglingChild);
        }

        /* Update the analysis under this experiment */
        super.refreshChildren();

        /*
         * If the experiment is opened, add any analysis that was not added by
         * the parent if it is available with the experiment
         */
        ITmfTrace experiment = getTrace();
        if (experiment == null) {
            return;
        }

        /* super.refreshChildren() above should have set this */
        TmfViewsElement viewsElement = checkNotNull(getChildElementViews());

        Map<String, TmfAnalysisElement> analysisMap = new HashMap<>();
        for (TmfAnalysisElement analysis : getAvailableAnalysis()) {
            analysisMap.put(analysis.getAnalysisId(), analysis);
        }
        for (IAnalysisModuleHelper module : TmfAnalysisManager.getAnalysisModules().values()) {
            if (!analysisMap.containsKey(module.getId()) && module.appliesToExperiment() && (experiment.getAnalysisModule(module.getId()) != null)) {
                IFolder newresource = ResourcesPlugin.getWorkspace().getRoot().getFolder(getResource().getFullPath().append(module.getId()));
                TmfAnalysisElement analysis = new TmfAnalysisElement(module.getName(), newresource, viewsElement, module);
                viewsElement.addChild(analysis);
                analysis.refreshChildren();
                analysisMap.put(module.getId(), analysis);
            }
        }
    }

    private List<IResource> getTraceResources() {
        IFolder folder = getResource();
        final List<IResource> list = new ArrayList<>();
        try {
            folder.accept(new IResourceProxyVisitor() {
                @Override
                public boolean visit(IResourceProxy resource) throws CoreException {
                    if (resource.isLinked()) {
                        list.add(resource.requestResource());
                    }
                    return true;
                }
            }, IResource.NONE);
        } catch (CoreException e) {
        }
        Comparator<IResource> comparator = new Comparator<IResource>() {
            @Override
            public int compare(IResource o1, IResource o2) {
                return o1.getFullPath().toString().compareTo(o2.getFullPath().toString());
            }
        };
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * @since 2.0
     */
    @Override
    public @NonNull Image getIcon() {
        Image icon = super.getIcon();
        return (icon == null ? TmfProjectModelIcons.DEFAULT_EXPERIMENT_ICON : icon);
    }

    /**
     * @since 2.0
     */
    @Override
    public String getLabelText() {
        return getName() + " [" + getTraces().size() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Refreshes the trace type filed by reading the trace type persistent
     * property of the resource reference.
     *
     * If trace type is null after refresh, set it to the generic trace type
     * (for seamless upgrade)
     */
    @Override
    public void refreshTraceType() {
        super.refreshTraceType();
        if (getTraceType() == null) {
            IConfigurationElement ce = TmfTraceType.getTraceAttributes(TmfTraceType.DEFAULT_EXPERIMENT_TYPE);
            if (ce != null) {
                try {
                    IFolder experimentFolder = getResource();
                    experimentFolder.setPersistentProperty(TmfCommonConstants.TRACETYPE, ce.getAttribute(TmfTraceType.ID_ATTR));
                    super.refreshTraceType();
                } catch (InvalidRegistryObjectException | CoreException e) {
                }
            }
        }
    }

    /**
     * Returns a list of TmfTraceElements contained in this experiment.
     *
     * @return a list of TmfTraceElements
     */
    @Override
    public List<TmfTraceElement> getTraces() {
        List<ITmfProjectModelElement> children = getChildren();
        List<TmfTraceElement> traces = new ArrayList<>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfTraceElement) {
                traces.add((TmfTraceElement) child);
            }
        }
        return traces;
    }

    /**
     * Adds a trace to the experiment
     *
     * @param trace
     *            The trace element to add
     */
    public void addTrace(TmfTraceElement trace) {
        addTrace(trace, true);
    }

    /**
     * Adds a trace to the experiment
     *
     * @param trace
     *            The trace element to add
     * @param refresh
     *            Flag for refreshing the project
     */
    public void addTrace(TmfTraceElement trace, boolean refresh) {
        /**
         * Create a link to the actual trace and set the trace type
         */
        IFolder experiment = getResource();
        IResource resource = trace.getResource();
        IPath location = resource.getLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            String traceTypeId = TmfTraceType.getTraceTypeId(trace.getResource());
            TraceTypeHelper traceType = TmfTraceType.getTraceType(traceTypeId);

            if (resource instanceof IFolder) {
                IFolder folder = experiment.getFolder(trace.getElementPath());
                TraceUtils.createFolder((IFolder) folder.getParent(), new NullProgressMonitor());
                IStatus result = workspace.validateLinkLocation(folder, location);
                if (result.isOK() || result.matches(IStatus.INFO | IStatus.WARNING)) {
                    folder.createLink(location, IResource.REPLACE, null);
                    if (traceType != null) {
                        TmfTraceTypeUIUtils.setTraceType(folder, traceType, refresh);
                    }

                } else {
                    Activator.getDefault().logError("Error creating link. Invalid trace location " + location); //$NON-NLS-1$
                }
            } else {
                IFile file = experiment.getFile(trace.getElementPath());
                TraceUtils.createFolder((IFolder) file.getParent(), new NullProgressMonitor());
                IStatus result = workspace.validateLinkLocation(file, location);
                if (result.isOK() || result.matches(IStatus.INFO | IStatus.WARNING)) {
                    file.createLink(location, IResource.REPLACE, null);
                    if (traceType != null) {
                        TmfTraceTypeUIUtils.setTraceType(file, traceType, refresh);
                    }
                } else {
                    Activator.getDefault().logError("Error creating link. Invalid trace location " + location); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating link to location " + location, e); //$NON-NLS-1$
        }

    }

    /**
     * Removes a trace from an experiment
     *
     * @param trace
     *            The trace to remove
     * @throws CoreException
     *             exception
     */
    public void removeTrace(TmfTraceElement trace) throws CoreException {

        // Close editors in UI Thread
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                closeEditors();
            }
        });

        /* Finally, remove the trace from experiment */
        removeChild(trace);
        deleteTraceResource(trace.getResource());
        deleteSupplementaryResources();
    }

    private void deleteTraceResource(IResource resource) throws CoreException {
        resource.delete(true, null);
        IContainer parent = resource.getParent();
        // delete empty folders up to the parent experiment folder
        if (!parent.equals(getResource()) && parent.members().length == 0) {
            deleteTraceResource(parent);
        }
    }

    @Override
    public IFile createBookmarksFile() throws CoreException {
        return createBookmarksFile(getProject().getExperimentsFolder().getResource(), ITmfEventsEditorConstants.EXPERIMENT_EDITOR_INPUT_TYPE);
    }

    @Override
    public String getEditorId() {
        /* See if a default editor was set for this experiment type */
        if (getTraceType() != null) {
            IConfigurationElement ce = TRACE_TYPE_UI_ATTRIBUTES.get(getTraceType());
            if (ce != null) {
                IConfigurationElement[] defaultEditorCE = ce.getChildren(TmfTraceTypeUIUtils.DEFAULT_EDITOR_ELEM);
                if (defaultEditorCE.length == 1) {
                    return defaultEditorCE[0].getAttribute(TmfTraceType.ID_ATTR);
                }
            }
        }

        /* No default editor, try to find a common editor for all traces */
        final List<TmfTraceElement> traceEntries = getTraces();
        String commonEditorId = null;

        for (TmfTraceElement element : traceEntries) {
            // If all traces use the same editorId, use it, otherwise use the
            // default
            final String editorId = element.getEditorId();
            if (commonEditorId == null) {
                commonEditorId = (editorId != null) ? editorId : TmfEventsEditor.ID;
            } else if (!commonEditorId.equals(editorId)) {
                commonEditorId = TmfEventsEditor.ID;
            }
        }
        return null;
    }

    /**
     * Instantiate a {@link TmfExperiment} object based on the experiment type
     * and the corresponding extension.
     *
     * @return the {@link TmfExperiment} or <code>null</code> for an error
     */
    @Override
    public TmfExperiment instantiateTrace() {
        try {

            // make sure that supplementary folder exists
            refreshSupplementaryFolder();

            if (getTraceType() != null) {

                IConfigurationElement ce = TRACE_TYPE_ATTRIBUTES.get(getTraceType());
                if (ce == null) {
                    return null;
                }
                TmfExperiment experiment = (TmfExperiment) ce.createExecutableExtension(TmfTraceType.EXPERIMENT_TYPE_ATTR);
                return experiment;
            }
        } catch (CoreException e) {
            Activator.getDefault().logError(NLS.bind(Messages.TmfExperimentElement_ErrorInstantiatingTrace, getName()), e);
        }
        return null;
    }

    @Override
    public String getTypeName() {
        return Messages.TmfExperimentElement_TypeName;
    }

    // ------------------------------------------------------------------------
    // IPropertySource2
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return Arrays.copyOf(DESCRIPTORS, DESCRIPTORS.length);
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
            return getLocation().toString();
        }

        if (EXPERIMENT_TYPE.equals(id)) {
            if (getTraceType() != null) {
                IConfigurationElement ce = TRACE_TYPE_ATTRIBUTES.get(getTraceType());
                if (ce == null) {
                    return ""; //$NON-NLS-1$
                }
                String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
                if (categoryId != null) {
                    IConfigurationElement category = TRACE_CATEGORIES.get(categoryId);
                    if (category != null) {
                        return category.getAttribute(TmfTraceType.NAME_ATTR) + ':' + ce.getAttribute(TmfTraceType.NAME_ATTR);
                    }
                }
                return ce.getAttribute(TmfTraceType.NAME_ATTR);
            }
        }

        return null;
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
     * Return the suffix for resource names
     *
     * @return The folder suffix
     */
    @Override
    public String getSuffix() {
        return FOLDER_SUFFIX;
    }

}
