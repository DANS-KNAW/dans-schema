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
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class ValidateXsdFilesTest {

    @DisplayName("loading should not throw")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provider")
    void readSchema(File schemaName) {
        try {
            new SchemaValidator(schemaName.toString());
        }
        catch (URISyntaxException | MalformedURLException | SAXException e) {
            assumeNoXmlLangReported(e.getMessage());
            fail(e);
        }
    }

    private static Stream<Arguments> provider() {
        var files = new File("src/main/resources/");
        var extensions = new String[] { "xsd" };
        return listFiles(files, extensions, true)
            .stream().map(Arguments::of);
    }

    public static void assumeNoXmlLangReported(String errorMessage) {
        assumeFalse(
            errorMessage.contains("Cannot resolve the name 'xml:lang'"),
            "Erratic false negative (3rd party resource not available): " + errorMessage);
    }
}
