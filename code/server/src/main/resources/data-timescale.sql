/*% if(feature.SensorViewer){ %*/
/*% data.dataWarehouse.sensors.forEach(function(sensor){
const tableName = "t_".concat(camelToSnakeCase(normalize(sensor.id))).concat("_measurement");
let catDimensions = [];
sensor.dimensions.filter(function(dimension){return dimension.type == 'CATEGORICAL'}).forEach(function(dimension, index){
const entityName = sensor.id.concat('Measurement');
if(dimension.categories){
  dimension.type = 'range';
}else{
  dimension.type = 'string';
};
catDimensions.push(dimension);
%*/
/*% }); %*/
CREATE EXTENSION IF NOT EXISTS dblink;

CREATE OR REPLACE FUNCTION conditional_alter_table()
RETURNS VOID AS '
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_views
        WHERE viewname = ''agg_minute_/*%= camelToSnakeCase(normalize(sensor.id)) %*/''
    ) THEN
        EXECUTE ''ALTER TABLE /*%= tableName %*/ ALTER COLUMN "date" TYPE TIMESTAMPTZ'';
        /*% if(catDimensions.length > 0) { %*/
        EXECUTE ''ALTER TABLE /*%= tableName %*/ ADD COLUMN category BIGINT'';
        /*% } %*/
    END IF;
END;
' LANGUAGE plpgsql;
SELECT conditional_alter_table();

SELECT create_hypertable('/*%= tableName %*/', 'date', if_not_exists => TRUE);
CREATE INDEX IF NOT EXISTS ix_sensor_time on /*%= tableName %*/ (sensor_id, date DESC);

/*% if (catDimensions.length != 0) { %*/
CREATE TABLE IF NOT EXISTS public.t_category_/*%= camelToSnakeCase(normalize(sensor.id)) %*/ (
    id SERIAL PRIMARY KEY/*%catDimensions.forEach(function(dimension, index){ %*/,
    /*%= camelToSnakeCase(normalize(dimension.field)) %*/ /*% if (dimension.type == 'string'){ %*/ VARCHAR(255)/*% }else{ %*/INT8 /*% }}); %*/
);
/*% } %*/

/*% catDimensions.filter((categoricalDim)=>categoricalDim.type == 'range').forEach(function(dimension){ %*/
CREATE TABLE IF NOT EXISTS public.t_/*%= camelToSnakeCase(normalize(dimension.field)) %*/_range_/*%= camelToSnakeCase(normalize(sensor.id)) %*/ (
  id SERIAL PRIMARY KEY,
  "from" FLOAT8,
  "to" FLOAT8);

INSERT INTO public.t_/*%= camelToSnakeCase(normalize(dimension.field)) %*/_range_/*%= camelToSnakeCase(normalize(sensor.id)) %*/ ("from", "to")
SELECT vals."from", vals."to"
FROM (VALUES
    /*% dimension.categories.forEach(function(category, index){ %*/
    (/*%= category.from %*/, /*%= category.to %*/)/*% if(index != dimension.categories.length-1){ %*/,/*% } %*/
    /*% }); %*/
) AS vals("from", "to")
WHERE NOT EXISTS (
    SELECT 1
    FROM public.t_/*%= camelToSnakeCase(normalize(dimension.field)) %*/_range_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
    WHERE public.t_/*%= camelToSnakeCase(normalize(dimension.field)) %*/_range_/*%= camelToSnakeCase(normalize(sensor.id)) %*/."from" = vals."from"
    AND public.t_/*%= camelToSnakeCase(normalize(dimension.field)) %*/_range_/*%= camelToSnakeCase(normalize(sensor.id)) %*/."to" = vals."to"
);
/*% }); %*/

/*% if(catDimensions.length > 0) { %*/
CREATE OR REPLACE FUNCTION insert_category_if_not_exists_/*%= camelToSnakeCase(normalize(sensor.id)) %*/()
RETURNS TRIGGER AS '
/*% catDimensions.filter(function(dim){return dim.type =='range'}).forEach(function(dimRange){ %*/
DECLARE /*%= dimRange.field %*/_range INT8;
/*% }); %*/
BEGIN
  /*% catDimensions.filter(function(dim){return dim.type =='range'}).forEach(function(dimRange){ %*/
  IF NEW./*%= dimRange.field %*/ IS NULL THEN
    /*%= dimRange.field %*/_range := NULL;
    /*% dimRange.categories.forEach(function(range, index){
      if(index < dimRange.categories.length-1){ %*/
    ELSIF NEW./*%= dimRange.field %*/ <= /*%= dimRange.categories[index].to %*/ THEN
      /*%= dimRange.field %*/_range := /*%= index+1 %*/;
      /*% }else{ %*/
    ELSE /*%= dimRange.field %*/_range := /*%= index+1 %*/;
      /*% } %*/
    /*% }); %*/
  END IF;

  /*% }); %*/
  INSERT INTO t_category_/*%= camelToSnakeCase(normalize(sensor.id)) %*/ (/*% catDimensions.forEach(function(dimension, index){ if(index < catDimensions.length-1) { %*/ /*%= camelToSnakeCase(normalize(dimension.field)), %*/,/*% } else { %*/ /*%= camelToSnakeCase(normalize(dimension.field)) %*/ /*% } }); %*/)
  SELECT /*% catDimensions.forEach(function(dim, index){ if(dim.type=='range'){ %*//*%= camelToSnakeCase(normalize(dim.field)) %*/_range/*% } else { %*/ NEW./*%=camelToSnakeCase(normalize(dim.field)) %*//*% }; if(index<catDimensions.length -1){ %*/, /*% }}); %*/
  WHERE NOT EXISTS (
      SELECT 1
      FROM t_category_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
      /*% catDimensions.forEach(function(dim, index){ if(index == 0){ %*/WHERE/*% }else{ %*/
        AND/*% } %*/ /*%= camelToSnakeCase(normalize(dim.field)) %*/ =/*% if(dim.type == 'range'){ %*/ /*%= camelToSnakeCase(normalize(dim.field)) %*/_range /*% } else{ %*/ NEW./*%= camelToSnakeCase(normalize(dim.field)) %*//*%}}); %*/
  );
  NEW.category :=  (SELECT id FROM t_category_/*%= camelToSnakeCase(normalize(sensor.id)) %*//*% catDimensions.forEach(function(dim, index){ if(index == 0){ %*/ WHERE/*% }else{ %*/
    AND/*% } %*/ /*%= camelToSnakeCase(normalize(dim.field)) %*/ =/*% if(dim.type == 'range'){ %*/ /*%= camelToSnakeCase(normalize(dim.field)) %*/_range /*% } else{ %*/ NEW./*%= camelToSnakeCase(normalize(dim.field)) %*//*%}}); %*/);
  RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE TRIGGER insert_combination_trigger_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
BEFORE INSERT ON /*%= tableName %*/
FOR EACH ROW
EXECUTE FUNCTION insert_category_if_not_exists_/*%= camelToSnakeCase(normalize(sensor.id)) %*/();
/*% } %*/

create materialized view if not exists agg_minute_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
with (timescaledb.continuous) as
select
time_bucket('/*%= sensor.time %*/ seconds', date) as bucket_minute,
sensor_id, /*% if(catDimensions.length > 0) { %*/category,/*% } %*/
/*% sensor.measureData.forEach(function(measure,index){ %*/
max(/*%= camelToSnakeCase(measure.name) %*/) as max_/*%= camelToSnakeCase(measure.name) %*/,
min(/*%= camelToSnakeCase(measure.name) %*/) as min_/*%= camelToSnakeCase(measure.name) %*/,
avg(/*%= camelToSnakeCase(measure.name) %*/) as avg_/*%= camelToSnakeCase(measure.name) %*//*% if (index < sensor.measureData.length -1) { %*/,/*% } %*/
/*%});%*/
from /*%= tableName %*/
group by bucket_minute, sensor_id/*% if(catDimensions.length > 0) { %*/, category/*% } %*/;

create materialized view if not exists agg_hour_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
with (timescaledb.continuous) as
select
time_bucket('1 hour', bucket_minute) as bucket_hour,
sensor_id, /*% if(catDimensions.length > 0) { %*/category,/*% } %*/
/*% sensor.measureData.forEach(function(measure,index){ %*/
max(max_/*%= camelToSnakeCase(measure.name) %*/) as max_/*%= camelToSnakeCase(measure.name) %*/,
min(min_/*%= camelToSnakeCase(measure.name) %*/) as min_/*%= camelToSnakeCase(measure.name) %*/,
avg(avg_/*%= camelToSnakeCase(measure.name) %*/) as avg_/*%= camelToSnakeCase(measure.name) %*//*% if (index < sensor.measureData.length -1) { %*/,/*% } %*/
/*%});%*/
from agg_minute_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
group by bucket_hour, sensor_id/*% if(catDimensions.length > 0) { %*/, category/*% } %*/;

create materialized view if not exists agg_day_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
with (timescaledb.continuous) as
select
time_bucket('1 day', bucket_hour) as bucket_day,
sensor_id, /*% if(catDimensions.length > 0) { %*/category,/*% } %*/
/*% sensor.measureData.forEach(function(measure,index){ %*/
max(max_/*%= camelToSnakeCase(measure.name) %*/) as max_/*%= camelToSnakeCase(measure.name) %*/,
min(min_/*%= camelToSnakeCase(measure.name) %*/) as min_/*%= camelToSnakeCase(measure.name) %*/,
avg(avg_/*%= camelToSnakeCase(measure.name) %*/) as avg_/*%= camelToSnakeCase(measure.name) %*//*% if (index < sensor.measureData.length -1) { %*/,/*% } %*/
/*%});%*/
from agg_hour_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
group by bucket_day, sensor_id/*% if(catDimensions.length > 0) { %*/, category/*% } %*/;

create materialized view if not exists agg_month_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
with (timescaledb.continuous) as
select
time_bucket('1 month', bucket_day) as bucket_month,
sensor_id, /*% if(catDimensions.length > 0) { %*/category,/*% } %*/
/*% sensor.measureData.forEach(function(measure,index){ %*/
max(max_/*%= camelToSnakeCase(measure.name) %*/) as max_/*%= camelToSnakeCase(measure.name) %*/,
min(min_/*%= camelToSnakeCase(measure.name) %*/) as min_/*%= camelToSnakeCase(measure.name) %*/,
avg(avg_/*%= camelToSnakeCase(measure.name) %*/) as avg_/*%= camelToSnakeCase(measure.name) %*//*% if (index < sensor.measureData.length -1) { %*/,/*% } %*/
/*%});%*/
from agg_day_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
group by bucket_month, sensor_id/*% if(catDimensions.length > 0) { %*/, category/*% } %*/;

create materialized view if not exists agg_year_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
with (timescaledb.continuous) as
select
time_bucket('1 year', bucket_month) as bucket_year,
sensor_id, /*% if(catDimensions.length > 0) { %*/category,/*% } %*/
/*% sensor.measureData.forEach(function(measure,index){ %*/
max(max_/*%= camelToSnakeCase(measure.name) %*/) as max_/*%= camelToSnakeCase(measure.name) %*/,
min(min_/*%= camelToSnakeCase(measure.name) %*/) as min_/*%= camelToSnakeCase(measure.name) %*/,
avg(avg_/*%= camelToSnakeCase(measure.name) %*/) as avg_/*%= camelToSnakeCase(measure.name) %*//*% if (index < sensor.measureData.length -1) { %*/,/*% } %*/
/*%});%*/
from agg_month_/*%= camelToSnakeCase(normalize(sensor.id)) %*/
group by bucket_year, sensor_id/*% if(catDimensions.length > 0) { %*/, category/*% } %*/;

ALTER MATERIALIZED VIEW agg_day_/*%= camelToSnakeCase(normalize(sensor.id)) %*/ set (timescaledb.materialized_only = false);
ALTER MATERIALIZED VIEW agg_hour_/*%= camelToSnakeCase(normalize(sensor.id)) %*/ set (timescaledb.materialized_only = false);

CALL refresh_continuous_aggregate('agg_minute_/*%= camelToSnakeCase(normalize(sensor.id)) %*/', NULL, NULL);
CALL refresh_continuous_aggregate('agg_hour_/*%= camelToSnakeCase(normalize(sensor.id)) %*/', NULL, NULL);
CALL refresh_continuous_aggregate('agg_day_/*%= camelToSnakeCase(normalize(sensor.id)) %*/', NULL, NULL);
CALL refresh_continuous_aggregate('agg_month_/*%= camelToSnakeCase(normalize(sensor.id)) %*/', NULL, NULL);
CALL refresh_continuous_aggregate('agg_year_/*%= camelToSnakeCase(normalize(sensor.id)) %*/', NULL, NULL);

/*% }); %*/
/*% } %*/
