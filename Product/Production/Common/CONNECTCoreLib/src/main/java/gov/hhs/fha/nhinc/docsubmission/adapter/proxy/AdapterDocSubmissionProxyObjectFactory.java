package gov.hhs.fha.nhinc.docsubmission.adapter.proxy;

import gov.hhs.fha.nhinc.proxy.ComponentProxyObjectFactory;

/**
 * @author svalluripalli
 */
public class AdapterDocSubmissionProxyObjectFactory extends ComponentProxyObjectFactory
{
    private static final String CONFIG_FILE_NAME = "DocumentSubmissionProxyConfig.xml";
    private static final String BEAN_NAME = "adapterdocsubmission";

    protected String getConfigFileName() {
        return CONFIG_FILE_NAME;
    }

    public AdapterDocSubmissionProxy getAdapterDocSubmissionProxy() {
        return getBean(BEAN_NAME, AdapterDocSubmissionProxy.class);
    }

}
