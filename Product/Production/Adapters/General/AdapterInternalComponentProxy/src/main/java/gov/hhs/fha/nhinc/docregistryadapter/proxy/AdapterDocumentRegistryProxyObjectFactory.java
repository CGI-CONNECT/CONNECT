/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.hhs.fha.nhinc.docregistryadapter.proxy;

import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * An object factory that uses the Spring Framework to create service
 * implementation objects. The configuration file is referenced in the
 * common properties file location to assist in installation and configuration
 * simplicity.
 *
 * The Spring configuration file is defined by the constant "SPRING_CONFIG_FILE".
 * This file is loaded into an application context in the static initializer
 * and then the objects defined in the config file are available to the framework
 * for creation. This configuration file can be located anywhere in the
 * classpath.
 *
 * To retrieve an object that is created by the framework, the
 * "getBean(String beanId)" method is called on the application context passing
 * in the beanId that is specified in the config file. Considering the default
 * correlation definition in the config file for this component:
 * <bean id="adapterdocumentregistry" class="gov.hhs.fha.nhinc.docregistryadapter.proxy.AdapterDocumentRegistryNoOpImpl"/>
 * the bean id is "adapterdocumentregistry" and an object of this type can be retrieved from
 * the application context by calling the getBean method like:
 * context.getBean("adapterdocumentregistry");. This returns an object that can be casted to
 * the appropriate interface and then used in the application code. See the
 * getAdapterDocumentRegistryProxy() method in this class.
 *
 * @author svalluripalli
 */
public class AdapterDocumentRegistryProxyObjectFactory {
    private static final String CONFIG_FILE_NAME = "AdapterDocumentRegistryProxyConfig.xml";
    private static final String BEAN_NAME_ADAPTER_DOCUMENT_REGISTRY = "adapterdocumentregistry";
    private static ApplicationContext context = null;

    static {
        context = new FileSystemXmlApplicationContext(PropertyAccessor.getPropertyFileURL() + CONFIG_FILE_NAME);
    }

    /**
     * Retrieve an adapter Document Registry implementation using the IOC framework.
     * This method retrieves the object from the framework that has an
     * identifier of "adapterdocumentregistry."
     *
     * @return AdapterDocumentRegistryProxy
     */
    public AdapterDocumentRegistryProxy getAdapterDocumentRegistryProxy() {
        AdapterDocumentRegistryProxy adapterDocumentRegistryProxy = null;
        if (context != null) {
            adapterDocumentRegistryProxy = (AdapterDocumentRegistryProxy) context.getBean(BEAN_NAME_ADAPTER_DOCUMENT_REGISTRY);
        }
        return adapterDocumentRegistryProxy;
    }

}
