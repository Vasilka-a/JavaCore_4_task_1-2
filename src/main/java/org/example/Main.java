package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        createCSV();
        createXML();

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, "data.json");

        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        writeString(json2, "data2.json");
    }


    private static List<Employee> parseXML(String fileName) {
        List<Employee> list = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getDocumentElement();
            NodeList nodelist = root.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                Node nNode = nodelist.item(i);
                if (Node.ELEMENT_NODE == nNode.getNodeType()) {
                    Element element = (Element) nNode;
                    long id = Long.parseLong(element.getAttribute("id"));
                    String firstName = element.getAttribute("firstname");
                    String lastName = element.getAttribute("lastname");
                    String country = element.getAttribute("country");
                    int age = Integer.parseInt(element.getAttribute("age"));
                    Employee employee = new Employee(id, firstName, lastName, country, age);
                    list.add(employee);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            return csv.parse();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return List.of();
    }

    public static void createCSV() {
        String[] employee1 = "1,John,Smith,USA,25".split(",");
        String[] employee2 = "2,Ivan,Petrov,RU,23".split(",");

        try (CSVWriter writer = new CSVWriter(new FileWriter("data.csv"))) {
            writer.writeNext(employee1);
            writer.writeNext(employee2);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createXML() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element staff = document.createElement("staff");
            document.appendChild(staff);
            Element employee = document.createElement("employee");
            staff.appendChild(employee);
            employee.setAttribute("id", "1");
            employee.setAttribute("firstname", "John");
            employee.setAttribute("lastname", "Smith");
            employee.setAttribute("country", "USA");
            employee.setAttribute("age", "25");
            Element employee2 = document.createElement("employee");
            staff.appendChild(employee2);
            employee2.setAttribute("id", "2");
            employee2.setAttribute("firstname", "Ivan");
            employee2.setAttribute("lastname", "Petrov");
            employee2.setAttribute("country", "RU");
            employee2.setAttribute("age", "23");

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("data.xml"));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException | TransformerException e) {
            System.out.println(e.getMessage());
        }
    }
}