/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat, Inc - Was TarFileStructureProvider, performed changes from
 *     IImportStructureProvider to ILeveledImportStructureProvider
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *     Marc-Andre Laperle <marc-andre.laperle@ericsson.com> - Copied to Trace Compass to work around bug 501379
 *     Marc-Andre Laperle <marc-andre.laperle@ericsson.com> - Adapted to use Apache Common Compress
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;

/**
 * This class provides information regarding the context structure and content
 * of specified tar file entry objects.
 */
@SuppressWarnings("restriction")
public class TarLeveledStructureProvider implements
        ILeveledImportStructureProvider {
    private TarFile tarFile;

    private TarArchiveEntry root = new TarArchiveEntry("/", true);//$NON-NLS-1$

    private Map<TarArchiveEntry, List<TarArchiveEntry>> children;

    private Map<IPath, TarArchiveEntry> directoryEntryCache = new HashMap<>();

    private int stripLevel;

    /**
     * Creates a <code>TarFileStructureProvider</code>, which will operate on
     * the passed tar file.
     *
     * @param sourceFile
     *            the source TarFile
     */
    public TarLeveledStructureProvider(TarFile sourceFile) {
        super();
        tarFile = sourceFile;
    }

    /**
     * Creates a new container tar entry with the specified name, iff it has
     * not already been created. If the parent of the given element does not
     * already exist it will be recursively created as well.
     * @param pathName The path representing the container
     * @return The element represented by this pathname (it may have already existed)
     */
    protected TarArchiveEntry createContainer(IPath pathName) {
        IPath newPathName = pathName;
        TarArchiveEntry existingEntry = directoryEntryCache.get(newPathName);
        if (existingEntry != null) {
            return existingEntry;
        }

        TarArchiveEntry parent;
        if (newPathName.segmentCount() == 1) {
            parent = root;
        } else {
            parent = createContainer(newPathName.removeLastSegments(1));
        }
        // Add trailing / so that the entry knows it's a folder
        newPathName = newPathName.addTrailingSeparator();
        TarArchiveEntry newEntry = new TarArchiveEntry(newPathName.toString());
        directoryEntryCache.put(newPathName, newEntry);
        List<TarArchiveEntry> childList = new ArrayList<>();
        children.put(newEntry, childList);

        List<TarArchiveEntry> parentChildList = children.get(parent);
        NonNullUtils.checkNotNull(parentChildList).add(newEntry);
        return newEntry;
    }

    /**
     * Creates a new tar file entry with the specified name.
     * @param entry the entry to create the file for
     */
    protected void createFile(TarArchiveEntry entry) {
        IPath pathname = new Path(entry.getName());
        TarArchiveEntry parent;
        if (pathname.segmentCount() == 1) {
            parent = root;
        } else {
            parent = directoryEntryCache.get(pathname
                    .removeLastSegments(1));
        }

        List<TarArchiveEntry> childList = children.get(parent);
        NonNullUtils.checkNotNull(childList).add(entry);
    }

    @Override
    public List getChildren(Object element) {
        if (children == null) {
            initialize();
        }

        return (children.get(element));
    }

    @Override
    public InputStream getContents(Object element) {
        try {
            return tarFile.getInputStream((TarArchiveEntry) element);
        } catch (IOException e) {
            IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Returns the resource attributes for this file.
     *
     * @param element the element to get the attributes from
     * @return the attributes of the file
     */
    public ResourceAttributes getResourceAttributes(Object element) {
        ResourceAttributes attributes = new ResourceAttributes();
        TarArchiveEntry entry = (TarArchiveEntry) element;
        attributes.setExecutable((entry.getMode() & 0100) != 0);
        attributes.setReadOnly((entry.getMode() & 0200) == 0);
        return attributes;
    }

    @Override
    public String getFullPath(Object element) {
        String name = stripPath(((TarArchiveEntry) element).getName());
        return ArchiveUtil.toValidNamesPath(name).toOSString();
    }

    @Override
    public String getLabel(Object element) {
        if (element.equals(root)) {
            return ((TarArchiveEntry) element).getName();
        }

        String name = ((TarArchiveEntry) element).getName();
        return stripPath(ArchiveUtil.toValidNamesPath(name).lastSegment());
    }

    /**
     * Returns the entry that this importer uses as the root sentinel.
     *
     * @return TarArchiveEntry entry
     */
    @Override
    public Object getRoot() {
        return root;
    }

    /**
     * Returns the tar file that this provider provides structure for.
     *
     * @return TarFile file
     */
    public TarFile getTarFile() {
        return tarFile;
    }

    @Override
    public boolean closeArchive(){
        try {
            getTarFile().close();
        } catch (IOException e) {
            IDEWorkbenchPlugin.log(DataTransferMessages.ZipImport_couldNotClose
                    + getTarFile().getName(), e);
            return false;
        }
        return true;
    }

    /**
     * Initializes this object's children table based on the contents of the
     * specified source file.
     */
    protected void initialize() {
        children = new HashMap<>(1000);

        children.put(root, new ArrayList<>());
        Enumeration<TarArchiveEntry> entries = tarFile.entries();
        while (entries.hasMoreElements()) {
            TarArchiveEntry entry = entries.nextElement();
            IPath path = new Path(entry.getName()).addTrailingSeparator();

            if (entry.isDirectory()) {
                createContainer(path);
            } else
            {
                // Ensure the container structure for all levels above this is initialized
                // Once we hit a higher-level container that's already added we need go no further
                int pathSegmentCount = path.segmentCount();
                if (pathSegmentCount > 1) {
                    createContainer(path.uptoSegment(pathSegmentCount - 1));
                }
                createFile(entry);
            }
        }
    }

    @Override
    public boolean isFolder(Object element) {
        return (((TarArchiveEntry) element).isDirectory());
    }

    /*
     * Strip the leading directories from the path
     */
    private String stripPath(String path) {
        String strippedPath = path;
        String pathOrig = strippedPath;
        for (int i = 0; i < stripLevel; i++) {
            int firstSep = strippedPath.indexOf('/');
            // If the first character was a seperator we must strip to the next
            // seperator as well
            if (firstSep == 0) {
                strippedPath = strippedPath.substring(1);
                firstSep = strippedPath.indexOf('/');
            }
            // No seperator wasw present so we're in a higher directory right
            // now
            if (firstSep == -1) {
                return pathOrig;
            }
            strippedPath = strippedPath.substring(firstSep);
        }
        return strippedPath;
    }

    @Override
    public void setStrip(int level) {
        stripLevel = level;
    }

    @Override
    public int getStrip() {
        return stripLevel;
    }
}
