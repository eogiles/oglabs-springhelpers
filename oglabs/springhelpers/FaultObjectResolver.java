package oglabs.springhelpers;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.soap.SoapMessage;

import java.io.IOException;

/**
 * Interface for classes that can handle faults once the fault has been converted into a JAXBElement.
 */
public interface FaultObjectResolver {

    /**
     * Does whatever it needs to do and then should throw an IOException of some sort.
     * Implementations of this will need to cast the provided Object to JAXBElement with the
     * appropriate generic type and then call getValue() to retrieve the custom fault object.
     * @param soapMessage
     * @param jaxbElement
     * @throws IOException
     */
    public void resolveFault(SoapMessage soapMessage, Object jaxbElement) throws IOException;

    /**
     * Should provide a properly configured marshaller that can be used by FaultMessageResolvers to unmarshall
     * the fault.
     * @return
     */
    public Jaxb2Marshaller getFaultUnmarshaller();
}
