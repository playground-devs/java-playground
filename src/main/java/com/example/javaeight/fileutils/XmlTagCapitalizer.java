package com.example.javaeight.fileutils;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class XmlTagCapitalizer {

    public static void main(String[] args) throws Exception {
        // Path to the XML file (replace with your file path)
        String xmlFilePath = "/Users/gramaraju/Documents/new_workspace/java-eight/src/main/resources/input.xml";

        // Read and parse the XML file
        File inputFile = new File(xmlFilePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputFile);

        // Normalize the document (optional, but recommended)
        document.getDocumentElement().normalize();

        // Update tag names
        updateTagsToCapitalLetter(document.getDocumentElement());

        // Print updated XML
        printDocument(document);
    }

    private static void updateTagsToCapitalLetter(Node node) {
        // If the node is an element, update the tag name
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String tagName = element.getTagName();
            if (!tagName.isEmpty()) {
                // Capitalize the first letter of the tag name
                String updatedTagName = tagName.substring(0, 1).toUpperCase() + tagName.substring(1);
                // Rename the tag
                element.getOwnerDocument().renameNode(element, null, updatedTagName);
            }
        }

        // Recursively process child nodes
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            updateTagsToCapitalLetter(nodeList.item(i));
        }
    }

    private static void printDocument(Document doc) {
        // Create a transformer to print the updated XML to console
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
