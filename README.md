# Quilt JSON5

Quilt JSON5 is a compliant JSON, JSONC, and JSON5 reader and writer.
The API design is inspired by the structure of GSON's stream api. The API surface should be similar to GSON's, but may
differ slightly.

This parser is suitable to be used to read and write JSON(5) files and further abstracted libraries which need a
simple JSON(5) parser. 

## Licensing

Quilt JSON5 is available under the Apache 2.0 license.
There are some components from GSON used, those are also Licensed under the Apache 2.0 License per GSON's own licensing
with modifications listed.

There are also testing files included in the `tests` folder.
See the [testing files license] for further details.
The testing files are not included in the built jar.

<!--Links-->
[testing files license]: LICENSE.TESTFILES
