package nl.knaw.dans.dansschema;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static nl.knaw.dans.dansschema.XmlReader.readXmlString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DdmTest {
    SchemaValidator validatorV2 = new SchemaValidator("md/ddm/v2/ddm.xsd");
    SchemaValidator validatorV1 = new SchemaValidator("md/ddm/v1/ddm.xsd");

    public DdmTest() throws MalformedURLException, URISyntaxException, SAXException {
    }

    @Test
    public void xml_without_root_namespace_should_not_parse() {
        assertThatThrownBy(() -> (readXmlString("<ddm:DDM/>")).normalize())
            .isInstanceOf(SAXParseException.class)
            .hasMessage("The prefix \"ddm\" for element \"ddm:DDM\" is not bound.");
    }

    @Test
    public void v1_should_require_schemas_dans_knaw_nl_name_space_() throws Exception {
        var result = validatorV1.validateString("<ddm:DDM  xmlns:ddm='http://schemas.dans.knaw.nl/dataset/ddm-v2/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessage("cvc-elt.1.a: Cannot find the declaration of element 'ddm:DDM'.");
    }

    @Test
    public void v2_should_require_easy_dans_knaw_nl_name_space() throws Exception {
        var result = validatorV2.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessage("cvc-elt.1.a: Cannot find the declaration of element 'ddm:DDM'.");
    }

    @Test
    public void v1_should_allow_additional_xml_in_root() throws Exception {
        var result = validatorV1.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:DDM' is not complete. One of")
            .hasMessageContaining(":profile, ")
            .hasMessageContaining(":dcmiMetadata, ")
            .hasMessageContaining(":additional-xml}' is expected.");
    }

    @Test
    public void v2_should_not_allow_additional_xml_in_root() throws Exception {
        var result = validatorV2.validateString("<ddm:DDM xmlns:ddm='http://schemas.dans.knaw.nl/dataset/ddm-v2/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:DDM' is not complete. One of")
            .hasMessageContaining(":profile, ")
            .hasMessageContaining(":dcmiMetadata}' is expected.");
    }

    @Test
    public void v2_should_require_personal_data() throws Exception {
        String xml = simpleXml("http://schemas.dans.knaw.nl/dataset/ddm-v2/", "");
        var result = validatorV2.validateString(xml);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:profile' is not complete. One of")
            .hasMessageContaining(":personalData}' is expected.");
    }

    @Test
    public void v2_should_validate_with_personal_data() throws Exception {
        String xml = simpleXml("http://schemas.dans.knaw.nl/dataset/ddm-v2/", "<ddm:personalData present='No' />");
        assertThat(validatorV2.validateString(xml)).isEmpty();
    }

    @Test
    public void v1_should_not_allow_personal_data() throws Exception {
        String xml = simpleXml("http://easy.dans.knaw.nl/schemas/md/ddm/", "<ddm:personalData present='No' />");
        var result = validatorV1.validateString(xml);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageEndingWith("Invalid content was found starting with element 'ddm:personalData'. No child element is expected at this point.");
    }

    @Test
    public void v1_should_validate_without_personal_data() throws Exception {
        String xml = simpleXml("http://easy.dans.knaw.nl/schemas/md/ddm/", "");
        assertThat(validatorV1.validateString(xml)).isEmpty();
    }

    private String simpleXml(String rootNameSpace, String lastProfileElement) {
        String simpleXml = "<ddm:DDM"
            + "        xmlns:dc='http://purl.org/dc/elements/1.1/'"
            + "        xmlns:ddm='%s'"
            + "        xmlns:dcterms='http://purl.org/dc/terms/'"
            + "        xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
            + "    <ddm:profile>"
            + "        <dc:title>Simple example</dc:title>"
            + "        <dcterms:description>This is a simple example.</dcterms:description>"
            + "        <dc:creator>Bergman, W.A.</dc:creator>"
            + "        <ddm:created>2015-09-09</ddm:created>"
            + "        <ddm:available>2015-09-08</ddm:available>"
            + "        <ddm:audience>D16300</ddm:audience>"
            + "        <ddm:accessRights>NO_ACCESS</ddm:accessRights>"
            + "        %s"
            + "    </ddm:profile>"
            + "    <ddm:dcmiMetadata>"
            + "        <dcterms:license xsi:type='dcterms:URI'>http://opensource.org/licenses/I-just-made-this-up</dcterms:license>"
            + "        <dcterms:rightsHolder>I Lastname</dcterms:rightsHolder>"
            + "    </ddm:dcmiMetadata>"
            + "</ddm:DDM>";
        return String.format(simpleXml, rootNameSpace, lastProfileElement);
    }
}
