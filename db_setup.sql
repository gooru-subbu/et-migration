DROP TABLE et_user_demographic;
DROP TABLE et_user_identity;
DROP TABLE et_content;
DROP TABLE et_collection;

DROP INDEX et_user_demographic_et_id_idx;
DROP INDEX et_user_identity_et_id_idx;
DROP INDEX et_content_et_id_idx;
DROP INDEX et_collection_et_id_idx;

CREATE TABLE et_user_demographic
(
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  firstname character varying(100),
  lastname character varying(100),
  parent_user_id uuid,
  user_category user_category_type,
  created_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, now()),
  updated_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, now()),
  birth_date date,
  grade jsonb,
  course jsonb,
  thumbnail_path character varying(1000),
  gender user_gender_type,
  about_me character varying(5000),
  school_id uuid,
  school character varying(2000),
  school_district_id uuid,
  school_district character varying(2000),
  email_id character varying(256),
  country_id uuid,
  country character varying(2000),
  state_id uuid,
  state character varying(2000),
  metadata jsonb,
  roster_id character varying(512),
  roster_global_userid character varying(512),
 et_insert_flag boolean NOT NULL DEFAULT true,
 et_id bigint NOT NULL,
  CONSTRAINT et_user_pkey PRIMARY KEY (et_id)
);

ALTER TABLE et_user_demographic OWNER TO nucleus;
CREATE INDEX et_user_demographic_et_id_idx
  ON et_user_demographic
  USING btree
  (et_id);

CREATE TABLE et_user_identity
(
  user_id uuid NOT NULL,
  username character varying(32),
  canonical_username character varying(32),
  reference_id character varying(100),
  email_id character varying(256),
  password character varying(64),
  client_id uuid NOT NULL,
  login_type user_identity_login_type NOT NULL,
  provision_type user_identity_provision_type NOT NULL,
  email_confirm_status boolean NOT NULL DEFAULT false,
  status user_identity_status_type NOT NULL,
  created_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, now()),
  updated_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, now()),
 et_insert_flag boolean NOT NULL DEFAULT true,
 et_id bigint NOT NULL,
  CONSTRAINT et_user_identity_pkey PRIMARY KEY (et_id)
);

ALTER TABLE et_user_identity OWNER TO nucleus;
CREATE INDEX et_user_identity_et_id_idx
  ON et_user_identity
  USING btree
  (et_id);


CREATE TABLE et_content
(
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  title character varying(1000) NOT NULL,
  url character varying(2000),
  created_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, now()),
  updated_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, now()),
  creator_id uuid NOT NULL,
  modifier_id uuid NOT NULL,
  original_creator_id uuid,
  original_content_id uuid,
  parent_content_id uuid,
  publish_date timestamp without time zone,
  publish_status publish_status_type DEFAULT 'unpublished'::publish_status_type,
  narration character varying(5000),
  description character varying(20000),
  content_format content_format_type NOT NULL,
  content_subformat content_subformat_type NOT NULL,
  answer jsonb,
  metadata jsonb,
  taxonomy jsonb,
  hint_explanation_detail jsonb,
  thumbnail character varying(2000),
  course_id uuid,
  unit_id uuid,
  lesson_id uuid,
  collection_id uuid,
  sequence_id smallint,
  is_copyright_owner boolean,
  copyright_owner jsonb,
  info jsonb,
  visible_on_profile boolean NOT NULL DEFAULT true,
  display_guide jsonb,
  accessibility jsonb,
  is_deleted boolean NOT NULL DEFAULT false,
  editorial_tags jsonb,
  license integer,
  creator_system character varying(255),
 et_id bigint NOT NULL,
 et_item_id bigint NOT NULL,
  CONSTRAINT et_content_pkey PRIMARY KEY (et_id, et_item_id)
);

ALTER TABLE et_content OWNER TO nucleus;
CREATE INDEX et_content_et_id_idx
  ON et_content
  (et_id, et_item_id);


CREATE TABLE et_collection
(
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  course_id uuid,
  unit_id uuid,
  lesson_id uuid,
  title character varying(1000) NOT NULL,
  created_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, now()),
  updated_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, now()),
  owner_id uuid NOT NULL,
  creator_id uuid NOT NULL,
  modifier_id uuid NOT NULL,
  original_creator_id uuid,
  original_collection_id uuid,
  parent_collection_id uuid,
  sequence_id smallint,
  publish_date timestamp without time zone,
  publish_status publish_status_type DEFAULT 'unpublished'::publish_status_type,
  format content_container_type NOT NULL,
  thumbnail character varying(2000),
  learning_objective character varying(20000),
  collaborator jsonb,
  metadata jsonb,
  taxonomy jsonb,
  url character varying(2000),
  login_required boolean,
  setting jsonb,
  grading grading_type,
  visible_on_profile boolean NOT NULL DEFAULT true,
  is_deleted boolean NOT NULL DEFAULT false,
  editorial_tags jsonb,
  class_visibility jsonb NOT NULL DEFAULT '[]'::jsonb,
  license integer,
  creator_system character varying(255),
 et_id bigint NOT NULL,
 CONSTRAINT et_collection_pkey PRIMARY KEY (et_id)
);

ALTER TABLE et_collection OWNER TO nucleus;
CREATE INDEX et_collection_et_id_idx
  ON et_collection
  USING btree
  (et_id);


