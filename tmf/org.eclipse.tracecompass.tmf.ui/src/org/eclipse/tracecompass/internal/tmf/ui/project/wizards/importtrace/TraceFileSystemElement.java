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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.model.AdaptableList;

/**
 * The <code>TraceFileSystemElement</code> is a <code>FileSystemElement</code>
 * that knows if it has been populated or not.
 */
public class TraceFileSystemElement extends FileSystemElement {

    private boolean fIsPopulated = false;
    private String fLabel = null;
    private IPath fDestinationContainerPath;
    private FileSystemObjectImportStructureProvider fProvider;
    private String fSourceLocation;

    /**
     * Constructs a new TraceFileSystemElement
     *
     * @param name the name of the element
     * @param parent the parent element
     * @param isDirectory whether or not this element is a directory
     * @param provider the provider associated with this element
     */
    public TraceFileSystemElement(String name, FileSystemElement parent, boolean isDirectory, FileSystemObjectImportStructureProvider provider) {
        super(name, parent, isDirectory);
        fProvider = provider;
    }

    /**
     * Set the path for the container that will contain the trace once imported.
     *
     * @param destinationContainerPath the destination container path
     */
    public void setDestinationContainerPath(IPath destinationContainerPath) {
        fDestinationContainerPath = destinationContainerPath;
    }

    /**
     * Mark this element as populated.
     */
    public void setPopulated() {
        fIsPopulated = true;
    }

    /**
     * Returns whether or not the children of the element have been populated.
     *
     * @return whether or not the children of the element have been populated.
     */
    public boolean isPopulated() {
        return fIsPopulated;
    }

    @Override
    public AdaptableList getFiles() {
        if (!fIsPopulated) {
            populateElementChildren();
        }
        return super.getFiles();
    }

    @Override
    public AdaptableList getFolders() {
        if (!fIsPopulated) {
            populateElementChildren();
        }
        return super.getFolders();
    }

    /**
     * Sets the label for the trace to be used when importing at trace.
     *
     * @param name
     *            the label for the trace
     */
    public void setLabel(String name) {
        fLabel = name;
    }

    /**
     * Returns the label for the trace to be used when importing at trace.
     *
     * @return the label of trace resource
     */
    public String getLabel() {
        if (fLabel == null) {
            return getProvider().getLabel(this.getFileSystemObject());
        }
        return fLabel;
    }

    /**
     * The full path to the container that will contain the trace once imported.
     *
     * @return the destination container path
     */
    public IPath getDestinationContainerPath() {
        return fDestinationContainerPath;
    }

    /**
     * Populates the children of the specified parent
     * <code>FileSystemElement</code>
     */
    private void populateElementChildren() {
        List<IFileSystemObject> allchildren = fProvider.getChildren(this.getFileSystemObject());
        Object child = null;
        TraceFileSystemElement newelement = null;
        Iterator<IFileSystemObject> iter = allchildren.iterator();
        while (iter.hasNext()) {
            child = iter.next();
            newelement = new TraceFileSystemElement(fProvider.getLabel(child), this, fProvider.isFolder(child), fProvider);
            newelement.setFileSystemObject(child);
        }
        setPopulated();
    }

    /**
     * Get the import provider associated with this element.
     *
     * @return the import provider.
     */
    public FileSystemObjectImportStructureProvider getProvider() {
        return fProvider;
    }

    @Override
    public IFileSystemObject getFileSystemObject() {
        return (IFileSystemObject) super.getFileSystemObject();
    }

    /**
     * Get the source location for this element.
     *
     * @return the source location
     */
    public String getSourceLocation() {
        if (fSourceLocation == null) {
            fSourceLocation = getFileSystemObject().getSourceLocation();
        }
        return fSourceLocation;
    }

    /**
     * Set the source location for this element.
     *
     * @param sourceLocation
     *            the source location
     */
    public void setSourceLocation(String sourceLocation) {
        fSourceLocation = sourceLocation;
    }

    /**
     * Get all the TraceFileSystemElements recursively.
     *
     * @param result
     *            the list accumulating the result
     */
    public void getAllChildren(List<TraceFileSystemElement> result) {
        AdaptableList files = getFiles();
        for (Object file : files.getChildren()) {
            result.add((TraceFileSystemElement) file);
        }

        AdaptableList folders = getFolders();
        for (Object folder : folders.getChildren()) {
            TraceFileSystemElement traceElementFolder = (TraceFileSystemElement) folder;
            traceElementFolder.getAllChildren(result);
        }
    }

    /**
     * Create a root TraceFileSystemElement suitable to be passed to a
     * {@link TraceValidateAndImportOperation}
     *
     * @param object
     *            the IFileSystemObject (abstracting File, Tar, Zip, etc) to
     *            create the root TraceFileSystemElement from
     * @param provider
     *            the import provider to be used, compatible with the
     *            IFileSystemObject
     * @return the resulting root TraceFileSystemElement
     *
     * @See {@link TraceValidateAndImportOperation}
     * @See {@link ArchiveUtil#getRootObjectAndProvider(File, org.eclipse.swt.widgets.Shell)}
     */
    public static TraceFileSystemElement createRootTraceFileElement(IFileSystemObject object,
            FileSystemObjectImportStructureProvider provider) {
        boolean isContainer = provider.isFolder(object);
        String elementLabel = provider.getLabel(object);

        // Use an empty label so that display of the element's full name
        // doesn't include a confusing label
        TraceFileSystemElement dummyParent = new TraceFileSystemElement("", null, true, provider);//$NON-NLS-1$
        Object dummyParentFileSystemObject = object;
        Object rawFileSystemObject = object.getRawFileSystemObject();
        if (rawFileSystemObject instanceof File) {
            dummyParentFileSystemObject = provider.getIFileSystemObject(((File) rawFileSystemObject).getParentFile());
        }
        dummyParent.setFileSystemObject(dummyParentFileSystemObject);
        dummyParent.setPopulated();
        TraceFileSystemElement result = new TraceFileSystemElement(
                elementLabel, dummyParent, isContainer, provider);
        result.setFileSystemObject(object);

        // Get the files for the element so as to build the first level
        result.getFiles();

        return dummyParent;
    }
}