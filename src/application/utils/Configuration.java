package application.utils;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
//import org.apache.crimson.tree.DOMImplementationImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class Configuration {
	private static Configuration instance;
	private HashMap configurationMap;
	private String fileName;
	
	private Configuration() {
		String applicationDir=CurrentDir();
		configurationMap=new HashMap();
		fileName=applicationDir+"\\fc_config.xml";		
	}
	
	/**
	 * save configuration to file fc_config.xml
	 * 	 * 
	 * @return
	 */
	private static boolean saveFile() throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document doc = factory.newDocumentBuilder().newDocument();
		
		Element root = doc.createElement("app-settings");
	    root.setAttribute("xmlns", "http://www.javacore.ru/schemas/");
	    doc.appendChild(root);
	    
		Element propertiesElement = doc.createElement("properties");
	    root.appendChild(propertiesElement);
	    Set set = instance.configurationMap.keySet();
	    if (set != null) {
	      for (Iterator iterator = set.iterator(); iterator.hasNext(); ) {
	        String key = iterator.next().toString();
	        Element propertyElement = doc.createElement("property");
	        propertyElement.setAttribute("key", key);
	        Text nameText = doc.createTextNode(instance.configurationMap.get(key).toString());
	        propertyElement.appendChild((Node) nameText);
	        propertiesElement.appendChild(propertyElement);
	      }
	    }
	    // Serialize DOM tree into file
	    File file = new File(instance.fileName);
	    
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.transform(new DOMSource(doc), new StreamResult(file));
	    return true;
	}
	
	/**
	 * load configuration from file fc_config.xml
	 * @return
	 * @throws Exception
	 */
	private static boolean loadFile() throws Exception {
				
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(instance.fileName);
		if (doc == null) {
		   throw new NullPointerException();
		}
		
		NodeList configurationNL = doc.getDocumentElement().getChildNodes();
	    if (configurationNL != null) 
	    {
	    	for (int i = 0; (i < configurationNL.getLength()); i++) 
	    	{
	    		if (configurationNL.item(i).getNodeName().equals("properties")){
	    			NodeList propertyList = configurationNL.item(i).getChildNodes();
	    			for (int j = 0; j < propertyList.getLength(); j++){
	    				NamedNodeMap attributes = propertyList.item(j).getAttributes();
	    				if (attributes != null){
	    					Node n = attributes.getNamedItem("key");
	    					NodeList childs = propertyList.item(j).getChildNodes();
	    					if (childs != null){
	    						for (int k = 0; k < childs.getLength(); k++){
	    							if (childs.item(k).getNodeType() == Node.TEXT_NODE){
	    								instance.configurationMap.put(n.getNodeValue(), childs.item(k).getNodeValue());
	    							}
	    						}
	    					}
	    				}
	    			}
	    		}
	      }
	      return true;
	    } 
	    else {
	      return false;
	    }
	}
	
	static {
		instance = new Configuration();
		try {
			loadFile();
		}
		catch(Exception ex){}
	}
	
	private static String CurrentDir(){
		String path=System.getProperty("java.class.path");
		String FileSeparator=(String)System.getProperty("file.separator");
		return path.substring(0, path.substring(0, path.indexOf(";")).lastIndexOf(FileSeparator)+1);
	}
	
	public static Object get(String key) {		
		return instance.configurationMap.get(key);
	}

	/**
	 * gets parameter by key as object, 
	 * if key not in configuration file - gets default value and write default value to configuration
	 * @param key
	 * @param deflt
	 * @return
	 */
	public static Object get(String key, Object deflt) {
	    Object obj = instance.configurationMap.get(key);
	    if (obj == null) {
	    	set(key,deflt);
	    	return deflt;
	    } else {
	      return obj;
	    }
	}
	
	/**
	 * save parametr to configuration file
	 * @param key
	 * @param value
	 */
	
	public static void set(String key,Object value) {
		instance.configurationMap.put(key, value);
		try {
			saveFile();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		}
}

