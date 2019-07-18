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
import org.w3c.dom.NamedNodeMap;
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
		else {
			out_element.setAttribute("is_ticket", "0");	
		}
		
		
		if(pattern.getTransport()) {
			out_element.setAttribute("is_transport", "1");			
		}	
		else {
			out_element.setAttribute("is_transport", "0");			
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
		Report report = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);

			NodeList forms=doc.getElementsByTagName("form");
			for(int i=0;i<forms.getLength();i++)
			{
				report=parseForm(forms.item(i),dao);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return report;
	}
	
	private static Report parseForm(Node form_node,Dao dao) {
		Report report = null;
		NamedNodeMap report_parameters=form_node.getAttributes();
		String name=report_parameters.getNamedItem("name").getNodeValue();
		String path_out=report_parameters.getNamedItem("path_out").getNodeValue();
		String path_in=report_parameters.getNamedItem("path_in").getNodeValue();
		String output_path_in=report_parameters.getNamedItem("output_path_in").getNodeValue();
		String output_path_out=report_parameters.getNamedItem("output_path_out").getNodeValue();
		String archive_path_in=report_parameters.getNamedItem("archive_path_in").getNodeValue();
		String archive_path_out=report_parameters.getNamedItem("archive_path_out").getNodeValue();
		
		report=new Report();
		report.setName(name);
		report.setPathOut(path_out);
		report.setPathIn(path_in);
		report.setPathOutputIn(output_path_in);
		report.setPathOutputOut(output_path_out);
		report.setPathArchiveIn(archive_path_in);
		report.setPathArchiveOut(archive_path_out);
		
		dao.insertReport(report);				
			
		NodeList form_childs=form_node.getChildNodes();
		for(int child_index=0;child_index<form_childs.getLength();child_index++)
		{
			if(form_childs.item(child_index).getNodeName().equalsIgnoreCase("file_types")) {
				NodeList file_types=form_childs.item(child_index).getChildNodes();
				for(int ft_index=0;ft_index<file_types.getLength();ft_index++){
					parseFileTypes(file_types.item(ft_index),report,dao);
				}				
			}
			else if(form_childs.item(child_index).getNodeName().equalsIgnoreCase("chains")) {
				NodeList chains=form_childs.item(child_index).getChildNodes();
				for(int ch_index=0;ch_index<chains.getLength();ch_index++){
					parseChain(chains.item(ch_index),report,dao);
				}
			}
		}
		
		
		return report;
	}
	
	private static void parseChain(Node chain_node,Report report,Dao dao) {
		NamedNodeMap chain_parameters=chain_node.getAttributes();
		String name=chain_parameters.getNamedItem("name").getNodeValue();
		boolean direction=chain_parameters.getNamedItem("name").getNodeValue().equalsIgnoreCase("1");
		Chain chain=new Chain();
		chain.setName(name);
		chain.setDirection(direction);
		dao.insertChain(chain);
		
		//import steps and actions
		NodeList steps=chain_node.getChildNodes();
		for(int step_index=0;step_index<steps.getLength();step_index++)
		{
			NamedNodeMap step_parameters=steps.item(step_index).getAttributes();
			int position=Integer.parseInt(step_parameters.getNamedItem("position").getNodeValue());

			Node action_node=steps.item(step_index).getChildNodes().item(0);//one step has only one action
			NamedNodeMap action_parameters=action_node.getAttributes();
			Action action=dao.getAction(action_parameters.getNamedItem("name").getNodeValue());			
			ProcessStep step=new ProcessStep();
			step.setAction(action);
			step.setData(action_parameters.getNamedItem("data").getNodeValue());
			step.setPosition(position);
			dao.insertStep(step, chain);
		}
		
	}
	
	private static void parseFileTypes(Node file_type_node,Report report,Dao dao) {
		NamedNodeMap file_type_parameters=file_type_node.getAttributes();
		String name=file_type_parameters.getNamedItem("name").getNodeValue();
		String mask=file_type_parameters.getNamedItem("mask").getNodeValue();
		boolean is_ticket=file_type_parameters.getNamedItem("is_ticket").getNodeValue().equalsIgnoreCase("1");
		boolean is_transport=file_type_parameters.getNamedItem("is_transport").getNodeValue().equalsIgnoreCase("1");
		int ft=Integer.parseInt(file_type_parameters.getNamedItem("file_type").getNodeValue());
		boolean direction=file_type_parameters.getNamedItem("direction").getNodeValue().equalsIgnoreCase("in");
		
		FileType file_type=new FileType();
		file_type.setName(name);
		file_type.setMask(mask);
		file_type.setTicket(is_ticket);
		file_type.setTransport(is_transport);
		file_type.setFileType(ft);
		file_type.setDirection(direction);
		dao.insertFileType(file_type, report.getId());
		
		NodeList file_type_childs=file_type_node.getChildNodes();
		for(int child_index=0;child_index<file_type_childs.getLength();child_index++){
			if(file_type_childs.item(child_index).getNodeName().equalsIgnoreCase("attribute")){
				parseAttribute(file_type_childs.item(child_index),file_type,dao);
			}
		}		
	}
	
	private static void parseAttribute(Node attribute_node,FileType file_type,Dao dao) {
		NamedNodeMap attribute_parameters=attribute_node.getAttributes();
		String place=attribute_parameters.getNamedItem("place").getNodeValue();
		boolean in_name=attribute_parameters.getNamedItem("in_name").getNodeValue().equalsIgnoreCase("1");
		String etc=attribute_parameters.getNamedItem("etc").getNodeValue();
		FileAttribute attr=dao.getFileAttribute(attribute_parameters.getNamedItem("operation_id").getNodeValue());
		
		AttributeDescr attribute=new AttributeDescr();
		attribute.setFile(file_type);
		attribute.setInName(in_name);
		attribute.setLocation(place);
		attribute.setEtc(etc);
		attribute.setAttr(attr);
		
		dao.insertAttr(attribute);
	}
	
}
