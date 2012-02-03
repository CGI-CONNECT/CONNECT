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
package gov.hhs.fha.nhinc.common.connectionmanager;

import javax.jws.WebService;
import javax.xml.ws.BindingType;

/**
 *
 * @author Sai Valluripalli
 */
@WebService(serviceName = "NhincComponentConnectionManager", portName = "NhincComponentConnectionManagerPortSoap", endpointInterface = "gov.hhs.fha.nhinc.nhinccomponentconnectionmanager.NhincComponentConnectionManagerPortType", targetNamespace = "urn:gov:hhs:fha:nhinc:nhinccomponentconnectionmanager", wsdlLocation = "WEB-INF/wsdl/NhincComponentConnectionManager/NhincComponentConnectionManager.wsdl")
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class NhincComponentConnectionManager {

    public gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType storeAssigningAuthorityToHomeCommunityMapping(gov.hhs.fha.nhinc.common.connectionmanagerinfo.StoreAssigningAuthorityToHomeCommunityMappingRequestType storeAssigningAuthorityToHomeCommunityMappingRequest) {
        return CMServiceHelper.storeAssigningAuthorityToHomeCommunityMapping(storeAssigningAuthorityToHomeCommunityMappingRequest);
    }

    public gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunitiesType getAllCommunities(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetAllCommunitiesRequestType getAllCommunitiesRequest) {
        return CMServiceHelper.getAllCommunities(getAllCommunitiesRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.BusinessEntitiesType getAllBusinessEntities(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetAllBusinessEntitiesRequestType getAllBusinessEntitiesRequest) {
        return CMServiceHelper.getAllBusinessEntities(getAllBusinessEntitiesRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.BusinessEntityType getBusinessEntity(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetBusinessEntityRequestType getBusinessEntityRequest) {
        return CMServiceHelper.getBusinessEntity(getBusinessEntityRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.ConnectionInfosType getConnectionInfoSet(gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunitiesType homeCommunities) {
        return CMServiceHelper.getConnectionInfoSet(homeCommunities);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.ConnectionInfoEndpointsType getConnectionInfoEndpointSet(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetConnectionInfoEndpointSetRequestType getConnectionInfoEndpointSetRequest) {
        return CMServiceHelper.getConnectionInfoEndpointSet(getConnectionInfoEndpointSetRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.BusinessEntitiesType getBusinessEntitySet(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetBusinessEntitySetRequestType getBusinessEntitySetRequest) {
        return CMServiceHelper.getBusinessEntitySet(getBusinessEntitySetRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.ConnectionInfosType getConnectionInfoSetByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.HomeCommunitiesWithServiceNameType getConnectionInfoSetByServiceNameRequest) {
        return CMServiceHelper.getConnectionInfoSetByServiceName(getConnectionInfoSetByServiceNameRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.ConnectionInfoEndpointsType getConnectionInfoEndpointSetByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetConnectionInfoEndpointSetByServiceNameRequestType getConnectionInfoEndpointSetByServiceNameRequest) {
        return CMServiceHelper.getConnectionInfoEndpointSetByServiceName(getConnectionInfoEndpointSetByServiceNameRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.BusinessEntitiesType getBusinessEntitySetByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetBusinessEntitySetByServiceNameRequestType getBusinessEntitySetByServiceNameRequest) {
        return CMServiceHelper.getBusinessEntitySetByServiceName(getBusinessEntitySetByServiceNameRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.ConnectionInfoType getConnectionInfoByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.HomeCommunityWithServiceNameType homeCommunityWithServiceName) {
        return CMServiceHelper.getConnectionInfoByServiceName(homeCommunityWithServiceName);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.ConnectionInfoEndpointType getConnectionInfoEndpointByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetConnectionInfoEndpointByServiceNameRequestType getConnectionInfoEndpointByServiceNameRequest) {
        return CMServiceHelper.getConnectionInfoEndpointByServiceName(getConnectionInfoEndpointByServiceNameRequest);
    }

    /**
     * This method retrieves the business entity and Connection Information for a specific service
     * at a specific home community.
     *
     * @param part1 This contains the home community identification and the name of the service that the
     *              connection info is desired.
     * @return The connection information for the service at the specified home community.
     */
    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.BusinessEntityType getBusinessEntityByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetBusinessEntityByServiceNameRequestType getBusinessEntityByServiceNameRequest) {
        return CMServiceHelper.getBusinessEntityByServiceName(getBusinessEntityByServiceNameRequest);
    }

    /**
     * This method returns the connection information for all known home communities that support the specified
     * service.
     *
     * @param part1 The name of the service that is desired.
     * @return The connection information for each known home community that supports the specified service.
     */
    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.ConnectionInfosType getAllConnectionInfoSetByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetAllConnectionInfoSetByServiceNameRequestType getAllConnectionInfoSetByServiceNameRequest) {
        return CMServiceHelper.getAllConnectionInfoSetByServiceName(getAllConnectionInfoSetByServiceNameRequest);
    }

    /**
     * This method returns the endpoint connection information for all known home communities that
     * support the specified service.
     *
     * @param part1 The name of the service that is desired.
     * @return The endpoint connection information for each known home community that
     *         supports the specified service.
     */
    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.ConnectionInfoEndpointsType getAllConnectionInfoEndpointSetByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetAllConnectionInfoEndpointSetByServiceNameRequestType getAllConnectionInfoEndpointSetByServiceNameRequest) {
        return CMServiceHelper.getAllConnectionInfoEndpointSetByServiceName(getAllConnectionInfoEndpointSetByServiceNameRequest);
    }

    /**
     * This method returns the business entity and service connection information for all known
     * home communities that support the specified service.
     *
     * @param part1 The name of the service that is desired.
     * @return The business entity and service connection information for each known
     *         home community that supports the specified service.
     */
    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.BusinessEntitiesType getAllBusinessEntitySetByServiceName(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetAllBusinessEntitySetByServiceNameRequestType getAllBusinessEntitySetByServiceNameRequest) {
        return CMServiceHelper.getAllBusinessEntitySetByServiceName(getAllBusinessEntitySetByServiceNameRequest);
    }

    /**
     * This method causes the UDDI service information to be refreshed.
     *
     * @param part1 The only purpose for this parameter is so that the
     *              web service has a unique document that identifies this
     *              operation.  The values themselves are not used.
     * @return Whether this succeeded or failed.
     */
    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.SuccessOrFailType forceRefreshUDDICache(gov.hhs.fha.nhinc.common.connectionmanagerinfo.ForceRefreshUDDICacheRequestType forceRefreshUDDICacheRequest) {
        return CMServiceHelper.forceRefreshUDDICache(forceRefreshUDDICacheRequest);
    }

    /**
     * This method causes the Internal Connection service information to be refreshed.
     *
     * @param part1 The only purpose for this parameter is so that the
     *              web service has a unique document that identifies this
     *              operation.  The values themselves are not used.
     * @return Whether this succeeded or failed.
     */
    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.SuccessOrFailType forceRefreshInternalConnectCache(gov.hhs.fha.nhinc.common.connectionmanagerinfo.ForceRefreshInternalConnectCacheRequestType forceRefreshInternalConnectCacheRequest) {
        return CMServiceHelper.forceRefreshInternalConnectCache(forceRefreshInternalConnectCacheRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetAssigningAuthoritiesByHomeCommunityResponseType getAssigningAuthoritiesByHomeCommunity(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetAssigningAuthoritiesByHomeCommunityRequestType getAssigningAuthoritiesByHomeCommunityRequest) {
        return CMServiceHelper.getAssigningAuthoritiesByHomeCommunity(getAssigningAuthoritiesByHomeCommunityRequest);
    }

    public gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetHomeCommunityByAssigningAuthorityResponseType getHomeCommunityByAssigningAuthority(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetHomeCommunityByAssigningAuthorityRequestType getHomeCommunityByAssigningAuthorityRequest) {
        return CMServiceHelper.getHomeCommunityByAssigningAuthority(getHomeCommunityByAssigningAuthorityRequest);
    }

    public gov.hhs.fha.nhinc.common.nhinccommon.EPRType getConnectionInfoEndpontFromNhinTarget(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetConnectionInfoEndpontFromNhinTargetType getConnectionInfoEndpontFromNhinTargetRequest) {
        return CMServiceHelper.getConnectionInfoEndpontFromNhinTarget(getConnectionInfoEndpontFromNhinTargetRequest);
    }

    public gov.hhs.fha.nhinc.common.nhinccommon.UrlSetType getUrlSetFromNhinTargetCommunities(gov.hhs.fha.nhinc.common.connectionmanagerinfo.GetUrlSetByServiceNameType getConnectionInfoEndpontFromNhinTargetRequest) {
        return CMServiceHelper.getUrlSetFromNhinTargetCommunities(getConnectionInfoEndpontFromNhinTargetRequest);
    }

}
