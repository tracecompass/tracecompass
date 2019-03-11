/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.io;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.io.ResourceUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for class {@link ResourceUtil}
 *
 * @author Bernd Hufmann
 */
@RunWith(Parameterized.class)
public class ResourceUtilTest {

    /** Temporary folder */
    @ClassRule
    public static TemporaryFolder fTemporaryFolder = new TemporaryFolder();
    private static TemporaryFolder fProjectFolder = initStaticTemp();

    private static final boolean IS_LINUX = System.getProperty("os.name").contains("Linux") ? true : false; //$NON-NLS-1$ //$NON-NLS-2$
    private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows") ? true : false; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String SOME_PROJECT_NAME = "SomeProject";
    private static final String SOME_OTHER_PROJECT_NAME = "SomeOtherProject";
    private static final String SOME_NEW_PROJECT_NAME = "SomeNewProject";
    private static final String SOME_FOLDER_NAME = "Folder";
    private static final String LINK_TARGET_FILE = "targetFile";
    private static final String LINK_TO_TARGET_FILE = "targetFileLink";
    private static final String LINK_TARGET_FOLDER = "targetFolder";
    private static final String SYMBOLIC_LINK_FILE_NAME = "symbolicLinkFile";
    private static final String TEST_FILE_NAME = "testFile";
    private static final String SYMBOLIC_LINK_FOLDER_NAME = "symbolicLinkFolder";
    private static final String TEST_FOLDER_NAME = "folderFolder";
    private static final String ECLIPSE_LINK_FILE_NAME = "eclipseLinkFile";
    private static final String ECLIPSE_LINK_FOLDER_NAME = "eclipseLinkFolder";
    private static final String COPY_SUFFIX = "_copy";
    private static final String PROPERTY_KEY = "KEY";
    private static final String PROPERTY_VALUE = "VALUE";

    private static IWorkspaceRoot fWorkspaceRoot;
    private static IProject fSomeProject;

    private static IProject fSomeOtherProject;

    private static File fTargetFile;
    private static File fTargetFolder;

    // Test parameters
    private IProject fTestProject;
    private IFolder fTestFolder;

    // ------------------------------------------------------------------------
    // Initialization
    // ------------------------------------------------------------------------
    /**
     * Create test parameter for the parameterized runner.
     *
     * @return The list of test parameters
     * @throws CoreException
     *             if core error occurs
     */
    @Parameters(name = "{index}: ({0})")
    public static Iterable<Object[]> getTracePaths() throws CoreException {
        IProgressMonitor progressMonitor = new NullProgressMonitor();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Create a project inside workspace location
        fWorkspaceRoot = workspace.getRoot();
        fSomeProject = fWorkspaceRoot.getProject(SOME_PROJECT_NAME);
        fSomeProject.create(progressMonitor);
        fSomeProject.open(progressMonitor);

        // Create an other project outside the workspace location
        URI projectLocation = fProjectFolder.getRoot().toURI();
        fSomeOtherProject = fWorkspaceRoot.getProject(SOME_OTHER_PROJECT_NAME);
        IProjectDescription description = workspace.newProjectDescription(fSomeOtherProject.getName());
        if (projectLocation != null) {
            description.setLocationURI(projectLocation);
        }
        fSomeOtherProject.create(description, progressMonitor);
        fSomeOtherProject.open(progressMonitor);
        return Arrays.asList(new Object[][] { {fSomeProject}, {fSomeOtherProject} });
    }

    /**
     * Class initialization
     *
     * @throws IOException
     *             if an IO error occurs
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        fTargetFile = fTemporaryFolder.newFile(LINK_TARGET_FILE).getCanonicalFile();
        fTargetFolder = fTemporaryFolder.newFolder(LINK_TARGET_FOLDER).getCanonicalFile();
    }

    /**
     * Class clean-ups
     *
     * @throws CoreException
     *             if core error occurs
     */
    @AfterClass
    public static void afterClass() throws CoreException {
        if (fSomeProject != null) {
            fSomeProject.delete(true, true, new NullProgressMonitor());
        }

        if (fSomeOtherProject != null) {
            fSomeOtherProject.delete(true, true, new NullProgressMonitor());
        }

        if (fProjectFolder != null) {
            fProjectFolder.delete();
        }
    }

    // Create a temporary folder manually because @ClassRule is done after the
    // @Parameters method call
    private static TemporaryFolder initStaticTemp() {
        try {
            return new TemporaryFolder() { { before(); } };
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // ------------------------------------------------------------------------
    // Test constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param project
     *              the project to use
     * @throws CoreException
     *             if core error occurs
     */
    public ResourceUtilTest(IProject project) throws CoreException {
        fTestProject = project;
        fTestFolder = project.getFolder(SOME_FOLDER_NAME);
        if (!fTestFolder.exists()) {
            fTestFolder.create(true, true, null);
        }
    }

    // ------------------------------------------------------------------------
    // Test Cases
    // ------------------------------------------------------------------------
    /**
     * Test {@link ResourceUtil#createSymbolicLink(IResource,IPath, boolean, IProgressMonitor) }
     *
     * @throws IOException
     *             if an IO error occurs
     * @throws CoreException
     *             if core error occurs
     */
    @Test
    public void testCreateSymbolicLink() throws IOException, CoreException {
        // Create file system symbolic link to a file
        IPath path = new Path(fTargetFile.getAbsolutePath());
        boolean success;
        boolean isSymLink = !IS_WINDOWS;
        createAndVerifyLink(path, SYMBOLIC_LINK_FILE_NAME, true, isSymLink);

        // Create file Eclipse link to a file
        createAndVerifyLink(path, ECLIPSE_LINK_FILE_NAME, true, false);

        // Re-do creation of file system symbolic link to a file
        IResource res1 = createAndVerifyLink(path, SYMBOLIC_LINK_FILE_NAME, true, isSymLink);

        // Re-do file Eclipse link to a file
        createAndVerifyLink(path, ECLIPSE_LINK_FILE_NAME, true, false);

        // Re-do with file system link to a file. Eclipse link won't be replaced since link to same location exists.
        IResource res2 = createAndVerifyLink(path, ECLIPSE_LINK_FILE_NAME, true, isSymLink, false);

        // Break file system symbolic link (eclipse resource won't exist)
        fTargetFile.delete();
        fTestFolder.refreshLocal(IResource.DEPTH_ONE, null);
        IFile file6 = fTestFolder.getFile(SYMBOLIC_LINK_FILE_NAME);
        assertNotNull(file6);
        assertEquals(IS_WINDOWS, file6.exists());
        success = ResourceUtil.createSymbolicLink(file6, path, true, null);
        assertTrue(success);
        assertTrue(isFileSystemSymbolicLink(file6) == isSymLink);
        assertEquals(IS_WINDOWS, file6.isLinked());

        // Re-create target file
        fTargetFile = fTemporaryFolder.newFile(LINK_TARGET_FILE).getCanonicalFile();

        if (IS_LINUX) {
            // Link to file system symbolic link
            java.nio.file.Path targetPath = Paths.get(path.toOSString());
            java.nio.file.Path linkPath = Paths.get(fTemporaryFolder.getRoot().getCanonicalPath(), LINK_TO_TARGET_FILE);
            // Create files system symbolic link
            Files.createSymbolicLink(linkPath, targetPath);

            path = new Path(linkPath.toString());

            // Follow file system symbolic link to a file
            IResource res4 = createAndVerifyLink(path, LINK_TO_TARGET_FILE, true, true);
            res4.delete(true, null);
            Files.delete(linkPath);
        }

        // Delete links in fTestFolder
        res1.delete(true, null);
        res2.delete(true, null);
        file6.delete(true, null);

        path = new Path(fTargetFolder.getAbsolutePath());

        // Create file system symbolic link to a folder
        createAndVerifyLink(path, SYMBOLIC_LINK_FOLDER_NAME, false, isSymLink);

        // Create file Eclipse link to a folder
        createAndVerifyLink(path, ECLIPSE_LINK_FOLDER_NAME, false, false);

        // Re-do creation of file system symbolic link to a folder
        res1 = createAndVerifyLink(path, SYMBOLIC_LINK_FOLDER_NAME, false, isSymLink);

        // Re-do file Eclipse link to a folder
        createAndVerifyLink(path, ECLIPSE_LINK_FOLDER_NAME, false, false);

        // re-do with file system link to a folder. Eclipse link won't be replaced since link to same location exists
        res2 = createAndVerifyLink(path, ECLIPSE_LINK_FOLDER_NAME, false, isSymLink, false);

        // delete linked folders
        res1.delete(true, null);
        res2.delete(true, null);

        // Test passing a resource that is not a file or folder
        assertFalse(ResourceUtil.createSymbolicLink(checkNotNull(fTestProject), path, true, null));
        assertFalse(ResourceUtil.createSymbolicLink(checkNotNull(fTestFolder), null, true, null));

        path = new Path(fTargetFile.getAbsolutePath());

        // Test Eclipse resource when file exist
        IResource resource = createAndVerifyResource(SYMBOLIC_LINK_FILE_NAME, true);
        success = ResourceUtil.createSymbolicLink(resource, path, true, new NullProgressMonitor());
        assertFalse(success);
        success = ResourceUtil.createSymbolicLink(resource, path, false, new NullProgressMonitor());
        assertFalse(success);
        resource.delete(true, null);

        // Link to file system symbolic link
        java.nio.file.Path linkPath = Paths.get(fTestFolder.getLocation().append(SYMBOLIC_LINK_FOLDER_NAME).toOSString());
        // Create a directory in the workspace
        Files.createDirectory(linkPath);
        // Create file system symbolic link to a folder
        IFolder folder = fTestFolder.getFolder(SYMBOLIC_LINK_FOLDER_NAME);
        assertNotNull(folder);
        path = new Path(fTargetFolder.getAbsolutePath());
        success = ResourceUtil.createSymbolicLink(folder, path, true, null);
        assertFalse(success);
        success = ResourceUtil.createSymbolicLink(folder, path, false, null);
        assertFalse(success);
        Files.delete(linkPath);
    }

    /**
     * Test {@link ResourceUtil#deleteResource(IResource, IProgressMonitor)}
     *
     * @throws IOException
     *             if an IO error occurs
     * @throws CoreException
     *             if core error occurs
     */
    @Test
    public void testDeleteResource() throws IOException, CoreException {
        // Create file system symbolic link to a file and delete it
        IPath path = new Path(fTargetFile.getAbsolutePath());
        boolean isSymLink = !IS_WINDOWS;
        deleteLinkAndVerify(path, SYMBOLIC_LINK_FILE_NAME, true, isSymLink);

        // Create file Eclipse link to a file and delete it
        deleteLinkAndVerify(path, ECLIPSE_LINK_FILE_NAME, true, false);

        path = new Path(fTargetFolder.getAbsolutePath());
        // Create file system symbolic link to a folder and delete it
        deleteLinkAndVerify(path, SYMBOLIC_LINK_FOLDER_NAME, false, isSymLink);

        // Create file Eclipse link to a folder and delete it
        deleteLinkAndVerify(path, ECLIPSE_LINK_FOLDER_NAME, false, false);

        // Create Eclipse resource file and delete it
        IResource resource = createAndVerifyResource(TEST_FILE_NAME, true);
        ResourceUtil.deleteResource(resource, new NullProgressMonitor());
        assertFalse(resource.exists());

        // Create Eclipse resource folder and delete it
        resource = createAndVerifyResource(TEST_FOLDER_NAME, false);
        ResourceUtil.deleteResource(resource, null);
        assertFalse(resource.exists());

        // Create File System Link to a file and break the link
        path = new Path(fTargetFile.getAbsolutePath());
        IResource res = createAndVerifyLink(path, SYMBOLIC_LINK_FILE_NAME, true, isSymLink);
        fTargetFile.delete();
        fTestFolder.refreshLocal(IResource.DEPTH_ONE, null);

        ResourceUtil.deleteResource(res, null);
        assertFalse(res.exists());

        // Re-create target file
        fTargetFile = fTemporaryFolder.newFile(LINK_TARGET_FILE).getCanonicalFile();

        // Create File System Link to a folder and break the link
        path = new Path(fTargetFolder.getAbsolutePath());
        res = createAndVerifyLink(path, SYMBOLIC_LINK_FOLDER_NAME, false, isSymLink);
        fTargetFolder.delete();
        fTestFolder.refreshLocal(IResource.DEPTH_ONE, null);

        ResourceUtil.deleteResource(res, null);
        assertFalse(res.exists());

        // Re-create target folder
        fTargetFolder = fTemporaryFolder.newFolder(LINK_TARGET_FOLDER).getCanonicalFile();

        // Test with null resource
        ResourceUtil.deleteResource(null, null);

        // Test deletion of project using utility
        IProject otherProject = fWorkspaceRoot.getProject(SOME_NEW_PROJECT_NAME);
        otherProject.create(null);
        otherProject.open(null);
        String location = otherProject.getLocation().toOSString();
        // Delete project from workspace (will delete content on file system)
        ResourceUtil.deleteResource(otherProject, null);
        java.nio.file.Path otherPath = Paths.get(location);
        assertFalse(Files.exists(otherPath));
    }

    /**
     * Test {@link ResourceUtil#copyResource(IResource, IPath, int, IProgressMonitor)}
     *
     * @throws IOException
     *             if an IO error occurs
     * @throws CoreException
     *             if core error occurs
     */
    @Test
    public void testCopyResource() throws IOException, CoreException {
        IPath path = new Path(fTargetFile.getAbsolutePath());
        boolean isSymLink = !IS_WINDOWS;
        // Copy file symbolic link (shallow)
        createLinkCopyAndVerify(path, SYMBOLIC_LINK_FILE_NAME, true, isSymLink, true);
        // Copy file with relative path
        createLinkCopyAndVerify(path, SYMBOLIC_LINK_FILE_NAME, true, isSymLink, true, false);
        // Copy file symbolic link (non-shallow)
        createLinkCopyAndVerify(path, SYMBOLIC_LINK_FILE_NAME, true, isSymLink, false);

        // Copy Eclipse file link (shallow)
        createLinkCopyAndVerify(path, ECLIPSE_LINK_FILE_NAME, true, false, true);
        // Copy Eclipse file link (non-shallow)
        createLinkCopyAndVerify(path, ECLIPSE_LINK_FILE_NAME, true, false, false);

        path = new Path(fTargetFolder.getAbsolutePath());

        // Copy folder symbolic link (shallow)
        createLinkCopyAndVerify(path, SYMBOLIC_LINK_FOLDER_NAME, false, isSymLink, true);
        // Copy folder with relative path
        createLinkCopyAndVerify(path, SYMBOLIC_LINK_FOLDER_NAME, false, isSymLink, true, false);
        // Copy folder symbolic link (non-shallow)
        createLinkCopyAndVerify(path, SYMBOLIC_LINK_FOLDER_NAME, false, isSymLink, false);

        // Copy file
        createCopyAndVerifyResource(TEST_FILE_NAME, true);

        // Copy folder
        createCopyAndVerifyResource(TEST_FOLDER_NAME, false);

        // Copy file with relative path
        createCopyAndVerifyResource(TEST_FILE_NAME, true, false);

        // Verify null parameters
        ResourceUtil.copyResource(null, null, 0, null);
        ResourceUtil.copyResource(fTestFolder, null, 0, null);

        // Copy a project
        path = new Path(SOME_NEW_PROJECT_NAME);
        IResource proj = ResourceUtil.copyResource(fTestProject, path, IResource.FORCE, null);
        assertNotNull(proj);
        assertTrue(proj instanceof IProject);
        ((IProject)proj).delete(true, true, null);
    }

    /**
     * Test {@link ResourceUtil#isSymbolicLink(IResource)}
     *
     * Most test cases are handled in {@link #testCreateSymbolicLink()} when
     * creating and verifying links
     *
     * @throws CoreException
     *             if core error occurs
     */
    @Test
    public void testIsSymbolicLink() throws CoreException {
        // Test missing cases

        // Create file and check for link
        IResource resource = createAndVerifyResource(TEST_FILE_NAME, true);
        assertFalse(ResourceUtil.isSymbolicLink(resource));
        resource.delete(true, null);

        // Create folder and check for link
        resource = createAndVerifyResource(TEST_FOLDER_NAME, false);
        assertFalse(ResourceUtil.isSymbolicLink(resource));
        resource.delete(true, null);

        // Null resource
        assertFalse(ResourceUtil.isSymbolicLink(null));
    }

    /**
     * Test {@link ResourceUtil#deleteIfBrokenSymbolicLink(IResource)}
     *
     * @throws IOException
     *             if an IO error occurs
     * @throws CoreException
     *             if core error occurs
     */
    @Test
    public void testDeleteIfBrokenSymbolicLink() throws IOException, CoreException {
        // Create file
        IResource resource = createAndVerifyResource(TEST_FILE_NAME, true);
        // No effect: resource is not a symbolic link
        ResourceUtil.deleteIfBrokenSymbolicLink(resource);
        assertTrue(resource.exists());
        resource.delete(true, null);

        IPath path = new Path(fTargetFile.getAbsolutePath());

        // Create file system symbolic link to a file
        boolean isSymLink = !IS_WINDOWS;
        resource = createAndVerifyLink(path, SYMBOLIC_LINK_FILE_NAME, true, isSymLink);
        // No effect: link target exists
        ResourceUtil.deleteIfBrokenSymbolicLink(resource);
        assertTrue(resource.exists());
        // Broken link is deleted
        fTargetFile.delete();
        fTestFolder.refreshLocal(IResource.DEPTH_ONE, null);
        ResourceUtil.deleteIfBrokenSymbolicLink(resource);
        assertFalse(resource.exists());

        // Re-create target file
        fTargetFile = fTemporaryFolder.newFile(LINK_TARGET_FILE).getCanonicalFile();

        // Create Eclipse link to a file
        resource = createAndVerifyLink(path, ECLIPSE_LINK_FILE_NAME, true, false);
        // No effect: link target exists
        ResourceUtil.deleteIfBrokenSymbolicLink(resource);
        assertTrue(resource.exists());
        // Broken link is deleted
        fTargetFile.delete();
        ResourceUtil.deleteIfBrokenSymbolicLink(resource);
        assertFalse(resource.exists());

        // Re-create target file
        fTargetFile = fTemporaryFolder.newFile(LINK_TARGET_FILE).getCanonicalFile();
    }

    /**
     * Test {@link ResourceUtil#getLocation(IResource)}
     *
     * Most test cases are handled in {@link #testCreateSymbolicLink()} when
     * creating and verifying links
     *
     * @throws IOException
     *             if an IO error occurs
     * @throws CoreException
     *             if core error occurs
     */
    @Test
    public void testGetLocation() throws IOException, CoreException {
        // Create file and check for link
        IResource resource = createAndVerifyResource(TEST_FILE_NAME, true);
        verifyLocation(resource.getLocation(), resource);
        resource.delete(true, null);

        // Create folder and check for link
        resource = createAndVerifyResource(TEST_FOLDER_NAME, false);
        verifyLocation(resource.getLocation(), resource);
        resource.delete(true, null);

        // Null resource
        assertNull(ResourceUtil.getLocation(null));
    }

    /**
     * Test {@link ResourceUtil#getLocationURI(IResource)}
     *
     * Most test cases are handled in {@link #testCreateSymbolicLink()} and
     * {@link #testGetLocation() }
     *
     */
    @Test
    public void testGetLocationUri() {
        // Null resource
        assertNull(ResourceUtil.getLocationURI(null));
    }

    private @NonNull IResource createAndVerifyLink(IPath path, String name, boolean isFile, boolean isSymLink) throws IOException, CoreException {
        return createAndVerifyLink(path, name, isFile, isSymLink, true);
    }

    private @NonNull IResource createAndVerifyLink(IPath path, String name, boolean isFile, boolean isSymLink, boolean checkSymLink) throws IOException, CoreException {
        IResource resource;
        if (isFile) {
            resource = fTestFolder.getFile(name);
        } else {
            resource = fTestFolder.getFolder(name);
        }
        assertNotNull(resource);
        boolean success = ResourceUtil.createSymbolicLink(resource, path, isSymLink, null);
        assertTrue(success);
        assertTrue(resource.exists());
        assertTrue(isFileSystemSymbolicLink(resource) == (checkSymLink ? isSymLink : false));
        assertTrue(resource.isLinked() == ((checkSymLink ? !isSymLink : true)));
        assertTrue(ResourceUtil.isSymbolicLink(resource));
        verifyLocation(path, resource);
        return resource;
    }

    private void createCopyAndVerifyResource(String name, boolean isFile) throws CoreException {
        createCopyAndVerifyResource(name, isFile, true);
    }

    private void createCopyAndVerifyResource(String name, boolean isFile, boolean isAbsolute) throws CoreException {
        IResource resource = createAndVerifyResource(name, true);
        IPath newPath;
        if (isAbsolute) {
            newPath = resource.getParent().getFullPath().addTrailingSeparator().append(name + COPY_SUFFIX);
        } else {
            newPath = new Path ("..").append(resource.getParent().getName()).append(name + COPY_SUFFIX);
        }
        resource.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, PROPERTY_KEY), PROPERTY_VALUE);
        IResource copyResource = ResourceUtil.copyResource(resource, newPath, IResource.FORCE, new NullProgressMonitor());
        assertNotNull(copyResource);
        Map<QualifiedName, String> persistentProperties = copyResource.getPersistentProperties();
        assertEquals(1, persistentProperties.size());
        for (Map.Entry<QualifiedName, String> entry: persistentProperties.entrySet()) {
            assertEquals(PROPERTY_KEY, entry.getKey().getLocalName());
            assertEquals(Activator.PLUGIN_ID, entry.getKey().getQualifier());
            assertEquals(PROPERTY_VALUE, entry.getValue());
        }
        resource.delete(true, null);
        copyResource.delete(true, null);
    }

    private @NonNull IResource createAndVerifyResource(String name, boolean isFile) throws CoreException {
        IResource resource;
        if (isFile) {
            resource = fTestFolder.getFile(name);
            ((IFile) resource).create(new ByteArrayInputStream(new byte[0]), false, new NullProgressMonitor());
        } else {
            resource = fTestFolder.getFolder(name);
            ((IFolder) resource).create(true, true, null);
        }
        assertNotNull(resource);
        assertTrue(resource.exists());
        return resource;
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------
    private static void verifyLocation(IPath path, IResource resource) throws IOException {
        String osString = path.toOSString();
        if (Files.isSymbolicLink(Paths.get(osString))) {
            // link to a link
            IPath myPath = new org.eclipse.core.runtime.Path(Files.readSymbolicLink(Paths.get(osString)).toString());
            assertEquals(new Path(Files.readSymbolicLink(Paths.get(osString)).toString()), ResourceUtil.getLocation(resource));
            URI uri = ResourceUtil.getLocationURI(resource);
            assertNotNull(uri);
            assertEquals(myPath, new Path(uri.getRawPath()));
        } else {
            assertEquals(path, ResourceUtil.getLocation(resource));
            URI uri = ResourceUtil.getLocationURI(resource);
            assertNotNull(uri);
            assertEquals(path, new Path(uri.getRawPath()));
        }
    }

    private void deleteLinkAndVerify(IPath path, String name, boolean isFile, boolean isSymLink) throws IOException, CoreException {
        IResource resource = createAndVerifyLink(path, name, isFile, isSymLink);
        ResourceUtil.deleteResource(resource, null);
        assertFalse(resource.exists());
    }

    private void createLinkCopyAndVerify(IPath path, String name, boolean isFile, boolean isSymLink, boolean isShallow) throws IOException, CoreException {
        createLinkCopyAndVerify(path, name, isFile, isSymLink, isShallow, true);
    }

    private void createLinkCopyAndVerify(IPath path, String name, boolean isFile, boolean isSymLink, boolean isShallow, boolean isAbsolute) throws IOException, CoreException {
        IResource originialResource = createAndVerifyLink(path, name, isFile, isSymLink);
        originialResource.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, PROPERTY_KEY), PROPERTY_VALUE);
        IPath newPath;
        if (isAbsolute) {
            newPath = originialResource.getParent().getFullPath().addTrailingSeparator().append(name + COPY_SUFFIX);
        } else {
            newPath = new Path ("..").append(originialResource.getParent().getName()).append(name + COPY_SUFFIX);
        }
        int flags = IResource.FORCE;
        flags |= (isShallow ? IResource.SHALLOW : 0);
        IResource copyResource = ResourceUtil.copyResource(originialResource, checkNotNull(newPath), flags, null);
        assertNotNull(copyResource);
        assertTrue(copyResource.exists());
        assertTrue(isFileSystemSymbolicLink(copyResource) == (isSymLink && isShallow));
        assertTrue(copyResource.isLinked() == (!isSymLink && isShallow));
        assertTrue(ResourceUtil.isSymbolicLink(copyResource) == ((isSymLink && isShallow) || (!isSymLink && isShallow)));

        Map<QualifiedName, String> persistentProperties = copyResource.getPersistentProperties();
        assertEquals(1, persistentProperties.size());
        for (Map.Entry<QualifiedName, String> entry: persistentProperties.entrySet()) {
            assertEquals(PROPERTY_KEY, entry.getKey().getLocalName());
            assertEquals(Activator.PLUGIN_ID, entry.getKey().getQualifier());
            assertEquals(PROPERTY_VALUE, entry.getValue());
        }
        originialResource.delete(true, null);
        copyResource.delete(true, null);
    }

    boolean isFileSystemSymbolicLink(IResource resource) {
        return Files.isSymbolicLink(Paths.get(resource.getLocationURI()));
    }
}

