package oglabs.springhelpers;

import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Source;
import java.io.IOException;

/**
 * This class will by default extract the first MessageID, StatusCode, and DefaultMessage from
 * a Peoplesoft webservice fault and put that information into an IOException.
 * Simply configure this as the faultMessageResolver for your webServiceTemplate.
 * It can optionally be configured with a FaultObjectResolver, in which case it will unmarshal
 * the fault object from the message and then hand off to the FaultObjectResolver,
 * which should then be easily able to convert to its custom fault shape and do whatever it needs to do.
  */
public class PeoplesoftFaultMessageResolver extends SoapFaultMessageResolver implements FaultMessageResolver {
    private final static String STATUS_CODE_TAG_NAME = "StatusCode";
    private final static String MESSAGE_ID_TAG_NAME = "MessageID";
    private final static String DEFAULT_MESSAGE_TAG_NAME = "DefaultMessage";
    private FaultObjectResolver faultObjectResolver;

    @Override
    public void resolveFault(WebServiceMessage message) throws IOException {
        SoapMessage soapMessage = (SoapMessage)message;
        SoapFault fault = soapMessage.getSoapBody().getFault();

        //First, if there is an object resolver present, we try to unmarshal and allow it to handle.
        XmlMappingException suppressedException = null;
        if(this.faultObjectResolver != null) {
            Jaxb2Marshaller marshaller = this.faultObjectResolver.getFaultUnmarshaller();
            if(marshaller == null) throw new IOException("FaultObjectResolver not configured correctly. Needs faultUnmarshaller!");
            //The custom fault should be in the first <detail> node. Custom faults will usually contain
            // their own list of errors so there is usually 1 and only 1 detail node.
            Source faultSource = fault.getFaultDetail().getDetailEntries().next().getSource();
            try {
                faultObjectResolver.resolveFault(soapMessage, marshaller.unmarshal(faultSource));
            } catch (XmlMappingException e) {
                suppressedException = e;
            }
        }

        //We should only get here if there is no faultObjectResolver, if the message could not be unmarshalled, or if for some
        // reason the faultObjectResolver did not throw an exception of its own.
        Document document = soapMessage.getDocument();
        String messageId = getValueFromFirstNodeWithName(MESSAGE_ID_TAG_NAME, document);
        String statusCode = getValueFromFirstNodeWithName(STATUS_CODE_TAG_NAME, document);
        String defaultMessage = getValueFromFirstNodeWithName(DEFAULT_MESSAGE_TAG_NAME, document);

        String errMsg = messageId == null ? "Unable to unmarshal or decode webservice exception"
                : String.format("MessageID: %s, StatusCode: %s, DefaultMessage: %s", messageId, statusCode, defaultMessage);

        IOException ioe = new IOException(errMsg, new SoapFaultClientException(soapMessage));
        if(suppressedException!=null) ioe.addSuppressed(suppressedException);
        throw ioe;
    }

    private String getValueFromFirstNodeWithName(String name, Node node) {
        if(node == null) return null;
        if(node.getNodeName().equals(name)) return node.getTextContent();
        NodeList nodes = node.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++) {
            String val = getValueFromFirstNodeWithName(name, nodes.item(i));
            if(val != null) return val;
        }
        return null;
    }

    public void setFaultObjectResolver(FaultObjectResolver faultObjectResolver) {
        this.faultObjectResolver = faultObjectResolver;
    }
}
