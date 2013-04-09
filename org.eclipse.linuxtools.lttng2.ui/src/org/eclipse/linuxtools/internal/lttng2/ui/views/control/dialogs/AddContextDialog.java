/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * Dialog box for collecting information about contexts to be added to channels/events.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class AddContextDialog extends Dialog implements IAddContextDialog  {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The icon file for this dialog box.
     */
    public static final String ADD_CONTEXT_ICON_FILE = "icons/elcl16/add-context.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The dialog composite.
     */
    private Composite fDialogComposite;

    /**
     * A tree viewer for displaying and selection of available contexts.
     */
    private CheckboxTreeViewer fContextsViewer;

    /**
     * A Tree model for the checkbox tree viewer.
     */
    private final ContextModel fContextModel = new ContextModel();

    /**
     * The contexts to add.
     */
    private final List<String> fSelectedContexts = new ArrayList<String>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public AddContextDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public void setAvalibleContexts(List<String> contexts) {
        fContextModel.setAvalibleContexts(contexts);
    }

    @Override
    public List<String> getContexts() {
        List<String> ret = new ArrayList<String>();
        ret.addAll(fSelectedContexts);
        return ret;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_AddContextDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(ADD_CONTEXT_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Contexts list
        Group contextGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        contextGroup.setText(Messages.TraceControl_AddContextAvailableContextsLabel);
        layout = new GridLayout(1, true);
        contextGroup.setLayout(layout);
        contextGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        fContextsViewer = new CheckboxTreeViewer(contextGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        fContextsViewer.getTree().setToolTipText(Messages.TraceControl_AddContextAvailableContextsTooltip);

        fContextsViewer.setContentProvider(new ContextsContentProvider());
        fContextsViewer.setLabelProvider(new ContextsLabelProvider());
        fContextsViewer.addCheckStateListener(new ContextCheckListener());
        fContextsViewer.setInput(fContextModel);
        fContextsViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

        getShell().setMinimumSize(new Point(500, 450));

        return fDialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        fSelectedContexts.clear();

        Object[] checkedElements = fContextsViewer.getCheckedElements();
        for (int i = 0; i < checkedElements.length; i++) {
            IContextModelComponent component = (IContextModelComponent)checkedElements[i];
            if (!Messages.TraceControl_AddContextAllLabel.equals(component.getName())) {
                fSelectedContexts.add(component.getName());
            }
        }

        // validation successful -> call super.okPressed()
        super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Helper classes and methods
    // ------------------------------------------------------------------------
    /**
     * Content provider for the contexts tree
     */
    final public static class ContextsContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IContextModelComponent) {
                return ((IContextModelComponent)parentElement).getChildren();
            }
            return null;
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof IContextModelComponent) {
                return ((IContextModelComponent)element).getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof IContextModelComponent) {
                return ((IContextModelComponent)element).hasChildren();
            }
            return false;
        }
    }

    /**
     * Label provider for the contexts tree
     */
    final public static class ContextsLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(Object element) {

            if ((element != null) && (element instanceof IContextModelComponent)) {
                return ((IContextModelComponent)element).getName();
            }

            return "";//$NON-NLS-1$
        }
    }

    /**
     * Check state listener for the contexts tree.
     */
    final public class ContextCheckListener implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
          if (event.getChecked()) {
              if (event.getElement() instanceof AllContexts) {
                  fContextsViewer.setSubtreeChecked(event.getElement(), true);
              }
          } else {
              if (event.getElement() instanceof AllContexts) {
                  fContextsViewer.setSubtreeChecked(event.getElement(), false);
              } else {
                  IContextModelComponent component = (IContextModelComponent) event.getElement();
                  fContextsViewer.setChecked(component.getParent(), false);
              }
          }
        }
    }

    /**
     * Model for the context tree viewer (root component)
     */
    public static class ContextModel implements IContextModelComponent {

        private final AllContexts fAllContexts;

        /**
         * Constructor
         */
        public ContextModel() {
            fAllContexts = new AllContexts(this);
        }

        /**
         * Sets the available contexts
         *
         * @param contexts
         *            The contexts to set
         */
        public void setAvalibleContexts(List<String> contexts) {
            fAllContexts.setAvalibleContexts(contexts);
        }

        @Override
        public String getName() {
            return "root"; //$NON-NLS-1$
        }

        @Override
        public Object getParent() {
            return null;
        }

        @Override
        public Object[] getChildren() {
            Object[] ret = new Object[1];
            ret[0] = fAllContexts;
            return ret;
        }

        @Override
        public boolean hasChildren() {
            return true;
        }
    }

    /**
     * Model element (to select/deselect) all contexts) for the context tree viewer
     */
    public static class AllContexts implements IContextModelComponent {
        /**
         * The available list of contexts.
         */
        private List<Context> fAvailableContexts;

        private final IContextModelComponent fParent;

        /**
         * Constructor
         *
         * @param parent
         *            The parent component
         */
        public AllContexts(IContextModelComponent parent) {
            fParent = parent;
        }

        /**
         * Sets the available contexts
         *
         * @param contexts
         *            The contexts to set
         */
        public void setAvalibleContexts(List<String> contexts) {
            fAvailableContexts = new ArrayList<Context>();
            if (contexts != null) {
                for (Iterator<String> iterator = contexts.iterator(); iterator.hasNext();) {
                    String name = iterator.next();
                    fAvailableContexts.add(new Context(this, name));
                }
            }
        }

        @Override
        public String getName() {
            return Messages.TraceControl_AddContextAllLabel;
        }

        @Override
        public Object[] getChildren() {
            return fAvailableContexts.toArray();
        }

        @Override
        public Object getParent() {
            return fParent;
        }

        @Override
        public boolean hasChildren() {
            return true;
        }
    }

    /**
     * Model element (the context) for the context tree viewer
     */
    public static class Context implements IContextModelComponent {

        private final String fContextName;
        private final IContextModelComponent fParent;

        /**
         * Constructor
         *
         * @param parent
         *            The parent component
         * @param name
         *            The name of this context
         */
        public Context(IContextModelComponent parent, String name) {
            fParent = parent;
            fContextName = name;
        }

        @Override
        public String getName() {
            return fContextName;
        }

        @Override
        public Object getParent() {
            return fParent;
        }

        @Override
        public Object[] getChildren() {
            return null;
        }

        @Override
        public boolean hasChildren() {
            return false;
        }
    }

    /**
     * Interface for the tree model used for the context tree viewer.
     */
    public interface IContextModelComponent {

        /**
         * @return The name of this component
         */
        public String getName();

        /**
         * @return The parent component
         */
        public Object getParent();

        /**
         * @return The array of children of this component
         */
        public Object[] getChildren();

        /**
         * @return If this component has children or not
         */
        public boolean hasChildren();
    }
}
