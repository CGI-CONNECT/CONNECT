/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.hhs.fha.nhinc.transform.policy;

import gov.hhs.fha.nhinc.common.eventcommon.AdhocQueryRequestEventType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.util.format.PatientIdFormatUtil;

import java.util.List;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;

/**
 *
 * @author rayj
 */
public class AdhocQueryTransformHelper {

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(AdhocQueryTransformHelper.class);
    private static final String ActionValue = "DocumentQueryIn";
    private static final String PatientAssigningAuthorityAttributeId = Constants.AssigningAuthorityAttributeId;
    private static final String PatientIdAttributeId = Constants.ResourceIdAttributeId;

    public static CheckPolicyRequestType transformAdhocQueryToCheckPolicy(AdhocQueryRequestEventType event) {
        log.debug("begin transformAdhocQueryToCheckPolicy");

//        if (log.isDebugEnabled()) {
//            AdhocQueryRequestEventMarshaller marshaller = new AdhocQueryRequestEventMarshaller();
//            String message = XmlUtility.serializeElementIgnoreFaults(marshaller.marshalAdhocQueryRequestEvent(event));
//            log.debug("message: [" + message + "]");
//        }

        CheckPolicyRequestType result = null;
        if (InboundOutboundChecker.IsInbound(event.getDirection())) {
            result = transformAdhocQueryInboundToCheckPolicy(event);
        }
        if (InboundOutboundChecker.IsOutbound(event.getDirection())) {
            result = transformAdhocQueryOutboundToCheckPolicy(event);
        }

        log.debug("end transformAdhocQueryToCheckPolicy");
        return result;
    }

    private static CheckPolicyRequestType transformAdhocQueryToCheckPolicyBase(AdhocQueryRequestEventType event) {
        CheckPolicyRequestType genericPolicyRequest = new CheckPolicyRequestType();

        AdhocQueryRequest docQuery = event.getMessage().getAdhocQueryRequest();

        RequestType request = new RequestType();

        request.setAction(ActionHelper.actionFactory(ActionValue));

        ResourceType resource = new ResourceType();
        AttributeHelper attrHelper = new AttributeHelper();
        resource.getAttribute().add(attrHelper.attributeFactory(PatientAssigningAuthorityAttributeId, Constants.DataTypeString, extractPatientIdentifierAssigningAuthority(docQuery)));
        String sPatientId = extractPatientIdentifierId(docQuery);
        String sStrippedPatientId = PatientIdFormatUtil.parsePatientId(sPatientId);
        log.debug("transformAdhocQueryToCheckPolicyBase: sPatientId = " + sPatientId);
        log.debug("transformAdhocQueryToCheckPolicyBase: sStrippedPatientId = " + sStrippedPatientId);
        resource.getAttribute().add(attrHelper.attributeFactory(PatientIdAttributeId, Constants.DataTypeString, sStrippedPatientId));
        request.getResource().add(resource);

        SubjectHelper subjHelp = new SubjectHelper();
        SubjectType subject = subjHelp.subjectFactory(event.getSendingHomeCommunity(), event.getMessage().getAssertion());
        request.getSubject().add(subject);

        AssertionHelper assertHelp = new AssertionHelper();
        assertHelp.appendAssertionDataToRequest(request, event.getMessage().getAssertion());

        CheckPolicyRequestType policyRequest = new CheckPolicyRequestType();
        policyRequest.setRequest(request);
        genericPolicyRequest.setRequest(request);
        genericPolicyRequest.setAssertion(event.getMessage().getAssertion());
        return genericPolicyRequest;
    }

    private static CheckPolicyRequestType transformAdhocQueryInboundToCheckPolicy(AdhocQueryRequestEventType event) {
        return transformAdhocQueryToCheckPolicyBase(event);
    }

    private static CheckPolicyRequestType transformAdhocQueryOutboundToCheckPolicy(AdhocQueryRequestEventType event) {
        CheckPolicyRequestType checkPolicy = transformAdhocQueryToCheckPolicyBase(event);
        AttributeHelper attrHelper = new AttributeHelper();
        checkPolicy.getRequest().getResource().get(0).getAttribute().add(attrHelper.attributeFactory(Constants.HomeCommunityAttributeId, Constants.DataTypeString, CommunityHelper.extractCommunityId(event.getReceivingHomeCommunity())));
        return checkPolicy;
    }

    public static String extractPatientIdentifierId(AdhocQueryRequest docQuery) {
        //return PatientIdFormatUtil.parsePatientId(extractPatientIdentifier(docQuery));
        return extractPatientIdentifier(docQuery);
    }

    public static String extractPatientIdentifierAssigningAuthority(AdhocQueryRequest docQuery) {
        return PatientIdFormatUtil.parseCommunityId(extractPatientIdentifier(docQuery));
    }

    private static String extractPatientIdentifier(AdhocQueryRequest docQuery) {
        String patientIdentifier = null;

        if ((docQuery != null) && (docQuery.getAdhocQuery() != null)) {
            List<SlotType1> slots = docQuery.getAdhocQuery().getSlot();
            for (SlotType1 slot : slots) {
                if ((slot.getName() != null) && (slot.getName().contentEquals("$XDSDocumentEntryPatientId"))) {
                    if (slot.getValueList() != null) {
                        if (slot.getValueList().getValue().size() == 1) {
                            patientIdentifier = slot.getValueList().getValue().get(0);
                        }
                    }
                }
            }
        }
        return patientIdentifier;
    }
}
