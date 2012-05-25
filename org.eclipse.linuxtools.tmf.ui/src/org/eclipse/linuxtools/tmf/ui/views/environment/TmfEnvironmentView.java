package org.eclipse.linuxtools.tmf.ui.views.environment;

import java.util.ArrayList;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TmfEnvironmentView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.environment"; //$NON-NLS-1$
    private TmfExperiment<?> fExperiment;
    private Table fTable;
//    final private String fTitlePrefix;
    private Composite fParent;

    public TmfEnvironmentView() {
        super("EnvironmentVariables"); //$NON-NLS-1$
//        fTitlePrefix = getTitle();
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------
    final private class Pair{
        final private String key;
        final private String value;
        public Pair(String k) { key = k ; value = "";} //$NON-NLS-1$
        public Pair(String k, String v){ key = k; value = v; }
        public String getKey() { return key; }
        public String getValue() { return value; }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void createPartControl(Composite parent) {
        fParent = parent;
        TableItem ti[];
        // If an experiment is already selected, update the table
        TmfExperiment<ITmfEvent> experiment = (TmfExperiment<ITmfEvent>) TmfExperiment
                .getCurrentExperiment();
        if (experiment == null) {
            return;
        }
        fTable = new Table(parent, SWT.BORDER|SWT.FILL);


        ArrayList<Pair> tableData = new ArrayList<Pair>();
        for (ITmfTrace trace : experiment.getTraces()) {
            Pair traceEntry = new Pair(trace.getName());
            tableData.add(traceEntry);
            if (trace instanceof CtfTmfTrace) {
                CtfTmfTrace ctfTrace = (CtfTmfTrace) trace;
                for (String varName : ctfTrace
                        .getEnvNames()) {
                    tableData.add(new Pair( varName, ctfTrace.getEnvValue(varName)));
                }
            }
        }
        TableColumn nameCol = new TableColumn(fTable, SWT.NONE, 0);
        TableColumn valueCol = new TableColumn(fTable, SWT.NONE, 1);
        nameCol.setText("Environment Variable"); //$NON-NLS-1$
        valueCol.setText("Value"); //$NON-NLS-1$

        final int tableSize = tableData.size();

        fTable.setItemCount(tableSize);
        ti = fTable.getItems();
        for(int i = 0; i < tableSize; i++){
            final Pair currentPair = tableData.get(i);
            ti[i].setText(0, currentPair.getKey());
            ti[i].setText(1, currentPair.getValue());
        }

        fTable.setHeaderVisible(true);
        nameCol.pack();
        valueCol.pack();
        fTable.pack();

        parent.layout();

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        fTable.setFocus();
    }

    @Override
    public void dispose() {
        if (fTable != null) {
            fTable.dispose();
        }
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<TmfEvent> signal) {
        // Update the trace reference
        TmfExperiment<TmfEvent> exp = (TmfExperiment<TmfEvent>) signal.getExperiment();
        if (!exp.equals(fExperiment)) {
            fExperiment = exp;
            if (fTable != null) {
                fTable.dispose();
            }
            createPartControl( fParent );
            fParent.layout();
        }
    }


}
