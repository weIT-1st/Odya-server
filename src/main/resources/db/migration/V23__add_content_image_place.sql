alter table CONTENT_IMAGE
    add coordinate SDO_GEOMETRY  NULL
    add place_id   VARCHAR2(400) NULL;

insert into user_sdo_geom_metadata(table_name, column_name, diminfo, srid)
values ('CONTENT_IMAGE',
        'coordinate',
        SDO_DIM_ARRAY(
                SDO_DIM_ELEMENT('Longitude', -180, 180, 0.5),
                SDO_DIM_ELEMENT('Latitude', -90, 90, 0.5)
            ),
        4326);

CREATE INDEX CONTENT_IMAGE_coordinate_index ON CONTENT_IMAGE(coordinate) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
