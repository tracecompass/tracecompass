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
package org.eclipse.linuxtools.lttng.ui.tracecontrol.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <b><u>TargetResourceAdapter</u></b>
 * <p>
 * This is the adapter which enables us to work with our remote target resources.
 * </p>
 */
public class TargetResourceAdapter extends AbstractSystemViewAdapter implements ISystemRemoteElementAdapter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public TargetResourceAdapter() {
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
    public ImageDescriptor getImageDescriptor(Object object) {
        return LTTngUiPlugin.getDefault().getImageDescriptor(LTTngUiPlugin.ICON_ID_TARGET);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemViewElementAdapter#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        return ((TargetResource) element).getName();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
     */
    @Override
    public String getAbsoluteName(Object object) {
        TargetResource tar = (TargetResource) object;
        return tar.getName();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getType(java.lang.Object)
     */
    @Override
    public String getType(Object element) {
        return Messages.Lttng_Resource_Target;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object object) {
        return ((TargetResource) object).getParent(); // not really used, which is good because it is ambiguous
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getChildren(org.eclipse.core.runtime.IAdaptable, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Object[] getChildren(IAdaptable element, IProgressMonitor monitor) {
        return ((TargetResource) element).getTraces();
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
        return "org.eclipse.linuxtools.lttng.ui.tracecontrol.subsystems.factory"; // as declared in extension in plugin.xml //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.ISystemRemoteObjectMatchProvider#getRemoteTypeCategory(java.lang.Object)
     */
    @Override
    public String getRemoteTypeCategory(Object element) {
        return TraceControlConstants.Rse_Target_Resource_Remote_Type_Category; // Course grained. Same for all our remote resources.
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.ISystemRemoteObjectMatchProvider#getRemoteType(java.lang.Object)
     */
    @Override
    public String getRemoteType(Object element) {
        return TraceControlConstants.Rse_Target_Resource_Remote_Type; // Fine grained. Unique to this resource type.
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.ISystemRemoteObjectMatchProvider#getRemoteSubType(java.lang.Object)
     */
    @Override
    public String getRemoteSubType(Object element) {
        return null; // Very fine grained. We don't use it.
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#refreshRemoteObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean refreshRemoteObject(Object oldElement, Object newElement) {
        TargetResource oldDevr = (TargetResource) oldElement;
        TargetResource newDevr = (TargetResource) newElement;
        newDevr.setName(oldDevr.getName());
        return true;
    }

    public boolean supportsUserDefinedActions(Object object) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#hasChildren(org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public boolean hasChildren(IAdaptable element) {
        return ((((TargetResource) element).getTraces() != null) && (((TargetResource) element).getTraces().length > 0));
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
     * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParentNamesInUse(java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public String[] getRemoteParentNamesInUse(Object element, IProgressMonitor monitor) throws Exception {
        return null;
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
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#saveExpansionState(java.lang.Object)
     */
    @Override
    public boolean saveExpansionState(Object element) {
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#testAttribute(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public boolean testAttribute(Object target, String name, String value) {
        
        if (target instanceof TargetResource) {
            if ((name.compareTo("canCreateNewTrace") == 0) && (((TargetResource)target).getCanCreateNewTrace().compareTo(value) == 0)) { //$NON-NLS-1$ 
                return true;
            }
        }
        
        return super.testAttribute(target, name, value);
    }
}
