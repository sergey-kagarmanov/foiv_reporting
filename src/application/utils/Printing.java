package application.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;


import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.Sides;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;


public class Printing {
	public static void printFormattedXML(String fileName,String fileContent)
	{	
			String formattedXML=toPrettyXmlString(convertStringToDocument(fileContent));
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


