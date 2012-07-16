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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponentChangedListener;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService;
import org.eclipse.swt.graphics.Image;

/**
 * <p>
 * Base implementation for trace control component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceControlComponent implements ITraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The name of the component
     */
    private String fName = ""; //$NON-NLS-1$
    /**
     * The image to be displayed for the component.
     */
    private Image fImage = null;
    /**
     * The tool tip to be displayed for the component.
     */
    private String fToolTip = null;
    /**
     * The parent component.
     */
    private ITraceControlComponent fParent = null;
    /**
     * The list if children components.
     */
    private final List<ITraceControlComponent> fChildren = new ArrayList<ITraceControlComponent>();
    /**
     * The list of listeners to be notified about changes.
     */
    private final ListenerList fListeners = new ListenerList();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param name - the name of the component.
     */
    public TraceControlComponent(String name) {
        this(name, null);
    }

    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent component.
     */
    public TraceControlComponent(String name, ITraceControlComponent parent) {
        fName = name;
        fParent = parent;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getName()
     */
    @Override
    public String getName() {
        return fName;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        fName = name;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getImage()
     */
    @Override
    public Image getImage() {
        return fImage;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#setImage(java.lang.String)
     */
    @Override
    public void setImage(String path) {
        fImage = Activator.getDefault().loadIcon(path);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#setImage(org.eclipse.swt.graphics.Image)
     */
    @Override
    public void setImage(Image image) {
        fImage = image;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getToolTip()
     */
    @Override
    public String getToolTip() {
        return fToolTip;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#setToolTip(java.lang.String)
     */
    @Override
    public void setToolTip(String toolTip) {
        fToolTip = toolTip;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getTargetNodeState()
     */
    @Override
    public TargetNodeState getTargetNodeState() {
        if (getParent() != null) {
            return getParent().getTargetNodeState();
        }
        return TargetNodeState.DISCONNECTED;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#setTargetNodeState(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent.TargetNodeState)
     */
    @Override
    public void setTargetNodeState(TargetNodeState state) {
        if (getParent() != null) {
            getParent().setTargetNodeState(state);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getParent()
     */
    @Override
    public ITraceControlComponent getParent() {
        return fParent;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#setParent(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void setParent(ITraceControlComponent parent) {
        fParent = parent;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getChildren()
     */
    @Override
    public ITraceControlComponent[] getChildren() {
        return fChildren.toArray(new ITraceControlComponent[fChildren.size()]);
    }

    /*
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponents#etChildren(java.util.List)
     */
    @Override
    public void setChildren(List<ITraceControlComponent> children) {
        for (Iterator<ITraceControlComponent> iterator = children.iterator(); iterator.hasNext();) {
            ITraceControlComponent traceControlComponent = iterator.next();
            fChildren.add(traceControlComponent);
            fireComponentChanged(this);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getChild(java.lang.String)
     */
    @Override
    public ITraceControlComponent getChild(String name) {
        ITraceControlComponent child = null;
        for (int i = 0; i < fChildren.size(); i++) {
            if (fChildren.get(i).getName().equals(name)) {
                child = fChildren.get(i);
                break;
            }
        }
        return child;
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getChildren(java.lang.Class)
     */
    @Override
    public List<ITraceControlComponent> getChildren(Class<? extends ITraceControlComponent> clazz) {
       List<ITraceControlComponent> list = new ArrayList<ITraceControlComponent>();

       for (Iterator<ITraceControlComponent> iterator = fChildren.iterator(); iterator.hasNext();) {
           ITraceControlComponent child = iterator.next();
           if (child.getClass() == clazz) {
               list.add(child);
           }
       }
       return list;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#getControlService()
     */
    @Override
    public ILttngControlService getControlService() {
        if (getParent() != null) {
            return getParent().getControlService();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#setControlService(org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService)
     */
    @Override
    public void setControlService(ILttngControlService service) {
        if (getParent() != null) {
            getParent().setControlService(service);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#dispose()
     */
    @Override
    public void dispose() {
        // default implementation
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#addChild(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void addChild(ITraceControlComponent component) {
        if (component != null) {
            fChildren.add(component);
        }
        fireComponentAdded(this, component);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#removeChild(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void removeChild(ITraceControlComponent component) {
        if (component != null) {
            fChildren.remove(component);
            component.dispose();
        }
        fireComponentRemoved(this, component);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#removeAllChildren()
     */
    @Override
    public void removeAllChildren() {
        for (Iterator<ITraceControlComponent> iterator = fChildren.iterator(); iterator.hasNext();) {
            ITraceControlComponent child = iterator.next();
            child.removeAllChildren();
        }
        fChildren.clear();
//        fireCompenentChanged(this);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#containsChild(java.lang.String)
     */
    @Override
    public boolean containsChild(String name) {
        boolean retValue = false;
        for (int i = 0; i < fChildren.size(); i++) {
            if (fChildren.get(i).getName().equals(name)) {
                retValue = true;
                break;
            }
        }
        return retValue;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return !fChildren.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#addComponentListener(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponentChangedListener)
     */
    @Override
    public void addComponentListener(ITraceControlComponentChangedListener listener) {
        if (fParent != null) {
            fParent.addComponentListener(listener);
        } else {
            fListeners.add(listener);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#removeComponentListener(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponentChangedListener)
     */
    @Override
    public void removeComponentListener(ITraceControlComponentChangedListener listener) {
        if (fParent != null) {
            fParent.removeComponentListener(listener);
        } else {
            fListeners.remove(listener);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#fireCompenentAdded(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void fireComponentAdded(ITraceControlComponent parent, ITraceControlComponent component) {
        if (component == null) {
            return;
        }

        if (fParent != null) {
            fParent.fireComponentAdded(parent, component);
        } else {
            Object[] listeners = fListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                ITraceControlComponentChangedListener listener = (ITraceControlComponentChangedListener) listeners[i];
                listener.componentAdded(parent, component);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#fireCompenentRemoved(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void fireComponentRemoved(ITraceControlComponent parent, ITraceControlComponent component) {
        if (component == null) {
            return;
        }

        if (fParent != null) {
            fParent.fireComponentRemoved(parent, component);
        } else {
            Object[] listeners = fListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                ITraceControlComponentChangedListener listener = (ITraceControlComponentChangedListener) listeners[i];
                listener.componentRemoved(parent, component);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent#fireCompenentChanged(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void fireComponentChanged(ITraceControlComponent component) {
        if (component == null) {
            return;
        }

        if (fParent != null) {
            fParent.fireComponentChanged(component);
        } else {
            Object[] listeners = fListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                ITraceControlComponentChangedListener listener = (ITraceControlComponentChangedListener) listeners[i];
                listener.componentChanged(component);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }
}
