/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Factored out events table
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.events;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.util.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomEventsTable;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ide.IGotoMarker;
import org.osgi.framework.Bundle;

/**
 * <b><u>TmfEventsView</u></b>
 * <p>
 *
 * TODO: Implement me. Please.
 * TODO: Handle column selection, sort, ... generically (nothing less...)
 * TODO: Implement hide/display columns
 */
public class TmfEventsView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.events"; //$NON-NLS-1$

    private TmfExperiment<?> fExperiment;
    private TmfEventsTable fEventsTable;
    private static final int DEFAULT_CACHE_SIZE = 100;
    private String fTitlePrefix;
    private Composite fParent;
    
	// ------------------------------------------------------------------------
    // Constructor
	// ------------------------------------------------------------------------

    public TmfEventsView(int cacheSize) {
    	super("TmfEventsView"); //$NON-NLS-1$
    }

    public TmfEventsView() {
    	this(DEFAULT_CACHE_SIZE);
    }

	// ------------------------------------------------------------------------
    // ViewPart
	// ------------------------------------------------------------------------

	@Override
    @SuppressWarnings("unchecked")
	public void createPartControl(Composite parent) {
        fParent = parent;

        fTitlePrefix = getTitle();
        
        // If an experiment is already selected, update the table
        TmfExperiment<TmfEvent> experiment = (TmfExperiment<TmfEvent>) TmfExperiment.getCurrentExperiment();
        if (experiment != null) {
            experimentSelected(new TmfExperimentSelectedSignal<TmfEvent>(this, experiment));
        } else {
            fEventsTable = createEventsTable(parent);
        }
    }

    @Override
    public void dispose() {
        if (fEventsTable != null) {
            fEventsTable.dispose();
        }
        super.dispose();
    }

    /**
     * Get the events table for an experiment.
     * If all traces in the experiment are of the same type,
     * use the extension point specified event table
     * @param parent the parent Composite
     * @return an events table of the appropriate type
     */
    protected TmfEventsTable createEventsTable(Composite parent) {
        if (fExperiment == null) {
            return new TmfEventsTable(parent, DEFAULT_CACHE_SIZE);
        }
        int cacheSize = fExperiment.getCacheSize();
        String commonTraceType = null;
        try {
            for (ITmfTrace<?> trace : fExperiment.getTraces()) {
                IResource resource = null;
                if (trace instanceof TmfTrace) {
                    resource = ((TmfTrace<?>) trace).getResource();
                }
                if (resource == null) {
                    return new TmfEventsTable(parent, cacheSize);
                }
                String traceType = resource.getPersistentProperty(TmfTraceElement.TRACETYPE);
                if (commonTraceType != null && !commonTraceType.equals(traceType)) {
                    return new TmfEventsTable(parent, cacheSize);
                }
                commonTraceType = traceType;
            }
            if (commonTraceType == null) {
                return new TmfEventsTable(parent, cacheSize);
            }
            if (commonTraceType.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                return new CustomEventsTable(((CustomTxtTrace) fExperiment.getTraces()[0]).getDefinition(), parent, cacheSize);
            }
            if (commonTraceType.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                return new CustomEventsTable(((CustomXmlTrace) fExperiment.getTraces()[0]).getDefinition(), parent, cacheSize);
            }
            for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(commonTraceType)) {
                    IConfigurationElement[] eventsTableTypeCE = ce.getChildren(TmfTraceType.EVENTS_TABLE_TYPE_ELEM);
                    if (eventsTableTypeCE.length != 1) {
                        break;
                    }
                    String eventsTableType = eventsTableTypeCE[0].getAttribute(TmfTraceType.CLASS_ATTR);
                    if (eventsTableType == null || eventsTableType.length() == 0) {
                        break;
                    }
                    Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    Class<?> c = bundle.loadClass(eventsTableType);
                    Class<?>[] constructorArgs = new Class[] { Composite.class, int.class };
                    Constructor<?> constructor = c.getConstructor(constructorArgs);
                    Object[] args = new Object[] { parent, cacheSize };
                    return (TmfEventsTable) constructor.newInstance(args);
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        } catch (InvalidRegistryObjectException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return new TmfEventsTable(parent, cacheSize);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
	public void setFocus() {
        fEventsTable.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (IGotoMarker.class.equals(adapter)) {
            return fEventsTable;
        }
        return super.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	@SuppressWarnings("nls")
	public String toString() {
    	return "[TmfEventsView]";
    }

    // ------------------------------------------------------------------------
    // Signal handlers
	// ------------------------------------------------------------------------
    
	@SuppressWarnings("unchecked")
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<TmfEvent> signal) {
        // Update the trace reference
        TmfExperiment<TmfEvent> exp = (TmfExperiment<TmfEvent>) signal.getExperiment();
        if (!exp.equals(fExperiment)) {
            fExperiment = exp;
            setPartName(fTitlePrefix + " - " + fExperiment.getName()); //$NON-NLS-1$
            if (fEventsTable != null) {
                fEventsTable.dispose();
            }
            fEventsTable = createEventsTable(fParent);
            fEventsTable.setTrace(fExperiment, false);
            fEventsTable.refreshBookmarks(fExperiment.getResource());
            fParent.layout();
        }
    }

	@SuppressWarnings("unchecked")
	@TmfSignalHandler
	public void experimentDisposed(TmfExperimentDisposedSignal<TmfEvent> signal) {
		// Clear the trace reference
		TmfExperiment<TmfEvent> experiment = (TmfExperiment<TmfEvent>) signal.getExperiment();
		if (experiment.equals(fExperiment)) {
			fEventsTable.setTrace(null, false);

            TmfUiPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getShell().getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    setPartName(fTitlePrefix);
                }
            });
		}
	}

}