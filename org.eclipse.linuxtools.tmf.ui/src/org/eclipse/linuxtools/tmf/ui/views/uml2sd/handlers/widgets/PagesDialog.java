/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PagesDialog.java,v 1.4 2008/01/24 02:29:09 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;

/**
 * This is the filters list dialog.<br>
 * It is associated to an SDView and to a ISDFilterProvider.<br>
 */
public class PagesDialog extends Dialog {

    /**
     * viewer and provided are kept here as attributes
     */
    protected ISDAdvancedPagingProvider provider = null;
    protected TextArea currentPage;
    protected Label totalPageComment;

    /**
     * This is a Text Control that accepts only digits and ensures that bounds are respected
     */
    protected static class TextArea {

        protected Text text;
        int min, max;

        public TextArea(Composite parent) {
            text = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
            text.setTextLimit(10);
        }

        public void setValue(int v) {
            int value = Math.max(min, Math.min(max, v));
            text.setText(Integer.toString(value));
        }

        public int getValue() {
            int res;
            try {
                res = Integer.parseInt(text.getText());
            } catch (Exception e) {
                // ignored
                res = 0;
            }
            return Math.max(min, Math.min(max, res));
        }

        public void setBounds(int min_, int max_) {
            min = Math.max(0, min_);
            max = Math.max(min, max_);
            Integer tab[] = new Integer[2];
            tab[0] = Integer.valueOf(min);
            tab[1] = Integer.valueOf(max);
            text.setToolTipText(MessageFormat.format(SDMessages._69, (Object[]) tab));
        }
    }

    /**
	 * 
	 */
    protected void updateComments() {
        int pages = Math.max(0, provider.pagesCount());
        String totalPageCommentText = SDMessages._70 + pages + " "; //$NON-NLS-1$
        if (pages == 0) {
            totalPageCommentText += SDMessages._71;
        } else if (pages == 1) {
            totalPageCommentText += SDMessages._72;
        } else {
            totalPageCommentText += SDMessages._73;
        }
        totalPageComment.setText(totalPageCommentText);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Control createDialogArea(Composite parent) {

        Group ret = new Group(parent, SWT.NONE);
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        ret.setLayoutData(data);
        ret.setText(SDMessages._67);

        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        ret.setLayout(fillLayout);

        Label label = new Label(ret, SWT.NONE);
        label.setText(SDMessages._75);

        currentPage = new TextArea(ret);
        currentPage.setBounds(1, provider.pagesCount());
        currentPage.setValue(provider.currentPage() + 1);

        totalPageComment = new Label(ret, SWT.NONE);
        totalPageComment.setAlignment(SWT.RIGHT);

        updateComments();

        getShell().setText(SDMessages._68);
        return ret;
    }

    /**
     * @param view_
     * @param loader_
     */
    public PagesDialog(IViewPart view_, ISDAdvancedPagingProvider loader_) {
        super(view_.getSite().getShell());
        provider = loader_;
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    /**
     * Called when the dialog box ok button is pressed
     */
    @Override
    public void okPressed() {
        int currentPageValue = currentPage.getValue() - 1;
        super.close();
        provider.pageNumberChanged(currentPageValue);
    }

}
