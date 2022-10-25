dans-schema
===========

DANS XML schemas

DESCRIPTION
-----------
The [DANS BagIt Profile v1]{:target=_blank} requires several XML metadata files. This project includes the XML schema files to validate those XML files. These
schemas will be published on a public website. <!-- TODO: change text to point to website when it is up -->. Alternatively you can
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
