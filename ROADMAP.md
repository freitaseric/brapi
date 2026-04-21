# brApi - Roadmap

## Table of Contents

<!-- TOC -->
* [brApi - Roadmap](#brapi---roadmap)
  * [Table of Contents](#table-of-contents)
  * [Version-based features](#version-based-features)
    * [v1: The starter laboratory](#v1-the-starter-laboratory)
  * [Future ideas](#future-ideas)
<!-- TOC -->

## Version-based features

### v1: The starter laboratory

- [x] Simple directory organization (design pattern: ports and adapters).
- [x] Basic documentation via Swagger UI (auto-generated).
- [x] Minimal interface to search for a CEP with normalized structure.
- [ ] Concurrent data fetching from multiple providers (Scatter-Gather pattern) for resilient responses.

## Future ideas

- [ ] Migrate from Scala's standard Futures to ZIO runtime for concurrent operations.