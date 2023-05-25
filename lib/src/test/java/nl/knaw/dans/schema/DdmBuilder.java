package nl.knaw.dans.schema;

public class DdmBuilder {
    private final String ddmNameSpace;
    private String lastProfileElements = "";
    private String additionalTitle = "";
    private String firstDcmiElements = "";

    public DdmBuilder(String ddmNameSpace) {
        this.ddmNameSpace = ddmNameSpace;
    }

    public DdmBuilder withMultipleTiles() {
        this.additionalTitle = "<dc:title>Another title</dc:title>";
        return this;
    }

    public DdmBuilder withAdditionalProfileElement(String value) {
        this.lastProfileElements += value;
        return this;
    }

    public DdmBuilder withAll3LanguageEncodingSchemes() {
        this.firstDcmiElements += ""
            + "        <ddm:language encodingScheme='ISO639-1' code='fry'>West-Fries</ddm:language>"
            + "        <ddm:language encodingScheme='ISO639-2' code='ka'>Groenlands</ddm:language>"
            + "        <ddm:language encodingScheme='ISO639-3' code='ba'>Baskisch</ddm:language>";
        return this;
    }

    public String build() {
        var titles = "<dc:title>A title</dc:title>" + additionalTitle;
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
        return String.format(simpleXml, ddmNameSpace, titles, lastProfileElements, firstDcmiElements);
    }

}
