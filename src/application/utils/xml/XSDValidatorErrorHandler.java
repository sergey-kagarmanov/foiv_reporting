package application.utils.xml;

import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XSDValidatorErrorHandler implements ErrorHandler {

    private List<Exception> exceptions;
    
    public XSDValidatorErrorHandler(List<Exception> exceptions) {
        this.exceptions = exceptions;
    }
     
    @Override
    public void error(SAXParseException exception) throws SAXException {
        exceptions.add(exception);
    }
 
    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        exceptions.add(exception);
    }
 
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        exceptions.add(exception);
    }
     

}
