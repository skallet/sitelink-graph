# site-graph

Analytic tool to make site linkage graph.

[![Build Status](https://travis-ci.org/skallet/sitelink-graph.svg?branch=master)](https://travis-ci.org/skallet/sitelink-graph)

## Dependencies

- [GraphViz](https://www.graphviz.org/)
- [Java](https://www.java.com/en/download/)

## Usage

```
$ java -jar sitegraph.jar [filename]
```

Filename should contains site URLs (each on own line). All sites will be fetched
and checked for external link. Graph is build and saved as PNG file.

## Example output

![Linkage of seznam.cz sites](/preview.png)

## License

Copyright © 2020 Milan Blazek

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
