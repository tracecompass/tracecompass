/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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
    String getName();
    /**
     * Sets the name of the component to the given value.
     * @param name - name to set
     */
    void setName(String name);

    /**
     * @return the image representing the component.
     */
    Image getImage();
    /**
     * Sets the image path of the component.
     * @param path - path to the image location
     */
    void setImage(String path);
    /**
     * Sets the image the component.
     * @param image - image to the image location
     */
    void setImage(Image image);

    /**
     * @return tool tip with information about the component.
     */
    String getToolTip();
    /**
     * Sets the tool tip with information about the component.
     * @param toolTip - the tool tip to set.
     */
    void setToolTip(String toolTip);

    /**
     * @return the node's connection state
     */
    TargetNodeState getTargetNodeState();
    /**
     * Sets the node's connection state.
     * @param state - the state to set
     */
    void setTargetNodeState(TargetNodeState state);

    /**
     * @return returns the parent component.
     */
    ITraceControlComponent getParent();
    /**
     * Sets the parent component.
     * @param parent - the parent to set.
     */
    void setParent(ITraceControlComponent parent);

    /**
     * @return the children components
     */
    ITraceControlComponent[] getChildren();
    /**
     * Sets the children components.
     * @param children - the children to set.
     */
    void setChildren(List<ITraceControlComponent> children);
    /**
     * Returns the child component with given name.
     * @param name - name of child to find.
     * @return child component or null.
     */
    ITraceControlComponent getChild(String name);
    /**
     * Gets children for given class type.
     * @param clazz - a class type to get
     * @return list of trace control components matching given class type.
     */
    List<ITraceControlComponent> getChildren(Class<? extends ITraceControlComponent> clazz);

    /**
     * @return the LTTng control service implementation.
     */
    ILttngControlService getControlService();

    /**
     * Sets the LTTng control service implementation.
     * @param service - the service to set.
     */
    void setControlService(ILttngControlService service);

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Dispose any resource.
     */
    void dispose();

    /**
     * Adds a child component.
     * @param component - child to add.
     */
    void addChild(ITraceControlComponent component);

    /**
     * Removes the given child component.
     * @param component - the child to remove.
     */
    void removeChild(ITraceControlComponent component);

    /**
     * Removes all children.
     */
    void removeAllChildren();

    /**
     * Checks if child with given name exists.
     * @param name - child name to search for.
     * @return - true if exists else false.
     */
    boolean containsChild(String name);

    /**
     * Checks for children.
     * @return true if one or more children exist else false
     */
    boolean hasChildren();

    /**
     * Adds a component listener for notification of component changes.
     * @param listener - listener interface implementation to add.
     */
    void addComponentListener(ITraceControlComponentChangedListener listener);

    /**
     * Removes a component listener for notification of component changes.
     * @param listener - listener interface implementation to remove.
     */
    void removeComponentListener(ITraceControlComponentChangedListener listener);

    /**
     * Notifies listeners about the addition of a child.
     * @param parent - the parent where the child was added.
     * @param component - the child that was added.
     */
    void fireComponentAdded(ITraceControlComponent parent, ITraceControlComponent component);

    /**
     * Notifies listeners about the removal of a child.
     * @param parent - the parent where the child was removed.
     * @param component - the child that was removed.
     */
    void fireComponentRemoved(ITraceControlComponent parent, ITraceControlComponent component);

    /**
     * Notifies listeners about the change of a component.
     * @param component - the component that was changed.
     */
    void fireComponentChanged(ITraceControlComponent component);
}
