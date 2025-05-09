package com.b2s.shop.common.order.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;

public class XMLUtil {

    private static final Logger logger = LoggerFactory.getLogger(XMLUtil.class);

    /*
	 * get single unique value from the xml
	 * <xml><sid>mysid</sid><code>mycode</code></xml>
	 */
	public static String getUniqueValue(String xmlDoc, String tagName){
		xmlDoc =  "<xml>"+xmlDoc+"</xml>";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// disable external entities
		 try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
		} catch (ParserConfigurationException e) {
			 logger.error("Error while parsing OptionXML to map structure..");
		}
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader( xmlDoc)));
			NodeList list = document.getElementsByTagName(tagName);
			return list.item(0).getTextContent();

		} catch (Exception e) {
			logger.error("Error parsing string",e);
		}
		return null;
	}

	public static void main (String[] args){
		String s = "<sid>haha</sid><code>mycode</code>";
		logger.debug(getUniqueValue(s,"sid"));

	}

}
