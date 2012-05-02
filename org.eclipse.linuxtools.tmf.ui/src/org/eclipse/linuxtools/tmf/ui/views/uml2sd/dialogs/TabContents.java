/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
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
    GraphNodeTypeListener graphNodeTypeListener = null;
    /**
     * A expression listener implementation.
     */
    ExpressionListener expressionListener = null;
    /**
     * The button for lifelines.
     */
    Button lifelineButton = null;
    /**
     * The button for stops.
     */
    Button stopButton = null;
    /**
     * The button for synchronous messages
     */
    Button synMessageButton = null;
    /**
     * The button for synchronous return messages
     */
    Button synMessageReturnButton = null;
    /**
     * The button for asynchronous messages
     */
    Button asynMessageButton = null;
    /**
     * The button for asynchronous return messages
     */
    Button asynMessageReturnButton = null;
    /**
     * The search text combo box.
     */
    Combo searchText = null;
    /**
     * The group for selection kind.
     */
    Group kindSelection = null;
    /**
     * The button for case sensitive expressions.
     */
    Button caseSensitive = null;
    /**
     * The label for the result string.
     */
    Label result = null;
    /**
     * The button for notifying parent about valid data.
     */
    Button parentOkButton = null;

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
        setOkButton(okButton);
        setLayout(new GridLayout());

        graphNodeTypeListener = new GraphNodeTypeListener();
        expressionListener = new ExpressionListener();

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
        searchText = new Combo(searchPart, SWT.DROP_DOWN);
        GridData comboData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        /*
         * GridData tabLayoutData2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL| GridData.VERTICAL_ALIGN_FILL);
         */
        searchText.setLayoutData(comboData);
        if (expressionList != null) {
            for (int i = 0; i < expressionList.length; i++) {
                searchText.add(expressionList[i]);
            }
        }
        searchText.addModifyListener(expressionListener);

        // Create the case sensitive check button
        caseSensitive = new Button(searchPart, SWT.CHECK);
        caseSensitive.setText(SDMessages._27);

        // Create the group for searched graph node kind selection
        kindSelection = new Group(this, SWT.SHADOW_NONE);
        kindSelection.setText(SDMessages._25);
        // kindSelection.setLayoutData(tabLayoutData2);
        GridLayout kindSelectionLayout = new GridLayout();
        kindSelectionLayout.numColumns = 1;
        kindSelection.setLayout(kindSelectionLayout);
        GridData kindSelectionData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        kindSelection.setLayoutData(kindSelectionData);

        // Create the lifeline check button
        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.LIFELINE)) {
            lifelineButton = new Button(kindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.LIFELINE, null);
            if (nodeName != null) {
                lifelineButton.setText(nodeName);
            } else {
                lifelineButton.setText(SDMessages._28);
            }
            lifelineButton.setEnabled(true);
            lifelineButton.addSelectionListener(graphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.STOP)) {
            // Create the stop check button
            stopButton = new Button(kindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.STOP, null);
            if (nodeName != null) {
                stopButton.setText(nodeName);
            } else {
                stopButton.setText(SDMessages._29);
            }
            
            stopButton.setEnabled(true);
            stopButton.addSelectionListener(graphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGE)) {
            // Create the synchronous message check button
            synMessageButton = new Button(kindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.SYNCMESSAGE, null);
            if (nodeName != null) {
                synMessageButton.setText(nodeName);
            } else {
                synMessageButton.setText(SDMessages._30);
            }
            synMessageButton.setEnabled(true);
            synMessageButton.addSelectionListener(graphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGERETURN)) {
            // Create the synchronous message return check button
            synMessageReturnButton = new Button(kindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.SYNCMESSAGERETURN, null);
            if (nodeName != null) {
                synMessageReturnButton.setText(nodeName);
            } else {
                synMessageReturnButton.setText(SDMessages._31);
            }
            synMessageReturnButton.setEnabled(true);
            synMessageReturnButton.addSelectionListener(graphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGE)) {
            // Create the asynchronous message check button
            asynMessageButton = new Button(kindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.ASYNCMESSAGE, null);
            if (nodeName != null) {
                asynMessageButton.setText(nodeName);
            } else {
                asynMessageButton.setText(SDMessages._32);
            }
            asynMessageButton.setEnabled(true);
            asynMessageButton.addSelectionListener(graphNodeTypeListener);
        }

        if (provider != null && provider.isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGERETURN)) {
            // Create the asynchronous message return check button
            asynMessageReturnButton = new Button(kindSelection, SWT.CHECK);
            String nodeName = provider.getNodeName(ISDGraphNodeSupporter.ASYNCMESSAGERETURN, null);
            if (nodeName != null) {
                asynMessageReturnButton.setText(nodeName);
            } else {
                asynMessageReturnButton.setText(SDMessages._33);
            }
            asynMessageReturnButton.setEnabled(true);
            asynMessageReturnButton.addSelectionListener(graphNodeTypeListener);
        }

        result = new Label(this, SWT.LEFT);
        result.setText(SDMessages._23);
        result.setVisible(false);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    
    /**
     * Set result text visibility  
     * @param found <code>true</code> for found (enable visibility) else false
     */
    public void setResult(boolean found) {
        result.setVisible(!found);
    }

    /**
	 * Updates parent OK button based on input data.
	 */
    public void updateOkButton() {
        if (parentOkButton == null) {
            return;
        }
        boolean enabled = (searchText.getText() != null && !searchText.getText().equals("")) && //$NON-NLS-1$
                (getLifelineButtonSelection() || getStopButtonSelection() || getSynMessageButtonSelection() || getSynMessageReturnButtonSelection() || getAsynMessageButtonSelection() || getAsynMessageReturnButtonSelection());
        parentOkButton.setEnabled(enabled);
    }

    /**
     * Sets the parent OK button reference.
     * 
     * @param okButton The parent OK button
     */
    public void setOkButton(Button okButton) {
        parentOkButton = okButton;
    }

    /**
     * Returns the asynchronous message check button state
     * 
     * @return true if check, false otherwise
     */
    public boolean getAsynMessageButtonSelection() {
        if (asynMessageButton != null) {
            return asynMessageButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the asynchronous message return check button state
     * 
     * @return true if check, false otherwise
     */
    public boolean getAsynMessageReturnButtonSelection() {
        if (asynMessageReturnButton != null) {
            return asynMessageReturnButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the case sensitive check button state
     * 
     * @return true if check, false otherwise
     */
    public boolean getCaseSensitiveSelection() {
        if (caseSensitive != null) {
            return caseSensitive.getSelection();
        }
        return false;
    }

    /**
     * Returns the lifeline check button state
     * 
     * @return true if check, false otherwise
     */
    public boolean getLifelineButtonSelection() {
        if (lifelineButton != null) {
            return lifelineButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the user input string
     * 
     * @return the string to search for
     */
    public String getSearchText() {
        return searchText.getText();
    }

    /**
     * Returns the stop check button state
     * 
     * @return true if check, false otherwise
     */
    public boolean getStopButtonSelection() {
        if (stopButton != null) {
            return stopButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the synchronous message check button state
     * 
     * @return true if check, false otherwise
     */
    public boolean getSynMessageButtonSelection() {
        if (synMessageButton != null) {
            return synMessageButton.getSelection();
        }
        return false;
    }

    /**
     * Returns the synchronous message return check button state
     * 
     * @return true if check, false otherwise
     */
    public boolean getSynMessageReturnButtonSelection() {
        if (synMessageReturnButton != null) {
            return synMessageReturnButton.getSelection();
        }
        return false;
    }

    /**
     * Set the asynchronous message check button state
     */
    public void setAsynMessageButtonSelection(boolean state) {
        if (asynMessageButton != null) {
            asynMessageButton.setSelection(state);
        }
    }

    /**
     * Set the asynchronous message return check button state
     */
    public void setAsynMessageReturnButtonSelection(boolean state) {
        if (asynMessageReturnButton != null) {
            asynMessageReturnButton.setSelection(state);
        }
    }

    /**
     * Set the case sensitive check button state
     */
    public void setCaseSensitiveSelection(boolean state) {
        if (caseSensitive != null) {
            caseSensitive.setSelection(state);
        }
    }

    /**
     * Set the lifeline check button state
     */
    public void setLifelineButtonSelection(boolean state) {
        if (lifelineButton != null) {
            lifelineButton.setSelection(state);
        }
    }

    /**
     * Set the user input string
     */
    public void setSearchText(String text) {
        searchText.setText(text);
    }

    /**
     * Set the stop check button state
     */
    public void setStopButtonSelection(boolean state) {
        if (stopButton != null) {
            stopButton.setSelection(state);
        }
    }

    /**
     * Set the synchronous message check button state
     */
    public void setSynMessageButtonSelection(boolean state) {
        if (synMessageButton != null) {
            synMessageButton.setSelection(state);
        }
    }

    /**
     * Set the synchronous message return check button state
     */
    public void setSynMessageReturnButtonSelection(boolean state) {
        if (synMessageReturnButton != null) {
            synMessageReturnButton.setSelection(state);
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
        /*
         * (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // Nothing to do
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
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

        /*
         * (non-Javadoc)
         * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
         */
        @Override
        public void modifyText(ModifyEvent e) {
            updateOkButton();
        }
    }

}
