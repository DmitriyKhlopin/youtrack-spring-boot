-- noinspection SqlResolveForFile
-- noinspection SqlNoDataSourceInspectionForFile

create view issues_pulse as
  WITH temp AS (
    SELECT issues.created_date AS dt, count(issues.created_date) AS created, 0 AS resolved
    FROM issues
    GROUP BY issues.created_date
    UNION ALL
    SELECT issues.resolved_date AS dt, 0 AS created, count(issues.resolved_date) AS resolved
    FROM issues
    GROUP BY issues.resolved_date
  ), temp2 AS (
      SELECT temp.dt, sum(temp.created) AS created, sum(temp.resolved) AS resolved
      FROM temp
      GROUP BY temp.dt
  )
  SELECT t.dt,
         t.created,
         t.resolved,
         (SELECT (sum(t2.created) - sum(t2.resolved)) FROM temp2 t2 WHERE (t2.dt <= t.dt)) AS remaining
  FROM temp2 t
  WHERE (t.dt IS NOT NULL)
  ORDER BY t.dt;

create view weeks as
  WITH temp AS (
      SELECT (min(issues.created_week) + ((generate_series(0, (trunc((date_part('day' :: text,
                                                                                (now() - (min(issues.created_week)) :: timestamp with time zone)) /
                                                                      (7) :: double precision))) :: integer)) :: double precision *
                                          '7 days' :: interval)) AS w
      FROM issues
  )
  SELECT temp.w, to_char(temp.w, 'YYYY.MM.DD' :: text) AS presentation
  FROM temp;

create view pp_dynamics as
  SELECT w.w,
         (SELECT count(ota.id) AS count
          FROM issues ota
          WHERE ((ota.created_week = w.w) AND ((ota.project) :: text = 'PP' :: text)))  AS created,
         (SELECT count(ota.id) AS count
          FROM issues ota
          WHERE ((ota.resolved_week = w.w) AND ((ota.project) :: text = 'PP' :: text))) AS resolved,
         (SELECT count(ota.id) AS count
          FROM issues ota
          WHERE ((ota.created_week <= w.w) AND ((ota.resolved_week IS NULL) OR (ota.resolved_week > w.w)) AND
                 ((ota.project) :: text = 'PP' :: text)))                               AS active
  FROM weeks w;

create view dynamics as
  SELECT w.w,
         p.short_name,
         (SELECT count(ota.id) AS count
          FROM issues ota
          WHERE ((ota.created_week = w.w) AND ((ota.project) :: text = (p.short_name) :: text)))  AS created,
         (SELECT count(ota.id) AS count
          FROM issues ota
          WHERE ((ota.resolved_week = w.w) AND ((ota.project) :: text = (p.short_name) :: text))) AS resolved,
         (SELECT count(ota.id) AS count
          FROM issues ota
          WHERE ((ota.created_week <= w.w) AND ((ota.resolved_week IS NULL) OR (ota.resolved_week > w.w)) AND
                 ((ota.project) :: text = (p.short_name) :: text)))                               AS active
  FROM weeks w,
       projects p;

create view issues_timeline_view as
  SELECT issue_history.issue_id,
         issue_history.update_date_time,
         issue_history.old_value_string,
         issue_history.new_value_string,
         NULL :: text AS time_spent
  FROM issue_history
  WHERE ((issue_history.field_name) :: text = 'Состояние' :: text)
  UNION ALL
  SELECT issues.id                AS issue_id,
         issues.created_date_time AS update_date_time,
         'Не существует' :: text  AS old_value_string,
         'Создана' :: text        AS new_value_string,
         NULL :: text             AS time_spent
  FROM issues
  UNION ALL
  SELECT issues.id                                                                                     AS issue_id,
         ((now() + ((5) :: double precision * '01:00:00' :: interval))) :: timestamp without time zone AS update_date_time,
         issues.state                                                                                  AS old_value_string,
         issues.state                                                                                  AS new_value_string,
         NULL :: text                                                                                  AS time_spent
  FROM issues
  WHERE (issues.resolved_date_time IS NULL)
  ORDER BY 1, 2;

create view sigma as
  WITH source AS (
      SELECT ((COALESCE(issues.time_agent, (0) :: bigint) + COALESCE(issues.time_developer, (0) :: bigint)) /
              ((9 * 60) * 60)) AS t
      FROM issues
      WHERE ((issues.project) :: text = ANY ((ARRAY['PP'::character varying, 'REA'::character varying]) :: text []))
  ), source_agg AS (
      SELECT source.t AS tu, count(source.t) AS c
      FROM source
      GROUP BY source.t
  ), avg_tu_value AS (
      SELECT (sum((((source_agg.tu * source_agg.c)) :: numeric * 1.0)) / sum(source_agg.c)) AS avg_tu
      FROM source_agg
  ), power_value AS (
      SELECT source_agg.tu,
             source_agg.c,
             avg_tu_value.avg_tu,
             power((avg_tu_value.avg_tu - (source_agg.tu) :: numeric), (2) :: numeric) AS p
      FROM source_agg,
           avg_tu_value
  ), sigma AS (
      SELECT round((sqrt((sum((power_value.p * (power_value.c) :: numeric)) / (sum(power_value.c) - (1) :: numeric))) *
                    (2) :: numeric), 1)                                                        AS sigma2,
             round((sqrt((sum((power_value.p * (power_value.c) :: numeric)) / (sum(power_value.c) - (1) :: numeric))) *
                    (3) :: numeric), 1)                                                        AS sigma3,
             (SELECT count(issues.id) AS count
              FROM issues
              WHERE ((issues.project) :: text = ANY
                     ((ARRAY['PP'::character varying, 'REA'::character varying]) :: text []))) AS count
      FROM power_value
  )
  SELECT sigma.sigma2,
         sigma.sigma3,
         CASE
           WHEN ((sigma.count) :: numeric < sigma.sigma3) THEN (((sigma.sigma3) :: integer + 2)) :: bigint
           ELSE sigma.count
             END                                                   AS count,
         (SELECT (max(source_agg.tu) + 1) FROM source_agg LIMIT 1) AS max_day,
         ((SELECT max(source_agg.c) AS max FROM source_agg) + 1)   AS max_val
  FROM sigma;

create view sigma_pp as
  WITH weeks AS (
      SELECT (((((ones.n + (10 * tens.n)) + (100 * hundreds.n)) + (1000 * thousands.n))) :: numeric(10, 2) /
              (10) :: numeric) AS id
      FROM (VALUES (0),
                   (1),
                   (2),
                   (3),
                   (4),
                   (5),
                   (6),
                   (7),
                   (8),
                   (9)) ones (n),
           (VALUES (0),
                   (1),
                   (2),
                   (3),
                   (4),
                   (5),
                   (6),
                   (7),
                   (8),
                   (9)) tens (n),
           (VALUES (0),
                   (1),
                   (2),
                   (3),
                   (4),
                   (5),
                   (6),
                   (7),
                   (8),
                   (9)) hundreds (n),
           (VALUES (0),
                   (1),
                   (2),
                   (3),
                   (4),
                   (5),
                   (6),
                   (7),
                   (8),
                   (9)) thousands (n)
      WHERE (((((ones.n + (10 * tens.n)) + (100 * hundreds.n)) + (1000 * thousands.n)) >= 0) AND
             ((((ones.n + (10 * tens.n)) + (100 * hundreds.n)) + (1000 * thousands.n)) <=
              ((SELECT sigma.max_day FROM sigma LIMIT 1) * 10)))
  )
  SELECT weeks.id,
         (SELECT sum(1) AS c
          FROM issues
          WHERE (((issues.project) :: text = ANY
                  ((ARRAY['PP'::character varying, 'REA'::character varying]) :: text [])) AND
                 ((((COALESCE(issues.time_agent, (0) :: bigint) + COALESCE(issues.time_developer, (0) :: bigint)) /
                    ((9 * 60) * 60))) :: numeric = weeks.id))) AS sigmavalue,
         CASE
           WHEN (weeks.id = ANY (ARRAY[(0)::numeric, ( SELECT sigma.sigma2
               FROM sigma
             LIMIT 1)])) THEN 0
           ELSE NULL :: integer
             END                                               AS gray1,
         CASE
           WHEN (weeks.id = ANY (ARRAY[(0)::numeric, ( SELECT sigma.sigma2
               FROM sigma
             LIMIT 1)])) THEN (SELECT sigma.max_val FROM sigma)
           ELSE NULL :: bigint
             END                                               AS gray2,
         CASE
           WHEN (weeks.id = ANY (ARRAY[( SELECT sigma.sigma2
               FROM sigma
             LIMIT 1), ( SELECT sigma.sigma3
               FROM sigma
             LIMIT 1)])) THEN 0
           ELSE NULL :: integer
             END                                               AS yeallow1,
         CASE
           WHEN (weeks.id = ANY (ARRAY[( SELECT sigma.sigma2
               FROM sigma
             LIMIT 1), ( SELECT sigma.sigma3
               FROM sigma
             LIMIT 1)])) THEN (SELECT sigma.max_val FROM sigma LIMIT 1)
           ELSE NULL :: bigint
             END                                               AS yeallow2,
         CASE
           WHEN (weeks.id = ANY (ARRAY[( SELECT sigma.sigma3
               FROM sigma
             LIMIT 1), (( SELECT sigma.max_day
               FROM sigma
             LIMIT 1))::numeric])) THEN 0
           ELSE NULL :: integer
             END                                               AS red1,
         CASE
           WHEN (weeks.id = ANY (ARRAY[( SELECT sigma.sigma3
               FROM sigma
             LIMIT 1), (( SELECT sigma.max_day
               FROM sigma
             LIMIT 1))::numeric])) THEN (SELECT sigma.max_val FROM sigma)
           ELSE NULL :: bigint
             END                                               AS red2
  FROM weeks
  ORDER BY weeks.id;

create view dynamics_extended as
  WITH temp AS (
      SELECT w.w,
             p.short_name                                                                                                                                     AS project,
             ets.field_value                                                                                                                                  AS subproject,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.created_week = w.w) AND ((ota.project) :: text = (p.short_name) :: text) AND
                     (ota.ets = ets.field_value) AND
                     ((ota.issue_type) :: text = 'Консультация' :: text)))                                                                                    AS consultation_created,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.resolved_week = w.w) AND ((ota.project) :: text = (p.short_name) :: text) AND
                     (ota.ets = ets.field_value) AND
                     ((ota.issue_type) :: text = 'Консультация' :: text)))                                                                                    AS consultation_resolved,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.created_week <= w.w) AND (ota.ets = ets.field_value) AND
                     ((ota.issue_type) :: text = 'Консультация' :: text) AND
                     ((ota.resolved_week IS NULL) OR (ota.resolved_week > w.w)) AND
                     ((ota.project) :: text = (p.short_name) :: text)))                                                                                       AS consultation_active,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.created_week = w.w) AND ((ota.project) :: text = (p.short_name) :: text) AND
                     (ota.ets = ets.field_value) AND ((ota.issue_type) :: text <> ALL
                                                      ((ARRAY['Консультация'::character varying, 'Новая функциональность'::character varying]) :: text [])))) AS error_created,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.resolved_week = w.w) AND ((ota.project) :: text = (p.short_name) :: text) AND
                     (ota.ets = ets.field_value) AND ((ota.issue_type) :: text <> ALL
                                                      ((ARRAY['Консультация'::character varying, 'Новая функциональность'::character varying]) :: text [])))) AS error_resolved,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.created_week <= w.w) AND (ota.ets = ets.field_value) AND ((ota.issue_type) :: text <> ALL
                                                                                    ((ARRAY['Консультация'::character varying, 'Новая функциональность'::character varying]) :: text [])) AND
                     ((ota.resolved_week IS NULL) OR (ota.resolved_week > w.w)) AND
                     ((ota.project) :: text = (p.short_name) :: text)))                                                                                       AS error_active,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.created_week = w.w) AND ((ota.project) :: text = (p.short_name) :: text) AND
                     (ota.ets = ets.field_value) AND
                     ((ota.issue_type) :: text = 'Новая функциональность' :: text)))                                                                          AS requirement_created,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.resolved_week = w.w) AND ((ota.project) :: text = (p.short_name) :: text) AND
                     (ota.ets = ets.field_value) AND
                     ((ota.issue_type) :: text = 'Новая функциональность' :: text)))                                                                          AS requirement_resolved,
             (SELECT count(ota.id) AS count
              FROM issues ota
              WHERE ((ota.created_week <= w.w) AND (ota.ets = ets.field_value) AND
                     ((ota.issue_type) :: text = 'Новая функциональность' :: text) AND
                     ((ota.resolved_week IS NULL) OR (ota.resolved_week > w.w)) AND
                     ((ota.project) :: text = (p.short_name) :: text)))                                                                                       AS requirement_active
      FROM weeks w,
           projects p,
           (SELECT DISTINCT custom_field_values.field_value
            FROM custom_field_values
            WHERE ((custom_field_values.field_name) :: text = 'Проект (ETS)' :: text)) ets
  )
  SELECT temp.w,
         temp.project,
         temp.subproject,
         temp.consultation_created,
         temp.consultation_resolved,
         temp.consultation_active,
         temp.error_created,
         temp.error_resolved,
         temp.error_active,
         temp.requirement_created,
         temp.requirement_resolved,
         temp.requirement_active
  FROM temp
  WHERE (((((((((temp.consultation_active + temp.consultation_created) + temp.consultation_resolved) +
               temp.error_active) + temp.error_created) + temp.error_resolved) + temp.requirement_active) +
           temp.requirement_created) + temp.requirement_resolved) > 0);

create view issues_pp as
  SELECT issues.id,
         issues.entity_id,
         issues.summary,
         issues.created_date_time,
         issues.created_date,
         issues.created_week,
         issues.updated_date_time,
         issues.updated_date,
         issues.updated_week,
         issues.resolved_date_time,
         issues.resolved_date,
         issues.resolved_week,
         (
             CASE
               WHEN ((issues.reporter_login) :: text ~~ 'system_user@%%' :: text) THEN 'guest' :: character varying
               ELSE issues.reporter_login
                 END) :: character varying(1024)                                      AS reporter_login,
         issues.comments_count,
         issues.votes,
         issues.subsystem,
         issues.sla,
         issues.sla_first_responce_index,
         issues.sla_first_responce_date_time,
         issues.sla_first_responce_date,
         issues.sla_first_responce_week,
         issues.sla_solution_index,
         issues.sla_solution_date_time,
         issues.sla_solution_date,
         issues.sla_solution_week,
         issues.project,
         CASE
           WHEN (comp.field_value = ANY
                 (ARRAY['4. Процессы и согласования'::text, '5. Моделирование и прогнозирование'::text, '6. Бюджетирование'::text, '7. Мобильное приложение'::text]))
                   THEN 'Бюджетирование' :: text
           ELSE 'Платформа' :: text
             END                                                                      AS product,
         COALESCE(pm.field_value, 'Не оценена' :: text)                               AS performance_measurement,
         COALESCE(comp.field_value, 'Компонент не указан' :: text)                    AS component,
         COALESCE(characteristic.field_value, 'Характеристика не определена' :: text) AS characteristic,
         COALESCE(state.field_value, 'Статус не определён' :: text)                   AS state,
         COALESCE(type.field_value, 'Тип не определён' :: text)                       AS type
  FROM (((((issues
      LEFT JOIN custom_field_values pm ON ((((issues.id) :: text = (pm.issue_id) :: text) AND
                                            ((pm.field_name) :: text = 'Оценка' :: text))))
      LEFT JOIN custom_field_values comp ON ((((issues.id) :: text = (comp.issue_id) :: text) AND
                                              ((comp.field_name) :: text = 'Subsystem' :: text))))
      LEFT JOIN custom_field_values characteristic ON ((((issues.id) :: text = (characteristic.issue_id) :: text) AND
                                                        ((characteristic.field_name) :: text =
                                                         'Характеристика' :: text))))
      LEFT JOIN custom_field_values state ON ((((issues.id) :: text = (state.issue_id) :: text) AND
                                               ((state.field_name) :: text = 'State' :: text))))
      LEFT JOIN custom_field_values type ON ((((issues.id) :: text = (type.issue_id) :: text) AND
                                              ((type.field_name) :: text = 'Type' :: text))))
  WHERE ((issues.project) :: text <> ALL
         ((ARRAY['PP_Lic'::character varying, 'SD'::character varying, 'SPAM'::character varying, 'PDP'::character varying, 'P_PROJ1'::character varying, 'W'::character varying, 'T'::character varying]) :: text []));

create view test_issues_pp_reporters as
  SELECT issues_pp.id, issues_pp.reporter_login, users.full_name
  FROM (issues_pp
      LEFT JOIN users ON (((issues_pp.reporter_login) :: text = (users.user_login) :: text)))
  WHERE (users.full_name IS NULL);

create view time_accounting as
  SELECT work_items.wi_date                                                       AS crdate,
         work_items.wi_duration                                                   AS units,
         ets_names.full_name                                                      AS agent,
         COALESCE(work_items.wi_updated, work_items.wi_created)                   AS changeddate,
         2                                                                        AS server,
         CASE
           WHEN ((i.project) :: text = 'W' :: text) THEN 'FV3002' :: text
           WHEN ((i.project) :: text = 'PP_Lic' :: text) THEN "left"(cfv_customer_proj.field_value,
                                                                     (strpos(cfv_customer_proj.field_value, '.' :: text) - 1))
           ELSE (ets.proj_ets) :: text
             END                                                                  AS projects,
         'youtrack' :: text                                                       AS teamproject,
         work_items.issue_id                                                      AS id,
         NULL :: text                                                             AS discipline,
         NULL :: text                                                             AS person,
         NULL :: text                                                             AS wit,
         i.summary                                                                AS title,
         (COALESCE(ets.iteration_path, (('ТП версии ' :: text ||
                                         CASE
                                           WHEN (cfv_pp_version.field_value ~~ 'PP 9%' :: text) THEN '9.0' :: text
                                           WHEN (cfv_pp_version.field_value ~~ 'PP 8.2%' :: text) THEN '8.2' :: text
                                           WHEN (cfv_pp_version.field_value ~~ 'PP 8%' :: text) THEN '8.0' :: text
                                           WHEN (cfv_pp_version.field_value ~~ 'PP 7.2' :: text) THEN '7.2' :: text
                                           WHEN (cfv_pp_version.field_value ~~ 'PP 7%' :: text) THEN '7.0' :: text
                                           WHEN (cfv_pp_version.field_value ~~ 'P 5%' :: text) THEN '5.26' :: text
                                           ELSE '9.0' :: text
                                             END)) :: character varying)) :: text AS iterationpath,
         '' :: text                                                               AS role
  FROM (((((((((((work_items
      LEFT JOIN issues i ON (((work_items.issue_id) :: text = (i.id) :: text)))
      LEFT JOIN users ON (((work_items.author_login) :: text = (users.user_login) :: text)))
      LEFT JOIN ets_names ON (((users.email) :: text = (ets_names.fsight_email) :: text)))
      LEFT JOIN custom_field_values cfv_proj ON ((((work_items.issue_id) :: text = (cfv_proj.issue_id) :: text) AND
                                                  ((cfv_proj.field_name) :: text = 'Проект (ETS)' :: text))))
      LEFT JOIN custom_field_values cfv_customer_proj ON ((
    ((work_items.issue_id) :: text = (cfv_customer_proj.issue_id) :: text) AND
    ((cfv_customer_proj.field_name) :: text = 'Проект заказчика' :: text))))
      LEFT JOIN custom_field_values cfv_prod ON ((((work_items.issue_id) :: text = (cfv_prod.issue_id) :: text) AND
                                                  ((cfv_prod.field_name) :: text = 'Продукт' :: text))))
      LEFT JOIN custom_field_values cfv_pp_version ON ((
    ((work_items.issue_id) :: text = (cfv_pp_version.issue_id) :: text) AND
    ((cfv_pp_version.field_name) :: text = 'Версия Prognoz Platform' :: text))))
      LEFT JOIN custom_field_values cfv_ext_tools ON ((
    ((work_items.issue_id) :: text = (cfv_ext_tools.issue_id) :: text) AND
    ((cfv_ext_tools.field_name) :: text = 'Инструменты расширений' :: text))))
      LEFT JOIN custom_field_values cfv_component ON ((
    ((work_items.issue_id) :: text = (cfv_component.issue_id) :: text) AND
    ((cfv_component.field_name) :: text = 'Subsystem' :: text))))
      LEFT JOIN custom_field_values customer ON ((((work_items.issue_id) :: text = (customer.issue_id) :: text) AND
                                                  ((customer.field_name) :: text = 'Заказчик' :: text))))
      LEFT JOIN dictionary_project_customer_ets ets ON ((((i.project) :: text = (ets.proj_short_name) :: text) AND
                                                         ((customer.field_value = (ets.customer) :: text) OR
                                                          (customer.field_value IS NULL)))));