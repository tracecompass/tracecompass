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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;

import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * An import provider that makes use of the IFileSystemObject abstraction
 * instead of using plain file system objects (File, TarEntry, ZipEntry, etc)
 */
public class FileSystemObjectImportStructureProvider implements IImportStructureProvider {

    private IImportStructureProvider fImportProvider;
    private String fArchivePath;

    /**
     * Constructor
     *
     * @param importStructureProvider
     *            the {@link IImportStructureProvider}
     * @param archivePath
     *            the path of the archive file
     */
    public FileSystemObjectImportStructureProvider(IImportStructureProvider importStructureProvider, String archivePath) {
        fImportProvider = importStructureProvider;
        fArchivePath = archivePath;
    }

    /**
     * This orders by files first then the folders. Then by lexical order.
     */
    private final class FileObjectPathComparator implements Comparator<IFileSystemObject> {
        @Override
        public int compare(IFileSystemObject o1, IFileSystemObject o2) {
            if (o1.isDirectory() != o2.isDirectory()) {
                if (o1.isDirectory()) {
                    return 1;
                }
                return -1;
            }

            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    @Override
    public List<IFileSystemObject> getChildren(Object element) {
        @SuppressWarnings("rawtypes")
        List children = fImportProvider.getChildren(((IFileSystemObject) element).getRawFileSystemObject());
        List<IFileSystemObject> adapted = new ArrayList<>(children.size());
        for (Object o : children) {
            adapted.add(getIFileSystemObject(o));
        }

        adapted.sort(new FileObjectPathComparator());
        return adapted;
    }

    /**
     * Get the IFileSystemObject corresponding to the specified raw object
     *
     * @param o
     *            the raw object
     * @return the corresponding IFileSystemObject
     */
    public IFileSystemObject getIFileSystemObject(Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof File) {
            return new FileFileSystemObject((File) o);
        } else if (o instanceof TarEntry) {
            return new TarFileSystemObject((TarEntry) o, fArchivePath);
        } else if (o instanceof ZipEntry) {
            return new ZipFileSystemObject((ZipEntry) o, fArchivePath);
        } else if (o instanceof GzipEntry) {
            return new GzipFileSystemObject((GzipEntry) o, fArchivePath);
        }

        throw new IllegalArgumentException("Object type not handled"); //$NON-NLS-1$
    }

    @Override
    public InputStream getContents(Object fileSystemObject) {
        return fImportProvider.getContents(((IFileSystemObject) fileSystemObject).getRawFileSystemObject());
    }

    @Override
    public String getFullPath(Object element) {
        return fImportProvider.getFullPath(((IFileSystemObject) element).getRawFileSystemObject());
    }

    @Override
    public String getLabel(Object element) {
        return fImportProvider.getLabel(((IFileSystemObject) element).getRawFileSystemObject());
    }

    @Override
    public boolean isFolder(Object element) {
        return fImportProvider.isFolder(((IFileSystemObject) element).getRawFileSystemObject());
    }

    /**
     * Disposes of the resources associated with the provider.
     */
    public void dispose() {
    }
}