package application.utils;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import application.db.Dao;
import application.models.AttributeDescr;
import application.models.Chain;
import application.models.FileType;
import application.models.Key;
import application.models.ProcessStep;
import application.models.Report;
import application.models.FileAttribute;
import application.models.AttributeDescr;
import application.models.Action;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class XMLCreator {

	
	
	private static Element createChainElement(Chain chain,Document doc,Dao dao)	{
		Element out_element=doc.createElement("chain");
		
		out_element.setAttribute("name", chain.getName());
		out_element.setAttribute("direction", chain.getDirection()?"1":"0");
		List<ProcessStep> steps_list=chain.getSteps();		
		for(ProcessStep step:steps_list)
		{
			Element step_element=doc.createElement("step");
			step_element.setAttribute("position", step.getPosition().toString());
			
			Action action=step.getAction();
			Element action_element=doc.createElement("action");
			action_element.setAttribute("name", action.getName());
			action_element.setAttribute("data", step.getData());
			step_element.appendChild(action_element);
			out_element.appendChild(step_element);
		}
		
		
		
		return out_element;
	}
	
	private static Element createAttributeElement(AttributeDescr descriptor,Document doc) {
		Element out_element=null;		
		
		out_element=doc.createElement("attribute");
		out_element.setAttribute("in_name", descriptor.getInName()?"1":"0");
		out_element.setAttribute("place", descriptor.getLocation());
		out_element.setAttribute("etc", descriptor.getEtc());
		out_element.setAttribute("operation_id", descriptor.getAttr().getName());
		
		return out_element;
	}
	
	private static Element createPatternElement(FileType pattern,Document doc,Dao dao)
	{		
		
		final Element out_element=doc.createElement("pattern");;
				
		if(pattern.getTicket()) {
			out_element.setAttribute("is_ticket", "1");			
		}
		if(pattern.getTransport()) {
			out_element.setAttribute("is_transport", "1");			
		}	
		out_element.setAttribute("file_type", pattern.getFileType().toString());
		if(pattern.getDirection()){
			out_element.setAttribute("direction", "in");			
		}
		else {
			out_element.setAttribute("direction", "out");
		}
		out_element.setAttribute("mask", pattern.getMask());
		out_element.setAttribute("name", pattern.getName());		
		
		Map<String, AttributeDescr> attributes_map=dao.getAttributes(pattern);
		
		attributes_map.forEach((name,descriptor)->out_element.appendChild(createAttributeElement(descriptor,doc)));
		
		return out_element;
	}
	
	public static String create(Report report, Dao dao, File file) {
		String content = "";
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("form");
			doc.appendChild(rootElement);
			
			//export reports table data
			rootElement.setAttribute("name", report.getName());
			rootElement.setAttribute("path_in", report.getPathIn());
			rootElement.setAttribute("path_out", report.getPathOut());
			rootElement.setAttribute("output_path_in", report.getPathOutputIn());
			rootElement.setAttribute("output_path_out", report.getPathOutputOut());
			rootElement.setAttribute("archive_path_in", report.getPathArchiveIn());
			rootElement.setAttribute("archive_path_out", report.getPathArchiveOut());	
			
			
			//export file_types table data
			Element file_types=doc.createElement("file_types");
			List<FileType> ft_list=new ArrayList<FileType>();
			ft_list.addAll(report.getPatternIn());
			ft_list.addAll(report.getPatternOut());
			ft_list.addAll(report.getTickets());
			ft_list.add(report.getTransportInPattern());
			ft_list.add(report.getTransportOutPattern());
			for(FileType currentPattern:ft_list){
				Element patternElement=createPatternElement(currentPattern,doc,dao);
				file_types.appendChild(patternElement);
			}
			rootElement.appendChild(file_types);
			
			//export chains table data
			Element chains=doc.createElement("chains");	
			List<Chain> chains_list=new ArrayList<Chain>();
			chains_list.addAll(dao.getChains(report, null));
			
			for(Chain chain:chains_list) {
				Element chain_element=createChainElement(chain,doc,dao);
				chains.appendChild(chain_element);
			}
			rootElement.appendChild(chains);
			
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			content = file.getAbsolutePath()+"\\report_"+report.getName()+"_"+ DateUtils.formatReport(LocalDate.now())+".xml";
			File tFile = new File(content);
			StreamResult result = new StreamResult(tFile);

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

		} catch (ParserConfigurationException | TransformerException pce) {
			pce.printStackTrace();
		}

		return content;
	}

	public static Report load(File file, Dao dao){
		Report report = new Report();
		report.setPatternIn(FXCollections.observableArrayList());
		report.setPatternOut(FXCollections.observableArrayList());
		report.setTickets(FXCollections.observableArrayList());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		boolean input = false;
		boolean output = false;
		FileType fType = null;
		boolean messages = false;
		boolean message = false;
		boolean attributes = false;
		AttributeDescr attr = null;
		List<AttributeDescr> attrs = new ArrayList<>();
		boolean ticket = false;
		List<Chain> chains = new ArrayList<>();
		Chain chain = null;
		ObservableList<ProcessStep> steps;
		ProcessStep step = null;
		try{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);

			Node node = null;
			NodeList list = doc.getChildNodes();
			for(int i = 0; i<list.getLength(); i++){
				node = list.item(i);
				
				switch (node.getNodeName()){
				case "form":
					report.setName(node.getAttributes().getNamedItem("name").getNodeValue());
					break;
				case "input":
					input = true;
					break;
				case "output":
					output = true;
					break;
				case "transport":
					fType = new FileType();
					fType.setName(node.getAttributes().getNamedItem("name").getNodeValue());
					fType.setMask(node.getAttributes().getNamedItem("mask").getNodeValue());
					if (input){
						fType.setDirection(true);
						report.setTransportInPattern(fType);
					}
					if (output){
						fType.setDirection(false);
						report.setTransportOutPattern(fType);
					}
					break;
				case "messages":
					messages = true;
					break;
				case "message":
					message = true;
					fType = new FileType();
					fType.setName(node.getAttributes().getNamedItem("name").getNodeValue());
					fType.setMask(node.getAttributes().getNamedItem("mask").getNodeValue());
					fType.setValidationSchema(node.getAttributes().getNamedItem("schema").getNodeValue());
					try{
						fType.setFileType(Integer.parseInt(node.getAttributes().getNamedItem("type").getNodeValue()));
					}catch(Exception e){
						e.printStackTrace();
					}
					if (ticket){
						fType.setTicket(true);
					}
					if (input){
						report.getPatternIn().add(fType);
					}
					if (output){
						report.getPatternOut().add(fType);
					}
					break;
				case "attributes":
					attributes = true;
					break;
				case "attribute":
					attr = new AttributeDescr();
					attr.setAttr(dao.getFileAttribute(node.getAttributes().getNamedItem("name").getNodeValue()));
					attr.setEtc(node.getAttributes().getNamedItem("addition").getNodeValue());
					attr.setInName(Boolean.valueOf(node.getAttributes().getNamedItem("in_name").getNodeValue()));
					attr.setLocation(node.getAttributes().getNamedItem("location").getNodeValue());
					attr.setFile(fType);
					attrs.add(attr);
				break;	
				case "tickets":
					ticket = true;
					break;
				case "ticket":
					fType = new FileType();
					fType.setTicket(true);
					fType.setName(node.getAttributes().getNamedItem("name").getNodeValue());
					fType.setMask(node.getAttributes().getNamedItem("mask").getNodeValue());
					fType.setValidationSchema(node.getAttributes().getNamedItem("schema").getNodeValue());
					try{
						fType.setFileType(Integer.parseInt(node.getAttributes().getNamedItem("type").getNodeValue()));
					}catch(Exception e){
						e.printStackTrace();
					}
					if (ticket){
						fType.setTicket(true);
					}
					if (input){
						report.getPatternIn().add(fType);
					}
					if (output){
						report.getPatternOut().add(fType);
					}
					break;
				case "processing":
					break;
				case "chains":
					break;
				case "chain":
					chain = new Chain();
					chain.setName(node.getAttributes().getNamedItem("name").getNodeValue());
					chain.setReport(report);
					steps = FXCollections.observableArrayList();
					chain.setSteps(steps);
					break;
				case "step":
					step = new ProcessStep();
					step.setData(node.getAttributes().getNamedItem("data").getNodeValue());
					try{
						step.setPosition(Integer.parseInt(node.getAttributes().getNamedItem("position").getNodeValue()));
					}catch(Exception e){
						e.printStackTrace();
					}
					step.setAction(dao.getAction(node.getAttributes().getNamedItem("action").getNodeValue()));
					NodeList childs = node.getChildNodes();
					Node keyNode = childs.item(0);
					Key key = dao.addKey(keyNode.getAttributes().getNamedItem("name").getNodeValue(), keyNode.getAttributes().getNamedItem("data").getNodeValue());
					step.setKey(key);
				break;	
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return report;
	}
	

	
}
