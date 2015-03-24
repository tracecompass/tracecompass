/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;

/**
 * An RemoteImportConnectionNodeElement representing a connection node.
 *
 * @author Marc-Andre Laperle
 */
public class RemoteImportConnectionNodeElement extends TracePackageElement {

    private static final String IMAGE_PATH = "icons/obj/connection_node.gif"; //$NON-NLS-1$

    private String fName;
    private String fURI;

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

}
