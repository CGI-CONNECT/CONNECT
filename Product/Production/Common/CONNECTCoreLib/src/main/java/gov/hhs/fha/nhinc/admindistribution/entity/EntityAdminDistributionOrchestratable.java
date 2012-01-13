/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.hhs.fha.nhinc.admindistribution.entity;

import gov.hhs.fha.nhinc.admindistribution.AdminDistributionHelper;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewaySendAlertMessageType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.orchestration.AuditTransformer;
import gov.hhs.fha.nhinc.orchestration.EntityOrchestratable;
import gov.hhs.fha.nhinc.orchestration.NhinAggregator;
import gov.hhs.fha.nhinc.orchestration.NhinDelegate;
import gov.hhs.fha.nhinc.orchestration.PolicyTransformer;

/**
 *
 * @author nnguyen
 */
public class EntityAdminDistributionOrchestratable implements EntityOrchestratable {
    protected NhinTargetSystemType target = null;
    private AssertionType assertion = null;
    private NhinDelegate nhinDelegate = null;
    private RespondingGatewaySendAlertMessageType request = null;

    public EntityAdminDistributionOrchestratable( NhinDelegate delegate)
    {
        nhinDelegate = delegate;
    }

    public RespondingGatewaySendAlertMessageType getRequest() {
        return request;
    }

    public void setRequest(RespondingGatewaySendAlertMessageType request) {
        this.request = request;
    }
    
    public NhinTargetSystemType getTarget() {
        return target;
    }

    public void setTarget(NhinTargetSystemType target) {
        this.target = target;
    }
    
    public void setAssertion(AssertionType _assertion) {
        this.assertion = _assertion;
    }
    
    public NhinDelegate getNhinDelegate() {
        return nhinDelegate;
    }

    public boolean isEnabled() {
        return new AdminDistributionHelper().isServiceEnabled();
    }

    public AssertionType getAssertion() {
        return assertion;
    }

    public String getServiceName() {
        return NhincConstants.ADMIN_DIST_SERVICE_NAME;
    }

    @Override
    public NhinDelegate getDelegate() {
	return getNhinDelegate();
    }

    public boolean isPassthru() {
        return new AdminDistributionHelper().isInPassThroughMode();
    }

    public AuditTransformer getAuditTransformer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PolicyTransformer getPolicyTransformer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NhinAggregator getAggregator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
