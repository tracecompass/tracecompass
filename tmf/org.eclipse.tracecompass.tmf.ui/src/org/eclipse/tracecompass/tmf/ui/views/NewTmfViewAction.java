/**********************************************************************
 * Copyright (c) 2016, 2017 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.project.model.TmfEditorLinkHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper.OpenProjectElementJob;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Action to instantiate a new instance of views that support it.
 * @author Jonathan Rajotte Julien
 * @since 3.2
 */
public class NewTmfViewAction extends Action {

    private final @NonNull String fViewId;
    private final @Nullable ITmfTrace fPinnedTrace;
    private final boolean fNewInstance;

    /**
     * Creates an action that opens a new view.
     *
     * @param view
     *            The view for which the action is created
     */
    public NewTmfViewAction(TmfView view) {
        super(MessageFormat.format(Messages.TmfView_NewViewActionText, view.getTitle()));
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEW_VIEW));
        fViewId = view.getViewId();
        fPinnedTrace = null;
        fNewInstance = false;
    }

    /**
     * Creates an action that opens a new pinnable view, optionally pinned.
     *
     * @param view
     *            The view for which the action is created
     * @param pinnedTrace
     *            The trace to which the view should be pinned, or null for unpinned
     */
    public NewTmfViewAction(TmfView view, ITmfTrace pinnedTrace) {
        super();
        if (pinnedTrace == null) {
            setText(Messages.TmfView_NewViewActionUnpinnedText);
            setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_UNPINNED_VIEW));
        } else {
            setText(MessageFormat.format(Messages.TmfView_NewViewActionPinnedText, TmfTraceManager.getInstance().getTraceUniqueName(pinnedTrace)));
            setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PIN_VIEW));
        }
        fViewId = view.getViewId();
        fPinnedTrace = pinnedTrace;
        fNewInstance = false;
    }

    /**
     * Creates an action that opens a new pinned view, optionally on a new instance
     * of the specified trace.
     *
     * @param view
     *            The view for which the action is created
     * @param pinnedTrace
     *            The trace to which the view should be pinned
     * @param newInstance
     *            true if a new instance of the pinned trace should be used
     */
    public NewTmfViewAction(TmfView view, @NonNull ITmfTrace pinnedTrace, boolean newInstance) {
        super(MessageFormat.format(Messages.TmfView_NewViewActionPinnedNewInstanceText, pinnedTrace.getName()));
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PIN_VIEW));
        fViewId = view.getViewId();
        fPinnedTrace = pinnedTrace;
        fNewInstance = newInstance;
    }

    @Override
    public void run() {
        ITmfTrace pinnedTrace = fPinnedTrace;
        if (fNewInstance) {
            if (pinnedTrace == null) {
                return;
            }
            /* We need to wait for the trace to be opened before opening the view */
            IFile file = TmfTraceManager.getInstance().getTraceContext(pinnedTrace).getEditorFile();
            Object element = new TmfEditorLinkHelper().findSelection(new FileEditorInput(file)).getFirstElement();
            if (element instanceof TmfCommonProjectElement) {
                OpenProjectElementJob openJob = new OpenProjectElementJob((TmfCommonProjectElement) element, file);
                openJob.schedule();
                Thread waitThread = new Thread() {
                    @Override
                    public void run() {
                        while (!TmfTraceManager.getInstance().getOpenedTraces().contains(openJob.getTrace())) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        Display.getDefault().asyncExec(() -> {
                            IViewPart view = TmfViewFactory.newView(fViewId, true);
                            if (view instanceof ITmfPinnable) {
                                ((ITmfPinnable) view).setPinned(openJob.getTrace());
                            }
                        });
                    }
                };
                waitThread.start();
            }
            return;
        }
        IViewPart view = TmfViewFactory.newView(fViewId, true);
        if (pinnedTrace != null && view instanceof ITmfPinnable) {
            ((ITmfPinnable) view).setPinned(pinnedTrace);
        }
    }
}
