/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.hhs.fha.nhinc.docretrieve.entity;

import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.docretrieve.nhin.proxy.NhinDocRetrieveProxy;
import gov.hhs.fha.nhinc.docretrieve.nhin.proxy.NhinDocRetrieveProxyObjectFactory;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.orchestration.Orchestratable;
import gov.hhs.fha.nhinc.orchestration.OrchestrationStrategy;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mweaver
 */
public class OutboundDocRetrieveStrategyImpl_g0 implements OrchestrationStrategy {

    private static Log log = LogFactory.getLog(OutboundDocRetrieveStrategyImpl_g0.class);

    public OutboundDocRetrieveStrategyImpl_g0() {
    }

    private Log getLogger() {
        return log;
    }

    @Override
    public void execute(Orchestratable message) {
        if (message instanceof OutboundDocRetrieveOrchestratable) {
            execute((OutboundDocRetrieveOrchestratable) message);
        }

    }

    public void execute(OutboundDocRetrieveOrchestratable message) {
        getLogger().debug("Begin NhinDocRetrieveOrchestratableImpl_g0.process");
        if (message == null) {
            getLogger().debug("NhinOrchestratable was null");
            return;
        }

        if (message instanceof EntityDocRetrieveOrchestratableImpl_a0) {
            EntityDocRetrieveOrchestratableImpl_a0 NhinDRMessage = (EntityDocRetrieveOrchestratableImpl_a0) message;
            String requestCommunityID = HomeCommunityMap.getCommunityIdForRDRequest(NhinDRMessage.getRequest());

            getLogger().debug("Calling audit log for doc retrieve request (a0) sent to nhin (g0)");
            auditRequestMessage(NhinDRMessage.getRequest(), NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_NHIN_INTERFACE,
                    NhinDRMessage.getAssertion(), requestCommunityID);

            getLogger().debug("Creating nhin (g0) doc retrieve proxy");
            NhinDocRetrieveProxy proxy = new NhinDocRetrieveProxyObjectFactory().getNhinDocRetrieveProxy();
            getLogger().debug("Sending nhin doc retrieve to nhin (g0)");
            NhinDRMessage.setResponse(proxy.respondingGatewayCrossGatewayRetrieve(NhinDRMessage.getRequest(), NhinDRMessage.getAssertion(), NhinDRMessage.getTarget()));

            getLogger().debug("Calling audit log for doc retrieve response received from nhin (g0)");
            auditResponseMessage(NhinDRMessage.getResponse(), NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_NHIN_INTERFACE,
                    NhinDRMessage.getAssertion(), requestCommunityID);
        } else {
            getLogger().error("NhinDocRetrieve_g0AdapterDelegateImpl_a0.process recieved a message which was not of type NhinDocRetrieveOrchestratableImpl_g0.");
        }
        getLogger().debug("End NhinDocRetrieveOrchestratableImpl_g0.process");
    }

    private void auditRequestMessage(RetrieveDocumentSetRequestType request, String direction, String connectInterface, AssertionType assertion, String requestCommunityID) {
        gov.hhs.fha.nhinc.common.auditlog.DocRetrieveMessageType message = new gov.hhs.fha.nhinc.common.auditlog.DocRetrieveMessageType();
        message.setRetrieveDocumentSetRequest(request);
        message.setAssertion(assertion);
        AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();
        LogEventRequestType auditLogMsg = auditLogger.logDocRetrieve(message, direction, connectInterface, requestCommunityID);
        if (auditLogMsg != null) {
            auditMessage(auditLogMsg, assertion);
        }
    }

    private void auditResponseMessage(RetrieveDocumentSetResponseType response, String direction, String connectInterface, AssertionType assertion, String requestCommunityID) {
        gov.hhs.fha.nhinc.common.auditlog.DocRetrieveResponseMessageType message = new gov.hhs.fha.nhinc.common.auditlog.DocRetrieveResponseMessageType();
        message.setRetrieveDocumentSetResponse(response);
        message.setAssertion(assertion);
        AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();
        LogEventRequestType auditLogMsg = auditLogger.logDocRetrieveResult(message, direction, connectInterface, requestCommunityID);
        if (auditLogMsg != null) {
            auditMessage(auditLogMsg, assertion);
        }
    }

    private AcknowledgementType auditMessage(LogEventRequestType auditLogMsg, AssertionType assertion) {
        AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
        AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
        return proxy.auditLog(auditLogMsg, assertion);
    }
}
