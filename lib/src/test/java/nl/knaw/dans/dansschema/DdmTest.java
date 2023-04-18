package nl.knaw.dans.dansschema;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static nl.knaw.dans.dansschema.XmlReader.readXmlString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DdmTest {
    SchemaValidator validatorV2 = new SchemaValidator("md/ddm/v2/ddm.xsd");
    SchemaValidator validatorV1 = new SchemaValidator("md/ddm/v1/ddm.xsd");

    public DdmTest() throws MalformedURLException, URISyntaxException, SAXException {
    }

    @Test
    public void too_simple_does_not_parse() {
        assertThatThrownBy(() -> (readXmlString("<ddm:DDM/>")).normalize())
            .isInstanceOf(SAXParseException.class)
            .hasMessage("The prefix \"ddm\" for element \"ddm:DDM\" is not bound.");
    }

    @Test
    public void empty_does_not_validate_with_v2() throws Exception {
        var result = validatorV2.validateString("<ddm:DDM xmlns:ddm='http://schemas.dans.knaw.nl/dataset/ddm-v2/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:DDM' is not complete. One of")
            .hasMessageContaining(":profile, ")
            .hasMessageContaining(":dcmiMetadata}' is expected.");
    }

    @Test
    public void old_name_space_does_not_validate_with_v2() throws Exception {
        var result = validatorV2.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessage("cvc-elt.1.a: Cannot find the declaration of element 'ddm:DDM'.");
    }

    @Test
    public void empty_does_not_validate_with_v1() throws Exception {
        var result = validatorV1.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessageContaining("The content of element 'ddm:DDM' is not complete. One of")
            .hasMessageContaining(":profile, ")
            .hasMessageContaining(":dcmiMetadata, ")
            .hasMessageContaining(":additional-xml}' is expected.");
    }

    @Test
    public void new_name_space_does_not_validate_with_v1() throws Exception {
        var result = validatorV2.validateString("<ddm:DDM xmlns:ddm='http://easy.dans.knaw.nl/schemas/md/ddm/'/>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .hasMessage("cvc-elt.1.a: Cannot find the declaration of element 'ddm:DDM'.");
    }

    @ParameterizedTest
    @MethodSource("v1_resources")
    void v1_resources_validate(File input) throws Exception {
        assertThat(validatorV1.validateFile(input)).isEmpty();
    }

    private static Stream<File> v1_resources() {
        return FileUtils.listFiles(new File("src/test/resources/md/ddm/v1/"), null, false).stream();
    }

    @ParameterizedTest
    @MethodSource("v2_resources")
    void v2_resources_validate(File input) throws Exception {
        // example1/2: added personal data, dropped additional-xml
        assertThat(validatorV2.validateFile(input)).isEmpty();
    }

    private static Stream<File> v2_resources() {
        return FileUtils.listFiles(new File("src/test/resources/md/ddm/v2/"), null, false).stream();
    }
}
