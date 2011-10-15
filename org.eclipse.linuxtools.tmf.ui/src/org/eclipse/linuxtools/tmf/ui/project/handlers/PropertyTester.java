package org.eclipse.linuxtools.tmf.ui.project.handlers;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        if (property.equals("isInTraceFolder")) { //$NON-NLS-1$
            boolean result = false;
            if (receiver instanceof IStructuredSelection) {
                Iterator<?> iter = ((IStructuredSelection) receiver).iterator();
                while (iter.hasNext()) {
                    Object o = iter.next();
                    if (o instanceof TmfTraceElement) {
                        if (((TmfTraceElement)o).getParent() instanceof TmfTraceFolder) {
                            result = true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return result;
        }
        return false;
    }

}
