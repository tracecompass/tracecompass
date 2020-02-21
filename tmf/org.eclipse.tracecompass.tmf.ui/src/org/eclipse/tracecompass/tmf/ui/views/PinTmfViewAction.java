/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.views;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 *
 * @author Bernd Hufmann
 */
public class PinTmfViewAction extends Action {

    private static final ImageDescriptor PIN_VIEW = Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PIN_VIEW);
    private static final ImageDescriptor UNPINNED_VIEW = Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_UNPINNED_VIEW);
    private final ITmfPinnable fPinnable;
    private ITmfTrace fPinnedTrace = null;

    /**
     * Creates a new <code>PinTmfViewAction</code> as a check box. The action is
     * controlled through the {@link IAction#CHECKED} property.
     */
    public PinTmfViewAction() {
        super(Messages.TmfView_PinActionNameText, IAction.AS_CHECK_BOX);
        fPinnable = null;

        setId("org.eclipse.linuxtools.tmf.ui.views.PinTmfViewAction"); //$NON-NLS-1$
        setImageDescriptor(PIN_VIEW);
    }

    /**
     * Creates a new <code>PinTmfViewAction</code> as a drop-down. The action is
     * controlled through the @link {@link ITmfPinnable} interface.
     *
     * @param pinnable
     *            the pinnable view
     * @since 3.2
     */
    public PinTmfViewAction(ITmfPinnable pinnable) {
        super(Messages.TmfView_PinActionNameText, IAction.AS_DROP_DOWN_MENU);
        fPinnable = pinnable;

        setId("org.eclipse.linuxtools.tmf.ui.views.PinTmfViewAction"); //$NON-NLS-1$
        setImageDescriptor(UNPINNED_VIEW);

        setMenuCreator(new IMenuCreator() {
            Menu menu = null;

            @Override
            public void dispose() {
                if (menu != null) {
                    menu.dispose();
                    menu = null;
                }
            }

            @Override
            public Menu getMenu(Control parent) {
                if (menu != null) {
                    menu.dispose();
                }
                menu = new Menu(parent);
                Set<@NonNull ITmfTrace> openedTraces = TmfTraceManager.getInstance().getOpenedTraces();
                for (ITmfTrace trace : openedTraces) {
                    final Action action = new Action(NLS.bind(Messages.TmfView_PinToActionText, TmfTraceManager.getInstance().getTraceUniqueName(trace)), IAction.AS_RADIO_BUTTON) {
                        @Override
                        public void runWithEvent(Event event) {
                            if (isChecked()) {
                                fPinnable.setPinned(trace);
                                setPinnedTrace(trace);
                            }
                        }
                    };
                    action.setChecked(trace.equals(fPinnedTrace));
                    new ActionContributionItem(action).fill(menu, -1);
                }
                return menu;
            }

            @Override
            public Menu getMenu(Menu parent) {
                return null;
            }
        });
    }

    @Override
    public void run() {
        if (fPinnable == null) {
            return;
        }
        if (fPinnedTrace == null) {
            ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
            if (trace != null) {
                fPinnable.setPinned(trace);
                setPinnedTrace(trace);
            }
        } else {
            fPinnable.setPinned(null);
            setPinnedTrace(null);
        }
    }

    /**
     * Returns the pin state.
     *
     * @return true if the action is pinned, false otherwise.
     * @since 3.2
     */
    public boolean isPinned() {
        if (fPinnable == null) {
            return super.isChecked();
        }
        return fPinnedTrace != null;
    }

    /**
     * Sets the pinned trace.
     *
     * @param trace the pinned trace, or null to unpin
     * @since 3.2
     */
    public void setPinnedTrace(ITmfTrace trace) {
        if (fPinnable == null) {
            setChecked(trace != null);
            return;
        }
        fPinnedTrace = trace;
        if (trace == null) {
            setText(Messages.TmfView_PinActionNameText);
            setImageDescriptor(UNPINNED_VIEW);
        } else {
            setText(Messages.TmfView_UnpinActionText);
            setImageDescriptor(PIN_VIEW);
        }
    }
}
