/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.tracecompass.tmf.core.project.model.ITmfPropertiesProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.osgi.framework.Bundle;

/**
 * Class for project elements of type analysis modules
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class TmfAnalysisElement extends TmfProjectModelElement implements ITmfStyledProjectModelElement, IPropertySource2 {

    private static final Styler ANALYSIS_CANT_EXECUTE_STYLER = new Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
            textStyle.strikeout = true;
        }
    };

    private final @NonNull IAnalysisModuleHelper fAnalysisHelper;
    private boolean fCanExecute = true;

    private static final String ANALYSIS_PROPERTIES_CATEGORY = Messages.TmfAnalysisElement_AnalysisProperties;
    private static final String HELPER_PROPERTIES_CATEGORY = Messages.TmfAnalysisElement_HelperProperties;

    /**
     * Constructor
     *
     * @param name
     *            Name of the analysis
     * @param resource
     *            The resource
     * @param parent
     *            Parent of the analysis
     * @param module
     *            The analysis module helper
     * @since 2.0
     */
    protected TmfAnalysisElement(String name, IResource resource,
            TmfViewsElement parent, @NonNull IAnalysisModuleHelper module) {
        super(name, resource, parent);
        fAnalysisHelper = module;
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public TmfViewsElement getParent() {
        /* Type enforced at constructor */
        return (TmfViewsElement) super.getParent();
    }

    /**
     * @since 2.0
     */
    @Override
    protected void refreshChildren() {
        fCanExecute = true;

        /* Refresh the outputs of this analysis */
        Map<String, TmfAnalysisOutputElement> childrenMap = new HashMap<>();
        for (TmfAnalysisOutputElement output : getAvailableOutputs()) {
            childrenMap.put(output.getName(), output);
        }

        /** Get base path for resource */
        IPath path = getProject().getTracesFolder().getPath();
        IResource resource = getResource();
        if (resource instanceof IFolder) {
            path = ((IFolder) resource).getFullPath();
        }

        /*
         * We can get a list of available outputs once the analysis is
         * instantiated when the trace is opened
         */
        TmfCommonProjectElement parentTraceElement = getParent().getParent();

        ITmfTrace trace = parentTraceElement.getTrace();
        if (trace == null) {
            deleteOutputs();
            return;
        }

        IAnalysisModule module = trace.getAnalysisModule(fAnalysisHelper.getId());
        if (module == null) {
            deleteOutputs();
            /*
             * Trace is opened, but the analysis is null, so it does not
             * apply
             */
            fCanExecute = false;
            return;
        }

        for (IAnalysisOutput output : module.getOutputs()) {
            TmfAnalysisOutputElement outputElement = childrenMap.remove(output.getName());
            if (outputElement == null) {
                IFolder newresource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path.append(output.getName()));
                outputElement = new TmfAnalysisOutputElement(output.getName(), newresource, this, output);
                addChild(outputElement);
            }
            outputElement.refreshChildren();
        }

        /* Remove outputs that are not children of this analysis anymore */
        for (TmfAnalysisOutputElement output : childrenMap.values()) {
            removeChild(output);
        }
    }

    /**
     * @since 2.0
     */
    @Override
    public Image getIcon() {
        String iconFile = getIconFile();
        if (iconFile != null) {
            Bundle bundle = getBundle();
            if (bundle != null) {
                Image icon = TmfProjectModelIcons.loadIcon(bundle, iconFile);
                if (icon != null) {
                    return icon;
                }
            }
        }
        return TmfProjectModelIcons.DEFAULT_ANALYSIS_ICON;
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public Styler getStyler() {
        if (!fCanExecute) {
            return ANALYSIS_CANT_EXECUTE_STYLER;
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Get the list of analysis output model elements under this analysis
     *
     * @return Array of analysis output elements
     */
    public List<TmfAnalysisOutputElement> getAvailableOutputs() {
        List<ITmfProjectModelElement> children = getChildren();
        List<TmfAnalysisOutputElement> outputs = new ArrayList<>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfAnalysisOutputElement) {
                outputs.add((TmfAnalysisOutputElement) child);
            }
        }
        return outputs;
    }

    /**
     * Gets the analysis id of this module
     *
     * @return The analysis id
     */
    public String getAnalysisId() {
        return fAnalysisHelper.getId();
    }

    /**
     * Gets the help message for this analysis
     *
     * @return The help message
     */
    public String getHelpMessage() {
        TmfCommonProjectElement parentTrace = getParent().getParent();

        ITmfTrace trace = null;
        if (parentTrace instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) parentTrace;
            trace = traceElement.getTrace();
            if (trace != null) {
                IAnalysisModule module = trace.getAnalysisModule(fAnalysisHelper.getId());
                if (module != null) {
                    return module.getHelpText(trace);
                }
            }
        }

        if (trace != null) {
            return fAnalysisHelper.getHelpText(trace);
        }

        return fAnalysisHelper.getHelpText();
    }

    /**
     * Gets the icon file name for the analysis
     *
     * @return The analysis icon file name
     */
    public String getIconFile() {
        return fAnalysisHelper.getIcon();
    }

    /**
     * Gets the bundle this analysis is from
     *
     * @return The analysis bundle
     */
    public Bundle getBundle() {
        return fAnalysisHelper.getBundle();
    }

    /** Delete all outputs under this analysis element */
    private void deleteOutputs() {
        for (TmfAnalysisOutputElement output : getAvailableOutputs()) {
            removeChild(output);
        }
    }

    /**
     * Make sure the trace this analysis is associated to is the currently
     * selected one
     * @since 2.0
     */
    public void activateParentTrace() {
        TmfCommonProjectElement parentTrace = getParent().getParent();

        if (parentTrace instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) parentTrace;
            TmfOpenTraceHelper.openTraceFromElement(traceElement);
        }
    }

    // ------------------------------------------------------------------------
    // IPropertySource2
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public Object getEditableValue() {
        return null;
    }

    /**
     * Get the analysis properties of this analysisElement if the corresponding
     * analysis exists for the current trace
     *
     * @return a map with the names and values of the trace properties
     *         respectively as keys and values
     */
    private Map<String, String> getAnalysisProperties() {
        ITmfProjectModelElement parent = getParent();

        if (parent instanceof TmfCommonProjectElement) {
            ITmfTrace trace = ((TmfCommonProjectElement) parent).getTrace();
            if (trace == null) {
                return Collections.EMPTY_MAP;
            }
            IAnalysisModule module = trace.getAnalysisModule(fAnalysisHelper.getId());
            if (module instanceof ITmfPropertiesProvider) {
                return ((ITmfPropertiesProvider) module).getProperties();
            }
        }

        return Collections.EMPTY_MAP;
    }

    private Map<String, String> getAnalysisHelperProperties() {
        if (fAnalysisHelper instanceof ITmfPropertiesProvider) {
            ITmfPropertiesProvider analysisProperties = (ITmfPropertiesProvider) fAnalysisHelper;
            return analysisProperties.getProperties();
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * @since 2.0
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        Map<String, String> helperProperties = getAnalysisHelperProperties();
        Map<String, String> analysisProperties = getAnalysisProperties();
        if (!analysisProperties.isEmpty() || !helperProperties.isEmpty()) {
            List<IPropertyDescriptor> propertyDescriptorArray = new ArrayList<>(analysisProperties.size() + helperProperties.size());
            for (Map.Entry<String, String> varName : helperProperties.entrySet()) {
                ReadOnlyTextPropertyDescriptor descriptor = new ReadOnlyTextPropertyDescriptor(this.getName() + '_' + varName.getKey(), varName.getKey());
                descriptor.setCategory(HELPER_PROPERTIES_CATEGORY);
                propertyDescriptorArray.add(descriptor);
            }
            for (Map.Entry<String, String> varName : analysisProperties.entrySet()) {
                ReadOnlyTextPropertyDescriptor descriptor = new ReadOnlyTextPropertyDescriptor(this.getName() + '_' + varName.getKey(), varName.getKey());
                descriptor.setCategory(ANALYSIS_PROPERTIES_CATEGORY);
                propertyDescriptorArray.add(descriptor);
            }
            return propertyDescriptorArray.toArray(new IPropertyDescriptor[analysisProperties.size() + helperProperties.size()]);
        }
        return new IPropertyDescriptor[0];
    }

    /**
     * @since 2.0
     */
    @Override
    public Object getPropertyValue(Object id) {
        if (id == null) {
            return null;
        }
        Map<String, String> properties = getAnalysisHelperProperties();
        String key = (String) id;
        /* Remove name from key */
        key = key.substring(this.getName().length() + 1);
        if (properties.containsKey(key)) {
            String value = properties.get(key);
            return value;
        }

        properties = getAnalysisProperties();
        if (properties.containsKey(key)) {
            String value = properties.get(key);
            return value;
        }

        return null;
    }

    /**
     * @since 2.0
     */
    @Override
    public final void resetPropertyValue(Object id) {
    }

    /**
     * @since 2.0
     */
    @Override
    public final void setPropertyValue(Object id, Object value) {
    }

    /**
     * @since 2.0
     */
    @Override
    public final boolean isPropertyResettable(Object id) {
        return false;
    }

    /**
     * @since 2.0
     */
    @Override
    public final boolean isPropertySet(Object id) {
        return false;
    }

}
