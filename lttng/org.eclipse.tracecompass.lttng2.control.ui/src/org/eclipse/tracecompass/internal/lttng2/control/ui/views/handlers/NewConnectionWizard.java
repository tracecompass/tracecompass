/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick-Jeffrey Pollo Guilbert - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
//import org.eclipse.swt.custom.CTabFolder;
//import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
//import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.INewConnectionDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.NewConnectionDialog;


public class NewConnectionWizard {

    static final int LABEL_WIDTH_CHARS = 4;
    private NewConnectionDialog fNewConnectionDialog;

    static final int CONNECTIONTREE_HEIGHT_CHARS = 10;
    static final int CONNECTIONTREE_WIDTH_CHARS = 40;

    TreeViewer fConnectionTree = null;
    private CheckboxTreeViewer treeViewer;



    // TODO: refactor name of class and all classes associated with it
    public NewConnectionWizard()
    {

        Shell shell = new Shell();

        shell.setLayout(new GridLayout());
        Composite composite = new Composite(shell, SWT.BORDER);
        Composite compositeDescription = new Composite(shell, SWT.BORDER);
        composite.setBackground(new Color(null, 255,255,255));
        compositeDescription.setBackground(new Color(null, 255,255,255));

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 2;








        Group groupProfile = new Group(composite, SWT.SHADOW_ETCHED_IN);
        groupProfile.setText("Profiles List");


        composite.setLayoutData(gridData);
        composite.setLayout(new GridLayout(1, false));
        groupProfile.setLayoutData(gridData);
        groupProfile.setLayout(new GridLayout());
        groupProfile.setBackground(new Color(null, 255,255,255));




        new FilteredTree(groupProfile, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true){
            @Override
            protected TreeViewer doCreateTreeViewer(Composite aparent, int style) {
                treeViewer = new CheckboxTreeViewer(aparent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
                //TODO: change string to one recorded in Messages class
                treeViewer.getTree().setToolTipText("Select one or multiple profiles from the list below."); //$NON-NLS-1$
                ArrayContentProvider test = new ArrayContentProvider();

                treeViewer.setContentProvider(test);

                test.inputChanged(treeViewer, null, new String[]{"allo", "bye"}); //$NON-NLS-1$ //$NON-NLS-2$

                treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
                return treeViewer;
            }
        };




        // TODO: modify the checkbox addition to be based on availble profiles
        Button b = new Button(groupProfile, SWT.CHECK);
        b.setText("CPU Usage");
        b.setBackground(new Color(null, 255,255,255));

        Button b2 = new Button(groupProfile, SWT.CHECK);
        b2.setText("Memory usage");
        b2.setBackground(new Color(null, 255,255,255));

        Button b3 = new Button(groupProfile, SWT.CHECK);
        b3.setText("Disk Activity");
        b3.setBackground(new Color(null, 255,255,255));


        Group groupDescription = new Group(composite, SWT.SHADOW_ETCHED_IN);
        groupDescription.setText("Description");

        groupDescription.setLayoutData(gridData);
        groupDescription.setLayout(new GridLayout());
        groupDescription.setBackground(new Color(null, 255,255,255));


        groupDescription.pack();

        Label descriptionLabel = new Label(groupDescription, 0);
        descriptionLabel.setText("No description available.");
        descriptionLabel.setBackground(new Color(null, 255,255,255));

        groupProfile.pack();

        shell.open();


    }

    WizardPage wp = new ConnectionPage("Wizard");

    protected void okPressed()
    {

    }

    WizardPage testPage = new WizardPage("Kappa")
    {

        @Override
        public void createControl(Composite parent) {
            // TODO Auto-generated method stub
            Composite canvas = new Composite(parent, SWT.NONE);
            canvas.setLayout(new GridLayout());
            this.setTitle("Session creation");

            setControl(canvas);
            Button b = new Button(canvas, SWT.RADIO);
            b.setText("Local");

        }

    };

    public INewConnectionDialog getfNewConnectionDialog() {
        return fNewConnectionDialog;
    }

    public void setNewConnectionDialog(INewConnectionDialog fNewConnectionDialog) {
        this.fNewConnectionDialog = (NewConnectionDialog) fNewConnectionDialog;
    }

    public CheckboxTreeViewer getTreeViewer() {
        return treeViewer;
    }

    public void setTreeViewer(CheckboxTreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

}
