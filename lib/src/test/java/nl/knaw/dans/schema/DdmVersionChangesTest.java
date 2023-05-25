/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.schema;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import static nl.knaw.dans.schema.XmlReader.readXmlString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DdmVersionChangesTest {
    private static final String xsdDir = "src/main/resources/md/ddm/";
    private static final String ddmNamespaceV1 = "http://easy.dans.knaw.nl/schemas/md/ddm/";
    private static final String ddmNamespaceV2 = "http://schemas.dans.knaw.nl/dataset/ddm-v2/";
    private static SchemaValidator ddmValidatorV2;
    private static SchemaValidator ddmValidatorV1;

    @BeforeAll
    public static void initValidators() throws Exception{
        ddmValidatorV2 = new SchemaValidator(xsdDir + "v2/ddm.xsd");
        ddmValidatorV1 = new SchemaValidator(xsdDir + "v1/ddm.xsd");
    }


    @Test
    public void xml_without_root_namespace_should_not_parse() {
        assertThatThrownBy(() -> (readXmlString("<ddm:DDM/>")).normalize())
            .isInstanceOf(SAXParseException.class)
            .hasMessage("The prefix \"ddm\" for element \"ddm:DDM\" is not bound.");
    }

    @Test
    public void v1_should_require_easy_dans_knaw_nl_namespace() throws Exception {
        var result = ddmValidatorV1.validateString("<ddm:DDM  xmlns:ddm='http://schemas.dans.knaw.nl/dataset/ddm-v2/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessage("cvc-elt.1.a: Cannot find the declaration of element 'ddm:DDM'.");
    }

    @Test
    public void v2_should_require_schemas_dans_knaw_nl_name_space() throws Exception {
        var result = ddmValidatorV2.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessage("cvc-elt.1.a: Cannot find the declaration of element 'ddm:DDM'.");
    }

    @Test
    public void v1_should_suggest_additional_xml_for_root() throws Exception {
        var result = ddmValidatorV1.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:DDM' is not complete. One of")
            .hasMessageContaining(":additional-xml}' is expected.");
    }

    @Test
    public void v2_should_not_suggest_additional_xml_for_root() throws Exception {
        var result = ddmValidatorV2.validateString("<ddm:DDM xmlns:ddm='http://schemas.dans.knaw.nl/dataset/ddm-v2/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:DDM' is not complete. One of")
            .hasMessageNotContaining("additional-xml");
    }

    @Test
    public void v2_should_require_personal_data() throws Exception {
        String xml = simpleXml("http://schemas.dans.knaw.nl/dataset/ddm-v2/", 1, "","");
        var result = ddmValidatorV2.validateString(xml);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:profile' is not complete. One of")
            .hasMessageContaining(":personalData}' is expected.");
    }

    @Test
    public void v2_should_validate_with_personal_data() throws Exception {
        String xml = simpleXml(ddmNamespaceV2, 1, "<ddm:personalData present='No' />","");
        assertThat(ddmValidatorV2.validateString(xml)).isEmpty();
    }

    @Test
    public void v1_should_not_allow_personal_data() throws Exception {
        String xml = simpleXml(ddmNamespaceV1, 1, "<ddm:personalData present='No' />","");
        var result = ddmValidatorV1.validateString(xml);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageEndingWith("Invalid content was found starting with element 'ddm:personalData'. No child element is expected at this point.");
    }

    @Test
    public void v1_should_validate_without_personal_data() throws Exception {
        String xml = simpleXml(ddmNamespaceV1, 1, "","");
        assertThat(ddmValidatorV1.validateString(xml)).isEmpty();
    }

    @Test
    public void v1_should_validate_with_multiple_titles() throws Exception {
        String xml = simpleXml(ddmNamespaceV1, 2, "","");
        assertThat(ddmValidatorV1.validateString(xml)).isEmpty();
    }

    @Test
    public void v2_should_not_validate_with_multiple_titles() throws Exception {
        String xml = simpleXml(ddmNamespaceV2, 2, "","");
        var result = ddmValidatorV2.validateString(xml);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageMatching(".*Invalid content was found starting with element.*title}'. One of .*:description}' is expected.");
    }

    @Test
    public void v1_should_report_invalid_usage_of_iso639_3() throws Exception {
        String xml = simpleXml(ddmNamespaceV1, 1, "", all3languageEncodingSchemes);
        var result = ddmValidatorV1.validateString(xml);
        assertThat(result).hasSize(2);
        assertThat(result.get(0))
            .hasMessage("cvc-enumeration-valid: Value 'ISO639-3' is not facet-valid with respect to enumeration '[ISO639-1, ISO639-2]'. It must be a value from the enumeration.");
        assertThat(result.get(1))
            .hasMessage("cvc-attribute.3: The value 'ISO639-3' of attribute 'encodingScheme' on element 'ddm:language' is not valid with respect to its type, 'LanguageEncodingScheme'.");
    }

    @Test
    public void v2_should_report_invalid_usage_of_iso639_1() throws Exception {
        String xml = simpleXml(ddmNamespaceV2, 1, "<ddm:personalData present='No' />", all3languageEncodingSchemes);
        var result = ddmValidatorV2.validateString(xml);
        assertThat(result).hasSize(2);
        assertThat(result.get(0))
            .hasMessageEndingWith("Value 'ISO639-1' is not facet-valid with respect to enumeration '[ISO639-2, ISO639-3]'. It must be a value from the enumeration.");
        assertThat(result.get(1))
            .hasMessage("cvc-attribute.3: The value 'ISO639-1' of attribute 'encodingScheme' on element 'ddm:language' is not valid with respect to its type, 'LanguageEncodingScheme'.");
    }
   private static final String all3languageEncodingSchemes = ""
        + "        <ddm:language encodingScheme='ISO639-1' code='fry'>West-Fries</ddm:language>"
        + "        <ddm:language encodingScheme='ISO639-2' code='ka'>Groenlands</ddm:language>"
        + "        <ddm:language encodingScheme='ISO639-3' code='ba'>Baskisch</ddm:language>";

    private static String simpleXml(String rootNameSpace, int nrOfTitles, String lastProfileElement, String dcmiElements) {
        var titles = "<dc:title>A title</dc:title>" + (nrOfTitles <= 1 ? "" : "<dc:title>Another title</dc:title>");
        var simpleXml = "<ddm:DDM"
            + "        xmlns:dc='http://purl.org/dc/elements/1.1/'"
            + "        xmlns:ddm='%s'"
            + "        xmlns:dcterms='http://purl.org/dc/terms/'"
            + "        xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
            + "    <ddm:profile>"
            + "        %s"
            + "        <dcterms:description>This is a simple example.</dcterms:description>"
            + "        <dc:creator>Bergman, W.A.</dc:creator>"
            + "        <ddm:created>2015-09-09</ddm:created>"
            + "        <ddm:available>2015-09-08</ddm:available>"
            + "        <ddm:audience>D16300</ddm:audience>"
            + "        <ddm:accessRights>NO_ACCESS</ddm:accessRights>"
            + "        %s"
            + "    </ddm:profile>"
            + "    <ddm:dcmiMetadata>"
            + "        %s"
            + "        <dcterms:license xsi:type='dcterms:URI'>http://opensource.org/licenses/I-just-made-this-up</dcterms:license>"
            + "        <dcterms:rightsHolder>I Lastname</dcterms:rightsHolder>"
            + "    </ddm:dcmiMetadata>"
            + "</ddm:DDM>";
        return String.format(simpleXml, rootNameSpace, titles, lastProfileElement, dcmiElements);
    }
}
