/**
 * Copyright (c) 2009-2014, United States Government, as represented by the Secretary of Health and Human Services. All
 * rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. Neither the name of the United States Government nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package gov.hhs.fha.nhinc.admingui.dashboard;

import gov.hhs.fha.nhinc.admingui.dashboard.impl.DashboardAppServer;
import gov.hhs.fha.nhinc.admingui.dashboard.impl.DashboardJava;
import gov.hhs.fha.nhinc.admingui.dashboard.impl.DashboardLastInbound;
import gov.hhs.fha.nhinc.admingui.dashboard.impl.DashboardLastOutbound;
import gov.hhs.fha.nhinc.admingui.dashboard.impl.DashboardMemory;
import gov.hhs.fha.nhinc.admingui.dashboard.impl.DashboardOs;
import gov.hhs.fha.nhinc.admingui.model.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author jasonasmith
 */
@Service
public class DashboardObserverImpl implements DashboardObserver {

    private final List<DashboardPanel> openPanels = new ArrayList<DashboardPanel>();
    private final List<DashboardPanel> closedPanels = new ArrayList<DashboardPanel>();

    private boolean started = false;

    @Override
    public List<DashboardPanel> getOpenDashboardPanels() {
        return openPanels;
    }

    @Override
    public void closePanel(Class c) {
        for (int i = 0; i < openPanels.size(); i++) {
            DashboardPanel panel = openPanels.get(i);
            if (panel.getClass().equals(c)) {
                closedPanels.add(openPanels.remove(i));
                break;
            }
        }
    }
    
    @Override
    public boolean isStarted(){
        return started;
    }

    @Override
    public void openPanel(DashboardPanel panel) {
        openPanels.add(panel);
    }

    @Override
    public void save(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDefaultPanels() {
        if (!started) {
            openPanels.add(new DashboardLastInbound(this, false).setData());
            openPanels.add(new DashboardLastOutbound(this, false).setData());
            openPanels.add(new DashboardMemory(this, false).setData());
            openPanels.add(new DashboardOs(this, false).setData());
            openPanels.add(new DashboardJava(this, false).setData());
            openPanels.add(new DashboardAppServer(this, true).setData());
            started = true;
        }
    }

    @Override
    public void setUserPanels(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refreshData() {
        for(DashboardPanel panel : openPanels){
            panel.setData();
        }
    }

}
