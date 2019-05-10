/*
 * Copyright (c) 2009-2019, United States Government, as represented by the Secretary of Health and Human Services.
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

package gov.hhs.fha.nhinc.docquery._30.entity;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.docquery.deferredresponse.adapter.proxy.AdapterDocQueryDeferredProxyObjectFactory;
import gov.hhs.fha.nhinc.docquery.deferredresponse.adapter.proxy.AdapterDocQueryDeferredResponseQueryProxy;
import gov.hhs.fha.nhinc.document.DocumentConstants;
import gov.hhs.fha.nhinc.event.error.ErrorEventException;
import gov.hhs.fha.nhinc.exchangemgr.ExchangeManagerException;
import gov.hhs.fha.nhinc.exchangemgr.InternalExchangeManager;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import java.util.List;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import org.apache.commons.lang.StringUtils;

/**
 * @author ptambellini
 *
 */
public final class DeferredDocQueryCheck {

    private EntityDocQueryImpl entityDocQueryImpl;

    public DeferredDocQueryCheck(EntityDocQueryImpl entityDocQueryImpl)
    {
        this.entityDocQueryImpl = entityDocQueryImpl;
    }

    public AdhocQueryResponse respondingGatewayCrossGatewayQuery(AdhocQueryRequest msg, AssertionType assertion,
        NhinTargetCommunitiesType nhinTarget) {
        if (null != assertion && StringUtils.isNotBlank(assertion.getDeferredResponseEndpoint())) {

            if (msg.getId() == null) {
                String error = "AdhocQueryRequest must contain an ID to use the Deferred Response Option";
                throw new ErrorEventException(new IllegalArgumentException(),
                    createAdhocFailureWithMessage(error), error);
            }
            AdapterDocQueryDeferredProxyObjectFactory oFactory = new AdapterDocQueryDeferredProxyObjectFactory();
            AdapterDocQueryDeferredResponseQueryProxy proxy = oFactory.getAdapterDocQueryProxy();
            String results = proxy.respondingGatewayCrossGatewayQuery(msg, assertion);
            if (StringUtils.isBlank(results)) {
                String error = "New ID for AdhocQueryRequest was not generated.";
                throw new ErrorEventException(new IllegalStateException(),
                    createAdhocFailureWithMessage(error), error);

            }

            //Overwrite the ID of the AdhocQueryRequest to match the response from our newly assigned ID from the adapter
            msg.setId(results);

            String serviceName = NhincConstants.DOC_QUERY_DEFERRED_RESULTS_SERVICE_NAME;
            try {
                assertion.setDeferredResponseEndpoint(InternalExchangeManager.getInstance().getEndpointURL(serviceName,
                    NhincConstants.ADAPTER_API_LEVEL.LEVEL_a0));
            } catch (ExchangeManagerException e) {
                String error = "Could not determine URL endpoint for deferred results.";
                throw new ErrorEventException(e, createAdhocFailureWithMessage(error), error +
                    " Missing internal service binding for " + serviceName);
            }
        }
        return entityDocQueryImpl.respondingGatewayCrossGatewayQuery(msg, assertion, nhinTarget);
    }


    private static RegistryErrorList createErrorList(String value) {
        RegistryErrorList errorList = new RegistryErrorList();
        List<RegistryError> list = errorList.getRegistryError();

        RegistryError error = new RegistryError();
        error.setValue(value);
        error.setErrorCode("XDSRegistryError");
        error.setSeverity(NhincConstants.XDS_REGISTRY_ERROR_SEVERITY_ERROR);
        list.add(error);

        return errorList;
    }

    public static AdhocQueryResponse createAdhocFailureWithMessage(String value ) {
        AdhocQueryResponse adhoc = new AdhocQueryResponse();
        adhoc.setStatus(DocumentConstants.XDS_QUERY_RESPONSE_STATUS_FAILURE);
        adhoc.setRegistryErrorList(createErrorList(value));
        return adhoc;
    }

}