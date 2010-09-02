package gov.hhs.fha.nhinc.policyengine.adapter.pdp.proxy;

import gov.hhs.fha.nhinc.properties.PropertyAccessException;

import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.xacml.common.XACMLException;
import com.sun.identity.xacml.context.Request;
import com.sun.identity.xacml.context.Response;

public interface AdapterPDPProxy
{
	public Response processPDPRequest(Request pdpRequest) throws PropertyAccessException, XACMLException, SAML2Exception;
}
