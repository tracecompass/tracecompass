/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Class implementation contains the controls that allows to create or update a find or filter Criteria.
 *
 * @version 1.0
 * @author sveyrier
 */
public class TabContents extends Composite {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * A graph node type listener implementation.
     */
    protected GraphNodeTypeListener fGraphNodeTypeListener = null;
    /**
     * A expression listener implementation.
     */
    protected ExpressionListener fExpressionListener = null;
    /**
     * The button for lifelines.
     */
    protected Button fLifelineButton = null;
    /**
     * The button for stops.
     */
    protected Button fStopButton = null;
    /**
     * The button for synchronous messages
     */
    protected Button fSynMessageButton = null;
    /**
     * The button for synchronous return messages
     */
    protected Button fSynMessageReturnButton = null;
    /**
     * The button for asynchronous messages
     */
    protected Button fAsynMessageButton = null;
    /**
     * The button for asynchronous return messages
     */
    protected Button fAsynMessageReturnButton = null;
    /**
     * The search text combo box.
     */
    protected Combo fSearchText = null;
    /**
     * The group for selection kind.
     */
    protected Group fKindSelection = null;
    /**
     * The button for case sensitive expressions.
     */
    protected Button fCaseSensitive = null;
    /**
     * The label for the result string.
     */
    protected Label fResult = null;
    /**
     * The button for notifying parent about valid data.
     */
    protected Button fParentOkButton = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates the dialog contents
     *
     * @param parent the parent widget
     * @param provider the provider which handle the action
     * @param okButton of the dialog (to be enabled/disabled)
     * @param expressionList list of strings already searched for
     */
    public TabContents(Composite parent, ISDGraphNodeSupporter provider, Button okButton, String[] expressionList) {
        super(parent, SWT.NONE);
        fParentOkButton = okButton;
        setLayout(new GridLayout());

        fGraphNodeTypeListener = new GraphNodeTypeListener();
        fExpressionListener = new ExpressionListener();

        // Inform the user how to fill the string to search
        Label searchTitle = new Label(this, SWT.LEFT);
        searchTitle.setText(SDMessages._26);
        Composite searchPart = new Composite(this, SWT.NONE);
        GridData searchPartData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        GridLayout searchPartLayout = new GridLayout();
        searchPartLayout.numColumns = 2;
        searchPart.setLayout(searchPartLayout);
        searchPart.setLayoutData(searchPartData);

        // Create the user string input area
        fSearchText = new Combo(searchPart, SWT.DROP_DOWN);
        GridData comboData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        /*
         * GridData tabLayoutData2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL| GridData.VERTICAL_ALIGN_FILL);
         */
        fSearchText.setLayoutData(comboData);
        if (expressionList != null) {
            for (int i = 0; i < expressionList.length; i++) {
                fSearchText.add(expressionList[i]);
            }
        }
        fSearchText.addModifyListener(fExpressionListener);

        // Create the case sensitive check button
        fCaseSensitive = new Button(searchPart, SWT.CHECK);
        fCaseSensitive.setText(SDMessages._27);

        // Create the group for searched graph node kind selection
        fKindSelection = new Group(this, SWT.SHADOW_NONE);
        fKindSelection.setText(SDMessages._25);
        // kindSelection.setLayoutData(tabLayoutData2);
        GridLayout kindSelectionLayout = new GridLayout();
        kindSelectionLayout.numColumns = 1;
        fKindSelection.setLayout(kindSelectionLayout);
        GridData kindSelectionData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        fKindSelection.setLayoutData(kindSelectionData);

        // Create the lifeline check button
        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.LIFELINE)) {
            fLifelineButton = new Button(fKindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.LIFELINE, null);
            if (nodeName != null) {
                fLifelineButton.setText(nodeName);
            } else {
                fLifelineButton.setText(SDMessages._28);
            }
            fLifelineButton.setEnabled(true);
            fLifelineButton.addSelectionListener(fGraphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.STOP)) {
            // Create the stop check button
            fStopButton = new Button(fKindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.STOP, null);
            if (nodeName != null) {
                fStopButton.setText(nodeName);
            } else {
                fStopButton.setText(SDMessages._29);
            }

            fStopButton.setEnabled(true);
            fStopButton.addSelectionListener(fGraphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGE)) {
            // Create the synchronous message check button
            fSynMessageButton = new Button(fKindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.SYNCMESSAGE, null);
            if (nodeName != null) {
                fSynMessageButton.setText(nodeName);
            } else {
                fSynMessageButton.setText(SDMessages._30);
            }
            fSynMessageButton.setEnabled(true);
            fSynMessageButton.addSelectionListener(fGraphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGERETURN)) {
            // Create the synchronous message return check button
            fSynMessageReturnButton = new Button(fKindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.SYNCMESSAGERETURN, null);
            if (nodeName != null) {
                fSynMessageReturnButton.setText(nodeName);
            } else {
                fSynMessageReturnButton.setText(SDMessages._31);
            }
            fSynMessageReturnButton.setEnabled(true);
            fSynMessageReturnButton.addSelectionListener(fGraphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGE)) {
            // Create the asynchronous message check button
            fAsynMessageButton = new Button(fKindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.ASYNCMESSAGE, null);
            if (nodeName != null) {
                fAsynMessageButton.setText(nodeName);
            } else {
                fAsynMessageButton.setText(SDMessages._32);
            }
            fAsynMessageButton.setEnabled(true);
            fAsynMessageButton.addSelectionListener(fGraphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGERETURN)) {
            // Create the asynchronous message return check button
            fAsynMessageReturnButton = new Button(fKindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.ASYNCMESSAGERETURN, null);
            if (nodeName != null) {
                fAsynMessageReturnButton.setText(nodeName);
            } else {
                fAsynMessageReturnButton.setText(SDMessages._33);
            }
            fAsynMessageReturnButton.setEnabled(true);
            fAsynMessageReturnButton.addSelectionListener(fGraphNodeTypeListener);
        }

        fResult = new Label(this, SWT.LEFT);
        fResult.setText(SDMessages._23);
        fResult.setVisible(false);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Set result text visibility
     * @param found <code>true</code> for found (enable visibility) else false
     */
    public void setResult(boolean found) {
        fResult.setVisible(!found);
    }

    /**
	 * Updates parent OK button based on input data.
	 */
    public void updateOkButton() {
        if (fParentOkButton == null) {
            return;
        }
        boolean enabled = (fSearchText.getText() != null && !fSearchText.getText().equals("")) && //$NON-NLS-1$
                (isLifelineButtonSelected() || isStopButtonSelected() || isSynMessageButtonSelected() || isSynMessageReturnButtonSelected() || isAsynMessageButtonSelected() || isAsynMessageReturnButtonSelected());
        fParentOkButton.setEnabled(enabled);
    }

    /**
     * Sets the parent OK button reference.
     *
     * @param okButton The parent OK button
     */
    public void setOkButton(Button okButton) {
        fParentOkButton = okButton;
    }

    /**
     * Returns the asynchronous message check button state
     *
     * @return true if check, false otherwise
     */
    public boolean isAsynMessageButtonSelected() {
        if (fAsynMessageButton != null) {
            return fAsynMessageButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the asynchronous message return check button state
     *
     * @return true if check, false otherwise
     */
    public boolean isAsynMessageReturnButtonSelected() {
        if (fAsynMessageReturnButton != null) {
            return fAsynMessageReturnButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the case sensitive check button state
     *
     * @return true if check, false otherwise
     */
    public boolean isCaseSensitiveSelected() {
        if (fCaseSensitive != null) {
            return fCaseSensitive.getSelection();
        }
        return false;
    }

    /**
     * Returns the lifeline check button state
     *
     * @return true if check, false otherwise
     */
    public boolean isLifelineButtonSelected() {
        if (fLifelineButton != null) {
            return fLifelineButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the user input string
     *
     * @return the string to search for
     */
    public String getSearchText() {
        return fSearchText.getText();
    }

    /**
     * Returns the stop check button state
     *
     * @return true if check, false otherwise
     */
    public boolean isStopButtonSelected() {
        if (fStopButton != null) {
            return fStopButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the synchronous message check button state
     *
     * @return true if check, false otherwise
     */
    public boolean isSynMessageButtonSelected() {
        if (fSynMessageButton != null) {
            return fSynMessageButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the synchronous message return check button state
     *
     * @return true if check, false otherwise
     */
    public boolean isSynMessageReturnButtonSelected() {
        if (fSynMessageReturnButton != null) {
            return fSynMessageReturnButton.getSelection();
        }
        return false;
    }

    /**
     * Set the asynchronous message check button state
     *
     * @param state
     *            The new state to set
     */
    public void setAsynMessageButtonSelection(boolean state) {
        if (fAsynMessageButton != null) {
            fAsynMessageButton.setSelection(state);
        }
    }

    /**
     * Set the asynchronous message return check button state
     *
     * @param state
     *            The new state to set
     */
    public void setAsynMessageReturnButtonSelection(boolean state) {
        if (fAsynMessageReturnButton != null) {
            fAsynMessageReturnButton.setSelection(state);
        }
    }

    /**
     * Set the case sensitive check button state
     *
     * @param state
     *            The new state to set
     */
    public void setCaseSensitiveSelection(boolean state) {
        if (fCaseSensitive != null) {
            fCaseSensitive.setSelection(state);
        }
    }

    /**
     * Set the lifeline check button state
     *
     * @param state
     *            The new state to set
     */
    public void setLifelineButtonSelection(boolean state) {
        if (fLifelineButton != null) {
            fLifelineButton.setSelection(state);
        }
    }

    /**
     * Set the user input string
     *
     * @param text
     *            The search text
     */
    public void setSearchText(String text) {
        fSearchText.setText(text);
    }

    /**
     * Set the stop check button state
     *
     * @param state
     *            The new state to set
     */
    public void setStopButtonSelection(boolean state) {
        if (fStopButton != null) {
            fStopButton.setSelection(state);
        }
    }

    /**
     * Set the synchronous message check button state
     *
     * @param state
     *            The new state to set
     */
    public void setSynMessageButtonSelection(boolean state) {
        if (fSynMessageButton != null) {
            fSynMessageButton.setSelection(state);
        }
    }

    /**
     * Set the synchronous message return check button state
     *
     * @param state
     *            The new state to set
     */
    public void setSynMessageReturnButtonSelection(boolean state) {
        if (fSynMessageReturnButton != null) {
            fSynMessageReturnButton.setSelection(state);
        }
    }

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    /**
     * Selection listener implementation for graph node types.
     * @version 1.0
     */
    protected class GraphNodeTypeListener implements SelectionListener {
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // Nothing to do
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            updateOkButton();
        }
    }

    /**
     * Modify listener implementation for the expression field.
     *
     * @version 1.0
     */
    protected class ExpressionListener implements ModifyListener {
        @Override
        public void modifyText(ModifyEvent e) {
            updateOkButton();
        }
    }

}
