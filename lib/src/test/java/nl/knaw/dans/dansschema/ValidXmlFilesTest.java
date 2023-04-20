package nl.knaw.dans.dansschema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidXmlFilesTest {

    @DisplayName("Should validate")
    @ParameterizedTest(name = "{index}: {1} --- {0}")
    @MethodSource("provider")
    void validate(File file, String ignoredSchemaName, SchemaValidator validator) throws Exception {
        // ignoredSchemaName is used for the name of the test
        assertThat(validator.validateFile(file)).isEmpty();
    }

    private static Stream<Arguments> provider() throws Exception {
        return Stream.of(
            argumentsForOneSchema("md/ddm/v1/ddm.xsd", "src/test/resources/md/ddm/v1/"),
            argumentsForOneSchema("md/ddm/v2/ddm.xsd", "src/test/resources/md/ddm/v2/")
        ).flatMap(identity());
    }

    private static Stream<Arguments> argumentsForOneSchema(String xsdFile, String xmlFiles) throws Exception {
        var validator = new SchemaValidator(xsdFile);
        return listFiles(new File(xmlFiles), null, false)
            .stream().map(f -> Arguments.of(f, xsdFile, validator));
    }
}
