/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *     Bernd Hufmann - Add connection handling
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.ui.messages.RemoteMessages;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.tmf.remote.core.proxy.RemoteSystemProxy;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;

/**
 * An RemoteImportConnectionNodeElement representing a connection node.
 *
 * @author Marc-Andre Laperle
 */
public class RemoteImportConnectionNodeElement extends TracePackageElement {

    private static final String IMAGE_PATH = "icons/obj/connection_node.gif"; //$NON-NLS-1$

    private String fName;
    private String fURI;
    private RemoteSystemProxy fRemoteProxy;

    /**
     * Constructs an instance of RemoteImportConnectionNodeElement.
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param name
     *            the node name
     * @param uri
     *            the URI in string form to connect to the node
     */
    public RemoteImportConnectionNodeElement(TracePackageElement parent,
            String name, String uri) {
        super(parent);
        fName = name;
        fURI = uri;
    }

    @Override
    public String getText() {
        return fName + " (" + fURI + ")"; //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    public Image getImage() {
        return Activator.getDefault().getImageFromImageRegistry(IMAGE_PATH);
    }

    /**
     * Get the name of the connection.
     *
     * @return the name of the connection
     */
    public String getName() {
        return fName;
    }

    /**
     * Set the name of the connection.
     *
     * @param name the name of the connection
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * Get the URI of the connection.
     *
     * @return the URI of the connection
     */
    public String getURI() {
        return fURI;
    }

    /**
     * Set the URI of the connection.
     *
     * @param uri the URI of the connection
     */
    public void setURI(String uri) {
        fURI = uri;
    }

    /**
     * Connects to the remote host
     *
     * @param monitor
     *                a progress monitor
     *
     * @return status of the executions
     */
    public IStatus connect(@NonNull IProgressMonitor monitor) {
        // Use local variables to avoid null annotation warning
        RemoteSystemProxy proxy = fRemoteProxy;
        String name = fName;
        if (name == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, RemoteMessages.RemoteImportConnectionNodeElement_NodeNameNullError);
        }
        if (proxy == null) {
            try {
                URI hostUri = null;
                hostUri = URIUtil.fromString(fURI);
                if (hostUri == null) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, RemoteMessages.RemoteImportConnectionNodeElement_UriNullError);
                }
                proxy = new RemoteSystemProxy(TmfRemoteConnectionFactory.createConnection(hostUri, name));
                fRemoteProxy = proxy;
            } catch (URISyntaxException e) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(RemoteMessages.RemoteImportConnectionNodeElement_InvalidUriString, fURI), e);
            } catch (RemoteConnectionException e) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(RemoteMessages.RemoteImportConnectionNodeElement_ConnectionFailure, fURI), e);
            }
        }
        try {
            proxy.connect(monitor);
            return Status.OK_STATUS;
        } catch (ExecutionException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(RemoteMessages.RemoteImportConnectionNodeElement_ConnectionFailure, fURI), e);
        }
    }

    /**
     * Disconnects the remote host
     */
    public void disconnect() {
        if (fRemoteProxy != null) {
            fRemoteProxy.dispose();
        }
    }

    /**
     * Returns the remote system proxy implementation
     *
     * @return the remote system proxy or null
     */
    public RemoteSystemProxy getRemoteSystemProxy() {
        return fRemoteProxy;
    }
}
