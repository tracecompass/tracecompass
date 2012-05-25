package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.Attributes;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.swt.graphics.RGB;

public class ResourcesPresentationProvider extends TimeGraphPresentationProvider {

    private enum State {
        UNKNOWN     (new RGB(100, 100, 100)),
        IDLE        (new RGB(200, 200, 200)),
        BUSY        (new RGB(0, 200, 0)),
        INTERRUPTED (new RGB(200, 100, 100)),
        RAISED      (new RGB(200, 200, 0)),
        ACTIVE      (new RGB(200, 150, 100));

        public final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }
    }

    @Override 
    public String getStateTypeName() {
        return Messages.ResourcesView_stateTypeName;
    }

    @Override
    public StateItem[] getStateTable() {
        StateItem[] stateTable = new StateItem[State.values().length];
        for (int i = 0; i < stateTable.length; i++) {
            State state = State.values()[i];
            stateTable[i] = new StateItem(state.rgb, state.toString());
        }
        return stateTable;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof ResourcesEvent) {
            ResourcesEvent resourcesEvent = (ResourcesEvent) event;
            if (resourcesEvent.getType() == Type.CPU) {
                int status = resourcesEvent.getValue();
                if (status == Attributes.CPU_STATUS_IDLE) {
                    return State.IDLE.ordinal();
                } else if (status == Attributes.CPU_STATUS_BUSY) {
                    return State.BUSY.ordinal();
                } else if (status == Attributes.CPU_STATUS_INTERRUPTED) {
                    return State.INTERRUPTED.ordinal();
                }
            } else if (resourcesEvent.getType() == Type.IRQ || resourcesEvent.getType() == Type.SOFT_IRQ) {
                int cpu = resourcesEvent.getValue();
                if (cpu == Attributes.SOFT_IRQ_RAISED) {
                    return State.RAISED.ordinal();
                }
                return State.ACTIVE.ordinal();
            } else {
                return -1; // NULL
            }
        }
        return State.UNKNOWN.ordinal();
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof ResourcesEvent) {
            ResourcesEvent resourcesEvent = (ResourcesEvent) event;
            if (resourcesEvent.getType() == Type.CPU) {
                int status = resourcesEvent.getValue();
                if (status == Attributes.CPU_STATUS_IDLE) {
                    return State.IDLE.toString();
                } else if (status == Attributes.CPU_STATUS_BUSY) {
                    return State.BUSY.toString();
                } else if (status == Attributes.CPU_STATUS_INTERRUPTED) {
                    return State.INTERRUPTED.toString();
                }
            } else if (resourcesEvent.getType() == Type.IRQ || resourcesEvent.getType() == Type.SOFT_IRQ) {
                int cpu = resourcesEvent.getValue();
                if (cpu == Attributes.SOFT_IRQ_RAISED) {
                    return State.RAISED.toString();
                }
                return State.ACTIVE.toString();
            } else {
                return null;
            }
        }
        return State.UNKNOWN.toString();
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        
        Map<String, String> retMap = new HashMap<String, String>();
        if (event instanceof ResourcesEvent) {

            ResourcesEvent resourcesEvent = (ResourcesEvent) event;

            if (resourcesEvent.getType().equals(Type.IRQ) || resourcesEvent.getType().equals(Type.SOFT_IRQ)) {
                int cpu = resourcesEvent.getValue();
                if (cpu >= 0) {
                    retMap.put(Messages.ResourcesView_attributeCpuName, String.valueOf(cpu));
                }
            }
        }

        return retMap;
    }

}
