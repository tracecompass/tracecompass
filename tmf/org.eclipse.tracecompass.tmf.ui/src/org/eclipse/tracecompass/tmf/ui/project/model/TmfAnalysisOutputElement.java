/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

/**
 * Class for project elements of type analysis output
 *
 * @author Geneviève Bastien
 */
public class TmfAnalysisOutputElement extends TmfProjectModelElement implements IPropertySource2 {

    private static final String OUTPUT_PROPERTIES_CATEGORY = Messages.TmfAnalysisOutputElement_Properties;

    private final IAnalysisOutput fOutput;

    /**
     * Constructor
     *
     * @param name
     *            Name of the view
     * @param resource
     *            Resource for the view
     * @param parent
     *            Parent analysis of the view
     * @param output
     *            The output object
     * @since 2.0
     */
    protected TmfAnalysisOutputElement(String name, IResource resource, TmfAnalysisElement parent, IAnalysisOutput output) {
        super(name, resource, parent);
        fOutput = output;
    }

    @Override
    public Image getIcon() {
        if (fOutput instanceof TmfAnalysisViewOutput) {
            IViewDescriptor descr = PlatformUI.getWorkbench().getViewRegistry().find(
                    ((TmfAnalysisViewOutput) fOutput).getViewId());
            if (descr != null) {
                Activator bundle = Activator.getDefault();
                String key = descr.getId();
                Image icon = bundle.getImageRegistry().get(key);
                if (icon == null) {
                    icon = descr.getImageDescriptor().createImage();
                    bundle.getImageRegistry().put(key, icon);
                }
                if (icon != null) {
                    return icon;
                }
            }
        }
        return TmfProjectModelIcons.DEFAULT_VIEW_ICON;
    }

    /**
     * Outputs the analysis
     */
    public void outputAnalysis() {
        ITmfProjectModelElement parent = getParent();
        if (parent instanceof TmfAnalysisElement) {
            ((TmfAnalysisElement) parent).activateParentTrace();
            fOutput.requestOutput();
        }
    }

    @Override
    protected void refreshChildren() {
        /* Nothing to do */
    }

    /**
     * Get the {@link IAnalysisOutput} element.
     *
     * @return Get the {@link IAnalysisOutput} element
     */
    IAnalysisOutput getOutput() {
        return fOutput;
    }

    // ------------------------------------------------------------------------
    // IPropertySource2
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    private Map<String, String> getOutpuProperties() {
        Map<String, String> properties = new HashMap<>();

        IAnalysisOutput output = fOutput;
        if (output instanceof TmfAnalysisViewOutput) {
            properties.put(Messages.TmfAnalysisOutputElement_ViewIdProperty, ((TmfAnalysisViewOutput) output).getViewId());
        }
        return properties;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        Map<String, String> outputProperties = getOutpuProperties();
        if (!outputProperties.isEmpty()) {
            List<IPropertyDescriptor> propertyDescriptorArray = new ArrayList<>(outputProperties.size());
            for (Map.Entry<String, String> varName : outputProperties.entrySet()) {
                ReadOnlyTextPropertyDescriptor descriptor = new ReadOnlyTextPropertyDescriptor(this.getName() + '_' + varName.getKey(), varName.getKey());
                descriptor.setCategory(OUTPUT_PROPERTIES_CATEGORY);
                propertyDescriptorArray.add(descriptor);
            }
            return propertyDescriptorArray.toArray(new IPropertyDescriptor[outputProperties.size()]);
        }
        return new IPropertyDescriptor[0];
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (id == null) {
            return null;
        }
        Map<String, String> properties = getOutpuProperties();
        String key = (String) id;
        /* Remove name from key */
        key = key.substring(this.getName().length() + 1);
        if (properties.containsKey(key)) {
            String value = properties.get(key);
            return value;
        }

        return null;
    }

    @Override
    public final void resetPropertyValue(Object id) {
        // Do nothing
    }

    @Override
    public final void setPropertyValue(Object id, Object value) {
        // Do nothing
    }

    @Override
    public final boolean isPropertyResettable(Object id) {
        return false;
    }

    @Override
    public final boolean isPropertySet(Object id) {
        return false;
    }

}
