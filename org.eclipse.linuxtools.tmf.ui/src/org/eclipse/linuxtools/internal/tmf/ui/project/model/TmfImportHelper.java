/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.model;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;

/**
 * Import helper used to import traces
 *
 * It has two purposes: - import files and directories into projects - set the
 * resource types
 *
 * @author Matthew Khouzam
 */
public class TmfImportHelper {

    /**
     * Create a link and replace what was already there.
     *
     * @param parentFolder
     *            the resource to import to, does not contain the element name
     * @param location
     *            where the resource (file/directory) is located
     * @param targetName
     *            the name to display
     * @return the resource created. Should not be null
     * @throws CoreException
     *             an exception made by createLink.
     */
    public static IResource createLink(IFolder parentFolder, IPath location, String targetName) throws CoreException {
        File source = new File(location.toString());
        IResource res = null;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (source.isDirectory()) {
            IFolder folder = parentFolder.getFolder(targetName);
            IStatus result = workspace.validateLinkLocation(folder, location);
            if (result.isOK()) {
                folder.createLink(location, IResource.REPLACE, new NullProgressMonitor());
            } else {
                Activator.getDefault().logError(result.getMessage());
            }
        } else {
            IFile file = parentFolder.getFile(targetName);
            IStatus result = workspace.validateLinkLocation(file, location);
            if (result.isOK()) {
                file.createLink(location, IResource.REPLACE,
                        new NullProgressMonitor());
            } else {
                Activator.getDefault().logError(result.getMessage());
            }
        }
        res = parentFolder.findMember(targetName);
        return res;
    }
}
