/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Marc-Andre Laperle - Added tests for extracting archives during import
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.ui.swtbot.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportConfirmation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards.SWTBotImportWizardUtils;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot Smoke test using ImportTraceWizard.
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class StandardImportAndReadSmokeTest extends AbstractImportAndReadSmokeTest {

    private static final String TRACE_FOLDER_PARENT_PATH = "../../ctf/org.eclipse.tracecompass.ctf.core.tests/traces/";
    private static final String ARCHIVE_FILE_NAME = "synctraces.tar.gz";
    private static final String EMPTY_ARCHIVE_FOLDER = "emptyArchiveFolder";
    private static final String EMPTY_FILE_NAME = "emptyFile";
    private static final String TRACE_ARCHIVE_PATH = TRACE_FOLDER_PARENT_PATH + ARCHIVE_FILE_NAME;
    private static final String TRACE_FOLDER_PARENT_NAME = "traces";
    private static final String TRACE_PROJECT_NAME = "Tracing";

    private static final String ARCHIVE_ROOT_ELEMENT_NAME = "/";
    private static final String GENERATED_ARCHIVE_NAME = "testtraces.zip";
    private static final String URI_SEPARATOR = "/";
    private static final String URI_FILE_SCHEME = "file:";
    private static final String URI_JAR_FILE_SCHEME = "jar:file:";
    private static final boolean IS_WIN32 = System.getProperty("os.name").startsWith("Windows");  //$NON-NLS-1$//$NON-NLS-2$
    private static final String URI_DEVICE_SEPARATOR = IS_WIN32 ? URI_SEPARATOR : "";

    /** Test Class setup */
    @BeforeClass
    public static void beforeClass() {
        createProject(TRACE_PROJECT_NAME);
    }

    /** Test Class tear down */
    @AfterClass
    public static void afterClass() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }

    /**
     * Test import from directory
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromDirectory() throws Exception {
        testImport(0, false, false);
    }

    /**
     * Test import from directory, create links
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromDirectoryLinks() throws Exception {
        testImport(ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE, false, false);
    }

    /**
     * Test import from directory, create experiment
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportWithExperiment() throws Exception {
        testImport(ImportTraceWizardPage.OPTION_CREATE_EXPERIMENT, false, false);
    }

    /**
     * Test import from directory, create experiment (validate experiment name)
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportWithExperimentValidation() throws Exception {
        testImport(ImportTraceWizardPage.OPTION_CREATE_EXPERIMENT, false, false, false, true, ImportConfirmation.CONTINUE);
    }

    /**
     * Test import from directory, preserve folder structure
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromDirectoryPreserveFolder() throws Exception {
        testImport(ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE, false, false);
    }

    /**
     * Test import from directory, create links, preserve folder structure
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromDirectoryLinksPreserveFolder() throws Exception {
        int options = ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE | ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE;
        testImport(options, false, false);
    }

    /**
     * Test import from directory, overwrite all
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromDirectoryOverwrite() throws Exception {
        testImport(0, false, false, true, false, ImportConfirmation.CONTINUE);
        testImport(ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES, false, false);
    }

    /**
     * Test import from directory, overwrite all
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromDirectoryOverwriteRenameAll() throws Exception {
        testImport(0, false, false, true, false, ImportConfirmation.CONTINUE);
        testImport(0, false, false, true, true, ImportConfirmation.RENAME_ALL);
    }

    /**
     * Test import from directory, overwrite all
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromDirectoryOverwriteOverwriteAll() throws Exception {
        testImport(0, false, false, true, false, ImportConfirmation.CONTINUE);
        testImport(0, false, false, true, true, ImportConfirmation.OVERWRITE_ALL);
    }

    /**
     * Test import from archive
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromArchive() throws Exception {
        testImport(ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE, true, true);
    }

    /**
     * Test import from archive, create Experiment
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromArchiveWithExperiment() throws Exception {
        testImport(ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE | ImportTraceWizardPage.OPTION_CREATE_EXPERIMENT, false, true);
    }

    /**
     * Test import from directory, preserve folder structure
     * @throws Exception on error
     */
    @Test
    public void testImportFromArchivePreserveFolder() throws Exception {
        testImport(ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE, false, true);
    }

    /**
     * Test import from directory, overwrite all
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testImportFromArchiveOverwrite() throws Exception {
        testImport(0, false, true, true, false, ImportConfirmation.CONTINUE);
        testImport(ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES, false, true);
    }

    /**
     * Test import from directory containing archives
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testExtractArchivesFromDirectory() throws Exception {
        testImportAndExtractArchives(ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES, false, false);
    }

    /**
     * Test import from directory containing archives, create links
     * @throws Exception on error
     */
    @Test
    public void testExtractArchivesFromDirectoryLinks() throws Exception {
        testImportAndExtractArchives(ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE | ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES, false, false);
    }

    /**
     * Test import from directory containing archives, create links, preserve folder structure
     * @throws Exception on error
     */
    @Test
    public void testExtractArchivesFromDirectoryLinksPreserveStruture() throws Exception {
        testImportAndExtractArchives(ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE | ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES | ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE, true, false);
    }

    /**
     * Test import from archive containing archives
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testExtractArchivesFromArchive() throws Exception {
        testImportAndExtractArchives(ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES, false, true);
    }

    /**
     * Test import from archive containing archives, preserve folder structure
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testExtractArchivesFromArchivePreserveFolder() throws Exception {
        testImportAndExtractArchives(ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES | ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE, false, true);
    }

    /**
     * Test import from an empty archive. This should not import anything.
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testEmptyArchive() throws Exception {
        String testArchivePath = createEmptyArchive();

        openImportWizard();
        SWTBotImportWizardUtils.selectImportFromArchive(fBot, testArchivePath);
        selectFolder(ARCHIVE_ROOT_ELEMENT_NAME);
        SWTBotImportWizardUtils.setOptions(fBot, 0, ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        importFinish();

        assertNoTraces();

        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
        Files.delete(Paths.get(testArchivePath));
    }

    /**
     * Test import from an empty directory. This should not import anything.
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testEmptyDirectory() throws Exception {
        IFolder emptyDirectory = createEmptyDirectory();
        String testDirectoryPath = emptyDirectory.getLocation().toOSString();

        openImportWizard();
        SWTBotImportWizardUtils.selectImportFromDirectory(fBot, testDirectoryPath);
        selectFolder(EMPTY_ARCHIVE_FOLDER);
        SWTBotImportWizardUtils.setOptions(fBot, 0, ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        importFinish();

        assertNoTraces();

        Files.delete(Paths.get(testDirectoryPath));
        emptyDirectory.delete(true, null);
        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
    }

    /**
     * Test import from an directory with an empty file. This should not import anything.
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testEmptyFile() throws Exception {
        IFolder folder = createEmptyDirectory();
        createEmptyFile(folder);
        String testDirectoryPath = folder.getLocation().toOSString();
        openImportWizard();
        SWTBotImportWizardUtils.selectImportFromDirectory(fBot, testDirectoryPath);
        SWTBotImportWizardUtils.selectFile(fBot, EMPTY_FILE_NAME, EMPTY_ARCHIVE_FOLDER);
        SWTBotImportWizardUtils.setOptions(fBot, ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES, ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        importFinish();

        assertNoTraces();

        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
        folder.delete(true, null);
    }

    /**
     * Test import from a directory containing an empty archive. This should not import anything.
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testDirectoryWithEmptyArchive() throws Exception {
        String testArchivePath = createEmptyArchive();

        openImportWizard();
        SWTBotImportWizardUtils.selectImportFromDirectory(fBot, getProjectResource().getLocation().toOSString());
        SWTBotImportWizardUtils.selectFile(fBot, GENERATED_ARCHIVE_NAME, TRACE_PROJECT_NAME);
        SWTBotImportWizardUtils.setOptions(fBot, 0, ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        importFinish();

        assertNoTraces();

        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
        Files.delete(Paths.get(testArchivePath));
    }

    /**
     * Test import from a nested empty archive. This should not import anything.
     *
     * @throws Exception
     *             on error
     */
    @Test
    public void testNestedEmptyArchive() throws Exception {
        IProject project = getProjectResource();

        // Create the empty archive from an empty folder
        String testArchivePath = createEmptyArchive();

        // Rename archive so that we can create a new one with the same name
        project.refreshLocal(IResource.DEPTH_ONE, null);
        IFile[] files = project.getWorkspace().getRoot().findFilesForLocationURI(new File(testArchivePath).toURI());
        IFile archiveFile = files[0];
        String newEmptyArchiveName = "nested" + archiveFile.getName();
        IPath dest = archiveFile.getFullPath().removeLastSegments(1).append(newEmptyArchiveName);
        archiveFile.move(dest, true, null);
        IFile renamedArchiveFile = archiveFile.getWorkspace().getRoot().getFile(dest);

        createArchive(renamedArchiveFile);
        renamedArchiveFile.delete(true, null);

        openImportWizard();
        SWTBotImportWizardUtils.selectImportFromArchive(fBot, testArchivePath);
        selectFolder(ARCHIVE_ROOT_ELEMENT_NAME);
        SWTBotImportWizardUtils.setOptions(fBot, 0, ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        importFinish();

        assertNoTraces();

        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
        Files.delete(Paths.get(testArchivePath));
    }

    /**
     * Test importing an archive with colons in the names. Those are invalid
     * characters in Windows paths so this test makes sure that they are
     * replaced properly with '_'
     */
    @Test
    public void testArchiveWithColons() {
        openImportWizard();
        IPath absolutePath = Activator.getAbsolutePath(new Path("testfiles/testcolon.zip"));

        SWTBotImportWizardUtils.selectImportFromArchive(fBot, absolutePath.toOSString());
        String subFolderName = IS_WIN32 ? "folder_colon" : "folder:colon";
        selectFolder(ARCHIVE_ROOT_ELEMENT_NAME, subFolderName);
        SWTBotImportWizardUtils.setOptions(fBot, 0, "Test trace : XML Trace Stub (ns)");
        importFinish();

        TmfProjectElement tmfProject = TmfProjectRegistry.getProject(getProjectResource(), true);
        assertNotNull(tmfProject);
        TmfTraceFolder tracesFolder = tmfProject.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertTrue(traces.size() == 1);
        String traceName = IS_WIN32 ? "trace_colon.xml" : "trace:colon.xml";
        assertEquals(traceName, traces.get(0).getName());

        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
    }

    private static void assertNoTraces() {
        TmfProjectElement tmfProject = TmfProjectRegistry.getProject(getProjectResource(), true);
        assertNotNull(tmfProject);
        TmfTraceFolder tracesFolder = tmfProject.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertTrue(traces.isEmpty());
    }

    private void testImport(int options, boolean testViews, boolean fromArchive) throws Exception {
        testImport(options, testViews, fromArchive, true, true, ImportConfirmation.CONTINUE);
    }

    private void testImport(int options, boolean testViews, boolean fromArchive, boolean defaultExperiment, boolean clearTraces, ImportConfirmation confirmationMode) throws Exception {
        String expectedSourceLocation = null;

        @NonNull String experimentName;
        if (fromArchive) {
            experimentName = checkNotNull(new Path(ARCHIVE_FILE_NAME).lastSegment());
        } else {
            experimentName = checkNotNull(new Path(TRACE_FOLDER_PARENT_PATH).lastSegment());
        }

        if (!defaultExperiment) {
            SWTBotUtils.createExperiment(fBot, TRACE_PROJECT_NAME, experimentName);
        }

        openImportWizard();
        if (fromArchive) {
            expectedSourceLocation = URI_JAR_FILE_SCHEME + URI_DEVICE_SEPARATOR + new Path(new File(TRACE_ARCHIVE_PATH).getCanonicalPath()) + "!" + URI_SEPARATOR + TRACE_FOLDER + URI_SEPARATOR + TRACE_NAME + URI_SEPARATOR;
            SWTBotImportWizardUtils.selectImportFromArchive(fBot, TRACE_ARCHIVE_PATH);
            selectFolder(ARCHIVE_ROOT_ELEMENT_NAME);
            SWTBotCheckBox checkBox = fBot.checkBox(Messages.ImportTraceWizard_CreateLinksInWorkspace);
            assertFalse(checkBox.isEnabled());
        } else {
            String sourcePath = TRACE_FOLDER_PARENT_PATH + File.separator + TRACE_FOLDER + File.separator + TRACE_NAME;
            expectedSourceLocation = URI_FILE_SCHEME + URI_DEVICE_SEPARATOR + new Path(new File(sourcePath).getCanonicalPath()) + URI_SEPARATOR;
            SWTBotImportWizardUtils.selectImportFromDirectory(fBot, TRACE_FOLDER_PARENT_PATH);
            selectFolder(new String [] {TRACE_FOLDER_PARENT_NAME, TRACE_FOLDER });
        }

        SWTBotImportWizardUtils.setOptions(fBot, options, ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);

        if (!defaultExperiment) {
            experimentName = verifyExperimentNameHandling(experimentName);
        }
        SWTBotImportWizardUtils.checkFinishButton(fBot, true);

        importFinish(confirmationMode);

        IPath expectedElementPath = new Path(TRACE_NAME);
        if ((options & ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE) != 0) {
            expectedElementPath = new Path(TRACE_FOLDER).append(expectedElementPath);
        }

        if (confirmationMode == ImportConfirmation.RENAME_ALL) {
            IPath expectedElementPathRenamed = new Path(TRACE_NAME + "(2)");
            checkOptions(options, expectedSourceLocation, expectedElementPath, experimentName, expectedElementPathRenamed);
        } else {
            checkOptions(options, expectedSourceLocation, expectedElementPath, experimentName, null);
        }

        TmfEventsEditor tmfEd = SWTBotUtils.openEditor(fBot, TRACE_PROJECT_NAME, expectedElementPath);
        if (testViews) {
            testViews(tmfEd);
        }

        fBot.closeAllEditors();

        SWTBotUtils.clearExperimentFolder(fBot, TRACE_PROJECT_NAME);
        if (clearTraces) {
            SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
        }
    }

    private void testImportAndExtractArchives(int options, boolean testViews, boolean fromArchive) throws Exception {
        String expectedSourceLocation;
        IPath expectedElementPath;
        String testArchivePath = null;
        if (fromArchive) {
            testArchivePath = createNestedArchive();
            openImportWizard();
            SWTBotImportWizardUtils.selectImportFromArchive(fBot, testArchivePath);
            SWTBotImportWizardUtils.selectFile(fBot, ARCHIVE_FILE_NAME, ARCHIVE_ROOT_ELEMENT_NAME, TRACE_PROJECT_NAME, TRACE_FOLDER_PARENT_NAME);

            expectedSourceLocation = URI_JAR_FILE_SCHEME + URI_DEVICE_SEPARATOR + new Path(new File(testArchivePath).getCanonicalPath()) + "!" + URI_SEPARATOR + TRACE_PROJECT_NAME + URI_SEPARATOR + TRACE_FOLDER_PARENT_NAME + URI_SEPARATOR + ARCHIVE_FILE_NAME
                    + URI_SEPARATOR + TRACE_FOLDER + URI_SEPARATOR + TRACE_NAME + URI_SEPARATOR;
            expectedElementPath = new Path(TRACE_PROJECT_NAME).append(TRACE_FOLDER_PARENT_NAME).append(ARCHIVE_FILE_NAME).append(TRACE_FOLDER).append(TRACE_NAME);
        } else {
            openImportWizard();
            SWTBotImportWizardUtils.selectImportFromDirectory(fBot, TRACE_FOLDER_PARENT_PATH);
            SWTBotImportWizardUtils.selectFile(fBot, ARCHIVE_FILE_NAME, TRACE_FOLDER_PARENT_NAME);
            expectedElementPath = new Path(ARCHIVE_FILE_NAME).append(TRACE_FOLDER).append(TRACE_NAME);
            expectedSourceLocation = URI_FILE_SCHEME + URI_DEVICE_SEPARATOR + new Path(new File(TRACE_FOLDER_PARENT_PATH).getCanonicalPath()) + URI_SEPARATOR + ARCHIVE_FILE_NAME + URI_SEPARATOR + TRACE_FOLDER + URI_SEPARATOR + TRACE_NAME + URI_SEPARATOR;
        }

        if ((options & ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE) == 0) {
            expectedElementPath = new Path(TRACE_NAME);
        }

        SWTBotImportWizardUtils.setOptions(fBot, options, ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        importFinish();
        // Archives should never be imported as links
        int expectedOptions = options & ~ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        checkOptions(expectedOptions, expectedSourceLocation, expectedElementPath);

        TmfEventsEditor editor = SWTBotUtils.openEditor(fBot, TRACE_PROJECT_NAME, expectedElementPath);
        if (testViews) {
            testViews(editor);
        }

        SWTBotUtils.clearExperimentFolder(fBot, TRACE_PROJECT_NAME);

        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
        if (testArchivePath != null) {
            Files.delete(Paths.get(testArchivePath));
        }
    }

    /**
     * Create a temporary archive containing a nested archive. For example,
     * testtraces.zip/synctraces.tar.gz can be used to test a nested archive.
     */
    private static String createNestedArchive() throws IOException, CoreException, URISyntaxException {
        // Link to the test traces folder. We use a link so that we can safely
        // delete the entire project when we are done.
        IProject project = getProjectResource();
        String canonicalPath = new File(TRACE_FOLDER_PARENT_PATH).getCanonicalPath();
        IFolder folder = project.getFolder(TRACE_FOLDER_PARENT_NAME);
        folder.createLink(new Path(canonicalPath), IResource.REPLACE, null);
        IFile file = folder.getFile(ARCHIVE_FILE_NAME);
        String archivePath = createArchive(file);
        folder.delete(true, null);
        return archivePath;
    }

    /**
     * Create the empty archive from an empty folder
     */
    private static String createEmptyArchive() throws CoreException, URISyntaxException {
        IFolder tempEmptyDirectory = createEmptyDirectory();
        String archivePath = createArchive(tempEmptyDirectory);
        tempEmptyDirectory.delete(true, null);
        return archivePath;
    }

    private static IFolder createEmptyDirectory() throws CoreException {
        IProject project = getProjectResource();
        IFolder folder = project.getFolder(EMPTY_ARCHIVE_FOLDER);
        folder.create(true, true, null);
        return folder;
    }

    private static void createEmptyFile(IFolder folder) throws CoreException {
        // Create empty file
        IFile file = folder.getFile(EMPTY_FILE_NAME);
        file.create(new ByteArrayInputStream(new byte[0]), true, null);
    }

    /**
     * Create a temporary archive from the specified resource.
     */
    private static String createArchive(IResource sourceResource) throws URISyntaxException {
        IPath exportedPath = sourceResource.getFullPath();

        SWTBotTreeItem traceFilesProject = SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME);
        traceFilesProject.contextMenu("Export...").click();

        fBot.waitUntil(Conditions.shellIsActive("Export"));
        SWTBotShell activeShell = fBot.activeShell();
        SWTBotTree exportWizardsTree = fBot.tree();
        SWTBotTreeItem treeItem = SWTBotUtils.getTreeItem(fBot, exportWizardsTree, "General", "Archive File");
        treeItem.select();
        fBot.button("Next >").click();
        fBot.button("&Deselect All").click();
        try {
            String resolveLinkedResLabel = "Resolve and export linked resources";
            fBot.waitUntil(Conditions.waitForWidget(withMnemonic(resolveLinkedResLabel)), 100);
            fBot.checkBox(resolveLinkedResLabel).select();
        } catch (TimeoutException e) {
            // Ignore, doesn't exist pre-4.6M5
        }

        if (sourceResource instanceof IFile) {
            String[] folderPath = exportedPath.removeLastSegments(1).segments();
            String fileName = exportedPath.lastSegment();
            SWTBotImportWizardUtils.selectFile(fBot, fileName, folderPath);
        } else {
            selectFolder(exportedPath.segments());
        }

        String workspacePath = URIUtil.toFile(URIUtil.fromString(System.getProperty("osgi.instance.area"))).getAbsolutePath();
        final String archiveDestinationPath = workspacePath + File.separator + TRACE_PROJECT_NAME + File.separator + GENERATED_ARCHIVE_NAME;
        fBot.comboBox().setText(archiveDestinationPath);
        fBot.button("&Finish").click();
        fBot.waitUntil(Conditions.shellCloses(activeShell));
        return archiveDestinationPath;
    }

    private void testViews(TmfEventsEditor editor) {
        testHistogramView(getViewPart("Histogram"), editor);
        testPropertyView(getViewPart("Properties"));
        testStatisticsView(getViewPart("Statistics"));
    }

    private static void openImportWizard() {
        fWizard = new ImportTraceWizard();

        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                final IWorkbench workbench = PlatformUI.getWorkbench();
                // Fire the Import Trace Wizard
                if (workbench != null) {
                    final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                    Shell shell = activeWorkbenchWindow.getShell();
                    assertNotNull(shell);
                    ((ImportTraceWizard) fWizard).init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
                    WizardDialog dialog = new WizardDialog(shell, fWizard);
                    dialog.open();
                }
            }
        });

        fBot.waitUntil(ConditionHelpers.isWizardReady(fWizard));
    }



    private static void selectFolder(String... treePath) {
        SWTBotImportWizardUtils.selectFolder(fBot, true, treePath);
    }

    private static void checkOptions(int optionFlags, String expectedSourceLocation, IPath expectedElementPath) throws CoreException {
        checkOptions(optionFlags, expectedSourceLocation, expectedElementPath, null, null);
    }

    private static void checkOptions(int optionFlags, String expectedSourceLocation, IPath expectedElementPath, String experimentName, IPath expectedElementPathRenamed) throws CoreException {
        IProject project = getProjectResource();
        assertTrue(project.exists());
        TmfProjectElement tmfProject = TmfProjectRegistry.getProject(project, true);
        assertNotNull(tmfProject);
        TmfTraceFolder tracesFolder = tmfProject.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertFalse(traces.isEmpty());
        Collections.sort(traces, new Comparator<TmfTraceElement>() {
            @Override
            public int compare(TmfTraceElement arg0, TmfTraceElement arg1) {
                return arg0.getElementPath().compareTo(arg1.getElementPath());
            }
        });

        TmfTraceElement tmfTraceElement = traces.get(0);
        IResource traceResource = tmfTraceElement.getResource();

        assertEquals((optionFlags & ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE) != 0, traceResource.isLinked());

        // i.e. /Tracing/Traces
        IPath expectedPath = Path.ROOT.append(new Path(TRACE_PROJECT_NAME)).append(TmfTracesFolder.TRACES_FOLDER_NAME).append(expectedElementPath);
        assertEquals(expectedPath, traceResource.getFullPath());

        if (expectedElementPathRenamed != null) {
            IPath expectedPathRenamed = Path.ROOT.append(new Path(TRACE_PROJECT_NAME)).append(TmfTracesFolder.TRACES_FOLDER_NAME).append(expectedElementPathRenamed);
            IResource traceResourceRenamed = traces.get(1).getResource();
            assertEquals(expectedPathRenamed, traceResourceRenamed.getFullPath());
        }

        String sourceLocation = traceResource.getPersistentProperty(TmfCommonConstants.SOURCE_LOCATION);
        assertNotNull(sourceLocation);
        assertEquals(expectedSourceLocation, sourceLocation);

        TmfExperimentFolder expFolder = tmfProject.getExperimentsFolder();
        assertNotNull(expFolder);
        if ((optionFlags & ImportTraceWizardPage.OPTION_CREATE_EXPERIMENT) != 0) {
            if (experimentName != null) {
                TmfExperimentElement expElement = expFolder.getExperiment(experimentName);
                assertNotNull(expElement);
                assertEquals(2, expElement.getTraces().size());
            }
        } else {
            assertTrue(expFolder.getExperiments().size() == 0);
        }
    }

    private static IProject getProjectResource() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(TRACE_PROJECT_NAME);
    }

    private @NonNull static String verifyExperimentNameHandling(String aExperimentName) {
        String experimentName = aExperimentName;

        // experiment already exists
        SWTBotImportWizardUtils.checkFinishButton(fBot, false);

        SWTBotText expText = fBot.textInGroup("Options");

        // Invalid experiment name (only whitespaces)
        expText.setText(String.valueOf(' '));
        SWTBotImportWizardUtils.checkFinishButton(fBot, false);

        // Invalid experiment name
        expText.setText(String.valueOf('/'));
        SWTBotImportWizardUtils.checkFinishButton(fBot, false);

        // Set valid experiment name
        experimentName += '_';
        expText.setText(experimentName);
        return experimentName;
    }

}
