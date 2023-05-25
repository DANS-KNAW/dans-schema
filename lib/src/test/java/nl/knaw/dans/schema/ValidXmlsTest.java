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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class ValidXmlsTest {

    @DisplayName("Should validate")
    @ParameterizedTest(name = "{index}: {1} --- {0}")
    @MethodSource("provider")
    void validate(File file, String schemaNameOrErrorMessage, SchemaValidator validator) throws Exception {
        if (validator == null)
            fail(schemaNameOrErrorMessage);
        else
            assertThat(validator.validateFile(file)).isEmpty();
    }

    private static Stream<Arguments> provider() {
        return listFiles(new File("src/main/resources/"), new String[] { "xsd" }, true)
            .stream().flatMap(ValidXmlsTest::toXmlFiles);
    }

    private static Stream<Arguments> toXmlFiles(File xsdFile) {
        var dirName = xsdFile.toString()
            .replace("/main/", "/test/")
            .replace(".xsd", "");
        var dirWithXmls = new File(dirName);
        if (!dirWithXmls.exists())
            return Stream.of();
        try {
            var validator = new SchemaValidator(xsdFile.toString());
            return listFiles(dirWithXmls, new String[] { "xml" }, true)
                .stream().map(file -> Arguments.of(file, xsdFile.toString(), validator));
        }
        catch (URISyntaxException | MalformedURLException | SAXException e) {
            return Stream.of(Arguments.of(null, String.format("%s --- %s", xsdFile, e), null));
        }
    }
}
