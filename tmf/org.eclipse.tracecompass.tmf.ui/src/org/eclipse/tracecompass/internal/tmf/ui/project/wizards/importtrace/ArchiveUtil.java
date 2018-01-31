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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceCoreUtils;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;

/**
 * Various utilities for dealing with archives in the context of importing
 * traces.
 */
public class ArchiveUtil {

    /**
     * Returns whether or not the source file is an archive file (Zip, tar,
     * tar.gz, gz).
     *
     * @param sourceFile
     *            the source file
     * @return whether or not the source file is an archive file
     */
    public static boolean isArchiveFile(File sourceFile) {
        String absolutePath = sourceFile.getAbsolutePath();
        return isTarFile(absolutePath) || isZipFile(absolutePath) || isGzipFile(absolutePath);
    }

    private static boolean isZipFile(String fileName) {
        try (ZipFile specifiedZipSourceFile = getSpecifiedZipSourceFile(fileName)) {
            if (specifiedZipSourceFile != null) {
                return true;
            }
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    private static boolean isTarFile(String fileName) {
        TarFile specifiedTarSourceFile = getSpecifiedTarSourceFile(fileName);
        if (specifiedTarSourceFile != null) {
            try {
                specifiedTarSourceFile.close();
                return true;
            } catch (IOException e) {
                // ignore
            }
        }
        return false;
    }

    private static boolean isGzipFile(String fileName) {
        if (!fileName.isEmpty()) {
            try (GzipFile specifiedTarSourceFile = new GzipFile(fileName);) {
                return true;
            } catch (IOException e) {
            }
        }
        return false;
    }

    private static ZipFile getSpecifiedZipSourceFile(String fileName) {
        if (fileName.length() == 0) {
            return null;
        }

        File file = new File(fileName);
        if (file.isDirectory()) {
            return null;
        }

        try {
            return new ZipFile(file);
        } catch (IOException e) {
            // ignore
        }

        return null;
    }

    private static TarFile getSpecifiedTarSourceFile(String fileName) {
        if (fileName.length() == 0) {
            return null;
        }

        // FIXME: Work around Bug 463633. Remove this block once we move to Eclipse 4.5.
        File tarCandidate = new File(fileName);
        if (tarCandidate.length() < 512) {
            return null;
        }

        try {
            return new TarFile(tarCandidate);
        } catch (IOException e) {
            // ignore
        }

        return null;
    }

    static boolean ensureZipSourceIsValid(String archivePath) {
        ZipFile specifiedFile = getSpecifiedZipSourceFile(archivePath);
        if (specifiedFile == null) {
            return false;
        }
        return closeZipFile(specifiedFile);
    }

    static boolean closeZipFile(ZipFile file) {
        try {
            file.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    static boolean ensureTarSourceIsValid(String archivePath) {
        TarFile specifiedFile = getSpecifiedTarSourceFile(archivePath);
        if (specifiedFile == null) {
            return false;
        }
        return closeTarFile(specifiedFile);
    }

    static boolean ensureGzipSourceIsValid(String archivePath) {
        return isGzipFile(archivePath);
    }

    static boolean closeTarFile(TarFile file) {
        try {
            file.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Get the archive size
     *
     * @param archivePath
     *            Path of the archive
     * @return Size of the archive in byte or -1 if there is an error
     */
    public static long getArchiveSize(String archivePath) {
        TarFile tarFile = getSpecifiedTarSourceFile(archivePath);
        long archiveSize = 0;
        if (tarFile != null) {
            ArrayList<TarArchiveEntry> entries = Collections.list(tarFile.entries());
            for (TarArchiveEntry tarArchiveEntry : entries) {
                archiveSize += tarArchiveEntry.getSize();
            }
            closeTarFile(tarFile);
            return archiveSize;
        }

        ZipFile zipFile = getSpecifiedZipSourceFile(archivePath);
        if (zipFile != null) {
            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
                archiveSize += Objects.requireNonNull(e.nextElement()).getSize();
            }
            closeZipFile(zipFile);
            return archiveSize;
        }

        return -1;
    }

    /**
     * Get the root file system object and it's associated import provider for
     * the specified source file. A shell is used to display messages in case of
     * errors.
     *
     * @param sourceFile
     *            the source file
     * @param shell
     *            the parent shell to use to display error messages
     * @return the root file system object and it's associated import provider
     */
    @SuppressWarnings("resource")
    public static Pair<IFileSystemObject, FileSystemObjectImportStructureProvider> getRootObjectAndProvider(File sourceFile, Shell shell) {
        if (sourceFile == null) {
            return null;
        }

        IFileSystemObject rootElement = null;
        FileSystemObjectImportStructureProvider importStructureProvider = null;

        // Import from directory
        if (!isArchiveFile(sourceFile)) {
            importStructureProvider = new FileSystemObjectImportStructureProvider(FileSystemStructureProvider.INSTANCE, null);
            rootElement = importStructureProvider.getIFileSystemObject(sourceFile);
        } else {
            // Import from archive
            FileSystemObjectLeveledImportStructureProvider leveledImportStructureProvider = null;
            String archivePath = sourceFile.getAbsolutePath();
            if (isTarFile(archivePath)) {
                if (ensureTarSourceIsValid(archivePath)) {
                    // We close the file when we dispose the import provider,
                    // see disposeSelectionGroupRoot
                    TarFile tarFile = getSpecifiedTarSourceFile(archivePath);
                    leveledImportStructureProvider = new FileSystemObjectLeveledImportStructureProvider(new TarLeveledStructureProvider(tarFile), archivePath);
                }
            } else if (ensureZipSourceIsValid(archivePath)) {
                // We close the file when we dispose the import provider, see
                // disposeSelectionGroupRoot
                ZipFile zipFile = getSpecifiedZipSourceFile(archivePath);
                leveledImportStructureProvider = new FileSystemObjectLeveledImportStructureProvider(new ZipLeveledStructureProvider(zipFile), archivePath);
            } else if (ensureGzipSourceIsValid(archivePath)) {
                // We close the file when we dispose the import provider, see
                // disposeSelectionGroupRoot
                GzipFile zipFile = null;
                try {
                    zipFile = new GzipFile(archivePath);
                    leveledImportStructureProvider = new FileSystemObjectLeveledImportStructureProvider(new GzipLeveledStructureProvider(zipFile), archivePath);
                } catch (IOException e) {
                    // do nothing
                }
            }
            if (leveledImportStructureProvider == null) {
                return null;
            }
            rootElement = leveledImportStructureProvider.getRoot();
            importStructureProvider = leveledImportStructureProvider;
        }

        if (rootElement == null) {
            return null;
        }

        return new Pair<>(rootElement, importStructureProvider);
    }

    /**
     * Convert a string path to a path containing valid names. See
     * {@link TmfTraceCoreUtils#validateName(String)}.
     *
     * @param path
     *            the string path to convert
     * @return the path contains valid segment names
     */
    public static IPath toValidNamesPath(String path) {
        IPath newSafePath = TmfTraceCoreUtils.newSafePath(path);
        IPath newFullPath = newSafePath;
        String[] segments = newSafePath.segments();
        for (int i = 0; i < segments.length; i++) {
            String segment = TmfTraceCoreUtils.validateName(TmfTraceCoreUtils.safePathToString(segments[i]));
            if (i == 0) {
                newFullPath = new Path(segment);
            } else {
                newFullPath = newFullPath.append(segment);
            }
        }
        return newFullPath;
    }
}
