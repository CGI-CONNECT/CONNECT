/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  
 * Copyright 2010(Year date of delivery) United States Government, as represented by the Secretary of Health and Human Services.  All rights reserved.
 *  
 */
package gov.hhs.fha.nhinc.docquery.nhin.deferred.response;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author JHOPPESC
 */
@WebService(serviceName = "RespondingGateway_QueryDeferredResponse_Service", portName = "RespondingGateway_QueryDeferredResponse_Port_Soap", endpointInterface = "ihe.iti.xds_b._2007.RespondingGatewayQueryDeferredResponsePortType", targetNamespace = "urn:ihe:iti:xds-b:2007", wsdlLocation = "WEB-INF/wsdl/NhinDocQueryDeferredResponse/NhinDocQueryDeferredResponse.wsdl")
@BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
@Addressing(enabled = true)
public class NhinDocQueryDeferredResponse {

    @Resource
    private WebServiceContext context;
    private static final Log log = LogFactory.getLog(NhinDocQueryDeferredResponse.class);

    public gov.hhs.healthit.nhin.DocQueryAcknowledgementType respondingGatewayCrossGatewayQuery(AdhocQueryResponse body) {
        return new NhinDocQueryDeferredResponseImpl().respondingGatewayCrossGatewayQuery(body, context);
    }
}
