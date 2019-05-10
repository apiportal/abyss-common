[![Build Status](https://travis-ci.org/apiportal/abyss-common.svg?branch=master)](https://travis-ci.org/apiportal/abyss-common)
# Abyss Common

This is the repository for Abyss Common 

This module defines common classes and methods for Abyss projects:

- OpenAPI utils
- Constants
- Api traffic message format
- Utils
- Annotation interface classes

## Build and Run
In order to create a fat jar package, install jdk >= 8 and Maven; afterwards, run this command:

```bash
mvn clean package
```

## Publish sites with Maven scm publish plugin

Execute following commands to publish sites:
```
mvn clean install site site:stage scm-publish:publish-scm
```