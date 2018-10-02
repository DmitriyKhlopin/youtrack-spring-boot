-- noinspection SqlNoDataSourceInspectionForFile

create table projects
(
  name        varchar(1024),
  short_name  varchar(256) not null
    constraint projects_short_name_pk
    primary key,
  description varchar(1024)
);

create unique index projects_short_name_uindex
  on projects (short_name);

create table custom_field_presets
(
  name             varchar(64) not null
    constraint custom_fields_presets_pkey
    primary key,
  type             varchar(64) not null,
  is_private       boolean     not null,
  is_visible       boolean     not null,
  is_auto_attached boolean     not null,
  "default"        text
);

create unique index custom_fields_presets_name_uindex
  on custom_field_presets (name);

create table bundles
(
  bundle         varchar(256) not null,
  val            varchar(256) not null,
  description    text,
  color_index    integer,
  localized_name varchar(256)
);

create table custom_field_values
(
  issue_id    varchar(64)  not null,
  field_name  varchar(256) not null,
  field_value text         not null
);

create table issue_comments
(
  id                     varchar(256) not null,
  issue_id               varchar(256) not null,
  parent_id              varchar(256),
  deleted                boolean,
  shown_for_issue_author boolean,
  author                 varchar(256) not null,
  author_full_name       varchar(256) not null,
  comment_text           text,
  created                timestamp    not null,
  updated                timestamp,
  permitted_group        varchar(256),
  replies                varchar(256)
);

create table users
(
  user_login varchar(255),
  ring_id    varchar(255) not null
    constraint users_ring_id_pk
    primary key,
  url        varchar(1024),
  email      varchar(255),
  full_name  varchar(255)
);

create unique index users_ring_id_uindex
  on users (ring_id);

create table issue_history
(
  issue_id            varchar(256),
  author              varchar(256),
  update_date_time    timestamp,
  field_name          varchar(256),
  value_type          varchar(256),
  old_value_int       integer,
  new_value_int       integer,
  old_value_string    text,
  new_value_string    text,
  old_value_date_time timestamp,
  new_value_date_time timestamp,
  update_week         timestamp
);

create table issues
(
  id                           varchar(64)   not null
    constraint issues_id_pk
    primary key,
  entity_id                    varchar(64)   not null,
  summary                      text,
  created_date_time            timestamp     not null,
  created_date                 timestamp     not null,
  created_week                 timestamp     not null,
  updated_date_time            timestamp,
  updated_date                 timestamp,
  updated_week                 timestamp,
  resolved_date_time           timestamp,
  resolved_date                timestamp,
  resolved_week                timestamp,
  reporter_login               varchar(1024) not null,
  comments_count               integer,
  votes                        integer,
  subsystem                    varchar(1024),
  sla                          varchar(64),
  sla_first_responce_index     varchar(64),
  sla_first_responce_date_time timestamp,
  sla_first_responce_date      timestamp,
  sla_first_responce_week      timestamp,
  sla_solution_index           varchar(64),
  sla_solution_date_time       timestamp,
  sla_solution_date            timestamp,
  sla_solution_week            timestamp,
  project                      varchar(255),
  issue_type                   varchar(255),
  state                        varchar(255),
  priority                     varchar(255),
  pp_version                   varchar(255),
  quality_evaluation           varchar(255),
  time_user                    bigint,
  time_agent                   bigint,
  time_developer               bigint,
  loaded_date                  timestamp,
  quality_evaluation_date_time timestamp,
  quality_evaluation_date      timestamp,
  quality_evaluation_week      timestamp,
  ets                          text
);

create table work_items
(
  issue_id                varchar(256)  not null,
  wi_url                  varchar(1024) not null,
  wi_id                   varchar(256)  not null,
  wi_date                 timestamp     not null,
  wi_created              timestamp     not null,
  wi_updated              timestamp,
  wi_duration             integer,
  author_login            varchar(256)  not null,
  author_ring_id          varchar(256),
  author_url              varchar(1024),
  work_name               varchar(256),
  work_type_id            varchar(256),
  work_type_auto_attached boolean,
  work_type_url           varchar(1024),
  description             varchar(4000)
);

create table ets_names
(
  fsight_email varchar(128),
  ets_name     varchar(128),
  full_name    varchar(128)
);

create table ets_projects
(
  id        integer,
  name      varchar(256),
  parent_id integer,
  ord       integer,
  num       integer
);

create table test
(
  column_1 bigint
);

create table issue_timeline
(
  issue_id         varchar(64),
  state_from       varchar(256),
  state_to         varchar(256),
  state_from_date  timestamp,
  state_to_date    timestamp,
  time_spent       bigint,
  transition_owner varchar(64)
);

create table states_holders
(
  state_name varchar(256),
  holder     varchar(256)
);

create table dictionary_project_customer_ets
(
  proj_short_name varchar(64),
  customer        varchar(128),
  proj_ets        varchar(128),
  iteration_path  varchar(128)
);

comment on table dictionary_project_customer_ets
is 'Справочная таблица для определения соотвествия проекта YT и заказчика YT  проекту ETS';

create table error_log
(
  date  timestamp,
  item  text,
  error text
);

create table import_log
(
  date                   timestamp default now(),
  source_url             text,
  destination_table_name text,
  items_count            integer
);

