josm-geojson
============

A plugin for displaying geodata stored in a [geojson](https://geojson.org/) format as a layer in JOSM.

Build
------

Download josm
```
gradle josm
```
Build
```
gradle clean build
```

Run
------

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