/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Receives various notifications for realignment and
 * performs the alignment on the appropriate views.
 *
 * @since 1.0
 */
public class TmfAlignmentSynchronizer {

    private static final long THROTTLE_DELAY = 500;
    private static final int NEAR_THRESHOLD = 10;

    /** Singleton instance */
    private static TmfAlignmentSynchronizer fInstance = null;

    private final Timer fTimer;
    private final List<AlignmentOperation> fPendingOperations = Collections.synchronizedList(new ArrayList<AlignmentOperation>());

    private TimerTask fCurrentTask;

    /**
     * Constructor
     */
    private TmfAlignmentSynchronizer() {
        TmfSignalManager.register(this);
        fTimer = new Timer();
        createPreferenceListener();
        fCurrentTask = new TimerTask() {
            @Override
            public void run() {
                /* Do nothing */
            }
        };
    }

    /**
     * Get the alignment synchronizer's instance
     *
     * @return The singleton instance
     */
    public static synchronized TmfAlignmentSynchronizer getInstance() {
        if (fInstance == null) {
            fInstance = new TmfAlignmentSynchronizer();
        }
        return fInstance;
    }

    /**
     * Disposes the alignment synchronizer
     */
    public void dispose() {
        TmfSignalManager.deregister(this);
        synchronized (fPendingOperations) {
            fTimer.cancel();
            fCurrentTask.cancel();
        }
    }

    private IPreferenceChangeListener createPreferenceListener() {
        IPreferenceChangeListener listener = new IPreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent event) {
                if (event.getKey().equals(ITmfUIPreferences.PREF_ALIGN_VIEWS)) {
                    Object oldValue = event.getOldValue();
                    Object newValue = event.getNewValue();
                    if (Boolean.toString(false).equals(oldValue) && Boolean.toString(true).equals(newValue)) {
                        realignViews();
                    } else if (Boolean.toString(true).equals(oldValue) && Boolean.toString(false).equals(newValue)) {
                        restoreViews();
                    }
                }
            }
        };
        InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).addPreferenceChangeListener(listener);
        return listener;
    }

    private class AlignmentOperation {
        final TmfView fView;
        final TmfTimeViewAlignmentInfo fAlignmentInfo;

        public AlignmentOperation(TmfView view, TmfTimeViewAlignmentInfo timeViewAlignmentInfo) {
            fView = view;
            fAlignmentInfo = timeViewAlignmentInfo;
        }
    }

    private class AlignTask extends TimerTask {

        @Override
        public void run() {
            final List<AlignmentOperation> fCopy;
            synchronized (fPendingOperations) {
                fCopy = new ArrayList<>(fPendingOperations);
                fPendingOperations.clear();
            }
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    performAllAlignments(fCopy);
                }
            });
        }
    }

    /**
     * Handle a view that was just resized.
     *
     * @param view
     *            the view that was resized
     */
    public void handleViewResized(TmfView view) {
        if (view.getParentComposite().isDisposed()) {
            return;
        }

        TmfTimeViewAlignmentInfo alignmentInfo = new TmfTimeViewAlignmentInfo(view.getParentComposite().getShell(), getViewLocation(view), 0);

        // Don't use a view that was just resized as a reference view.
        // Otherwise, a view that was just
        // created might use itself as a reference but we want to
        // keep the existing alignment from the other views.
        ITmfTimeAligned referenceView = getReferenceView(alignmentInfo, view);
        if (referenceView != null) {
            queueAlignment(referenceView.getTimeViewAlignmentInfo(), false);
        }
    }

    /**
     * Handle a view that was just closed.
     *
     * @param view
     *            the view that was closed
     */
    public void handleViewClosed(TmfView view) {
        // Realign views so that they can use the maximum available width in the
        // event that a narrow view was just closed
        realignViews(view.getSite().getPage());
    }

    /**
     * Process signal for alignment.
     *
     * @param signal the alignment signal
     */
    @TmfSignalHandler
    public void timeViewAlignmentUpdated(TmfTimeViewAlignmentSignal signal) {
        queueAlignment(signal.getTimeViewAlignmentInfo(), signal.IsSynchronous());
    }

    /**
     * Perform all alignment operations for the specified alignment
     * informations.
     *
     * <pre>
     * - The alignment algorithm chooses the narrowest width to accommodate all views.
     * - View positions are recomputed for extra accuracy since the views could have been moved or resized.
     * - Based on the up-to-date view positions, only views that are near and aligned with each other
     * </pre>
     */
    private static void performAllAlignments(final List<AlignmentOperation> alignments) {
        for (final AlignmentOperation info : alignments) {
            performAlignment(info);
        }
    }

    private static void performAlignment(AlignmentOperation info) {

        TmfView referenceView = info.fView;
        if (isDisposedView(referenceView)) {
            return;
        }

        TmfTimeViewAlignmentInfo alignmentInfo = info.fAlignmentInfo;
        // The location of the view might have changed (resize, etc). Update the alignment info.
        alignmentInfo = new TmfTimeViewAlignmentInfo(alignmentInfo.getShell(), getViewLocation(referenceView), getClampedTimeAxisOffset(alignmentInfo));

        TmfView narrowestView = getNarrowestView(alignmentInfo);
        if (narrowestView == null) {
            // No valid view found for this alignment. This could mean that the views for this alignment are now too narrow (width == 0) or that shell is not a workbench window.
            return;
        }

        int narrowestWidth = ((ITmfTimeAligned) narrowestView).getAvailableWidth(getClampedTimeAxisOffset(alignmentInfo));
        narrowestWidth = getClampedTimeAxisWidth(alignmentInfo, narrowestWidth);
        IViewReference[] viewReferences = referenceView.getSite().getPage().getViewReferences();
        for (IViewReference ref : viewReferences) {
            IViewPart view = ref.getView(false);
            if (isTimeAlignedView(view)) {
                TmfView tmfView = (TmfView) view;
                ITmfTimeAligned alignedView = (ITmfTimeAligned) view;
                if (!isDisposedView(tmfView) && isViewLocationNear(getViewLocation(tmfView), alignmentInfo.getViewLocation())) {
                    alignedView.performAlign(getClampedTimeAxisOffset(alignmentInfo), narrowestWidth);
                }
            }
        }
    }

    /**
     * Realign all views
     */
    private void realignViews() {
        for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            for (IWorkbenchPage page : window.getPages()) {
                realignViews(page);
            }
        }
    }

    /**
     * Realign views inside a given page
     *
     * @param page
     *            the workbench page
     */
    private void realignViews(IWorkbenchPage page) {
        IViewReference[] viewReferences = page.getViewReferences();
        for (IViewReference ref : viewReferences) {
            IViewPart view = ref.getView(false);
            if (isTimeAlignedView(view)) {
                queueAlignment(((ITmfTimeAligned) view).getTimeViewAlignmentInfo(), false);
            }
        }
    }

    /**
     * Restore the views to their respective maximum widths
     */
    private static void restoreViews() {
        // We set the width to Integer.MAX_VALUE so that the
        // views remove any "filler" space they might have.
        for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            for (IWorkbenchPage page : window.getPages()) {
                for (IViewReference ref : page.getViewReferences()) {
                    restoreView(ref);
                }
            }
        }
    }

    private static void restoreView(IViewReference ref) {
        IViewPart view = ref.getView(false);
        if (isTimeAlignedView(view)) {
            ITmfTimeAligned alignedView = (ITmfTimeAligned) view;
            alignedView.performAlign(getClampedTimeAxisOffset(alignedView.getTimeViewAlignmentInfo()), Integer.MAX_VALUE);
        }
    }

    private static boolean isTimeAlignedView(IViewPart view) {
        if (view instanceof TmfView && view instanceof ITmfTimeAligned) {
            Composite parentComposite = ((TmfView) view).getParentComposite();
            if (parentComposite != null && !parentComposite.isDisposed()) {
                return true;
            }
        }
        return view instanceof TmfView && view instanceof ITmfTimeAligned;
    }

    private static boolean isDisposedView(TmfView view) {
        Composite parentComposite = (view).getParentComposite();
        return parentComposite != null && parentComposite.isDisposed();
    }

    /**
     * Queue the operation for processing. If an operation is considered the
     * same alignment (shell, location) as a previously queued one, it will
     * replace the old one. This way, only one up-to-date alignment operation is
     * kept per set of time-axis aligned views. The processing of the operation
     * is also throttled (TimerTask).
     *
     * @param operation
     *            the operation to queue
     */
    private void queue(AlignmentOperation operation) {
        synchronized(fPendingOperations) {
            fCurrentTask.cancel();
            for (AlignmentOperation pendingOperation : fPendingOperations) {
                if (isSameAlignment(operation, pendingOperation)) {
                    fPendingOperations.remove(pendingOperation);
                    break;
                }
            }
            fPendingOperations.add(operation);
            fCurrentTask = new AlignTask();
            fTimer.schedule(fCurrentTask, THROTTLE_DELAY);
        }
    }

    /**
     * Two operations are considered to be for the same set of time-axis aligned
     * views if they are on the same Shell and near the same location.
     */
    private static boolean isSameAlignment(AlignmentOperation operation1, AlignmentOperation operation2) {
        if (operation1.fView == operation2.fView) {
            return true;
        }

        if (operation1.fAlignmentInfo.getShell() != operation2.fAlignmentInfo.getShell()) {
            return false;
        }

        if (isViewLocationNear(getViewLocation(operation1.fView), getViewLocation(operation2.fView))) {
            return true;
        }

        return false;
    }

    private static boolean isViewLocationNear(Point location1, Point location2) {
        return Math.abs(location1.x - location2.x) < NEAR_THRESHOLD;
    }

    private static Point getViewLocation(TmfView view) {
        return view.getParentComposite().toDisplay(0, 0);
    }

    private void queueAlignment(TmfTimeViewAlignmentInfo timeViewAlignmentInfo, boolean synchronous) {
        if (isAlignViewsPreferenceEnabled()) {
            IWorkbenchWindow workbenchWindow = getWorkbenchWindow(timeViewAlignmentInfo.getShell());
            if (workbenchWindow == null || workbenchWindow.getActivePage() == null) {
                // Only time aligned views that are part of a workbench window are supported
                return;
            }

            // We need a view so that we can compute position right as we are
            // about to realign the views. The view could have been resized,
            // moved, etc.
            TmfView view = (TmfView) getReferenceView(timeViewAlignmentInfo, null);
            if (view == null) {
                // No valid view found for this alignment
                return;
            }

            AlignmentOperation operation = new AlignmentOperation(view, timeViewAlignmentInfo);
            if (synchronous) {
                performAlignment(operation);
            } else {
                queue(operation);
            }
        }
    }

    private static boolean isAlignViewsPreferenceEnabled() {
        return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).getBoolean(ITmfUIPreferences.PREF_ALIGN_VIEWS, true);
    }

    /**
     * Get a view that corresponds to the alignment information. The view is
     * meant to be used as a "reference" for other views to align on. Heuristics
     * are applied to choose the best view. For example, the view has to be
     * visible. It also will prioritize the view with lowest time axis offset
     * because most of the interesting data should be in the time widget.
     *
     * @param alignmentInfo
     *            alignment information
     * @param blackListedView
     *            an optional black listed view that will not be used as
     *            reference (useful for a view that just got created)
     * @return the reference view
     */
    private static ITmfTimeAligned getReferenceView(TmfTimeViewAlignmentInfo alignmentInfo, TmfView blackListedView) {
        IWorkbenchWindow workbenchWindow = getWorkbenchWindow(alignmentInfo.getShell());
        if (workbenchWindow == null || workbenchWindow.getActivePage() == null) {
            // Only time aligned views that are part of a workbench window are supported
            return null;
        }
        IWorkbenchPage page = workbenchWindow.getActivePage();

        int lowestTimeAxisOffset = Integer.MAX_VALUE;
        ITmfTimeAligned referenceView = null;
        for (IViewReference ref : page.getViewReferences()) {
            IViewPart view = ref.getView(false);
            if (view != blackListedView && isTimeAlignedView(view)) {
                if (isCandidateForReferenceView((TmfView) view, alignmentInfo, lowestTimeAxisOffset)) {
                    referenceView = (ITmfTimeAligned) view;
                    lowestTimeAxisOffset = getClampedTimeAxisOffset(referenceView.getTimeViewAlignmentInfo());
                }
            }
        }
        return referenceView;
    }

    private static boolean isCandidateForReferenceView(TmfView tmfView, TmfTimeViewAlignmentInfo alignmentInfo, int lowestTimeAxisOffset) {
        ITmfTimeAligned alignedView = (ITmfTimeAligned) tmfView;
        TmfTimeViewAlignmentInfo timeViewAlignmentInfo = alignedView.getTimeViewAlignmentInfo();
        if (timeViewAlignmentInfo == null) {
            return false;
        }

        if (isDisposedView(tmfView)) {
            return false;
        }

        Composite parentComposite = tmfView.getParentComposite();
        boolean isVisible = parentComposite != null && parentComposite.isVisible();
        if (isVisible) {
            boolean isViewLocationNear = isViewLocationNear(alignmentInfo.getViewLocation(), getViewLocation(tmfView));
            boolean isLowestTimeAxisOffset = getClampedTimeAxisOffset(timeViewAlignmentInfo) < lowestTimeAxisOffset;
            if (isViewLocationNear && isLowestTimeAxisOffset) {
                int availableWidth = alignedView.getAvailableWidth(getClampedTimeAxisOffset(timeViewAlignmentInfo));
                availableWidth = getClampedTimeAxisWidth(timeViewAlignmentInfo, availableWidth);
                if (availableWidth > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the narrowest view that corresponds to the given alignment information.
     */
    private static TmfView getNarrowestView(TmfTimeViewAlignmentInfo alignmentInfo) {
        IWorkbenchWindow workbenchWindow = getWorkbenchWindow(alignmentInfo.getShell());
        if (workbenchWindow == null || workbenchWindow.getActivePage() == null) {
            // Only time aligned views that are part of a workbench window are supported
            return null;
        }
        IWorkbenchPage page = workbenchWindow.getActivePage();

        int narrowestWidth = Integer.MAX_VALUE;
        TmfView narrowestView = null;
        for (IViewReference ref : page.getViewReferences()) {
            IViewPart view = ref.getView(false);
            if (isTimeAlignedView(view)) {
                TmfView tmfView = (TmfView) view;
                if (isCandidateForNarrowestView(tmfView, alignmentInfo, narrowestWidth)) {
                    narrowestWidth = ((ITmfTimeAligned) tmfView).getAvailableWidth(getClampedTimeAxisOffset(alignmentInfo));
                    narrowestWidth = getClampedTimeAxisWidth(alignmentInfo, narrowestWidth);
                    narrowestView = tmfView;
                }
            }
        }

        return narrowestView;
    }

    private static int getClampedTimeAxisWidth(TmfTimeViewAlignmentInfo alignmentInfo, int width) {
        int max = getMaxInt(alignmentInfo.getShell());
        if (validateInt(width, max)) {
            Activator.getDefault().logError("Time-axis width out of range (" + width + ")", new Throwable());  //$NON-NLS-1$//$NON-NLS-2$
        }
        return Math.min(max, Math.max(0, width));
    }

    private static int getClampedTimeAxisOffset(TmfTimeViewAlignmentInfo alignmentInfo) {
        int timeAxisOffset = alignmentInfo.getTimeAxisOffset();
        int max = getMaxInt(alignmentInfo.getShell());
        if (validateInt(timeAxisOffset, max)) {
            Activator.getDefault().logError("Time-axis offset out of range (" + timeAxisOffset + ")", new Throwable());  //$NON-NLS-1$//$NON-NLS-2$
        }
        return Math.min(max, Math.max(0, timeAxisOffset));
    }

    private static boolean validateInt(int value, int max) {
        return value < 0 || value > max;
    }

    private static int getMaxInt(Shell shell) {
        // Consider an integer to be buggy if it's bigger than 10 times the
        // width of *all* monitors combined.
        final int DISPLAY_WIDTH_FACTOR = 10;
        return shell.getDisplay().getBounds().width * DISPLAY_WIDTH_FACTOR;
    }

    private static boolean isCandidateForNarrowestView(TmfView tmfView, TmfTimeViewAlignmentInfo alignmentInfo, int narrowestWidth) {
        ITmfTimeAligned alignedView = (ITmfTimeAligned) tmfView;
        TmfTimeViewAlignmentInfo timeViewAlignmentInfo = alignedView.getTimeViewAlignmentInfo();
        if (timeViewAlignmentInfo == null) {
            return false;
        }

        if (isDisposedView(tmfView)) {
            return false;
        }

        Composite parentComposite = tmfView.getParentComposite();
        boolean isVisible = parentComposite != null && parentComposite.isVisible();
        if (isVisible) {
            if (isViewLocationNear(getViewLocation(tmfView), alignmentInfo.getViewLocation())) {
                int availableWidth = alignedView.getAvailableWidth(getClampedTimeAxisOffset(alignmentInfo));
                availableWidth = getClampedTimeAxisWidth(alignmentInfo, availableWidth);
                boolean isNarrower = availableWidth < narrowestWidth && availableWidth > 0;
                return isNarrower;
            }
        }

        return false;
    }

    private static IWorkbenchWindow getWorkbenchWindow(Shell shell) {
        for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            if (window.getShell() != null && window.getShell().equals(shell)) {
                return window;
            }
        }

        return null;
    }
}
