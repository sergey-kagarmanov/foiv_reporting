package application.utils;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;


public class Printing {
	public static void printFormattedXML(String fileName,byte[] fileContent) throws UnsupportedEncodingException
	{	
			
			String contentWOSign=new String(fileContent,("Windows-1251"));
			
			if(fileContent[0]==0x30 && fileContent[1]==(byte)0x82)
			{
				contentWOSign=new String(getPKCS7data(fileContent),("Windows-1251"));				
			}
			
			
			String formattedXML=toPrettyXmlString(convertStringToDocument(contentWOSign));
			formattedXML=fileName+"\n"+formattedXML;			
			try 
	        {	           
				PrintService service = PrintServiceLookup.lookupDefaultPrintService();
				
				PrinterJob job = PrinterJob.getPrinterJob();
				job.setPrintService(service);
				
				PageFormat pf = job.defaultPage();
			    Paper paper = pf.getPaper();			    
			    paper.setSize(8.26 * 72, 11.6 * 72);
			    paper.setImageableArea(0.4 * 72, 0.4 * 72, 7.86 * 72, 11.2 * 72);
			    pf.setPaper(paper);
			    
			    job.setPrintable(new OutputPrinter(formattedXML),pf);			         
			    job.print();			    
		    }
		        catch (PrinterException e)
		        {
		            // Print job did not complete.
		        }
	}	    
	
	public static byte[] getPKCS7data(byte[] fileContent)
	{
		byte[] data_oid= {0x06, 0x09, 0x2A, (byte)0x86, 0x48, (byte)0x86, (byte)0xF7, 0x0D, 0x01, 0x07, 0x01};// OBJECT IDENTIFIER 1.2.840.113549.1.7.1 data (PKCS #7)
		int indexOfDataBlock=indexOfSequence(fileContent,data_oid);
		indexOfDataBlock=indexOfDataBlock+11;//skip OID;
		indexOfDataBlock=indexOfDataBlock+4;//skip Element record;
		indexOfDataBlock=indexOfDataBlock+1;//skip class tag - it must be 0x04(OCTET STRING)
		byte firstLengthByte=fileContent[indexOfDataBlock];
		Integer dataLength=0;
		if((firstLengthByte&0x80)==0) //length of data > 127
		{
			dataLength=(firstLengthByte&0x7F);
		}
		else
		{
			firstLengthByte=(byte)(firstLengthByte&0x7F);
			indexOfDataBlock=indexOfDataBlock+1;//set to first byte of length
			for(int i=0;i<firstLengthByte;i++)
			{
				dataLength=dataLength*256+(fileContent[indexOfDataBlock]&0xFF);
				indexOfDataBlock++;
			}
		}
		//reading dataLength bytes from indexOfDataBlock position
		byte[] data=new byte[dataLength];
		for(int i=0;i<dataLength;i++)
		{
			data[i]=fileContent[i+indexOfDataBlock];
		}
		return data;
	}
	
	public static int indexOfSequence(byte[] inputArray,byte[] sequence)
	{
		int result=-1;
		for(int i=0;i<inputArray.length-sequence.length;i++)
		{
			int sameBytesCount=0;
			for(int si=0;si<sequence.length && inputArray[i+si]==sequence[si];si++)
			{				
					sameBytesCount++;				
			}
			if(sameBytesCount==sequence.length)
			{
				result=i;
				break;
			}			
		}
		return result;
	}
	
	
    public static String toPrettyXmlString(Document document) {
        try {
        	removeWhitespaceNodes(document.getDocumentElement());
            TransformerFactory transformerFactory = new TransformerFactoryImpl();
            
            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();
     
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Writer out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
    public static void removeWhitespaceNodes(Element e) {
        NodeList children = e.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child instanceof Text && ((Text) child).getData().trim().length() == 0) {
                e.removeChild(child);
            }
            else if (child instanceof Element) {
                removeWhitespaceNodes((Element) child);
            }
        }
    }
    
    private static Document convertStringToDocument(String xml) {
        try {
            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new ByteArrayInputStream(xml.getBytes("Windows-1251"))));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
}


