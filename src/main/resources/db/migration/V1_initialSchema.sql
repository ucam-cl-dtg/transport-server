CREATE TABLE allowed_keys (
    key text,
    permission_id integer,
    id sequence PRIMARY KEY,
    contact_email text,
    info text
);
CREATE TABLE available_stops (
    atco_code text PRIMARY KEY,
    geometry geometry,
    lat text,
    long text,
    data_source text,
    stop_name text,
    CONSTRAINT enforce_dims_geometry CHECK ((ndims(geometry) = 2)),
    CONSTRAINT enforce_srid_geometry CHECK ((srid(geometry) = (-1)))
);
CREATE TABLE data_providers (
    provider text PRIMARY KEY,
    handler text
);
CREATE TABLE naptan_extended_stop_info (
    atco_code text NOT NULL PRIMARY KEY,
    naptan_code text,
    common_name text,
    short_name text,
    landmark text,
    street text,
    location_indicator text,
    bearing text,
    geopoint geometry,
    lat text,
    long text,
    CONSTRAINT enforce_dims_geopoint CHECK ((ndims(geopoint) = 2)),
    CONSTRAINT enforce_srid_geopoint CHECK ((srid(geopoint) = (-1)))
);
CREATE TABLE naptan_group_memberships (
    atco_code text NOT NULL,
    group_ref text NOT NULL,
    CONSTRAINT naptan_group_memberships_pkey PRIMARY KEY (atco_code, group_ref)
);
CREATE TABLE naptan_groups (
    group_ref text PRIMARY KEY,
    name text,
    lat text,
    long text,
    geopoint geometry,
    CONSTRAINT enforce_dims_geopoint CHECK ((ndims(geopoint) = 2)),
    CONSTRAINT enforce_srid_geopoint CHECK ((srid(geopoint) = (-1)))
);
CREATE TABLE permissions (
    permission_id sequence PRIMARY KEY,
    permission_string text
);