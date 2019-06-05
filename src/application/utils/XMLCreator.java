package application.utils;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class XMLCreator {

	public static String create(Report report, Dao dao, File file) {
		String content = "";
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("form");
			doc.appendChild(rootElement);
			
			Attr name = doc.createAttribute("name");
			rootElement.setAttributeNode(name);
			name.setValue(report.getName());
			
			Element input = doc.createElement("input");
			rootElement.appendChild(input);
			
			/*
			 * Input
			 */
			Element ti = doc.createElement("transport");
			input.appendChild(ti);
			Attr tiName = doc.createAttribute("name");
			tiName.setValue(report.getTransportInPattern().getName());
			ti.setAttributeNode(tiName);
			Attr tiMask = doc.createAttribute("mask");
			tiMask.setValue(report.getTransportInPattern().getMask());
			ti.setAttributeNode(tiMask);
			
			final Element imsgs = doc.createElement("messages");
			input.appendChild(imsgs);
			
			Element msg = null;
			Attr msgName = null;
			Attr msgMask = null;
			Attr msgSchema = null;
			Element msgAttrs = null;
			Element msgAttr = null;
			Attr attrName = null;
			Attr attrInName = null;
			Attr attrLoc = null;
			Attr attrAdd = null;
			Attr msgType = null;
			for(FileType fType : report.getPatternIn()){
				msg = doc.createElement("message");
				imsgs.appendChild(msg);
				
				msgName = doc.createAttribute("name");
				msgName.setValue(fType.getName());
				msg.setAttributeNode(msgName);
				
				msgMask = doc.createAttribute("mask");
				msgMask.setValue(fType.getMask());
				msg.setAttributeNode(msgMask);
				
				msgSchema = doc.createAttribute("schema");
				msgSchema.setValue(fType.getValidationSchema());
				msg.setAttributeNode(msgSchema);
				
				msgType = doc.createAttribute("type");
				msg.setAttributeNode(msgType);
				msgType.setValue(fType.getFileType().toString());
				
				msgAttrs = doc.createElement("attributes");
				msg.appendChild(msgAttrs);
				
				
				
				for(AttributeDescr attr : dao.getAttributes(fType).values()){
					msgAttr = doc.createElement("attribute");
					msgAttrs.appendChild(msgAttr);
					
					attrName = doc.createAttribute("name");
					attrName.setValue(attr.getAttr().getName());
					msgAttr.setAttributeNode(attrName);
					
					attrInName = doc.createAttribute("in_name");
					attrInName.setValue(attr.getInName().toString());
					msgAttr.setAttributeNode(attrInName);
					
					attrLoc = doc.createAttribute("location");
					attrLoc.setValue(attr.getLocation());
					msgAttr.setAttributeNode(attrLoc);
					
					attrAdd = doc.createAttribute("addition");
					attrAdd.setValue(attr.getEtc());
					msgAttr.setAttributeNode(attrAdd);
					
				}
			}
			
			Element tickets = doc.createElement("tickets");
			input.appendChild(tickets);
			for(FileType fType : report.getTickets()){
				msg = doc.createElement("ticket");
				tickets.appendChild(msg);
				
				msgName = doc.createAttribute("name");
				msgName.setValue(fType.getName());
				msg.setAttributeNode(msgName);
				
				msgMask = doc.createAttribute("mask");
				msgMask.setValue(fType.getMask());
				msg.setAttributeNode(msgMask);
				
				msgSchema = doc.createAttribute("schema");
				msgSchema.setValue(fType.getValidationSchema());
				msg.setAttributeNode(msgSchema);
				
				msgType = doc.createAttribute("type");
				msg.setAttributeNode(msgType);
				msgType.setValue(fType.getFileType().toString());

				msgAttrs = doc.createElement("attributes");
				msg.appendChild(msgAttrs);
				
				for(AttributeDescr attr : dao.getAttributes(fType).values()){
					msgAttr = doc.createElement("attribute");
					msgAttrs.appendChild(msgAttr);
					
					attrName = doc.createAttribute("name");
					attrName.setValue(attr.getAttr().getName());
					msgAttr.setAttributeNode(attrName);
					
					attrInName = doc.createAttribute("in_name");
					attrInName.setValue(attr.getInName().toString());
					msgAttr.setAttributeNode(attrInName);
					
					attrLoc = doc.createAttribute("location");
					attrLoc.setValue(attr.getLocation());
					msgAttr.setAttributeNode(attrLoc);
					
					attrAdd = doc.createAttribute("addition");
					attrAdd.setValue(attr.getEtc());
					msgAttr.setAttributeNode(attrAdd);
				}
				
			}
			
			Element iProc = doc.createElement("processing");
			input.appendChild(iProc);
			Element chains = doc.createElement("chains");
			iProc.appendChild(chains);
			Element eChain = null;
			Element steps = null;
			Element eStep = null;
			Attr data = null;
			Element key = null;
			Attr action = null;
			Attr position = null;
			for(Chain chain : dao.getChains(report, Constants.IN)){
				eChain = doc.createElement("chain");
				chains.appendChild(eChain);
				name = doc.createAttribute("name");
				name.setValue(chain.getName());
				eChain.setAttributeNode(name);
				steps = doc.createElement("steps");
				eChain.appendChild(steps);
				for(ProcessStep step : chain.getSteps()){
					eStep = doc.createElement("step");
					steps.appendChild(eStep);
					
					data = doc.createAttribute("data");
					data.setValue(step.getData());
					eStep.setAttributeNode(data);
					
					if (step.getKey()!=null){
						key = doc.createElement("key");
						eStep.appendChild(key);
						
						name = doc.createAttribute("name");
						name.setValue(step.getKey().getName());
						key.setAttributeNode(name);
						
						data = doc.createAttribute("data");
						data.setValue(step.getKey().getData());
						key.setAttributeNode(data);
					}
					
					action = doc.createAttribute("action");
					action.setValue(step.getAction().getName());
					eStep.setAttributeNode(action);
					
					position = doc.createAttribute("position");
					position.setValue(step.getPosition().toString());
					eStep.setAttributeNode(position);
					
				}
			}
			
			/**
			 * Output
			 */
			Element output = doc.createElement("output");
			rootElement.appendChild(output);
			Element to = doc.createElement("transport");
			output.appendChild(to);
			Attr oname = doc.createAttribute("name");
			to.setAttributeNode(oname);
			oname.setValue(report.getTransportOutPattern().getName());
			Attr omask = doc.createAttribute("mask");
			omask.setValue(report.getTransportOutPattern().getMask());
			to.setAttributeNode(omask);
			
			Element omsgs = doc.createElement("messages");
			output.appendChild(omsgs);
			for(FileType fType : report.getPatternOut()){
				msg = doc.createElement("message");
				omsgs.appendChild(msg);
				
				msgName = doc.createAttribute("name");
				msgName.setValue(fType.getName());
				msg.setAttributeNode(msgName);
				
				msgMask = doc.createAttribute("mask");
				msgMask.setValue(fType.getMask());
				msg.setAttributeNode(msgMask);
				
				msgSchema = doc.createAttribute("schema");
				msgSchema.setValue(fType.getValidationSchema());
				msg.setAttributeNode(msgSchema);
				
				msgType = doc.createAttribute("type");
				msg.setAttributeNode(msgType);
				msgType.setValue(fType.getFileType().toString());

				msgAttrs = doc.createElement("attributes");
				msg.appendChild(msgAttrs);
				
				for(AttributeDescr attr : dao.getAttributes(fType).values()){
					msgAttr = doc.createElement("attribute");
					msgAttrs.appendChild(msgAttr);
					
					attrName = doc.createAttribute("name");
					attrName.setValue(attr.getAttr().getName());
					msgAttr.setAttributeNode(attrName);
					
					attrInName = doc.createAttribute("in_name");
					attrInName.setValue(attr.getInName().toString());
					msgAttr.setAttributeNode(attrInName);
					
					attrLoc = doc.createAttribute("location");
					attrLoc.setValue(attr.getLocation());
					msgAttr.setAttributeNode(attrLoc);
					
					attrAdd = doc.createAttribute("addition");
					attrAdd.setValue(attr.getEtc());
					msgAttr.setAttributeNode(attrAdd);
				}
			}
			
			Element oProc = doc.createElement("processing");
			output.appendChild(oProc);
			chains = doc.createElement("chains");
			oProc.appendChild(chains);
			for(Chain chain : dao.getChains(report, Constants.OUT)){
				eChain = doc.createElement("chain");
				chains.appendChild(eChain);
				name = doc.createAttribute("name");
				name.setValue(chain.getName());
				eChain.setAttributeNode(name);
				steps = doc.createElement("steps");
				eChain.appendChild(steps);
				for(ProcessStep step : chain.getSteps()){
					eStep = doc.createElement("step");
					steps.appendChild(eStep);
					
					data = doc.createAttribute("data");
					data.setValue(step.getData());
					eStep.setAttributeNode(data);
					
					if (step.getKey()!=null){
						key = doc.createElement("key");
						eStep.appendChild(key);
						
						name = doc.createAttribute("name");
						name.setValue(step.getKey().getName());
						key.setAttributeNode(name);
						
						data = doc.createAttribute("data");
						data.setValue(step.getKey().getData());
						key.setAttributeNode(data);
					}
					
					action = doc.createAttribute("action");
					action.setValue(step.getAction().getName());
					eStep.setAttributeNode(action);
					
					position = doc.createAttribute("position");
					position.setValue(step.getPosition().toString());
					eStep.setAttributeNode(position);
					
				}
			}

			
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
