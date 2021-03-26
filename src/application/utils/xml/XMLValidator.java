package application.utils.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class XMLValidator {

	public static List<Exception> validate(File xmlDocument, File xsdSchema){
		List<Exception> exceptions = new ArrayList<Exception>();
        try {
            Validator validator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(xsdSchema)
                    .newValidator();
            validator.setErrorHandler(new XSDValidatorErrorHandler(exceptions));
            validator.validate(new StreamSource(xmlDocument));
        } catch (SAXException e) {
        	exceptions.add(e);
            System.out.println(xmlDocument);
            e.printStackTrace();
        } catch (IOException ioe) {
        	exceptions.add(ioe);
        	System.out.println(xmlDocument);
        	ioe.printStackTrace();
       }
        return exceptions;
	}
	
	public static List<Exception> validate(InputStream xmlDocument, File xsdSchema){
		List<Exception> exceptions = new ArrayList<Exception>();
        try {
            Validator validator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(xsdSchema)
                    .newValidator();
            validator.setErrorHandler(new XSDValidatorErrorHandler(exceptions));
            validator.validate(new StreamSource(xmlDocument));
        } catch (SAXException e) {
        	exceptions.add(e);
            e.printStackTrace();
        } catch (IOException ioe) {
        	ioe.printStackTrace();
        }
        return exceptions;
	}

}
