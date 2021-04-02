package application.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import application.MainApp;
import application.errors.ReportError;
import application.models.AttributeDescr;
import application.models.FileAttribute;
import application.models.FileType;
import application.models.WorkingFile;
import application.utils.xml.XMLValidator;

public class SingleFileCheck implements Callable<WorkingFile> {

	private File file;
	private List<FileType> types;
	private WorkingFile wFile;

	public SingleFileCheck(File file, WorkingFile wFile, List<FileType> types) {
		this.file = file;
		this.types = types;
		this.wFile = wFile;
	}

	@Override
	public WorkingFile call() throws Exception {
		List<Exception> exception = null;
		if (file != null) {
			FileType cType = null;
			for (FileType type : types) {
				if (Pattern.matches(type.getMask(), file.getName())) {
					cType = type;
					break;
				}
			}
		
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wFile = new WorkingFile(WorkingFile.NEW);
			wFile.setType(cType);
			exception = XMLValidator.validate(FileUtils.splitToInputAndOutput(new FileInputStream(file), baos),
					new File(cType.getValidationSchema()));
			wFile.setData(baos.toByteArray());
			wFile.setOriginalName(file.getName());
		}else {
			FileType cType = null;
			for (FileType type : types) {
				if (Pattern.matches(type.getMask(), wFile.getName())) {
					cType = type;
					break;
				}
			}
			wFile.setType(cType);
		}
		if (exception == null || exception.size() == 0) {
			parseAttributes(wFile);
		} else {
			wFile.setExceptions(exception);
		}

		return wFile;
	}

	private Map<String, FileAttribute> parseAttributes(WorkingFile wFile) throws ReportError {
		Map<String, FileAttribute> attr = new HashMap<>();
		Map<String, AttributeDescr> map = MainApp.getDb().getAttributes(wFile.getType());
		for (AttributeDescr ad : map.values()) {
			attr.put(ad.getAttr().getName(), new FileAttribute(ad.getId(), ad.getAttr().getName(), getValue(wFile, ad)));
		}
		wFile.setAttributes(attr);
		return attr;

	}

	private String getValue(WorkingFile wFile, AttributeDescr attributeDescr) throws ReportError {
		if (attributeDescr.getInName()) {
			Pattern p = Pattern.compile(attributeDescr.getLocation());
			Matcher m = p.matcher(wFile.getName());
			if (m.find())
				return m.group(1);// First group is whole expression,
			else
				return null;
		} else {
			if (wFile.getType().getFileType() == 0) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				try {
					dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(FileUtils.getStreamWithSaveData(wFile));
					String[] par = attributeDescr.getLocation().split("\\|");
					// think about use only node name, not work.. this change,
					// if @ is first letter value is took from attribute with
					// name after @
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
							if (i < par.length && par[i].startsWith("@")) {
								String parName = par[i].substring(1);
								NamedNodeMap mapAttributes = node.getAttributes();
								for (int k = 0; k < mapAttributes.getLength(); k++) {
									if (mapAttributes.item(k).getNodeName().equals(parName)) {
										return mapAttributes.item(k).getNodeValue();
									}
								}
							}
						} else if (node == null) {
							throw new ReportError("Node not found");
						} else {
							i++;
						}
					}

					if ("".equals(attributeDescr.getAttr().getValue()) || attributeDescr.getAttr() == null) {
						if (node != null && node.getFirstChild() != null)
							return node.getFirstChild().getNodeValue();
						else
							return null;
					}
				} catch (ParserConfigurationException | SAXException | IOException e) {
					e.printStackTrace();
				}
			} else if (wFile.getType().getFileType() == 1) {
				String[] tmp = attributeDescr.getLocation().split(":|,");

				Object[] text = new String(wFile.getData(), Charset.forName("cp866")).split(System.lineSeparator());
				// Files.lines(file.toPath(),
				// Charset.forName("cp866")).toArray();
				String value = "";
				if (tmp[0].equals(tmp[2])) {
					value = (String) text[Integer.parseInt(tmp[0]) >= 0 ? Integer.parseInt(tmp[0]) : (text.length + Integer.parseInt(tmp[0]))];
					value = (value).substring(
							(Integer.parseInt(tmp[1]) >= 0 ? Integer.parseInt(tmp[1]) : (value.length() + Integer.parseInt(tmp[1]))),
							(Integer.parseInt(tmp[3]) >= 0 ? Integer.parseInt(tmp[3]) : (value.length() + Integer.parseInt(tmp[3]))));
				} else {
					for (int i = (Integer.parseInt(tmp[0]) >= 0 ? Integer.parseInt(tmp[0])
							: (text.length + Integer.parseInt(tmp[0]))); i < (Integer.parseInt(tmp[2]) >= 0 ? Integer.parseInt(tmp[2])
									: (text.length + Integer.parseInt(tmp[2]))); i++) {
						if (i == (Integer.parseInt(tmp[0]) >= 0 ? Integer.parseInt(tmp[0]) : (text.length + Integer.parseInt(tmp[0])))) {
							value += ((String) text[i]).substring((Integer.parseInt(tmp[1]) >= 0 ? Integer.parseInt(tmp[1])
									: (((String) text[i]).length() + Integer.parseInt(tmp[1]))));
						} else if (i == (Integer.parseInt(tmp[2]) >= 0 ? Integer.parseInt(tmp[2]) : (text.length + Integer.parseInt(tmp[2])))) {
							value += ((String) text[i]).substring(0, (Integer.parseInt(tmp[3]) >= 0 ? Integer.parseInt(tmp[3])
									: (((String) text[i]).length() + Integer.parseInt(tmp[3]))));
						} else {
							value += text[i];
						}
					}
				}
				return value;

			}
		}
		return null;

	}

}
