/*******************************************************************************
 * Copyright (c) 2011 MontaVista Software
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Yufen Kuo (ykuo@mvista.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.project.dialogs;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TraceLibraryPathWizardPage extends WizardPage {
    private static final String LTTVTRACEREAD_LOADER_LIBNAME = "lttvtraceread_loader"; //$NON-NLS-1$
    private Button browsePathButton;
    private Text traceLibraryPath;

    protected TraceLibraryPathWizardPage(String pageName) {
        super(pageName);
    }

    @Override
    public void createControl(Composite parent) {
        Composite client = new Composite(parent, SWT.NONE);
        client.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        client.setLayout(layout);

        Label label = new Label(client, SWT.NONE);
        label.setText(Messages.TraceLibraryPath_label);
        traceLibraryPath = new Text(client, SWT.BORDER);
        traceLibraryPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        traceLibraryPath.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                boolean valid = validatePage();
                setPageComplete(valid);
            }

        });
        browsePathButton = new Button(client, SWT.PUSH);
        browsePathButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        browsePathButton.setText(Messages.TraceLibraryPath_browseBtn);
        browsePathButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String dir = new DirectoryDialog(Display.getDefault()
                        .getActiveShell()).open();
                if (dir != null) {
                    traceLibraryPath.setText(dir);
                }

            }

        });

        Label noLabel = new Label(client, SWT.NONE);
        noLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

        Label descTextLabel = new Label(client, SWT.WRAP);
        descTextLabel.setText(Messages.TraceLibraryPathWizard_Message);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = 400;
        gd.horizontalSpan = 2;
        descTextLabel.setLayoutData(gd);

        Label noteBoldLabel = new Label(client, SWT.BOLD);
        noteBoldLabel.setText(Messages.TraceLibraryPath_Note);
        noteBoldLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        Font font = noteBoldLabel.getFont();
        if (font.getFontData().length > 0)
            noteBoldLabel.setFont(new Font(client.getDisplay(), font
                    .getFontData()[0].getName(), font.getFontData()[0]
                    .getHeight(), SWT.BOLD));

        Label noteTextLabel = new Label(client, SWT.WRAP);
        noteTextLabel.setText(Messages.TraceLibraryPath_Message);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = 400;
        gd.horizontalSpan = 2;
        noteTextLabel.setLayoutData(gd);
        setControl(client);

    }

    public String getPath() {
        if (traceLibraryPath != null && !traceLibraryPath.isDisposed()) {
            String path = traceLibraryPath.getText();
            if (path != null && !path.trim().isEmpty())
                return path;
        }
        return null;
    }

    private boolean validatePage() {
        String path = getPath();
        if (path != null) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                File loaderLib = new File(path,
                        System.mapLibraryName(LTTVTRACEREAD_LOADER_LIBNAME));
                if (!loaderLib.exists()) {
                    setErrorMessage(Messages.TraceLibraryPathWizardPage_TraceLoaderLibrary_notExists);
                    return false;
                }
            } else {
                setErrorMessage(Messages.TraceLibraryPathWizardPage_SpecifiedTraceLibraryLocation_notExists);
                return false;
            }
        }
        setErrorMessage(null);
        return true;

    }

}
