/**********************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick-Jeffrey Pollo Guilbert, William Enright,
 *      William Tri-Khiem Truong - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;

/**
 * TODO document
 *
 */
public final class ConnectionContentProvider implements ITreeContentProvider {
    private static final Object[] NO_CHILDREN = {};

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<Object> children = new ArrayList<>();
        if (inputElement instanceof IRemoteConnectionType) {
            IRemoteConnectionType irc = (IRemoteConnectionType) inputElement;
            children.addAll(irc.getRemoteServicesManager().getAllRemoteConnections());
        }
        return children.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IRemoteConnectionType) {
            return getConnections((IRemoteConnectionType) parentElement);
        }
        return NO_CHILDREN;
    }

    static IRemoteConnection[] getConnections(IRemoteConnectionType parentElement) {
        List<IRemoteConnection> connectionList = parentElement.getConnections();
        IRemoteConnection[] result = connectionList.toArray(new IRemoteConnection[connectionList.size()]);
        Arrays.sort(result);
        return result;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IRemoteConnection) {
            return ((IRemoteConnection) element).getConnectionType();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

}