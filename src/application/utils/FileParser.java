package application.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import application.db.Dao;
import application.errors.ReportError;
import application.models.AttributeDescr;
import application.models.FileAttribute;
import application.models.FileType;
import application.models.Report;
import javafx.collections.ObservableList;

public class FileParser {

	private Report report;
	private boolean direction;
	private Dao dao;
	private FileType fType;
	private List<Exception> schemaExceptions;

	public FileParser(Dao dao, Report report, boolean encrypt) {
		this.dao = dao;
		this.report = report;
		this.direction = encrypt;
		schemaExceptions = new ArrayList<Exception>();
	}

	/**
	 * Parse exist file in temp directory
	 * @param file
	 * @return
	 * @throws ReportError
	 */
	public Map<String, FileAttribute> parse(File file) throws ReportError {
		Map<String, FileAttribute> attr = new HashMap<>();

		fType = getType(file);
		if (fType==null) {
			throw new ReportError("Неизвестный тип файла");
		}
		if (fType.getValidationSchema() != null && !"".equals(fType.getValidationSchema())) {
			List<Exception> list = XMLValidator.validate(file,
					new File(fType.getValidationSchema()));
			if (list.size() > 0)
				schemaExceptions.addAll(list);
		}
		
		if (fType != null) {
			Map<String, AttributeDescr> map = dao.getAttributes(fType);
			try {
				for (AttributeDescr ad : map.values()) {
					attr.put(ad.getAttr().getName(), new FileAttribute(ad.getId(),
							ad.getAttr().getName(), getValue(file, ad)));
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (ReportError e) {
				e.printStackTrace();
			}
			return attr;
		} else {
			throw new ReportError("Неизвестный тип файла " + file.getName());
		}
	}

	public List<Exception> getExceptions(){
		return schemaExceptions;
	}
	
	private String getValue(File file, AttributeDescr attributeDescr) throws ReportError {
		if (attributeDescr.getInName()) {
			return file.getName().replace(attributeDescr.getLocation(), "");// TODO:
																			// Something
																			// strange...
			//attributeDescr.getLocation()
		} else {
			if (fType.getFileType() == 0) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				try {
					dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(file);
					String[] par = attributeDescr.getLocation().split("\\|");// think
																				// about
																				// use
																				// only
																				// node
																				// name,
																				// not
																				// work.. this change, if @ is first letter value is took from attribute with name after @ 
					int i = 0;
					Node node = null;
					NodeList list = doc.getChildNodes();
					while (i < par.length) {

						int j = 0;
						boolean flag = true;
						while (j < list.getLength() && flag) {
							Node n = list.item(j);
							if (n.getNodeName().equals(par[i])) {
								flag = false;
								node = n;
							} else {
								j++;
							}
						}
						if (!flag) {
							list = node.getChildNodes();
							i++;
							if (i<par.length && par[i].startsWith("@")) {
								String parName = par[i].substring(1);
								NamedNodeMap mapAttributes = node.getAttributes();
								for (int k = 0; k<mapAttributes.getLength(); k++) {
									if (mapAttributes.item(k).getNodeName().equals(parName)) {
										return mapAttributes.item(k).getNodeValue();
									}
								}
							}
						} else if (node==null) {
							throw new ReportError("Node not found");
						} else {
							i++;
						}
					}

					if ("".equals(attributeDescr.getAttr().getValue()) || attributeDescr.getAttr() == null) {
						if (node !=null && node.getFirstChild()!=null)
							return node.getFirstChild().getNodeValue();
						else return null;
					} /*else {
						NamedNodeMap m = node.getAttributes();
						if (m.getNamedItem(par[par.length - 1]) != null)// This
																		// is
																		// for
																		// if
																		// data
																		// not
																		// in
																		// attributes
							return m.getNamedItem(par[par.length - 1]).getNodeValue();
						else {
							if (node!=null && node.getFirstChild()!=null)
							return node.getFirstChild().getNodeValue();
							else
								return null;
						}
					}*/
				} catch (ParserConfigurationException | SAXException | IOException e) {
					e.printStackTrace();
				}
			} else if (fType.getFileType() == 1) {
				String[] tmp = attributeDescr.getLocation().split(":|,");// Regexp
																			// :
																			// OR
																			// ,

				try {
					Object[] text = Files.lines(file.toPath(), Charset.forName("cp866")).toArray();
					String value = "";
					if (tmp[0].equals(tmp[2])) {
						value = (String) text[Integer.parseInt(tmp[0]) >= 0
								? Integer.parseInt(tmp[0])
								: (text.length + Integer.parseInt(tmp[0]))];
						value = (value).substring(
								(Integer.parseInt(tmp[1]) >= 0 ? Integer.parseInt(tmp[1])
										: (value.length() + Integer.parseInt(tmp[1]))),
								(Integer.parseInt(tmp[3]) >= 0 ? Integer.parseInt(tmp[3])
										: (value.length() + Integer.parseInt(tmp[3]))));
					} else {
						for (int i = (Integer.parseInt(tmp[0]) >= 0 ? Integer.parseInt(tmp[0])
								: (text.length + Integer.parseInt(tmp[0]))); i < (Integer
										.parseInt(tmp[2]) >= 0 ? Integer.parseInt(tmp[2])
												: (text.length + Integer.parseInt(tmp[2]))); i++) {
							if (i == (Integer.parseInt(tmp[0]) >= 0 ? Integer.parseInt(tmp[0])
									: (text.length + Integer.parseInt(tmp[0])))) {
								value += ((String) text[i]).substring(
										(Integer.parseInt(tmp[1]) >= 0 ? Integer.parseInt(tmp[1])
												: (((String) text[i]).length()
														+ Integer.parseInt(tmp[1]))));
							} else if (i == (Integer.parseInt(tmp[2]) >= 0
									? Integer.parseInt(tmp[2])
									: (text.length + Integer.parseInt(tmp[2])))) {
								value += ((String) text[i]).substring(0,
										(Integer.parseInt(tmp[3]) >= 0 ? Integer.parseInt(tmp[3])
												: (((String) text[i]).length()
														+ Integer.parseInt(tmp[3]))));
							} else {
								value += text[i];
							}
						}
					}
					return value;

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}
		return null;

	}

	public FileType getType(File file) {
		ObservableList<FileType> list = direction ? report.getPatternIn() : report.getPatternOut();
		if (direction)
			list.addAll(report.getTickets());
		for (FileType type : list) {
			if (Pattern.compile(type.getMask()).matcher(file.getName()).matches()) {
				return type;
			}
		}
		return null;
	}
}
