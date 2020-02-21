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
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;

/**
 * An RemoteImportProfileElement representing a profile for importing traces remotely.
 *
 * @author Marc-Andre Laperle
 */
public class RemoteImportProfileElement extends TracePackageElement {

    private static final String IMAGE_PATH = "icons/obj/profile.gif"; //$NON-NLS-1$
    private String fProfileName;

    /**
     * Constructs an instance of RemoteImportProfileElement
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param profileName
     *            the profile name
     */
    public RemoteImportProfileElement(TracePackageElement parent,
            String profileName) {
        super(parent);
        fProfileName = profileName;
    }

    @Override
    public String getText() {
        return fProfileName;
    }

    @Override
    public Image getImage() {
        return Activator.getDefault().getImageFromImageRegistry(IMAGE_PATH);
    }

    /**
     * Get the name of the profile.
     *
     * @return the name of the profile
     */
    public String getProfileName() {
        return fProfileName;
    }

    /**
     * Set the name of the profile.
     *
     * @param profileName the name of the profile
     */
    public void setProfileName(String profileName) {
        fProfileName = profileName;
    }

    /**
     * Returns the list of remote connection elements
     *
     * @return a list of remote connection elements
     */
    public List<RemoteImportConnectionNodeElement> getConnectionNodeElements() {
        List<RemoteImportConnectionNodeElement> remoteHosts = new ArrayList<>();
        for (TracePackageElement element : getChildren()) {
            if (element instanceof RemoteImportConnectionNodeElement) {
                // only one node per profile is supported
                remoteHosts.add((RemoteImportConnectionNodeElement) element);
            }
        }
        return remoteHosts;
    }
}
