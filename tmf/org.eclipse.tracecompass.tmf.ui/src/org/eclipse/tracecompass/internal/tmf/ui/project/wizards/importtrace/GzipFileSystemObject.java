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

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;

/**
 * The "GZIP" implementation of an IFileSystemObject. For a GZIP file that is
 * not in a tar.
 */
class GzipFileSystemObject implements IFileSystemObject {

    private GzipEntry fFileSystemObject;
    private String fArchivePath;

    GzipFileSystemObject(GzipEntry fileSystemObject, String archivePath) {
        fFileSystemObject = fileSystemObject;
        fArchivePath = archivePath;
    }

    @Override
    public String getName() {
        return new Path(fFileSystemObject.getName()).lastSegment();
    }

    @Override
    public String getAbsolutePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public String getSourceLocation() {
        File file = new File(fArchivePath);
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            // Will still work but might have extra ../ in the path
        }
        URI uri = file.toURI();
        IPath entryPath = new Path(fFileSystemObject.getName());

        URI jarURI = entryPath.isRoot() ? URIUtil.toJarURI(uri, Path.EMPTY) : URIUtil.toJarURI(uri, entryPath);
        return URIUtil.toUnencodedString(jarURI);
    }

    @Override
    public Object getRawFileSystemObject() {
        return fFileSystemObject;
    }

    @Override
    public boolean isDirectory() {
        return fFileSystemObject.getFileType() == GzipEntry.DIRECTORY;
    }
}