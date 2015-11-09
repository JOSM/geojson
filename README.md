# josm-geojson

A plugin for displaying geodata stored in a [geojson](https://geojson.org/) format as a layer in JOSM.

## Status

<!-- [![Build Status](https://travis-ci.org/matthieun/josm-geojson.svg?branch=master)](https://travis-ci.org/matthieun/josm-geojson) -->

## Build

Create a lib folder
```
mkdir lib/
```
Download josm
```
gradle josm
```
Build
```
gradle clean build
```

## Run

Depending on the JOSM version used...

Before:

```
cp build/libs/josm-geojson-0.0.0-SNAPSHOT.jar ~/.josm/plugins/geojson.jar
```

After:
```
cp build/libs/josm-geojson-0.0.0-SNAPSHOT.jar ~/Library/JOSM/plugins/geojson.jar
```

And Josm should pick up the plugin at startup.

To Activate it, Josm Preferences > Plugins > Search geojson > Click the check box > Ok.

To open a `*.json` or `*.geojson` file, File > Open, select the file, and to make the list appear, click Windows > Geojson