/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the trace package export wizard
 *
 * @author Marc-Andre Laperle
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport.messages"; //$NON-NLS-1$

    /**
     * The message under the select trace wizard page title
     */
    public static String ExportTracePackageSelectTraceWizardPage_ChooseTrace;

    /**
     * The description of the project selection list
     */
    public static String ExportTracePackageSelectTraceWizardPage_ProjectSelection;

    /**
     * The description of the trace selection list
     */
    public static String ExportTracePackageSelectTraceWizardPage_TraceSelection;

    /**
     * Dialog text when target file already exists
     */
    public static String ExportTracePackageWizardPage_AlreadyExitst;

    /**
     * The approximate size label
     */
    public static String ExportTracePackageWizardPage_ApproximateSizeLbl;

    /**
     * The message under the wizard page title
     */
    public static String ExportTracePackageWizardPage_ChooseContent;

    /**
     * Text for the compress contents checkbox
     */
    public static String ExportTracePackageWizardPage_CompressContents;

    /**
     * Text for the first column (content)
     */
    public static String ExportTracePackageWizardPage_ContentColumnName;

    /**
     * Text for the options group
     */
    public static String ExportTracePackageWizardPage_Options;

    /**
     * Text for the tar format option
     */
    public static String ExportTracePackageWizardPage_SaveInTarFormat;

    /**
     * Text for the zip format option
     */
    public static String ExportTracePackageWizardPage_SaveInZipFormat;

    /**
     * Byte units
     */
    public static String ExportTracePackageWizardPage_SizeByte;

    /**
     * Text for the second column (size)
     */
    public static String ExportTracePackageWizardPage_SizeColumnName;

    /**
     * Gigabyte units
     */
    public static String ExportTracePackageWizardPage_SizeGigabyte;

    /**
     * Kilobyte units
     */
    public static String ExportTracePackageWizardPage_SizeKilobyte;

    /**
     * Megabyte units
     */
    public static String ExportTracePackageWizardPage_SizeMegabyte;

    /**
     * Terabyte units
     */
    public static String ExportTracePackageWizardPage_SizeTerabyte;

    /**
     * Title for the wizard page
     */
    public static String ExportTracePackageWizardPage_Title;

    /**
     * Label for the file path
     */
    public static String ExportTracePackageWizardPage_ToArchive;

    /**
     * Dialog text when a trace with the same name already exists
     */
    public static String ImportTracePackageWizardPage_AlreadyExists;

    /**
     * Title for the import page
     */
    public static String ImportTracePackageWizardPage_Title;

    /**
     * Text for the source archive label
     */
    public static String ImportTracePackageWizardPage_FromArchive;

    /**
     * Text for the reading package job
     */
    public static String ImportTracePackageWizardPage_ReadingPackage;

    /**
     * Message when file is not found
     */
    public static String ImportTracePackageWizardPage_ErrorFileNotFound;

    /**
     * Message when trace type could not be set
     */
    public static String ImportTracePackageWizardPage_ErrorSettingTraceType;

    /**
     * Message when the trace could not be found after importing the files
     */
    public static String ImportTracePackageWizardPage_ErrorFindingImportedTrace;

    /**
     * The message displayed under the title
     */
    public static String ImportTracePackageWizardPage_Message;

    /**
     * Generic error message for the import operation
     */
    public static String ImportTracePackageWizardPage_ErrorOperation;

    /**
     * Project text label
     */
    public static String ImportTracePackageWizardPage_Project;

    /**
     * The select project button text
     */
    public static String ImportTracePackageWizardPage_SelectProjectButton;

    /**
     * The select project dialog title
     */
    public static String ImportTracePackageWizardPage_SelectProjectDialogTitle;

    /**
     * Text for the generating package job
     */
    public static String TracePackageExportOperation_GeneratingPackage;

    /**
     * Text when error occurs creating a bookmark
     */
    public static String TracePackageImportOperation_ErrorCreatingBookmark;

    /**
     * Text when error occurs creating a bookmark file
     */
    public static String TracePackageImportOperation_ErrorCreatingBookmarkFile;

    /**
     * Text for the importing package job
     */
    public static String TracePackageImportOperation_ImportingPackage;

    /**
     * Text when error occurs when the manifest is not found in the archive
     */
    public static String TracePackageExtractManifestOperation_ErrorManifestNotFound;

    /**
     * Text when error occurs when the manifest is not valid
     */
    public static String TracePackageExtractManifestOperation_ErrorManifestNotValid;

    /**
     * Generic error message when reading the manifest
     */
    public static String TracePackageExtractManifestOperation_ErrorReadingManifest;

    /**
     * Error message when the file is an invalid format
     */
    public static String TracePackageExtractManifestOperation_InvalidFormat;

    /**
     * Error when the schema file cannot be found to validate the export
     * manifest
     */
    public static String TracePackageExtractManifestOperation_SchemaFileNotFound;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
