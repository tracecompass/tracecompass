/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SearchFilterDialog.java,v 1.4 2008/01/24 02:29:09 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.AsyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.AsyncMessageReturn;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Stop;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessageReturn;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFindProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;

/**
 * This is the common dialog to define Find and/or Filter Criteria(s)
 */
public class SearchFilterDialog extends Dialog {

    protected static final String FIND_CRITERIA = "findCriteria"; //$NON-NLS-1$
    protected static final String FIND_EXPRESSION_LIST = "findExpressionList"; //$NON-NLS-1$
    protected static final String FILTER_CRITERIA = "filterCriteria"; //$NON-NLS-1$
    protected static final String FILTER_EXPRESSION_LIST = "filterExpressionList"; //$NON-NLS-1$
    protected static final int MAX_EXPRESSION_LIST = 7;

    /**
     * viewer
     */
    protected SDView viewer = null;

    /**
     * tab with the controls for a Criteria
     */
    protected TabFolder tab = null;

    /**
     * Criteria updated by this dialog
     */
    protected Criteria criteria = null;

    /**
     * find/filter provider telling which graph nodes are supported
     */
    protected ISDGraphNodeSupporter provider = null;

    /**
     * okText is the text for the Ok button and title is the title of the dialog.<br>
     * Both depend on the usage that is done of this dialog (find or filter).
     */
    protected String okText, title;

    /**
     * List of string expressions that have been searched already
     */
    protected String[] expressionList;

    /**
     * find is true if the dialog is for the find feature and false for filter feature
     */
    protected boolean find;

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Control createDialogArea(Composite arg0) {
        if (find)
            expressionList = TmfUiPlugin.getDefault().getDialogSettings().getArray(FIND_EXPRESSION_LIST);
        else
            expressionList = TmfUiPlugin.getDefault().getDialogSettings().getArray(FILTER_EXPRESSION_LIST);
        if (expressionList == null) {
            expressionList = new String[0];
        }
        return new TabContents(arg0, provider, getButton(IDialogConstants.OK_ID), expressionList);
    }

    /**
     * @param view_
     * @param provider_
     * @param filter_
     * @param style
     */
    public SearchFilterDialog(SDView view_, ISDGraphNodeSupporter provider_, boolean filter_, int style) {
        super(view_.getSDWidget().getShell());
        setShellStyle(SWT.DIALOG_TRIM | style);
        provider = provider_;
        viewer = view_;
        find = !filter_;
    }

    /**
     * Open the dialog box
     */
    @Override
    public int open() {
        create();

        if (criteria == null)
            loadCriteria();
        if (criteria == null) {
            criteria = new Criteria();
            criteria.setLifeLineSelected(provider.isNodeSupported(ISDGraphNodeSupporter.LIFELINE));
            criteria.setSyncMessageSelected(provider.isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGE));
            criteria.setSyncMessageReturnSelected(provider.isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGERETURN));
            criteria.setAsyncMessageSelected(provider.isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGE));
            criteria.setAsyncMessageReturnSelected(provider.isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGERETURN));
            criteria.setStopSelected(provider.isNodeSupported(ISDGraphNodeSupporter.STOP));
        }
        copyFromCriteria(criteria);

        if (okText != null) {
            getButton(IDialogConstants.OK_ID).setText(okText);
        } else {
            getButton(IDialogConstants.OK_ID).setText(SDMessages._21);
        }

        if (find) {
            getButton(IDialogConstants.CANCEL_ID).setText(SDMessages._22);
        }

        Button okButton = getButton(IDialogConstants.OK_ID);
        ((TabContents) getDialogArea()).setOkButton(okButton);
        if (criteria == null || !((criteria.getExpression() != null && !criteria.getExpression().equals("")) && //$NON-NLS-1$
                (criteria.isAsyncMessageReturnSelected() || criteria.isAsyncMessageSelected() || criteria.isLifeLineSelected() || criteria.isStopSelected() || criteria.isSyncMessageReturnSelected() || criteria.isSyncMessageSelected()))) {
            okButton.setEnabled(false);
        }

        if (title != null) {
            getShell().setText(title);
        } else {
            getShell().setText(SDMessages._24);
        }

        getShell().pack();
        getShell().setLocation(getShell().getDisplay().getCursorLocation());

        criteria = null;
        return super.open();
    }

    /**
	 * 
	 */
    @SuppressWarnings("rawtypes")
    protected void loadCriteria() {

        String CRITERIA = FIND_CRITERIA;
        if (!find)
            CRITERIA = FILTER_CRITERIA;

        DialogSettings section = (DialogSettings) TmfUiPlugin.getDefault().getDialogSettings().getSection(CRITERIA);
        List selection = viewer.getSDWidget().getSelection();
        if ((selection == null || selection.size() != 1) || (!find)) {
            if (section != null) {
                criteria = new Criteria();
                criteria.load(section);
            }
        } else {
            GraphNode gn = (GraphNode) selection.get(0);
            criteria = new Criteria();
            criteria.setExpression(gn.getName());
            criteria.setCaseSenstiveSelected(true);
            if (gn instanceof Lifeline && provider.isNodeSupported(ISDGraphNodeSupporter.LIFELINE)) {
                criteria.setLifeLineSelected(true);
            } else if (gn instanceof SyncMessageReturn && provider.isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGERETURN)) {
                criteria.setSyncMessageReturnSelected(true);
            } else if ((gn instanceof SyncMessageReturn || gn instanceof SyncMessage) && provider.isNodeSupported(ISDGraphNodeSupporter.SYNCMESSAGE)) {
                criteria.setSyncMessageSelected(true);
            } else if (gn instanceof AsyncMessageReturn && provider.isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGERETURN)) {
                criteria.setAsyncMessageReturnSelected(true);
            } else if ((gn instanceof AsyncMessageReturn || gn instanceof AsyncMessage) && provider.isNodeSupported(ISDGraphNodeSupporter.ASYNCMESSAGE)) {
                criteria.setAsyncMessageSelected(true);
            } else if (gn instanceof Stop && provider.isNodeSupported(ISDGraphNodeSupporter.STOP)) {
                criteria.setStopSelected(true);
            }
        }
    }

    /**
     * Called when the dialog box ok button is pressed and call back the appropriate action provider (ISDFilterProvider
     * or ISDFindProvider)
     */
    @Override
    public void okPressed() {
        copyToCriteria();
        if (!find) {
            saveCriteria();
            super.close(); // Filter is modal
        }
        if ((provider != null) && (provider instanceof ISDFindProvider) && find) {
            boolean result = ((ISDFindProvider) provider).find(criteria);
            TabContents content = getTabContents();
            content.setResult(result);
        }
    }

    /**
	 *
	 */
    @Override
    public void cancelPressed() {
        if (find) {
            copyToCriteria();
            if (provider instanceof ISDFindProvider) {
                ((ISDFindProvider) provider).cancel();
            }
            saveCriteria();
        }
        super.cancelPressed();
    }

    /**
	 * 
	 */
    public void saveCriteria() {
        String CRITERIA = FIND_CRITERIA;
        String EXPRESSION_LIST = FIND_EXPRESSION_LIST;
        if (!find) {
            CRITERIA = FILTER_CRITERIA;
            EXPRESSION_LIST = FILTER_EXPRESSION_LIST;
        }
        DialogSettings settings = (DialogSettings) TmfUiPlugin.getDefault().getDialogSettings();
        DialogSettings section = (DialogSettings) settings.getSection(CRITERIA);
        if (section == null) {
            section = (DialogSettings) settings.addNewSection(CRITERIA);
        }
        criteria.save(section);

        if (criteria.getExpression().length() > 0) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < expressionList.length; i++) {
                list.add(expressionList[i]);
            }
            // Remove the used expression if one from the dropdown list
            list.remove(criteria.getExpression());
            // Put the new expression at the beginning
            list.add(0, criteria.getExpression());
            // Fill in the expressionList, truncating to MAX_EXPRESSION_LIST
            int size = Math.min(list.size(), MAX_EXPRESSION_LIST);
            String[] temp = new String[size];
            for (int i = 0; i < size; i++) {
                temp[i] = (String) list.get(i);
            }
            expressionList = temp;
            settings.put(EXPRESSION_LIST, expressionList);
        }
    }

    /**
	 *
	 */
    public Criteria getCriteria() {
        return criteria;
    }

    /**
     * @param criteria_
     */
    public void setCriteria(Criteria criteria_) {
        criteria = criteria_;
    }

    /**
     * Get the current end-user settings from the dialog to a Criteria
     */
    public void copyToCriteria() {
        criteria = new Criteria();
        TabContents content = getTabContents();
        criteria.setLifeLineSelected(content.getLifelineButtonSelection());
        criteria.setSyncMessageSelected(content.getSynMessageButtonSelection());
        criteria.setSyncMessageReturnSelected(content.getSynMessageReturnButtonSelection());
        criteria.setAsyncMessageSelected(content.getAsynMessageButtonSelection());
        criteria.setAsyncMessageReturnSelected(content.getAsynMessageReturnButtonSelection());
        criteria.setStopSelected(content.getStopButtonSelection());
        criteria.setCaseSenstiveSelected(content.getCaseSensitiveSelection());
        criteria.setExpression(content.getSearchText());
    }

    /**
     * @return the tab content
     */
    protected TabContents getTabContents() {
        TabContents content = null;
        if (tab == null)
            content = (TabContents) getDialogArea();
        else
            content = (TabContents) tab.getSelection()[0].getControl();
        return content;
    }

    /**
     * Initialize the dialog with the settings of an existing Criteria<br>
     * Criteria must not be null and the TabContents must have been created
     * 
     * @param from
     */
    public void copyFromCriteria(Criteria from) {
        TabContents content = getTabContents();
        content.setLifelineButtonSelection(from.isLifeLineSelected());
        content.setSynMessageButtonSelection(from.isSyncMessageSelected());
        content.setSynMessageReturnButtonSelection(from.isSyncMessageReturnSelected());
        content.setAsynMessageButtonSelection(from.isAsyncMessageSelected());
        content.setAsynMessageReturnButtonSelection(from.isSyncMessageReturnSelected());
        content.setStopButtonSelection(from.isStopSelected());
        content.setCaseSensitiveSelection(from.isCaseSenstiveSelected());
        if (from.getExpression() != null) {
            content.setSearchText(from.getExpression());
        }
    }

    /**
     * @param okText_
     */
    public void setOkText(String okText_) {
        okText = okText_;
    }

    /**
     * @param title_
     */
    public void setTitle(String title_) {
        title = title_;
    }
}
