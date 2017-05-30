/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.maps;

import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jorge
 */
public abstract class ContextAction extends AbstractAction
        implements LookupListener, ContextAwareAction {

    private List<Lookup.Result> results = new LinkedList<Lookup.Result>();

    protected void listenResult(Lookup.Result res) {
        results.add(res);
        res.addLookupListener(this);
        res.allInstances();
        resultChanged(null);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        for (Lookup.Result result : results) {
            if (result.allInstances().isEmpty()) {
                setEnabled(false);
                return;
            }
        }
        if(results.isEmpty())
            setEnabled(false);
        else
            setEnabled(true);
    }
}
