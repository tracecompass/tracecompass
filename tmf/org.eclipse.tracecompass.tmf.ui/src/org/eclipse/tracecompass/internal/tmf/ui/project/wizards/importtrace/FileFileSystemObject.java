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

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;

/**
 * The "File" implementation of an IFileSystemObject
 */
class FileFileSystemObject implements IFileSystemObject {

    private File fFileSystemObject;

    FileFileSystemObject(File fileSystemObject) {
        fFileSystemObject = fileSystemObject;
    }

    @Override
    public String getName() {
        String name = fFileSystemObject.getName();
        if (name.length() == 0) {
            return fFileSystemObject.getPath();
        }
        return name;
    }

    @Override
    public String getAbsolutePath() {
        return fFileSystemObject.getAbsolutePath();
    }

    @Override
    public boolean exists() {
        return fFileSystemObject.exists();
    }

    @Override
    public String getSourceLocation() {
        IResource sourceResource;
        String sourceLocation = null;
        if (fFileSystemObject.isDirectory()) {
            sourceResource = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(Path.fromOSString(fFileSystemObject.getAbsolutePath()));
        } else {
            sourceResource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(fFileSystemObject.getAbsolutePath()));
        }
        if (sourceResource != null && sourceResource.exists()) {
            try {
                sourceLocation = sourceResource.getPersistentProperty(TmfCommonConstants.SOURCE_LOCATION);
            } catch (CoreException e) {
                // Something went wrong with the already existing resource.
                // This is not a problem, we'll assign a new location below.
            }
        }
        if (sourceLocation == null) {
            try {
                sourceLocation = URIUtil.toUnencodedString(fFileSystemObject.getCanonicalFile().toURI());
            } catch (IOException e) {
                // Something went wrong canonicalizing the file. We can still
                // use the URI but there might be extra ../ in it.
                sourceLocation = URIUtil.toUnencodedString(fFileSystemObject.toURI());
            }
        }
        return sourceLocation;
    }

    @Override
    public Object getRawFileSystemObject() {
        return fFileSystemObject;
    }

    @Override
    public boolean isDirectory() {
        return fFileSystemObject.isDirectory();
    }
}