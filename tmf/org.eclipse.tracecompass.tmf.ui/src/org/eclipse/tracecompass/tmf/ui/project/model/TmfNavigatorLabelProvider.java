/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Add support for unknown trace type icon
 *   Simon Delisle - Move the job in its own class
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * The TMF project label provider for the tree viewer in the project explorer view.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfNavigatorLabelProvider implements ICommonLabelProvider, IStyledLabelProvider {

    private static Queue<TmfTraceElement> boundsToUpdate = new ConcurrentLinkedQueue<>();
    private static UpdateTraceBoundsJob updateBounds = new UpdateTraceBoundsJob(Messages.TmfNavigatorLabelProvider_UpdateBoundsJobName, boundsToUpdate);

    // ------------------------------------------------------------------------
    // ICommonLabelProvider
    // ------------------------------------------------------------------------

    @Override
    public Image getImage(Object element) {
        if (element instanceof ITmfProjectModelElement) {
            return ((ITmfProjectModelElement) element).getIcon();
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ITmfProjectModelElement) {
            return ((ITmfProjectModelElement) element).getLabelText();
        }
        return null;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        // Do nothing
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // Do nothing
    }

    @Override
    public void restoreState(IMemento aMemento) {
        // Do nothing
    }

    @Override
    public void saveState(IMemento aMemento) {
        // Do nothing
    }

    @Override
    public String getDescription(Object anElement) {
        return getText(anElement);
    }

    @Override
    public void init(ICommonContentExtensionSite aConfig) {
        // Do nothing
    }

    @Override
    public StyledString getStyledText(Object element) {
        String text = getText(element);
        StyledString styledString = null;
        if (text != null) {
            if (element instanceof ITmfStyledProjectModelElement) {
                Styler styler = ((ITmfStyledProjectModelElement) element).getStyler();
                if (styler != null) {
                    styledString = new StyledString(text, styler);
                }
            }
            if (styledString == null) {
                styledString = new StyledString(text);
            }
            boolean displayTimeRange = Activator.getDefault().getPreferenceStore().getBoolean(ITmfUIPreferences.TRACE_DISPLAY_RANGE_PROJECTEXPLORER);
            if (displayTimeRange && element instanceof TmfTraceElement) {
                styledString.append(formatTraceRange(((TmfTraceElement) element).getElementUnderTraceFolder()));
            }
        }
        return styledString;
    }

    private static StyledString formatTraceRange(TmfTraceElement traceElement) {
        ITmfTimestamp start = traceElement.getStartTime();
        ITmfTimestamp end = traceElement.getEndTime();

        if (start == null) {
            boundsToUpdate.add(traceElement);
            if (updateBounds.getState() != Job.RUNNING) {
                updateBounds.schedule();
            }
            return new StyledString(" [...]", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
        }

        if (start.equals(TmfTimestamp.BIG_BANG)) {
            /* Not a trace or empty */
            return new StyledString();
        }

        if (end == null || end.equals(TmfTimestamp.BIG_BANG)) {
            return new StyledString(" [" + TmfTimestampFormat.getDefaulTimeFormat().format(start.toNanos()) //$NON-NLS-1$
                    + " - ...]", //$NON-NLS-1$
                    StyledString.DECORATIONS_STYLER);
        }

        return new StyledString(" [" + TmfTimestampFormat.getDefaulTimeFormat().format(start.toNanos()) //$NON-NLS-1$
                + " - " + TmfTimestampFormat.getDefaulTimeFormat().format(end.toNanos()) + "]", //$NON-NLS-1$ //$NON-NLS-2$
                StyledString.DECORATIONS_STYLER);
    }

}
