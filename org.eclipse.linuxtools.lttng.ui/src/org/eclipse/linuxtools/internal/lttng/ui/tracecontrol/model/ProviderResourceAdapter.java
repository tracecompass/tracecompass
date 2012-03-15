/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.ProviderResource;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <b><u>ProviderResourceAdapter</u></b>
 * <p>
 * This is the adapter which enables us to work with our remote provider resources.
 * </p>
 */
public class ProviderResourceAdapter extends AbstractSystemViewAdapter implements ISystemRemoteElementAdapter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default Constructor.
     */
    public ProviderResourceAdapter() {
        super();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#addActions(org.eclipse.rse.ui.SystemMenuManager, org.eclipse.jface.viewers.IStructuredSelection, org.eclipse.swt.widgets.Shell, java.lang.String)
     */
    @Override
    public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell parent, String menuGroup) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object element) {
        return Activator.getDefault().getImageDescriptor(Activator.ICON_ID_PROVIDER);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemViewElementAdapter#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        return ((ProviderResource) element).getName();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
     */
    @Override
    public String getAbsoluteName(Object object) {
        ProviderResource provider = (ProviderResource) object;
        return Messages.Lttng_Resource_Provider + "_" + provider.getName(); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getType(java.lang.Object)
     */
    @Override
    public String getType(Object element) {
        return Messages.Lttng_Resource_Provider;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        return null; // not really used, which is good because it is ambiguous
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getChildren(org.eclipse.core.runtime.IAdaptable, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Object[] getChildren(IAdaptable element, IProgressMonitor monitor) {
        return ((ProviderResource) element).getTargets();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyDescriptors()
     */
    @Override
    protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyValue(java.lang.Object)
     */
    @Override
    protected Object internalGetPropertyValue(Object key) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#canRename(java.lang.Object)
     * 
     * Intercept of parent method to indicate these objects can be renamed using the RSE-supplied rename action.
     */
    @Override
    public boolean canRename(Object element) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#showDelete(java.lang.Object)
     */
    @Override
    public boolean showDelete(Object element) {
        return false;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#showRefresh(java.lang.Object)
     */
    @Override
    public boolean showRefresh(Object element) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#showRename(java.lang.Object)
     */
    @Override
    public boolean showRename(Object element) {
        return false;
    }

    // --------------------------------------
    // ISystemRemoteElementAdapter methods...
    // --------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getAbsoluteParentName(java.lang.Object)
     */
    @Override
    public String getAbsoluteParentName(Object element) {
        return Messages.Lttng_Resource_Root; // not really applicable as we have no unique hierarchy
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.ISystemRemoteObjectMatchProvider#getSubSystemConfigurationId(java.lang.Object)
     */
    @Override
    public String getSubSystemConfigurationId(Object element) {
        return "org.eclipse.linuxtools.lttng.ui.tracecontrol.subsystems.TraceSubSystemConfiguration"; // as declared in extension in plugin.xml  //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteTypeCategory(java.lang.Object)
     */
    @Override
    public String getRemoteTypeCategory(Object element) {
        return TraceControlConstants.Rse_Provider_Resource_Remote_Type_Category; // Course grained. Same for all our remote resources.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteType(java.lang.Object)
     */
    @Override
    public String getRemoteType(Object element) {
        return TraceControlConstants.Rse_Provider_Resource_Remote_Type; // Fine grained. Unique to this resource type.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubType(java.lang.Object)
     */
    @Override
    public String getRemoteSubType(Object element) {
        return null; // Very fine grained. We don't use it.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#refreshRemoteObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean refreshRemoteObject(Object oldElement, Object newElement) {
        ProviderResource oldProvider = (ProviderResource) oldElement;
        ProviderResource newProvider = (ProviderResource) newElement;
        newProvider.setName(oldProvider.getName());
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParentNamesInUse(org.eclipse.swt.widgets.Shell, java.lang.Object)
     */
    @Override
    public String[] getRemoteParentNamesInUse(Object element, IProgressMonitor monitor) throws Exception {
        TraceSubSystem ourSS = (TraceSubSystem) getSubSystem(element);
        ProviderResource[] allProviders = ourSS.getAllProviders();
        String[] allNames = new String[allProviders.length];
        for (int idx = 0; idx < allProviders.length; idx++) {
            allNames[idx] = allProviders[idx].getName();
        }
        return allNames; // Return list of all team names
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParent(java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Object getRemoteParent(Object element, IProgressMonitor monitor) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#hasChildren(org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public boolean hasChildren(IAdaptable element) {
        return ((((ProviderResource) element).getTargets() != null) && (((ProviderResource) element).getTargets().length > 0));
    }

}
