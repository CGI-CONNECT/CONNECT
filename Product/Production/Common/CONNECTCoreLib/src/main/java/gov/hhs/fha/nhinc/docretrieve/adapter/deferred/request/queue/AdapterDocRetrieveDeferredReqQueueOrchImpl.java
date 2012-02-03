/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
 * All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met: 
 *     * Redistributions of source code must retain the above 
 *       copyright notice, this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution. 
 *     * Neither the name of the United States Government nor the 
 *       names of its contributors may be used to endorse or promote products 
 *       derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package gov.hhs.fha.nhinc.docretrieve.adapter.deferred.request.queue;

import java.util.List;

import gov.hhs.fha.nhinc.async.AsyncMessageIdCreator;
import gov.hhs.fha.nhinc.async.AsyncMessageProcessHelper;
import gov.hhs.fha.nhinc.asyncmsgs.dao.AsyncMsgRecordDao;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewayCrossGatewayRetrieveRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.connectmgr.UrlInfo;
import gov.hhs.fha.nhinc.docrepository.adapter.proxy.AdapterComponentDocRepositoryProxyJavaImpl;
import gov.hhs.fha.nhinc.docretrieve.DocRetrieveDeferredAuditLogger;
import gov.hhs.fha.nhinc.docretrieve.DocRetrieveDeferredPolicyChecker;
import gov.hhs.fha.nhinc.docretrieve.passthru.deferred.response.proxy.PassthruDocRetrieveDeferredRespProxyObjectFactory;
import gov.hhs.fha.nhinc.docretrieve.passthru.deferred.response.proxy.PassthruDocRetrieveDeferredRespProxy;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.transform.document.DocRetrieveAckTranforms;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;
import gov.hhs.healthit.nhin.DocRetrieveAcknowledgementType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author narendra.reddy
 */
public class AdapterDocRetrieveDeferredReqQueueOrchImpl {

    private static final Log log = LogFactory.getLog(AdapterDocRetrieveDeferredReqQueueOrchImpl.class);

    protected AsyncMessageProcessHelper createAsyncProcesser() {
        return new AsyncMessageProcessHelper();
    }

    /**
     * Document Retrieve Deferred Response implementation method
     * @param response
     * @param assertion
     * @param target
     * @return DocRetrieveAcknowledgementType
     */
    public DocRetrieveAcknowledgementType crossGatewayRetrieveResponse(RetrieveDocumentSetRequestType request, AssertionType assertion, NhinTargetCommunitiesType target) {
        log.debug("Begin AdapterDocRetrieveDeferredReqQueueOrchImpl.crossGatewayRetrieveResponse");

        DocRetrieveAcknowledgementType respAck = new DocRetrieveAcknowledgementType();
        String ackMsg = "";

        RespondingGatewayCrossGatewayRetrieveRequestType respondingGatewayCrossGatewayRetrieveRequestType = new RespondingGatewayCrossGatewayRetrieveRequestType();
        respondingGatewayCrossGatewayRetrieveRequestType.setRetrieveDocumentSetRequest(request);
        respondingGatewayCrossGatewayRetrieveRequestType.setAssertion(assertion);
        respondingGatewayCrossGatewayRetrieveRequestType.setNhinTargetCommunities(target);

        String homeCommunityId = HomeCommunityMap.getLocalHomeCommunityId();
        String responseCommunityId = assertion.getHomeCommunity().getHomeCommunityId();

        // Audit the incoming doc retrieve request Message
        DocRetrieveDeferredAuditLogger auditLog = new DocRetrieveDeferredAuditLogger();
        auditLog.auditDocRetrieveDeferredRequest(request, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE, assertion, homeCommunityId);

        // ASYNCMSG PROCESSING - RSPPROCESS
        AsyncMessageProcessHelper asyncProcess = createAsyncProcesser();
        String messageId = assertion.getMessageId();

        // Generate a new response assertion
        AssertionType responseAssertion = asyncProcess.copyAssertionTypeObject(assertion);
        // Original request message id is now set as the relates to id
        responseAssertion.getRelatesToList().add(messageId);
        // Generate a new unique response assertion Message ID
        responseAssertion.setMessageId(AsyncMessageIdCreator.generateMessageId());
        // Set the reponse assertions home community with the local gateway's community id
        HomeCommunityType homeCommunityType = new HomeCommunityType();
        homeCommunityType.setHomeCommunityId(homeCommunityId);
        homeCommunityType.setName(homeCommunityId);
        responseAssertion.setHomeCommunity(homeCommunityType);
        if (responseAssertion.getUserInfo() != null &&
                responseAssertion.getUserInfo().getOrg() != null) {
            responseAssertion.getUserInfo().getOrg().setHomeCommunityId(homeCommunityId);
            responseAssertion.getUserInfo().getOrg().setName(homeCommunityId);
        }

        boolean bIsQueueOk = asyncProcess.processMessageStatus(messageId, AsyncMsgRecordDao.QUEUE_STATUS_RSPPROCESS);

        if (bIsQueueOk) {
            try {
                List<UrlInfo> urlInfoList = getEndpoints(target);

                if (urlInfoList != null &&
                        NullChecker.isNotNullish(urlInfoList) &&
                        urlInfoList.get(0) != null &&
                        NullChecker.isNotNullish(urlInfoList.get(0).getHcid()) &&
                        NullChecker.isNotNullish(urlInfoList.get(0).getUrl())) {

                    NhinTargetSystemType oTargetSystem = new NhinTargetSystemType();
                    oTargetSystem.setUrl(urlInfoList.get(0).getUrl());

                    // Audit the Retrieve Documents Request Message sent to the Adapter Interface
                    auditLog.auditDocRetrieveDeferredRequest(request, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE, assertion, homeCommunityId);

                    // Get the RetrieveDocumentSetResponseType by passing the request to local adapter doc retrieve java implementation
                    RetrieveDocumentSetResponseType response = null;
                    AdapterComponentDocRepositoryProxyJavaImpl docRepositoryImpl = new AdapterComponentDocRepositoryProxyJavaImpl();
                    response = docRepositoryImpl.retrieveDocument(request, assertion);

                    /*
                     * TODO - Redaction Engine has a known issue with Retrieve Documents - GATEWAY-295.
                     * Uncomment and re-test use of the Redaction Engine when this issue has been resolved.
                     */
                    //AdapterRedactionEngineProxyJavaImpl redactEngineImpl = new AdapterRedactionEngineProxyJavaImpl();
                    //response = redactEngineImpl.filterRetrieveDocumentSetResults(request, response, assertion);

                    // Audit the Retrieve Documents Query Response Message sent to the Adapter Interface
                    auditLog.auditDocRetrieveDeferredResponse(response, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE, assertion, homeCommunityId);

                    DocRetrieveDeferredPolicyChecker policyCheck = new DocRetrieveDeferredPolicyChecker();

                    if (policyCheck.checkOutgoingPolicy(response, assertion, homeCommunityId)) {
                        // Use passthru proxy to call NHIN
                        PassthruDocRetrieveDeferredRespProxyObjectFactory objFactory = new PassthruDocRetrieveDeferredRespProxyObjectFactory();
                        PassthruDocRetrieveDeferredRespProxy docRetrieveProxy = objFactory.getNhincProxyDocRetrieveDeferredRespProxy();

                        // Create new target system for outbound NHIN DRD response request
                        oTargetSystem = new NhinTargetSystemType();
                        HomeCommunityType responseCommunityType = new HomeCommunityType();
                        responseCommunityType.setHomeCommunityId(responseCommunityId);
                        responseCommunityType.setName(responseCommunityId);
                        oTargetSystem.setHomeCommunity(responseCommunityType);

                        // Send NHIN DRD response request
                        respAck = docRetrieveProxy.crossGatewayRetrieveResponse(request, response, responseAssertion, oTargetSystem);

                        asyncProcess.processAck(messageId, AsyncMsgRecordDao.QUEUE_STATUS_RSPSENTACK, AsyncMsgRecordDao.QUEUE_STATUS_RSPSENTERR, respAck);
                    } else {
                        ackMsg = "Outgoing Policy Check Failed";
                        log.error(ackMsg);
                        respAck = DocRetrieveAckTranforms.createAckMessage(NhincConstants.DOC_RETRIEVE_DEFERRED_RESP_ACK_FAILURE_STATUS_MSG, NhincConstants.DOC_RETRIEVE_DEFERRED_ACK_ERROR_AUTHORIZATION, ackMsg);
                        asyncProcess.processAck(messageId, AsyncMsgRecordDao.QUEUE_STATUS_RSPSENTERR, AsyncMsgRecordDao.QUEUE_STATUS_RSPSENTERR, respAck);
                    }
                } else {
                    ackMsg = "Failed to obtain target URL from connection manager";
                    log.error(ackMsg);
                    respAck = DocRetrieveAckTranforms.createAckMessage(NhincConstants.DOC_RETRIEVE_DEFERRED_RESP_ACK_FAILURE_STATUS_MSG, NhincConstants.DOC_RETRIEVE_DEFERRED_ACK_ERROR_INVALID, ackMsg);
                    asyncProcess.processAck(messageId, AsyncMsgRecordDao.QUEUE_STATUS_RSPSENTERR, AsyncMsgRecordDao.QUEUE_STATUS_RSPSENTERR, respAck);
                }
            } catch (Exception e) {
                ackMsg = "Exception processing Deferred Retrieve Documents: " + e.getMessage();
                log.error(ackMsg, e);
                respAck = DocRetrieveAckTranforms.createAckMessage(NhincConstants.DOC_RETRIEVE_DEFERRED_RESP_ACK_FAILURE_STATUS_MSG, NhincConstants.DOC_RETRIEVE_DEFERRED_ACK_ERROR_INVALID, ackMsg);
                asyncProcess.processAck(messageId, AsyncMsgRecordDao.QUEUE_STATUS_RSPSENTERR, AsyncMsgRecordDao.QUEUE_STATUS_RSPSENTERR, respAck);
            }
        } else {
            ackMsg = "Deferred Retrieve Documents response processing halted; deferred queue repository error encountered";

            // Set the error acknowledgement status
            // fatal error with deferred queue repository
            respAck = DocRetrieveAckTranforms.createAckMessage(NhincConstants.DOC_RETRIEVE_DEFERRED_RESP_ACK_FAILURE_STATUS_MSG, NhincConstants.DOC_RETRIEVE_DEFERRED_ACK_ERROR_INVALID, ackMsg);
        }

        // Audit log - response
        auditLog.auditDocRetrieveDeferredAckResponse(respAck.getMessage(), request, null, responseAssertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE, homeCommunityId);

        log.debug("End AdapterDocRetrieveDeferredRespOrchImpl.crossGatewayRetrieveResponse");

        return respAck;
    }

    /**
     *
     * @param targetCommunities
     * @return List<UrlInfo>
     */
    protected List<UrlInfo> getEndpoints(NhinTargetCommunitiesType targetCommunities) {
        List<UrlInfo> urlInfoList = null;
        try {
            urlInfoList = ConnectionManagerCache.getInstance().getEndpontURLFromNhinTargetCommunities(targetCommunities, NhincConstants.NHIN_DOCRETRIEVE_DEFERRED_RESPONSE);
        } catch (ConnectionManagerException ex) {
            log.error("Failed to obtain target URLs", ex);
        }
        return urlInfoList;
    }
}
