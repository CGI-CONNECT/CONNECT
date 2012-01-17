package gov.hhs.fha.nhinc.docquery.entity;

import gov.hhs.fha.nhinc.gateway.executorservice.NhinCallableRequest;
import gov.hhs.fha.nhinc.gateway.executorservice.NhinTaskExecutor;

import gov.hhs.fha.nhinc.orchestration.NhinDelegate;
import gov.hhs.fha.nhinc.orchestration.NhinResponseProcessor;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gov.hhs.fha.nhinc.common.auditlog.AdhocQueryResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommon.QualifiedSubjectIdentifierType;
import gov.hhs.fha.nhinc.docquery.DocQueryAuditLog;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewayCrossGatewayQuerySecuredRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.data.CMUrlInfos;
import gov.hhs.fha.nhinc.gateway.executorservice.ExecutorServiceHelper;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;
import gov.hhs.fha.nhinc.transform.document.DocumentQueryTransform;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import gov.hhs.fha.nhinc.policyengine.PolicyEngineChecker;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxy;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxyObjectFactory;
import gov.hhs.fha.nhinc.common.eventcommon.AdhocQueryRequestEventType;
import gov.hhs.fha.nhinc.common.eventcommon.AdhocQueryRequestMessageType;

import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Orchestrates the Entity (i.e. from Adapter) DocQuery transaction
 * @author Neil Webb
 * @author paul.eftis (updated 10/15/2011 to implement new concurrent request handling/fanout)
 * @author paul.eftis (updated 01/15/2011 to implement new multispec delegate)
 */
public class EntityDocQueryOrchImpl{

    private Log log = null;
    private ExecutorService regularExecutor = null;
    private ExecutorService largejobExecutor = null;


    /**
     * Add default constructor that is used by test cases
     * Note that implementations should always use constructor that takes
     * the executor services as input
     */
    public EntityDocQueryOrchImpl(){
        // for this default test case, we just create default executor services
        // with a thread pool of 1
        regularExecutor = Executors.newFixedThreadPool(1);
        largejobExecutor = Executors.newFixedThreadPool(1);
    }


    /**
     * We construct the orch impl class with references to both executor services
     * that could be used for this particular orchestration instance.
     * Determination of which executor service to use (largejob or regular) is based on
     * the size of the correlationsResult and configs
     */
    public EntityDocQueryOrchImpl(ExecutorService e, ExecutorService le){
        log = createLogger();
        regularExecutor = e;
        largejobExecutor = le;
    }


    protected Log createLogger(){
        return ((log != null) ? log : LogFactory.getLog(getClass()));
    }


    /**
     * If testList is passed in (i.e. not null/empty), will fan-out DQ
     * to entire testList (i.e. will ignore correlations)
     * If testList is null/empty, will just do normal DQ fan-out using
     * correlations for patientId passed in adhocQueryRequest
     *
     * @param adhocQueryRequest
     * @param assertion
     * @param testList
     * @return <code>AdhocQueryResponse</code>
     */
    public AdhocQueryResponse entityDocQueryOrchImplLoadTest(AdhocQueryRequest adhocQueryRequest,
            AssertionType assertion, List<QualifiedSubjectIdentifierType> testList){
        log.debug("EntityDocQueryOrchImpl::entityDocQueryOrchImplLoadTest");

        boolean responseIsSpecA0 = true;
        AdhocQueryResponse response = null;


        CMUrlInfos urlInfoList = null;
        boolean isTargeted = false;

        // audit initial request
        NhinTargetCommunitiesType targets = null;
        DocQueryAuditLog auditLog = new DocQueryAuditLog();
        RespondingGatewayCrossGatewayQuerySecuredRequestType request = new RespondingGatewayCrossGatewayQuerySecuredRequestType();
        request.setAdhocQueryRequest(adhocQueryRequest);
        request.setNhinTargetCommunities(targets);
        String targetHomeCommunityId = HomeCommunityMap.getCommunityIdFromTargetCommunities(targets);
        auditInitialEntityRequest(request, assertion, auditLog, targetHomeCommunityId);

        try{
            if (targets != null &&
                    NullChecker.isNotNullish(targets.getNhinTargetCommunity())){
                isTargeted = true;
            }

            /*******************************************************************
             * TODO test/check handling of targets is correct!!!!!!!!!!!!!!!
             ******************************************************************/

            // Obtain all the URLs for the targets being sent to
            try{
                urlInfoList = ConnectionManagerCache.getEndpontURLFromNhinTargetCommunities(targets, NhincConstants.DOC_QUERY_SERVICE_NAME);
            }catch(Exception ex){
                log.error("Failed to obtain target URLs", ex);
            }

            // Validate that the message is not null
            if (adhocQueryRequest != null &&
                    adhocQueryRequest.getAdhocQuery() != null &&
                    NullChecker.isNotNullish(adhocQueryRequest.getAdhocQuery().getSlot())){
                List<SlotType1> slotList = adhocQueryRequest.getAdhocQuery().getSlot();
                String localAA = new EntityDocQueryHelper().getLocalAssigningAuthority(slotList);
                String uniquePatientId = new EntityDocQueryHelper().getUniquePatientId(slotList);
                log.debug("respondingGatewayCrossGatewayQuery EntityDocQueryHelper uniquePatientId: " + uniquePatientId
                        + " and localAA=" + localAA);

                 List<QualifiedSubjectIdentifierType> correlationsResult =
                         new EntityDocQueryHelper().retreiveCorrelations(slotList, urlInfoList,
                         assertion, isTargeted, getLocalHomeCommunityId());

                if(testList != null && testList.size() > 0){
                    // this is load test fanout to testList
                    correlationsResult = testList;
                }

                if(NullChecker.isNotNullish(correlationsResult)){
                    /************************************************************************
                     * We replaced the 3.2.1 connect code here with the new 3.3 concurrent fanout impl
                     * Note that all response processing is done in the NhinResponseProcessor
                    ***********************************************************************/
                    List<NhinCallableRequest<EntityDocQueryOrchestratable>> callableList =
                            new ArrayList<NhinCallableRequest<EntityDocQueryOrchestratable>>();
                    String transactionId = (UUID.randomUUID()).toString();
                    NhinDelegate nd = new NhinDocQueryDelegate();
                    NhinResponseProcessor np = new EntityDocQueryProcessor();

                    // we hold the error messages for any failed policy checks in policyErrList
                    RegistryErrorList policyErrList = new RegistryErrorList();

                    for(QualifiedSubjectIdentifierType identifier : correlationsResult){
                        NhinTargetSystemType target = new NhinTargetSystemType();

                        HomeCommunityType targetCommunity = new EntityDocQueryHelper().lookupHomeCommunityId(
                                identifier.getAssigningAuthorityIdentifier(), localAA, getLocalHomeCommunityId());
                        if(targetCommunity != null){
                            target.setHomeCommunity(targetCommunity);
                        }

                        if(isValidPolicy(adhocQueryRequest, assertion, targetCommunity)){
                            // Replace the patient id in the document query message
                            // and clone the original adhocQueryRequest
                            DocumentQueryTransform transform = new DocumentQueryTransform();
                            AdhocQueryRequest clonedQueryRequest = transform.replaceAdhocQueryPatientId(
                                    cloneRequest(adhocQueryRequest), getLocalHomeCommunityId(),
                                    identifier.getAssigningAuthorityIdentifier(),
                                    identifier.getSubjectIdentifier());

                            EntityDocQueryOrchestratable message = new EntityDocQueryOrchestratable(
                                        nd, np, null, null, assertion, NhincConstants.DOC_QUERY_SERVICE_NAME,
                                        target, clonedQueryRequest);
                            callableList.add(new NhinCallableRequest<EntityDocQueryOrchestratable>(message));

                            log.debug(Thread.currentThread().getName() + " added NhinCallableRequest"
                                    + " for hcid=" + target.getHomeCommunity().getHomeCommunityId());
                        }else{
                            log.debug("Policy Check Failed for homeId=" + target.getHomeCommunity().getHomeCommunityId()
                                    + " and aaId=" + identifier.getAssigningAuthorityIdentifier());
                            RegistryError regErr = new RegistryError();
                            regErr.setCodeContext("Policy Check Failed for homeId=" + target.getHomeCommunity().getHomeCommunityId()
                                    + " and aaId=" + identifier.getAssigningAuthorityIdentifier());
                            regErr.setErrorCode("XDSRepositoryError");
                            regErr.setSeverity("Error");
                            policyErrList.getRegistryError().add(regErr);
                        }
                    }

                    // note that this impl sets taskexecutor to return EntityDocQueryOrchestratable_a0
                    // you can change this to EntityDocQueryOrchestratable_a1 to return new spec response
                    EntityDocQueryOrchestratable_a0 orchResponse = null;
//                    EntityDocQueryOrchestratable_a1 orchResponse = null;
                    if(responseIsSpecA0){
                        @SuppressWarnings("static-access")
                        NhinTaskExecutor<EntityDocQueryOrchestratable_a0, EntityDocQueryOrchestratable> dqexecutor =
                                new NhinTaskExecutor<EntityDocQueryOrchestratable_a0, EntityDocQueryOrchestratable>(
                                ExecutorServiceHelper.getInstance().checkExecutorTaskIsLarge(callableList.size()) ? largejobExecutor : regularExecutor,
                                callableList, transactionId);
                        dqexecutor.executeTask();
                        orchResponse = (EntityDocQueryOrchestratable_a0)dqexecutor.getFinalResponse();
                    }else{
//                        @SuppressWarnings("static-access")
//                        NhinTaskExecutor<EntityDocQueryOrchestratable_a1, EntityDocQueryOrchestratable> dqexecutor =
//                                new NhinTaskExecutor<EntityDocQueryOrchestratable_a1, EntityDocQueryOrchestratable>(
//                                ExecutorServiceHelper.getInstance().checkExecutorTaskIsLarge(callableList.size()) ? largejobExecutor : regularExecutor,
//                                callableList, transactionId);
//                        dqexecutor.executeTask();
//                        orchResponse = (EntityDocQueryOrchestratable_a1)dqexecutor.getFinalResponse();
                    }

                    response = orchResponse.getCumulativeResponse();

                    // add any errors from policyErrList to response
                    if(response.getRegistryErrorList() != null
                            && response.getRegistryErrorList().getRegistryError() != null){
                        for(RegistryError re : policyErrList.getRegistryError()){
                            response.getRegistryErrorList().getRegistryError().add(re);
                        }
                    }
                    log.debug("EntityDocQueryOrchImpl taskexecutor done and received response");
                }else{
                    log.error("No patient correlations found.");
                    response = createErrorResponse("No patient correlations found.");
                }
            }else{
                log.error("Incomplete doc query message");
                response = createErrorResponse("Incomplete/empty adhocquery message");
            }
        }catch(Exception e){
            log.error("Error occured processing doc query on entity interface: " + e.getMessage(), e);
            response = createErrorResponse("Fault encountered processing internal document query"
                    + " exception=" + e.getMessage());
        }
        auditDocQueryResponse(response, assertion, auditLog, targetHomeCommunityId);
        log.debug("Exiting EntityDocQueryOrchImpl.respondingGatewayCrossGatewayQuery...");
        return response;
    }




    /**
     *
     * @param adhocQueryRequest
     * @param assertion
     * @param targets
     * @return <code>AdhocQueryResponse</code>
     */
    public AdhocQueryResponse respondingGatewayCrossGatewayQuery(AdhocQueryRequest adhocQueryRequest,
            AssertionType assertion, NhinTargetCommunitiesType targets){
        log.debug("Entering EntityDocQueryOrchImpl.respondingGatewayCrossGatewayQuery...");

        boolean responseIsSpecA0 = true;
        AdhocQueryResponse response = null;


        CMUrlInfos urlInfoList = null;
        boolean isTargeted = false;

        // audit initial request
        DocQueryAuditLog auditLog = new DocQueryAuditLog();
        RespondingGatewayCrossGatewayQuerySecuredRequestType request = new RespondingGatewayCrossGatewayQuerySecuredRequestType();
        request.setAdhocQueryRequest(adhocQueryRequest);
        request.setNhinTargetCommunities(targets);
        String targetHomeCommunityId = HomeCommunityMap.getCommunityIdFromTargetCommunities(targets);
        auditInitialEntityRequest(request, assertion, auditLog, targetHomeCommunityId);

        try{
            if (targets != null &&
                    NullChecker.isNotNullish(targets.getNhinTargetCommunity())){
                isTargeted = true;
            }

            /*******************************************************************
             * TODO test/check handling of targets is correct!!!!!!!!!!!!!!!
             ******************************************************************/

            // Obtain all the URLs for the targets being sent to
            try{
                urlInfoList = ConnectionManagerCache.getEndpontURLFromNhinTargetCommunities(targets, NhincConstants.DOC_QUERY_SERVICE_NAME);
            }catch(Exception ex){
                log.error("Failed to obtain target URLs", ex);
            }

            // Validate that the message is not null
            if (adhocQueryRequest != null &&
                    adhocQueryRequest.getAdhocQuery() != null &&
                    NullChecker.isNotNullish(adhocQueryRequest.getAdhocQuery().getSlot())){
                List<SlotType1> slotList = adhocQueryRequest.getAdhocQuery().getSlot();
                String localAA = new EntityDocQueryHelper().getLocalAssigningAuthority(slotList);
                String uniquePatientId = new EntityDocQueryHelper().getUniquePatientId(slotList);
                log.debug("respondingGatewayCrossGatewayQuery EntityDocQueryHelper uniquePatientId: " + uniquePatientId
                        + " and localAA=" + localAA);

                 List<QualifiedSubjectIdentifierType> correlationsResult =
                         new EntityDocQueryHelper().retreiveCorrelations(slotList, urlInfoList,
                         assertion, isTargeted, getLocalHomeCommunityId());

                if(NullChecker.isNotNullish(correlationsResult)){
                    /************************************************************************
                     * We replaced the 3.2.1 connect code here with the new 3.3 concurrent fanout impl
                     * Note that all response processing is done in the NhinResponseProcessor
                    ***********************************************************************/
                    List<NhinCallableRequest<EntityDocQueryOrchestratable>> callableList =
                            new ArrayList<NhinCallableRequest<EntityDocQueryOrchestratable>>();
                    String transactionId = (UUID.randomUUID()).toString();
                    NhinDelegate nd = new NhinDocQueryDelegate();
                    NhinResponseProcessor np = new EntityDocQueryProcessor();

                    // we hold the error messages for any failed policy checks in policyErrList
                    RegistryErrorList policyErrList = new RegistryErrorList();

                    for(QualifiedSubjectIdentifierType identifier : correlationsResult){
                        NhinTargetSystemType target = new NhinTargetSystemType();

                        HomeCommunityType targetCommunity = new EntityDocQueryHelper().lookupHomeCommunityId(
                                identifier.getAssigningAuthorityIdentifier(), localAA, getLocalHomeCommunityId());
                        if(targetCommunity != null){
                            target.setHomeCommunity(targetCommunity);
                        }

                        if(isValidPolicy(adhocQueryRequest, assertion, targetCommunity)){
                            // Replace the patient id in the document query message
                            // and clone the original adhocQueryRequest
                            DocumentQueryTransform transform = new DocumentQueryTransform();
                            AdhocQueryRequest clonedQueryRequest = transform.replaceAdhocQueryPatientId(
                                    cloneRequest(adhocQueryRequest), getLocalHomeCommunityId(),
                                    identifier.getAssigningAuthorityIdentifier(),
                                    identifier.getSubjectIdentifier());

                            EntityDocQueryOrchestratable message = new EntityDocQueryOrchestratable(
                                        nd, np, null, null, assertion, NhincConstants.DOC_QUERY_SERVICE_NAME,
                                        target, clonedQueryRequest);
                            callableList.add(new NhinCallableRequest<EntityDocQueryOrchestratable>(message));

                            log.debug(Thread.currentThread().getName() + " added NhinCallableRequest"
                                    + " for hcid=" + target.getHomeCommunity().getHomeCommunityId());
                        }else{
                            RegistryError regErr = new RegistryError();
                            regErr.setCodeContext("Policy Check Failed for homeId=" + target.getHomeCommunity().getHomeCommunityId()
                                    + " and aaId=" + identifier.getAssigningAuthorityIdentifier());
                            regErr.setErrorCode("XDSRepositoryError");
                            regErr.setSeverity("Error");
                            policyErrList.getRegistryError().add(regErr);
                        }
                    }

                    // note that this impl sets taskexecutor to return EntityDocQueryOrchestratable_a0
                    // you can change this to EntityDocQueryOrchestratable_a1 to return new spec response
                    // but need to ensure the EntityDocQueryProcessor is setup for this
                    EntityDocQueryOrchestratable_a0 orchResponse = null;
//                    EntityDocQueryOrchestratable_a1 orchResponse = null;
                    if(responseIsSpecA0){
                        @SuppressWarnings("static-access")
                        NhinTaskExecutor<EntityDocQueryOrchestratable_a0, EntityDocQueryOrchestratable> dqexecutor =
                                new NhinTaskExecutor<EntityDocQueryOrchestratable_a0, EntityDocQueryOrchestratable>(
                                ExecutorServiceHelper.getInstance().checkExecutorTaskIsLarge(correlationsResult.size()) ? largejobExecutor : regularExecutor,
                                callableList, transactionId);
                        dqexecutor.executeTask();
                        orchResponse = (EntityDocQueryOrchestratable_a0)dqexecutor.getFinalResponse();
                    }else{
//                        @SuppressWarnings("static-access")
//                        NhinTaskExecutor<EntityDocQueryOrchestratable_a1, EntityDocQueryOrchestratable> dqexecutor =
//                                new NhinTaskExecutor<EntityDocQueryOrchestratable_a1, EntityDocQueryOrchestratable>(
//                                ExecutorServiceHelper.getInstance().checkExecutorTaskIsLarge(correlationsResult.size()) ? largejobExecutor : regularExecutor,
//                                callableList, transactionId);
//                        dqexecutor.executeTask();
//                        orchResponse = (EntityDocQueryOrchestratable_a1)dqexecutor.getFinalResponse(); 
                    }

                    response = orchResponse.getCumulativeResponse();

                    // add any errors from policyErrList to response
                    if(response.getRegistryErrorList() != null
                            && response.getRegistryErrorList().getRegistryError() != null){
                        for(RegistryError re : policyErrList.getRegistryError()){
                            response.getRegistryErrorList().getRegistryError().add(re);
                        }
                    }
                    log.debug("EntityDocQueryOrchImpl taskexecutor done and received response");
                }else{
                    log.error("No patient correlations found.");
                    response = createErrorResponse("No patient correlations found.");
                }
            }else{
                log.error("Incomplete doc query message");
                response = createErrorResponse("Incomplete/empty adhocquery message");
            }
        }catch(Exception e){
            log.error("Error occured processing doc query on entity interface: " + e.getMessage(), e);
            response = createErrorResponse("Fault encountered processing internal document query"
                    + " exception=" + e.getMessage());
        }
        auditDocQueryResponse(response, assertion, auditLog, targetHomeCommunityId);
        log.debug("Exiting EntityDocQueryOrchImpl.respondingGatewayCrossGatewayQuery...");
        return response;
    }


    private void auditInitialEntityRequest(RespondingGatewayCrossGatewayQuerySecuredRequestType request,
            AssertionType assertion, DocQueryAuditLog auditLog, String targetHomeCommunityId){

        if(auditLog != null){
            auditLog.auditDQRequest(request.getAdhocQueryRequest(), assertion, targetHomeCommunityId, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE);
        }
    }

    private void auditDocQueryResponse(AdhocQueryResponse response, AssertionType assertion, 
            DocQueryAuditLog auditLog, String targetHomeCommunityId){

        if (auditLog != null) {
            AdhocQueryResponseMessageType auditMsg = new AdhocQueryResponseMessageType();
            auditMsg.setAdhocQueryResponse(response);
            auditMsg.setAssertion(assertion);
            auditLog.auditDQResponse(response, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
                    NhincConstants.AUDIT_LOG_ENTITY_INTERFACE, targetHomeCommunityId);
        }
    }


    protected String getLocalHomeCommunityId(){
        String sHomeCommunity = null;
        try{
            sHomeCommunity = PropertyAccessor.getProperty(NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.HOME_COMMUNITY_ID_PROPERTY);
        }catch(Exception ex){
            log.error(ex.getMessage());
        }
        return sHomeCommunity;
    }


     /**
     * Policy Check verification done here
     * @param queryRequest
     * @param assertion
     * @return boolean
     */
    private boolean isValidPolicy(AdhocQueryRequest queryRequest, AssertionType assertion, HomeCommunityType targetCommunity) {
        boolean isValid = false;
        AdhocQueryRequestEventType checkPolicy = new AdhocQueryRequestEventType();
        AdhocQueryRequestMessageType checkPolicyMessage = new AdhocQueryRequestMessageType();
        checkPolicyMessage.setAdhocQueryRequest(queryRequest);
        checkPolicyMessage.setAssertion(assertion);
        checkPolicy.setMessage(checkPolicyMessage);
        checkPolicy.setDirection(NhincConstants.POLICYENGINE_OUTBOUND_DIRECTION);
        checkPolicy.setInterface(NhincConstants.AUDIT_LOG_ENTITY_INTERFACE);
        checkPolicy.setReceivingHomeCommunity(targetCommunity);
        PolicyEngineChecker policyChecker = new PolicyEngineChecker();
        CheckPolicyRequestType policyReq = policyChecker.checkPolicyAdhocQuery(checkPolicy);
        PolicyEngineProxyObjectFactory policyEngFactory = new PolicyEngineProxyObjectFactory();
        PolicyEngineProxy policyProxy = policyEngFactory.getPolicyEngineProxy();
        CheckPolicyResponseType policyResp = policyProxy.checkPolicy(policyReq, assertion);
        /* if response='permit' */
        if (policyResp.getResponse().getResult().get(0).getDecision().value().equals(NhincConstants.POLICY_PERMIT)) {
            isValid = true;
        }
        return isValid;
    }


    private AdhocQueryRequest cloneRequest(AdhocQueryRequest request){
        AdhocQueryRequest newRequest = new AdhocQueryRequest();
        newRequest.setAdhocQuery(request.getAdhocQuery());
        newRequest.setComment(request.getComment());
        newRequest.setFederated(request.isFederated());
        newRequest.setFederation(request.getFederation());
        newRequest.setId(request.getId());
        newRequest.setMaxResults(request.getMaxResults());
        newRequest.setRequestSlotList(request.getRequestSlotList());
        newRequest.setResponseOption(request.getResponseOption());
        newRequest.setStartIndex(request.getStartIndex());
        return newRequest;
    }


    private AdhocQueryResponse createErrorResponse(String codeContext) {
        AdhocQueryResponse response = new AdhocQueryResponse();
        RegistryErrorList regErrList = new RegistryErrorList();
        response.setRegistryErrorList(regErrList);
        response.setStatus("urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure");
        RegistryError regErr = new RegistryError();
        regErrList.getRegistryError().add(regErr);
        regErr.setCodeContext(codeContext);
        regErr.setErrorCode("XDSRepositoryError");
        regErr.setSeverity("Error");
        return response;
    }




}
