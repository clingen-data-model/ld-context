# ld-context

Generates a json-ld context file from a set of OWL ontologies.

## Installation

* git clone git@github.com:clingen-data-model/ld-context.git
* lein uberjar

## Usage

    $ java -jar ld-context-0.1.0-standalone.jar -o <output_context.jsonld> <input_list.owl> ...

## Options

```
-o --output Output context file, defaults to context.jsonld
```

## Examples

    $ java -jar ld-context-0.1.0-standalone.jar -o context.jsonld sepio.owl sepio-clingen.owl

## License

Copyright © 2018 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
