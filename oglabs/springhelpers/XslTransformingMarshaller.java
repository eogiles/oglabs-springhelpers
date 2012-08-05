package oglabs.springhelpers;

import org.springframework.core.io.Resource;
import org.springframework.oxm.UncategorizedMappingException;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.xml.transform.TransformerHelper;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * This class provides a way to intercept and transform a web service
 * message with XSLT prior to unmarshalling.
 * Can be used in place of the Jaxb2Marshaller in a WebServiceTemplate.
 * This marshaller should be threadsafe as it uses the threadsafe Templates
 * class and creates a new Transformer for each message.
 */
public class XslTransformingMarshaller extends Jaxb2Marshaller {
    private Templates unmarshalTemplates;
    private Class<? extends TransformerFactory> transformerFactoryClass;

    /**
     * Allows you to set a specific TransformerFactory implementation if you need something specific.
     * This is optional. By default the transformer will be whatever is available at runtime based
     * on the jvm or system settings.
     * @param transformerFactoryClass
     */
    public void setTransformerFactoryClass(Class<? extends TransformerFactory> transformerFactoryClass) {
        this.transformerFactoryClass = transformerFactoryClass;
    }

    /**
     * Use this method to set the XSLT you want to use for transforming the message
     * prior to unmarshalling.
     * @param unmarshalXslt
     * @throws IOException
     * @throws TransformerException
     */
    public void setUnmarshalXslt(Resource unmarshalXslt) throws IOException, TransformerException, ClassNotFoundException {
        //Creates a Templates object that is a threadsafe factory for Transformers based on this XSLT
        Source xsltSource = new StreamSource(unmarshalXslt.getInputStream());
        TransformerFactory transFact;
        if(transformerFactoryClass != null) {
            TransformerHelper helper = new TransformerHelper(transformerFactoryClass);
            transFact = helper.getTransformerFactory();
        } else {
            transFact = TransformerFactory.newInstance();
        }
        this.unmarshalTemplates = transFact.newTemplates(xsltSource);
    }

    @Override
    public Object unmarshal(Source source, MimeContainer mimeContainer) throws XmlMappingException {
        if(unmarshalTemplates == null) return super.unmarshal(source, mimeContainer);

        DOMSource transformedSource;
        try {
            //Transform the response
            Transformer transformer = unmarshalTemplates.newTransformer();
            DOMResult resultOfTransform = new DOMResult();
            transformer.transform(source, resultOfTransform);
            transformedSource = new DOMSource(resultOfTransform.getNode());
        } catch (TransformerException e) {
            throw new UncategorizedMappingException("Could not unmarshal due to an exception with the Transformer.", e);
        }
        return super.unmarshal(transformedSource, mimeContainer);
    }
}
