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
package nl.knaw.dans.dansschema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SchemaTest {

    @DisplayName("should not throw")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provider")
    void readSchema(File schemaName) {
        assertDoesNotThrow(() -> new SchemaValidator(schemaName.toString()));
    }

    private static Stream<Arguments> provider() throws Exception {
        var files = new File("src/main/resources/");
        var extensions = new String[] { "xsd" };
        return listFiles(files, extensions, true)
            .stream()
            .filter(file -> !file.toString().endsWith("v1/provenance.xsd")) // The namespace attribute, 'http://easy.dans.knaw.nl/schemas/md/ddm/', of an <import> element information item must be identical to the targetNamespace attribute, 'http://schemas.dans.knaw.nl/dataset/ddm-v2/', of the imported document.
            .filter(file -> !file.toString().endsWith("extern/xml.xsd")) // stumbles over "<!DOCTYPE"
            .map(Arguments::of);
    }
}
