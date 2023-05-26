dans-schema
===========

DANS XML schemas

DESCRIPTION
-----------
The [DANS BagIt Profile v1]{:target=_blank} requires several XML metadata files. This project includes the XML schema files to validate those XML files. These
schemas will be published on a public website. <!-- TODO: change text to point to website when it is up --> Alternatively you can
[clone and build this project](#building-from-source) which will package site up both as a jar-file and as an RPM.

!!! warning "DANS Schema vs EASY Schema"

    DANS Schema contains the schemas for the Data Stations. It is forked from the legacy project [easy-schema]{:target=_blank} which will be phased out with 
    EASY itself. 

[DANS BagIt Profile v1]: https://dans-knaw.github.io/dans-bagit-profile/versions/1.0.0/

[easy-schema]: https://github.com/DANS-KNAW/easy-schema

BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 11 or higher
* Maven 3.3.3 or higher
* RPM

Steps:

    git clone https://github.com/DANS-KNAW/dans-schema.git
    cd dans-schema 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM packaging will be activated. If `rpm` is available, but at a
different path, then activate it by using Maven's `-P` switch: `mvn -Prpm install`.

Example Resources
-----------------

The example resources directory `lib/src/test/resources` has a number of XML files that can be used
to become more familiar with the XML formats used by the DANS services.

The [DANS bag] documentation provides best practices for [namespace prefixes].
Note that actual practice also uses `dct` for `dcterms`.

The table maps formats to files in a bag.

| XSD          | file in DANS V1 bag                      |
|--------------|------------------------------------------|
| `ddm`        | `metadata/dataset.xml`                   |
| `files`      | `metadata/files.xml`                     |

[DANS bag]: https://github.com/DANS-KNAW/dans-bagit-profile/blob/master/docs/versions/1.0.0.md#dans-bagit-profile-v0
[namespace prefixes]: https://github.com/DANS-KNAW/dans-bagit-profile/blob/master/docs/versions/0.0.0.md#xml-namespaces

Directories in this `lib/src/test/resources` directory match with XSD files in the `lib/src/main/resources` directory.
Examples for XSDs that are not in the table above may be standalone,
or embedded within examples for XSDs from the table.
The namespace URI of the root element in the examples should match the
`targetNamespace` of the schema to validate the example.

```
├── bag/metadata
│   ├── agreements/agreements
│   └── files/files
├── dcx
│   ├── dcx-dai
│   └── dcx-gml
└── ddm
    ├── v1/ddm
    └── v2/ddm
```

Validation
----------

### Unit tests

* `ValidateXsdFilesTest` shows that the provided XSDs are valid.
* `ValidateXmlFilesTest` shows that the provided examples are valid.
* `DdmVersionChangesTest` documents the effect of the differences between V1 and V2 of the DDM schemas.

### DIY

Various online and offline tools can validate an XML file against an XSD schema.
They may silently ignore problems with loading 3rd party schemas referenced by the main schema.
This can cause error messages like:

    Cannot resolve the name 'xml:lang' to a(n) 'element declaration' component

Some loading problems might be caused by services refusing the default
`User-Agent` header that is sent with the request to fetch the XSD.
A system property for Java applications can override the default value,
for example with a command line argument:

    -Dhttp.agent=something/1.0

Below an example to write your own validator in Java.
It silently ignores loading problems of 3rd party schemas.

```java
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import static javax.xml.validation.SchemaFactory.newInstance;

public class Validate {

  public static void main(String[] args) throws IOException, SAXException {
    System.setProperty("http.agent", "Test");
    String xsd = " https://easy.dans.knaw.nl/schemas/dcx/2020/03/dcx-dai.xsd";
    String xml = "src/main/resources/examples/dcx-dai/example2.xml";
    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .newSchema(new URL(xsd))
        .newValidator()
        .validate(new StreamSource(new FileInputStream(xml)));
  }
}
```

