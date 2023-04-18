package nl.knaw.dans.dansschema;

import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SchemaValidator {
    private final Schema schema;
    public SchemaValidator(String path) throws URISyntaxException, MalformedURLException, SAXException {
        URI schemaLocation = new URI("file://"+new File("src/main/resources/"+path).getAbsolutePath());
        schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
            .newSchema(new URL(schemaLocation.toASCIIString()));
    }
    public List<SAXParseException> validateString(String xml) throws IOException, SAXException, ParserConfigurationException {
        return validateDocument(XmlReader.readXmlString(xml));
    }
    public List<SAXParseException> validateFile(File xml) throws IOException, SAXException, ParserConfigurationException {
        return validateDocument(XmlReader.readXmlFile(xml));
    }
    private List<SAXParseException> validateDocument(Node node) throws IOException, SAXException {

        var validator = schema.newValidator();
        var exceptions = new ArrayList<SAXParseException>();

        validator.setErrorHandler(new ErrorHandler() {

            // TODO verify that a warning should also result in an error
            @Override
            public void warning(SAXParseException e) {
                exceptions.add(e);
            }

            @Override
            public void error(SAXParseException e) {
                exceptions.add(e);
            }

            @Override
            public void fatalError(SAXParseException e) {
                exceptions.add(e);
            }
        });

        validator.validate(new DOMSource(node));

        return exceptions;
    }
}