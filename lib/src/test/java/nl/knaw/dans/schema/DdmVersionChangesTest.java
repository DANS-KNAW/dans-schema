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
    public static void initValidators() throws Exception {
        // erratically appearing error: Cannot resolve the name 'xml:lang'
        // results in a failure of the test class and an ignored of all test methods
        ddmValidatorV2 = new SchemaValidator(xsdDir + "v2/ddm.xsd");
        ddmValidatorV1 = new SchemaValidator(xsdDir + "v1/ddm.xsd");
    }


    @Test
    public void xml_without_ddm_namespace_should_not_parse() {
        assertThatThrownBy(() -> (readXmlString("<ddm:DDM/>")).normalize())
            .isInstanceOf(SAXParseException.class)
            .hasMessage("The prefix \"ddm\" for element \"ddm:DDM\" is not bound.");
    }

    @Test
    public void v1_should_require_namespace_schema_easy_dans_knaw_nl() throws Exception {
        var result = ddmValidatorV1.validateString("<ddm:DDM  xmlns:ddm='http://schemas.dans.knaw.nl/dataset/ddm-v2/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessage("cvc-elt.1.a: Cannot find the declaration of element 'ddm:DDM'.");
    }

    @Test
    public void v2_should_require_namespace_schema_dans_knaw_nl() throws Exception {
        var result = ddmValidatorV2.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessage("cvc-elt.1.a: Cannot find the declaration of element 'ddm:DDM'.");
    }

    @Test
    public void v1_should_suggest_additional_xml_for_empty_ddm() throws Exception {
        var result = ddmValidatorV1.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:DDM' is not complete. One of")
            .hasMessageContaining(":additional-xml}' is expected.");
    }

    @Test
    public void v2_should_not_suggest_additional_xml_for_empty_dmm() throws Exception {
        var result = ddmValidatorV2.validateString("<ddm:DDM xmlns:ddm='http://schemas.dans.knaw.nl/dataset/ddm-v2/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:DDM' is not complete. One of")
            .hasMessageNotContaining("additional-xml");
    }

    @Test
    public void v2_should_require_personal_data() throws Exception {
        String xml = new DdmBuilder("http://schemas.dans.knaw.nl/dataset/ddm-v2/")
            .build();
        var result = ddmValidatorV2.validateString(xml);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:profile' is not complete. One of")
            .hasMessageContaining(":personalData}' is expected.");
    }

    @Test
    public void v2_should_validate_with_personal_data() throws Exception {
        String xml = new DdmBuilder(ddmNamespaceV2).withAdditionalProfileElement("<ddm:personalData present='No' />").build();
        assertThat(ddmValidatorV2.validateString(xml)).isEmpty();
    }

    @Test
    public void v1_should_not_allow_personal_data() throws Exception {
        String xml = new DdmBuilder(ddmNamespaceV1)
            .withAdditionalProfileElement("<ddm:personalData present='No' />")
            .build();
        var result = ddmValidatorV1.validateString(xml);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageEndingWith("Invalid content was found starting with element 'ddm:personalData'. No child element is expected at this point.");
    }

    @Test
    public void v1_should_validate_without_personal_data() throws Exception {
        String xml = new DdmBuilder(ddmNamespaceV1).build();
        assertThat(ddmValidatorV1.validateString(xml)).isEmpty();
    }

    @Test
    public void v1_should_validate_with_multiple_titles() throws Exception {
        String xml = new DdmBuilder(ddmNamespaceV1).withMultipleTiles().build();
        assertThat(ddmValidatorV1.validateString(xml)).isEmpty();
    }

    @Test
    public void v2_should_not_allow_multiple_titles() throws Exception {
        String xml = new DdmBuilder(ddmNamespaceV2).withMultipleTiles().build();
        var result = ddmValidatorV2.validateString(xml);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageMatching(".*Invalid content was found starting with element.*title}'. One of .*:description}' is expected.");
    }
}
