/*% if (feature.DM_DI_DF_CSV) { %*/
### Instructions to make work CSV auto-importer.

1. Add CSV files inside `resources/csvs/`.
2. Fill `resources/csvs/csvsSpec.json` file. This file is an array of objects,
   where each object represents a file to import and each file has the following attributes:
  1. *file* - the name of the file (example: "agency.csv").
  2. *entity* - the name of the entity where the data is going to be inserted.
  3. *mapping* - an object that represents the mapping between the CSV column names with
     the entity attributes.

An example is represented in the following lines.

```json

[
  {
    "file": "agency.csv",
    "entity": "Agency",
    "package": "es.lbd.udc.entities"
  },
  {
    "file": "stops.csv",
    "entity": "Stop",
    "mapping": {
      "name": "myName",
      "stop_lat": "stopLocCustom.y",
      "stop_lon": "stopLocCustom.x",
      "agency": "agencyCustom.name"
    },
    "properties": {
      "encoding": "UTF-8",
      "quote": "\"",
      "separator": ";"
    }
  }
]
```

In this example, we are importing two files: *agency.csv* and *stops.csv*. The first one
is going to be imported inside the entity Agency, which is contained in a package different from *es.udc.lbd.gema.lps.model*,
meanwhile the second one is going to be imported inside the entity Stop; for the last one it is specified a mapping, so the column *name* from
the CSV is going to be saved inside the colum *myName* from the entity Stop, and the CSV file specific properties, such as encoding, quote character and column separator.

**IMPORTANT:** when there are points inside the CSV, you must map then inside the object "mapping".
All the points must be mapped to the name of the attribute in the entity followed by "Custom.y" or "Custom.x".

**IMPORTANT 2:** if you want to link entities by natural keys instead of primary keys, you must create a map  for then inside the object "mapping".
with the name of the attribute in the entity followed by "Custom." and the name of the natural key in the linked class.

/*% } %*/
