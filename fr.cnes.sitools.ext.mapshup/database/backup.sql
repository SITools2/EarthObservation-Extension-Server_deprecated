--
-- PostgreSQL database dump
--

-- Dumped from database version 8.4.7
-- Dumped by pg_dump version 9.0.1
-- Started on 2012-02-03 10:51:26

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 8 (class 2615 OID 35744)
-- Name: sitools_used_for_tests; Type: SCHEMA; Schema: -; Owner: sitools
--

CREATE SCHEMA sitools_used_for_tests;


ALTER SCHEMA sitools_used_for_tests OWNER TO sitools;

SET search_path = sitools_used_for_tests, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 2265 (class 1259 OID 35745)
-- Dependencies: 2561 2562 2563 8 994
-- Name: jeo_entries; Type: TABLE; Schema: sitools_used_for_tests; Owner: sitools; Tablespace: 
--

CREATE TABLE jeo_entries (
    identifier character varying NOT NULL,
    date date,
    notes character varying,
    building_identifier character varying,
    building_peoplenb integer,
    building_state character varying,
    ele double precision,
    coord public.geometry,
    CONSTRAINT enforce_dims_coord CHECK ((public.ndims(coord) = 2)),
    CONSTRAINT enforce_geotype_coord CHECK (((public.geometrytype(coord) = 'POINT'::text) OR (coord IS NULL))),
    CONSTRAINT enforce_srid_coord CHECK ((public.srid(coord) = 4326))
);


ALTER TABLE sitools_used_for_tests.jeo_entries OWNER TO sitools;

--
-- TOC entry 2266 (class 1259 OID 35754)
-- Dependencies: 8
-- Name: jeo_medias; Type: TABLE; Schema: sitools_used_for_tests; Owner: sitools; Tablespace: 
--

CREATE TABLE jeo_medias (
    type character varying NOT NULL,
    identifier character varying NOT NULL,
    name character varying,
    directory character varying,
    entry_id character varying,
    extension character varying
);


ALTER TABLE sitools_used_for_tests.jeo_medias OWNER TO sitools;

--
-- TOC entry 2267 (class 1259 OID 35760)
-- Dependencies: 8
-- Name: jeobrowser; Type: TABLE; Schema: sitools_used_for_tests; Owner: sitools; Tablespace: 
--

CREATE TABLE jeobrowser (
    "DATASET_NAME" character varying(30) NOT NULL,
    "DATASET_INDEX" character varying(30),
    "DATASET_LOCATION" character varying(30),
    "COUNTRY_NAME" character varying(30),
    "COUNTRY_CODE" character varying(3),
    "COPYRIGHT" character varying(50),
    "DATASET_TN_PATH" character varying(50),
    "DATASET_QL_PATH" character varying(50),
    "METADATA_FORMAT" character varying(10),
    "METADATA_FORMAT_VERSION" real,
    "METADATA_PROFILE" character varying(20),
    "DATA_FILE_FORMAT" character varying(20),
    "DATA_FILE_FORMAT_VERSION" real,
    "DATA_FILE_ORGANISATION" character varying(30),
    "DATA_FILE_PATH" character varying(30),
    "JOB_ID" character varying(30),
    "PRODUCT_TYPE" character varying(10),
    "DATASET_PRODUCTION_DATE" timestamp(6) without time zone,
    "DATASET_PRODUCER_NAME" character varying(30),
    "DATASET_PRODUCER_URL" character varying(50),
    "GEO_TABLES" character varying(20),
    "GEO_TABLES_VERSION" real,
    "HORIZONTAL_CS_TYPE" character varying(30),
    "HORIZONTAL_CS_NAME" character varying(50),
    "HORIZONTAL_CS_CODE" character varying(30),
    "GEOGRAPHIC_CS_NAME" character varying(20),
    "GEOGRAPHIC_CS_CODE" character varying(20),
    "RASTER_CS_TYPE" character varying(10),
    "PIXEL_ORIGIN" integer,
    "ULXMAP" real,
    "ULXMAP_UNIT" character(4),
    "ULYMAP" real,
    "ULYMAP_UNIT" character(4),
    "XDIM" real,
    "XDIM_UNIT" character(4),
    "YDIM" real,
    "YDIM_UNIT" character(4),
    "RASTER_NCOLS" integer,
    "RASTER_NROWS" integer,
    "NBANDS" integer,
    "GEOMETRIC_PROCESSING" character varying(20),
    "RADIOMETRIC_PROCESSING" character varying(20),
    "SPECTRAL_PROCESSING" character(20),
    "BAND_INDEX" integer,
    "STX_MIN" integer,
    "STX_MAX" integer,
    "STX_MEAN" integer,
    "STX_STDV" integer,
    "STX_LIN_MIN" integer,
    "STX_LIN_MAX" integer,
    "SOURCE_ID" character varying(50),
    "SOURCE_TYPE" character varying(50),
    "SOURCE_DESCRIPTION" character varying(50),
    "GEO_TABLES_1" character varying(50),
    "GEO_TABLES_VERSION_1" real,
    "HORIZONTAL_CS_TYPE_1" character varying(50),
    "HORIZONTAL_CS_NAME_1" character varying(50),
    "HORIZONTAL_CS_CODE_1" character varying(50),
    "IMAGING_DATE" timestamp(6) without time zone,
    "IMAGING_TIME" character varying(50),
    "MISSION" character varying(50),
    "MISSION_INDEX" integer,
    "INSTRUMENT" character varying(50),
    "INSTRUMENT_INDEX" integer,
    "IMAGING_MODE" character varying(50),
    "GRID_REFERENCE" character varying(50),
    "SHIFT_VALUE" integer,
    "INCIDENCE_ANGLE" real,
    "INCIDENCE_ANGLE_UNIT" character varying(4),
    "VIEWING_ANGLE" real,
    "VIEWING_ANGLE_UNIT" character varying(4),
    "SUN_AZIMUTH" real,
    "SUN_AZIMUTH_UNIT" character varying(4),
    "SUN_ELEVATION" real,
    "SUN_ELEVATION_UNIT" character varying(4),
    "SCENE_PROCESSING_LEVEL" character varying(50),
    "POLYGON" character varying(15)
);


ALTER TABLE sitools_used_for_tests.jeobrowser OWNER TO sitools;

--
-- TOC entry 2570 (class 0 OID 35745)
-- Dependencies: 2265
-- Data for Name: jeo_entries; Type: TABLE DATA; Schema: sitools_used_for_tests; Owner: sitools
--

COPY jeo_entries (identifier, date, notes, building_identifier, building_peoplenb, building_state, ele, coord) FROM stdin;
9eddd6f4-b535-443e-896d-098c43dfd1c7	2011-10-24	Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec in est magna, vel dictum nulla. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin porttitor ante at orci tempus nec.	1	7	Moyen	172.5	0101000020E610000035B56CAD2F12F73FE8D9ACFA5CCD4540
465456	2011-02-23	texte de la note 465456	789879879	510	bon	189	0101000020E610000035B56CAD2F12F73FE8D9ACFA5CCD4440
\.


--
-- TOC entry 2571 (class 0 OID 35754)
-- Dependencies: 2266
-- Data for Name: jeo_medias; Type: TABLE DATA; Schema: sitools_used_for_tests; Owner: sitools
--

COPY jeo_medias (type, identifier, name, directory, entry_id, extension) FROM stdin;
audio	465456_1	audio1	/audio/	465456	.mp3
photo	465456_1	photo1	/photo/	465456	.jpg
photo	465456_2	photo2	/photo/	465456	.jpg
photo	465456_3	photo3	/photo/	465456	.jpg
video	465456_1	video1	/video/	465456	.mp4
video	465456_2	video2	/video/	465456	.mp4
video	1	Vidéo 1	/Video/	9eddd6f4-b535-443e-896d-098c43dfd1c7	.3gp
video	2	Vidéo 2	/Video/	9eddd6f4-b535-443e-896d-098c43dfd1c7	.3gp
photo	1	Photo 1	/Photo/	9eddd6f4-b535-443e-896d-098c43dfd1c7	.jpg
photo	2	Photo 2	/Photo/	9eddd6f4-b535-443e-896d-098c43dfd1c7	.jpg
photo	5	Photo 1	/Photo/	9eddd6f4-b535-443e-896d-098c43dfd1c7	.jpg
audio	1	Audio 1	/Audio/	9eddd6f4-b535-443e-896d-098c43dfd1c7	.mp3
\.


--
-- TOC entry 2572 (class 0 OID 35760)
-- Dependencies: 2267
-- Data for Name: jeobrowser; Type: TABLE DATA; Schema: sitools_used_for_tests; Owner: sitools
--

COPY jeobrowser ("DATASET_NAME", "DATASET_INDEX", "DATASET_LOCATION", "COUNTRY_NAME", "COUNTRY_CODE", "COPYRIGHT", "DATASET_TN_PATH", "DATASET_QL_PATH", "METADATA_FORMAT", "METADATA_FORMAT_VERSION", "METADATA_PROFILE", "DATA_FILE_FORMAT", "DATA_FILE_FORMAT_VERSION", "DATA_FILE_ORGANISATION", "DATA_FILE_PATH", "JOB_ID", "PRODUCT_TYPE", "DATASET_PRODUCTION_DATE", "DATASET_PRODUCER_NAME", "DATASET_PRODUCER_URL", "GEO_TABLES", "GEO_TABLES_VERSION", "HORIZONTAL_CS_TYPE", "HORIZONTAL_CS_NAME", "HORIZONTAL_CS_CODE", "GEOGRAPHIC_CS_NAME", "GEOGRAPHIC_CS_CODE", "RASTER_CS_TYPE", "PIXEL_ORIGIN", "ULXMAP", "ULXMAP_UNIT", "ULYMAP", "ULYMAP_UNIT", "XDIM", "XDIM_UNIT", "YDIM", "YDIM_UNIT", "RASTER_NCOLS", "RASTER_NROWS", "NBANDS", "GEOMETRIC_PROCESSING", "RADIOMETRIC_PROCESSING", "SPECTRAL_PROCESSING", "BAND_INDEX", "STX_MIN", "STX_MAX", "STX_MEAN", "STX_STDV", "STX_LIN_MIN", "STX_LIN_MAX", "SOURCE_ID", "SOURCE_TYPE", "SOURCE_DESCRIPTION", "GEO_TABLES_1", "GEO_TABLES_VERSION_1", "HORIZONTAL_CS_TYPE_1", "HORIZONTAL_CS_NAME_1", "HORIZONTAL_CS_CODE_1", "IMAGING_DATE", "IMAGING_TIME", "MISSION", "MISSION_INDEX", "INSTRUMENT", "INSTRUMENT_INDEX", "IMAGING_MODE", "GRID_REFERENCE", "SHIFT_VALUE", "INCIDENCE_ANGLE", "INCIDENCE_ANGLE_UNIT", "VIEWING_ANGLE", "VIEWING_ANGLE_UNIT", "SUN_AZIMUTH", "SUN_AZIMUTH_UNIT", "SUN_ELEVATION", "SUN_ELEVATION_UNIT", "SCENE_PROCESSING_LEVEL", "POLYGON") FROM stdin;
\.


--
-- TOC entry 2565 (class 2606 OID 35768)
-- Dependencies: 2265 2265
-- Name: jeo_entries_pkkey; Type: CONSTRAINT; Schema: sitools_used_for_tests; Owner: sitools; Tablespace: 
--

ALTER TABLE ONLY jeo_entries
    ADD CONSTRAINT jeo_entries_pkkey PRIMARY KEY (identifier);


--
-- TOC entry 2567 (class 2606 OID 35770)
-- Dependencies: 2266 2266 2266
-- Name: jeo_media_pkkey; Type: CONSTRAINT; Schema: sitools_used_for_tests; Owner: sitools; Tablespace: 
--

ALTER TABLE ONLY jeo_medias
    ADD CONSTRAINT jeo_media_pkkey PRIMARY KEY (type, identifier);


--
-- TOC entry 2569 (class 2606 OID 35772)
-- Dependencies: 2267 2267
-- Name: jeobrowser_pkey; Type: CONSTRAINT; Schema: sitools_used_for_tests; Owner: sitools; Tablespace: 
--

ALTER TABLE ONLY jeobrowser
    ADD CONSTRAINT jeobrowser_pkey PRIMARY KEY ("DATASET_NAME");


--
-- TOC entry 2575 (class 0 OID 0)
-- Dependencies: 8
-- Name: sitools_used_for_tests; Type: ACL; Schema: -; Owner: sitools
--

REVOKE ALL ON SCHEMA sitools_used_for_tests FROM PUBLIC;
REVOKE ALL ON SCHEMA sitools_used_for_tests FROM sitools;
GRANT ALL ON SCHEMA sitools_used_for_tests TO sitools;
GRANT ALL ON SCHEMA sitools_used_for_tests TO PUBLIC;


-- Completed on 2012-02-03 10:51:27

--
-- PostgreSQL database dump complete
--

