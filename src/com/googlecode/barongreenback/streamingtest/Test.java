package com.googlecode.barongreenback.streamingtest;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Test {
    public static void main(String[] args) throws XMLStreamException {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newFactory().createXMLStreamReader(Test.class.getResourceAsStream("test.xml"));

        int indentCount = 0;
        while(xmlStreamReader.hasNext()) {
            int eventType = xmlStreamReader.next();
            if (XMLStreamConstants.START_ELEMENT == eventType) {
                printSpaces(indentCount);
//                System.out.printf("<%s>\n", xmlStreamReader.getName());
                if (xmlStreamReader.getName().getLocalPart().equals("item")) {
                    System.out.println(xmlStreamReader.getText());
                }
                indentCount++;
            }

            if (XMLStreamConstants.END_ELEMENT == eventType) {
                indentCount--;
                printSpaces(indentCount);
//                System.out.printf("</%s>\n", xmlStreamReader.getName());
            }
        }
    }

    private static void printSpaces(int indent) {
        for(int i = 0; i< indent; i++) {
            System.out.print(" ");
        }
    }

}
