/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.io;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;

/**
 * Utility class for handling {@link IResource} instances.
 *
 * @author Bernd Hufmann
 * @since 4.0
 */
@NonNullByDefault
public class ResourceUtil {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows"); //$NON-NLS-1$ //$NON-NLS-2$
    private static boolean fIsSymLinkSupported = !IS_WINDOWS;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    private ResourceUtil() {
        // Do nothing
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Creates a symbolic link to a target file or folder. If the local file system
     * supports symbolic links and the user requests to use a file system link it
     * will create a symbolic link on the file system. If the local file system
     * doesn't support symbolic links or on Windows, it will always use Eclipse
     * links.
     *
     * If a file system or Eclipse symbolic link already exists at the destination
     * it will replace the existing link. If a file or folder exists at the
     * destination the symbolic link won't be created and the method will return
     * false.
     *
     * @param link
     *            the resource in the workspace representing the link
     * @param targetLocation
     *            the target location in the local file system
     * @param useFileSystemLinks
     *            true for using file system symbolic links, false for Eclipse links
     *            Note: If file system doesn't support symbolic links or on Windows
     *            Eclipse links will be used.
     * @param monitor
     *            the progress monitor or null.
     * @return <code>true</code> if link was created successfully, else
     *         <code>false</code>
     * @throws CoreException
     *             if an error occurs
     */
    public static boolean createSymbolicLink(IResource link, @Nullable IPath targetLocation, boolean useFileSystemLinks, @Nullable IProgressMonitor monitor) throws CoreException {
        // Validate the input parameters
        if (!(link instanceof IFile) && !(link instanceof IFolder) || targetLocation == null) {
            return false;
        }

        SubMonitor subMon = SubMonitor.convert(monitor, 3);
        try {
            IPath location = getLocation(link);
            // Only create link if it doesn't already exist.
            if ((location != null) && link.exists() && location.equals(targetLocation)) {
                return true;
            }
            Path linkPath = Paths.get(link.getProject().getLocation().append(link.getProjectRelativePath()).toOSString());
            if (!checkResource(link, linkPath)) {
                return false;
            }
            if (!fIsSymLinkSupported || !useFileSystemLinks) {
                return createEclipseLink(link, targetLocation, subMon);
            }

            String targetPathString = targetLocation.toOSString();
            Path targetPath = Paths.get(targetPathString);

            // if the target is a file system symbolic link
            if (Files.isSymbolicLink(targetPath)) {
                targetPath = Files.readSymbolicLink(targetPath);
            }
            // Create files system symbolic link
            Files.createSymbolicLink(linkPath, targetPath);
            subMon.worked(1);
            if (link.getParent() != null) {
                link.getParent().refreshLocal(IResource.DEPTH_ONE, subMon.split(1));
            }
            link.refreshLocal(IResource.DEPTH_INFINITE, subMon.split(1));
            return true;
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error creating symbolic link", e)); //$NON-NLS-1$
        } catch (UnsupportedOperationException e) {
            // Remember that the symbolic link creation is not supported
            fIsSymLinkSupported = false;
            // Use file system symbolic link
            return createEclipseLink(link, targetLocation, subMon.split(2));
        }
    }

    /**
     * Deletes a resource from the file system.
     *
     * Notes: If the resource is a project, then the project will be deleted as well
     * as the content on the file system.
     *
     * The resource of a broken file system symbolic link will be also deleted.
     *
     * @param resource
     *            the resource in the workspace.
     * @param monitor
     *            the progress monitor or null.
     * @throws CoreException
     *             if an error occurs
     */
    public static void deleteResource(@Nullable IResource resource, @Nullable IProgressMonitor monitor) throws CoreException {
        SubMonitor subMon = SubMonitor.convert(monitor, 1);
        if (resource == null) {
            return;
        }
        if (isFileSystemSymbolicLink(resource) &&
                !resource.exists() &&
                ((resource instanceof IFile) ||
                        (resource instanceof IFolder))) {
            // Broken link
            try {
                Path linkPath = Paths.get(resource.getProject().getLocation().append(resource.getProjectRelativePath()).toOSString());
                Files.delete(linkPath);
                subMon.worked(1);
                return;
            } catch (IOException e) {
                // do nothing ... try Resource.delete() below
            }
        }
        resource.delete(true, subMon);
    }

    /**
     * Copy a resource in the file system.
     *
     * The supplied destination path may be absolute or relative. Absolute paths
     * fully specify the new location for the resource relative to the workspace
     * root, including its project. Relative paths are considered to be relative to
     * the container of the resource being copied.
     *
     * @param resource
     *            the resource in the workspace
     * @param destinationPath
     *            the destination path
     * @param flags
     *            update flags according to IResource.copy(). Note for file system
     *            symbolic links only IResource.SHALLOW is supported
     * @param monitor
     *            the progress monitor or null.
     * @return the copied resource or null
     * @throws CoreException
     *             if an error occurs
     */
    public static @Nullable IResource copyResource(@Nullable IResource resource, @Nullable IPath destinationPath, int flags, @Nullable IProgressMonitor monitor) throws CoreException {
        if (resource == null || destinationPath == null) {
            return null;
        }
        SubMonitor subMon = SubMonitor.convert(monitor, 1);
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        boolean isFileSystemSymbolicLink = ResourceUtil.isFileSystemSymbolicLink(resource);
        // make path absolute
        IPath target = destinationPath.isAbsolute() ? destinationPath : resource.getParent().getFullPath().append(destinationPath);
        if (isFileSystemSymbolicLink &&
                ((flags & IResource.SHALLOW) != 0) &&
                ((resource instanceof IFile)
                        || (resource instanceof IFolder))) {
            return copySymlink(resource, checkNotNull(target), checkNotNull(subMon), checkNotNull(workspaceRoot));
        }
        // use eclipse copy
        resource.copy(destinationPath, flags, subMon);
        return workspaceRoot.findMember(target);
    }

    /**
     * Checks whether the resource is a linked resource (file system or Eclipse
     * symbolic link) regardless of link is broken or not.
     *
     * @param resource
     *            the resources to check
     * @return <code>true</code> if it is a linked resource else <code>false</code>
     */
    public static boolean isSymbolicLink(@Nullable IResource resource) {
        boolean isLinked = false;
        if (resource != null) {
            isLinked = resource.isLinked();
            if (!isLinked) {
                isLinked = isFileSystemSymbolicLink(resource);
            }
        }
        return isLinked;
    }

    /**
     * Delete a broken symbolic link. Has no effect if the resource is not a
     * symbolic link or if the target of the symbolic link exists.
     *
     * @param resource
     *            the resource in the workspace.
     * @throws CoreException
     *             if an error occurs
     */
    public static void deleteIfBrokenSymbolicLink(@Nullable IResource resource) throws CoreException {
        if (resource == null) {
            return;
        }
        if (resource.isLinked()) {
            IPath location = resource.getLocation();
            if (location == null || !location.toFile().exists()) {
                resource.delete(true, null);
            }
        } else {
            URI uri = resource.getLocationURI();
            if (uri != null) {
                Path linkPath = Paths.get(uri);
                if (Files.isSymbolicLink(linkPath) && !resource.exists()) {
                    try {
                        Files.delete(linkPath);
                    } catch (Exception e) {
                        // Do nothing.
                    }
                }
            }
        }
    }

    /**
     * Returns the location path of the given resource. If the resource is file
     * system symbolic link then it will return the link target location.
     *
     * @param resource
     *            the resource to check
     * @return the {@link IPath} of the resource or null
     */
    public static @Nullable IPath getLocation(@Nullable IResource resource) {
        URI locationUri = getLocationURI(resource);
        if (locationUri != null) {
            return new org.eclipse.core.runtime.Path(locationUri.getPath());
        }
        return null;
    }

    /**
     * Returns the location URI of the given resource. If the resource is file
     * system symbolic link then it will return the link target location.
     *
     * @param resource
     *            the resource to check
     * @return the URI of the resource or null
     */
    public static @Nullable URI getLocationURI(@Nullable IResource resource) {
        if (resource != null) {
            if (isFileSystemSymbolicLink(resource)) {
                try {
                    return Files.readSymbolicLink(Paths.get(resource.getLocationURI())).toUri();
                } catch (IOException e) {
                    // Do nothing... return null below
                }
            } else {
                return resource.getLocationURI();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------
    private static boolean createEclipseLink(IResource link, IPath targetLocation, @Nullable IProgressMonitor monitor) throws CoreException {
        SubMonitor subMon = SubMonitor.convert(monitor);
        if (link instanceof IFile) {
            ((IFile) link).createLink(targetLocation, IResource.REPLACE, subMon);
        } else if (link instanceof IFolder) {
            ((IFolder) link).createLink(targetLocation, IResource.REPLACE, subMon);
        }
        return true;
    }

    private static boolean isFileSystemSymbolicLink(IResource resource) {
        URI uri = resource.getLocationURI();
        return (uri == null ? false : Files.isSymbolicLink(Paths.get(uri)));
    }

    private static boolean checkResource(IResource link, Path linkPath) throws CoreException, IOException {
        if (link.exists()) {
            // Eclipse resource already exists
            if (link.isLinked() ||
                    Files.isSymbolicLink(linkPath)) {
                // Remove the link
                link.delete(true, null);
            } else {
                // Abort because folder or file exists
                return false;
            }
        }
        if (Files.isSymbolicLink(linkPath)) {
            // Delete the broken file system symbolic link
            Files.delete(linkPath);
        } else if (linkPath.toFile().exists()) {
            // Abort because folder or file exists
            return false;
        }
        return true;
    }

    private static @Nullable IResource copySymlink(IResource resource, IPath destinationPath, SubMonitor monitor, IWorkspaceRoot workspaceRoot) throws CoreException {
        AtomicReference<IResource> newResourceRef = new AtomicReference<>();
        Path originalLinkedFile = Paths.get(resource.getProject().getLocation().append(resource.getProjectRelativePath()).toOSString());
        IPath newLocation = workspaceRoot.findMember(destinationPath.segment(0)).getLocation().append(destinationPath.removeFirstSegments(1));
        Path newLinkedFile = Paths.get(newLocation.toOSString());
        IResource parent = workspaceRoot.findMember(destinationPath.removeLastSegments(1));
        if (parent == null) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Parent resource does not exist: " + destinationPath.removeLastSegments(1))); //$NON-NLS-1$ )
        }
        ResourcesPlugin.getWorkspace().run((ICoreRunnable) (mon -> {
            try {
                SubMonitor subMon = SubMonitor.convert(mon, 2);
                Files.copy(originalLinkedFile, newLinkedFile, LinkOption.NOFOLLOW_LINKS);
                parent.refreshLocal(IResource.DEPTH_ONE, subMon.split(1));
                IResource newResource = workspaceRoot.findMember(destinationPath);
                if (newResource == null) {
                    return;
                }
                newResource.refreshLocal(IResource.DEPTH_INFINITE, subMon.split(1));
                @SuppressWarnings("null")
                Map<QualifiedName, String> persistentProperties = resource.getPersistentProperties();
                if (persistentProperties != null) {
                    for (Map.Entry<QualifiedName, String> entry : persistentProperties.entrySet()) {
                        newResource.setPersistentProperty(entry.getKey(), entry.getValue());
                    }
                }
                newResourceRef.set(newResource);
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error copying symbolic link", e)); //$NON-NLS-1$ )
            }
        }), parent.getParent(), IWorkspace.AVOID_UPDATE, monitor);
        return newResourceRef.get();
    }

}
