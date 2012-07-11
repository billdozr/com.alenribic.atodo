package com.alenribic.atodo.parse;

import java.io.CharArrayReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.alenribic.atodo.model.MetaInfo;
import com.alenribic.atodo.model.TodoEntry;
import com.alenribic.atodo.model.TodoEntryError;

public class TodoXMLParser {
	
	public static Document getDocument(char[] data) 
		throws Exception {	         
	    // Step 1: create a DocumentBuilderFactory
	     DocumentBuilderFactory dbf =
	      DocumentBuilderFactory.newInstance();

	    // Step 2: create a DocumentBuilder
	     DocumentBuilder db = dbf.newDocumentBuilder();

	    // Step 3: parse the input data
	     Document doc = db.parse(new InputSource(
	    		 new CharArrayReader(data)));
	     return doc;
	}
	
	private MetaInfo getMetaInfo(Node node) throws DOMException, ParseException {
		MetaInfo srcInfo = new MetaInfo();
		srcInfo.setSrcName(node.getTextContent());
		NamedNodeMap metaInfoAttrs = node.getAttributes();
		srcInfo.setSrcLine(new Integer(metaInfoAttrs.getNamedItem("line").getTextContent()));
		srcInfo.setSrcColumn(new Integer(metaInfoAttrs.getNamedItem("column").getTextContent()));
		srcInfo.setModTime(metaInfoAttrs.getNamedItem("modtime") != null 
				? new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(
						metaInfoAttrs.getNamedItem("modtime").getTextContent()) 
			    : null);
		return srcInfo;
	}
	
	public Object[] parse(String xmlTodos) throws Exception {
		List entries = new ArrayList();
		NodeList topChildElements = getDocument(xmlTodos.toCharArray())
			.getDocumentElement().getChildNodes();
		for (int i = 0; i < topChildElements.getLength(); i++) {
			Node node = topChildElements.item(i);
			if (node.getNodeName().equalsIgnoreCase("todo")) {
				TodoEntry entry = new TodoEntry();
				List<String> labels = new ArrayList<String>();
				List<String> users = new ArrayList<String>();
				NodeList todoChildElements = node.getChildNodes();
				for (int j = 0; j < todoChildElements.getLength(); j++) {
					if (todoChildElements.item(j).getNodeName().equalsIgnoreCase("subject")) {
						entry.setSubject(todoChildElements.item(j).getTextContent());
					} else if (todoChildElements.item(j).getNodeName().equalsIgnoreCase("action")) {
						entry.setAction(todoChildElements.item(j).getTextContent());
					} else if (todoChildElements.item(j).getNodeName().equalsIgnoreCase("label")) {
						labels.add(todoChildElements.item(j).getTextContent());
					} else if (todoChildElements.item(j).getNodeName().equalsIgnoreCase("user")) {
						users.add(todoChildElements.item(j).getTextContent());
					} else if (todoChildElements.item(j).getNodeName().equalsIgnoreCase("attributes")) {
						NodeList attrChildElements = todoChildElements.item(j).getChildNodes();
						for (int k = 0; k < attrChildElements.getLength(); k++) {
							if (attrChildElements.item(k).getNodeName().equalsIgnoreCase("priority")) {
								entry.setPriority(attrChildElements.item(k).getTextContent());
							} else if (attrChildElements.item(k).getNodeName().equalsIgnoreCase("time-spent")) {
								entry.setTimeSpent(new Float(attrChildElements.item(k).getTextContent()));
							}
						}
					} else if (todoChildElements.item(j).getNodeName().equalsIgnoreCase("meta-info")) {
						entry.setSrcInfo(getMetaInfo(todoChildElements.item(j)));
					}
				}
				if (labels.size() > 0) {
					String[] labelLst = new String[labels.size()];
					int c = 0;
					for (String label : labels) {
						labelLst[c] = label; 
						c++;
					}
					entry.setLabels(labelLst);
				}
				if (users.size() > 0) {
					String[] userLst = new String[users.size()];
					int c = 0;
					for (String user : users) {
						userLst[c] = user; 
						c++;
					}
					entry.setUsers(userLst);
				}
				entries.add(entry);
			} else if (node.getNodeName().equalsIgnoreCase("error")) {
				TodoEntryError error = new TodoEntryError();
				NodeList errorChildElements = node.getChildNodes();
				for (int k = 0; k < errorChildElements.getLength(); k++) {
					if (errorChildElements.item(k).getNodeName().equalsIgnoreCase("message")) {
						error.setMessage(errorChildElements.item(k).getTextContent());
					} else if (errorChildElements.item(k).getNodeName().equalsIgnoreCase("meta-info")) {
						error.setSrcInfo(getMetaInfo(errorChildElements.item(k)));
					}
				}
				entries.add(error);
			}
		}
		return entries.toArray();
	}

	public static void main(String[] args) throws Exception {
		String todos = "<?xml version='1.0' ?><todos><todo><subject>xxx123</subject><action>yyyy</action><attributes><priority>High</priority><time-spent>2</time-spent></attributes><meta-info line=\"4\" column=\"1\" modtime=\"Sun Aug  1 13:38:39 SAST 2010\">/Users/alen/Documents/java_dev/runtime-favorites/TestPrj/src/Test1.java</meta-info></todo><todo><action>Do complete main method</action><label>v612</label><user>alen</user><attributes><priority>Normal</priority></attributes><meta-info line=\"8\" column=\"1\" modtime=\"Sun Aug  1 13:38:39 SAST 2010\">/Users/alen/Documents/java_dev/runtime-favorites/TestPrj/src/Test1.java</meta-info></todo><todo><action>this is good stuff</action><meta-info line=\"2\" column=\"1\" modtime=\"Sun Aug  1 13:47:40 SAST 2010\">/Users/alen/Documents/java_dev/runtime-favorites/TestPrj/src/StringUtil.java</meta-info></todo></todos>";
		Object[] todoEntries = new TodoXMLParser().parse(todos);
		System.out.println("todoEntries.length: " + todoEntries.length);
	}
}
