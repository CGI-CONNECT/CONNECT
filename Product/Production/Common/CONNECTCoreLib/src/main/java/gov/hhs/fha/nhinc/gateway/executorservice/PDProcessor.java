package gov.hhs.fha.nhinc.gateway.executorservice;

import gov.hhs.fha.nhinc.nhinclib.NhincConstants;

import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.connectmgr.UrlInfo;
import gov.hhs.fha.nhinc.transform.subdisc.HL7PRPA201306Transforms;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscovery201306Processor;
import gov.hhs.fha.nhinc.patientdiscovery.response.ResponseFactory;
import gov.hhs.fha.nhinc.patientdiscovery.response.ResponseParams;

import org.hl7.v3.RespondingGatewayPRPAIN201305UV02RequestType;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.ProxyPRPAIN201305UVProxySecuredRequestType;
import org.hl7.v3.CommunityPRPAIN201306UV02ResponseType;
import org.hl7.v3.RespondingGatewayPRPAIN201306UV02ResponseType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements the PRPAIN201306UV02 (PDClient Response) Aggregation Strategy
 * Each response returned from a CallableRequest comes to processResponse
 * where it is aggregated into the cumulativeResponse
 *
 * Also implements PD response processing:
 * Update correlation in database for this response with patient id and associated
 * assigning authority id
 *
 * Update home community id to assigning authority id mapping in database
 *
 * @author paul.eftis, zack melnick
 */
public class PDProcessor<Target extends UrlInfo,
        Request extends RespondingGatewayPRPAIN201305UV02RequestType,
        Response extends PRPAIN201306UV02,
        CumulativeResponse extends RespondingGatewayPRPAIN201306UV02ResponseType>
        extends ResponseProcessor<Target, Request, Response, CumulativeResponse>{

    private Log log = LogFactory.getLog(getClass());

    private RespondingGatewayPRPAIN201306UV02ResponseType cumulativeResponse = null;
    private AssertionType pdassertion = null;
    int count = 0;

    
    public PDProcessor(AssertionType assertion){
        super();
        pdassertion = assertion;
        cumulativeResponse = new RespondingGatewayPRPAIN201306UV02ResponseType();
    }


    @Override
    public CumulativeResponse getCumulativeResponse(){
        return (CumulativeResponse)cumulativeResponse;
    }


    /**
     * Synchronization is covered by blocking queue implemented
     * by the ExecutorCompletionService (i.e. we do not need to synchronize
     * anything in processResponse or combineResponses)
     *
     * In TaskExecutor we have the following:
     * Future<Result> fut = executorCompletionService.take();
     * which will block until a result is available.  Once result
     * is available it will be processed here and complete processing
     * before the next future is retrieved and processed, so processor does not
     * have to be concerned with synchronization.
     *
     * @param request is the RespondingGatewayPRPAIN201305UV02RequestType
     * for the PD web service client call
     * @param individual is the PRPAIN201306UV02 response returned from
     * the CallableRequest
     * @param t is the UrlInfo target that returned the response
     */
    @Override
    public void processResponse(Request request, Response individual, Target t){
        try{
            processPDResponse(request, individual, t);
        }catch(Exception e){
            // add error response for exception to cumulativeResponse
            CommunityPRPAIN201306UV02ResponseType communityResponse = new CommunityPRPAIN201306UV02ResponseType();
            communityResponse.setPRPAIN201306UV02(
                    processError(e.getMessage(), request, null));
            cumulativeResponse.getCommunityResponse().add(communityResponse);
        }
    }



    /**
     * Called from CallableRequest for any exception from the WebServiceClient
     * Generate a new PRPAIN201306UV02 with the error and the source
     * of the error
     *
     * @param String error (exception message)
     * @param request is the ProxyPRPAIN201305UVProxySecuredRequestType
     * for the PD web service client call
     * @param Target t is the UrlInfo target
     * @return Response PRPAIN201306UV02 object with the error
     */
    @Override
    public Response processError(String error, Request r, Target t){
        log.debug("PDProcessor::processError has error=" + error);
        Response response = (Response)
            new HL7PRPA201306Transforms().createPRPA201306ForErrors(
                r.getPRPAIN201305UV02(), NhincConstants.PATIENT_DISCOVERY_ANSWER_NOT_AVAIL_ERR_CODE);

        return response;
    }



    /**
     * PD response processing does the following:
     * 1. Aggregate responses
     * 2. Update correlation in database for this response with patient id and associated
     * assigning authority id
     * 3. Update home community id to assigning authority id mapping in database
     *
     * Response processing code is from connect sources
     *
     * @param current is the PRPAIN201306UV02 returned from the CallableRequest
     * @param request is the RespondingGatewayPRPAIN201305UV02RequestType sent by
     * the web service client (needed for response processing)
     * @param t is the UrlInfo target to send the web service request
     * (needed for response processing)
     * WebServiceClient
     */
    @SuppressWarnings("static-access")
    private void processPDResponse(RespondingGatewayPRPAIN201305UV02RequestType request,
            PRPAIN201306UV02 current, Target t) throws Exception{
        
        // for debug
        count++;
        log.debug("PDProcessor::processPDResponse combine next response count=" + count);

        try{
            // store the correlation result and handle Trust/Verify Mode
            ProxyPRPAIN201305UVProxySecuredRequestType oProxyPRPAIN201305UVProxySecuredRequestType =
                    new ProxyPRPAIN201305UVProxySecuredRequestType();
            oProxyPRPAIN201305UVProxySecuredRequestType.setPRPAIN201305UV02(request.getPRPAIN201305UV02());
            
            NhinTargetSystemType target = new NhinTargetSystemType();
            HomeCommunityType home = new HomeCommunityType();
            home.setHomeCommunityId(t.getHcid());
            target.setHomeCommunity(home);
            target.setUrl(t.getUrl());
            oProxyPRPAIN201305UVProxySecuredRequestType.setNhinTargetSystem(target);
           
            ResponseParams params = new ResponseParams();
            params.assertion = pdassertion;
            params.origRequest = oProxyPRPAIN201305UVProxySecuredRequestType;
            params.response = current;

            // process response (store correlation and handle trust/verify mode)
            current = new ResponseFactory().getResponseMode().processResponse(params);
            // store the AA to HCID mapping
            new PatientDiscovery201306Processor().storeMapping(current);

            // aggregate the response
            CommunityPRPAIN201306UV02ResponseType communityResponse = new CommunityPRPAIN201306UV02ResponseType();
            communityResponse.setPRPAIN201306UV02(current);
            cumulativeResponse.getCommunityResponse().add(communityResponse);
            log.debug("PDProcessor::processPDResponse done count=" + count);
        }catch(Exception ex){
            ExecutorServiceHelper.getInstance().outputCompleteException(ex);
            throw ex;
        }
    }

}
