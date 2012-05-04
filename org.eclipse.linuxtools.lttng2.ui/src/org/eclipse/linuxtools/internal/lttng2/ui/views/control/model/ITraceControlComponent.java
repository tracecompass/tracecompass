/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService;
import org.eclipse.swt.graphics.Image;

/**
 * <p>
 * Interface for trace control components that can be displayed in the 
 * trace control tree viewer. 
 * </p>
 * 
 * @author Bernd Hufmann
 */
public interface ITraceControlComponent extends IAdaptable {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the name of the component
     */
    public String getName();
    /**
     * Sets the name of the component to the given value.
     * @param name - name to set
     */
    public void setName(String name);

    /**
     * @return the image representing the component.
     */
    public Image getImage();
    /**
     * Sets the image path of the component.
     * @param path - path to the image location
     */
    public void setImage(String path);
    /**
     * Sets the image the component.
     * @param image - image to the image location
     */
    public void setImage(Image image);

    /**
     * @return tool tip with information about the component.
     */
    public String getToolTip();
    /**
     * Sets the tool tip with information about the component.
     * @param toolTip - the tool tip to set.
     */
    public void setToolTip(String toolTip);
    
    /**
     * @return the node's connection state
     */
    public TargetNodeState getTargetNodeState();
    /**
     * Sets the node's connection state. 
     * @param state - the state to set
     */
    public void setTargetNodeState(TargetNodeState state);
    
    /**
     * @return returns the parent component.
     */
    public ITraceControlComponent getParent();
    /**
     * Sets the parent component.
     * @param parent - the parent to set.
     */
    public void setParent(ITraceControlComponent parent);

    /**
     * @return the children components
     */
    public ITraceControlComponent[] getChildren();
    /**
     * Sets the children components.
     * @param children - the children to set.
     */
    public void setChildren(List<ITraceControlComponent> children);
    /**
     * Returns the child component with given name.
     * @param name - name of child to find.
     * @return child component or null.
     */
    public ITraceControlComponent getChild(String name);
    /**
     * Gets children for given class type.
     * @param clazz - a class type to get
     * @return list of trace control components matching given class type. 
     */
    public List<ITraceControlComponent> getChildren(Class<? extends ITraceControlComponent> clazz);

    /**
     * @return the LTTng control service implementation.
     */
    public ILttngControlService getControlService();

    /**
     * Sets the LTTng control service implementation.
     * @param service - the service to set.
     */
    public void setControlService(ILttngControlService service); 

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Dispose any resource.
     */
    public void dispose();
    
    /**
     * Adds a child component.
     * @param component - child to add.
     */
    public void addChild(ITraceControlComponent component);
    
    /**
     * Adds several components.
     * @param components - array of components to add.
     */
//    public void addChildren(ITraceControlComponent[] components);
    
    /**
     * Removes the given child component. 
     * @param component - the child to remove.
     */
    public void removeChild(ITraceControlComponent component);
    
    /**
     * Removes all children.
     */
    public void removeAllChildren();
    
    /**
     * Checks if child with given name exists.
     * @param name - child name to search for.
     * @return - true if exists else false.
     */
    public boolean containsChild(String name);
    
    /**
     * Checks for children. 
     * @return true if one or more children exist else false
     */
    public boolean hasChildren();

    /**
     * Adds a component listener for notification of component changes.
     * @param listener - listener interface implementation to add.
     */
    public void addComponentListener(ITraceControlComponentChangedListener listener);
    
    /**
     * Removes a component listener for notification of component changes.
     * @param listener - listener interface implementation to remove.
     */
    public void removeComponentListener(ITraceControlComponentChangedListener listener);
    
    /**
     * Notifies listeners about the addition of a child.
     * @param parent - the parent where the child was added.
     * @param component - the child that was added.
     */
    public void fireComponentAdded(ITraceControlComponent parent, ITraceControlComponent component);
    
    /**
     * Notifies listeners about the removal of a child.
     * @param parent - the parent where the child was removed.
     * @param component - the child that was removed.
     */
    public void fireComponentRemoved(ITraceControlComponent parent, ITraceControlComponent component);
    
    /**
     * Notifies listeners about the change of a component.
     * @param component - the component that was changed.
     */
    public void fireComponentChanged(ITraceControlComponent component);
}
