# josm-geojson

A plugin for displaying geodata stored in a [geojson](https://geojson.org/) format as a layer in JOSM.

[![Travis CI build status](https://img.shields.io/travis/JOSM/geojson/master.svg?style=flat-square)](https://travis-ci.org/JOSM/geojson)

## Build

You need to have Java 8 or later installed.

Then run
```bash
  ./gradlew build
```

## Run

Run
```bash
  ./gradlew runJosm
```
You do not need to build first, Gradle will check if you already built the current state of the source code and if not, it will build it for you on the fly.

You also do not need to have JOSM installed. The version of JOSM that is firing up is independent of any installed JOSM versions and uses a different `JOSM_HOME` directory than other JOSM installations.

To open a `*.json` or `*.geojson` file, File > Open, select the file, and to make the list appear, click Windows > Geojson

## Before developing in Eclipse

Run
```bash
  ./gradlew eclipse
```

Then import the project as "Already existing project" and all should be set up.


Have fun :+1: :tada:
