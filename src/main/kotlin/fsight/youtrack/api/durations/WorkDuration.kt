package fsight.youtrack.api.durations

import com.google.gson.annotations.SerializedName
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class WorkDuration(private val dsl: DSLContext) : IWorkDuration {
    internal data class WorkDurationModel(
        var id: String? = null,
        var summary: String? = null,
        @SerializedName("created_date_time")
        var createdDateTime: Timestamp? = null,
        @SerializedName("resolved_date_time")
        var resolvedDateTime: Timestamp? = null,
        var priority: String? = null,
        var type: String? = null,
        var component: String? = null,
        var state: String? = null,
        @SerializedName("first_comment_date")
        var firstCommentDate: Timestamp? = null,
        @SerializedName("time_spent_before_first_comment")
        var timeSpentBeforeFirstComment: Double? = null,
        @SerializedName("resolved_date")
        var resolvedDate: Timestamp? = null,
        @SerializedName("time_spent_before_resolved")
        var timeSpentBeforeResolved: Double? = null
    )

    override fun getByProject(projectShortName: String): List<Any> {
        val p = projectShortName.split(",").joinToString(separator = ",") { v -> "'$v'" }
        println(p)
        val query = """SELECT id,
       summary,
       created_date_time,
       resolved_date_time,
       priority.field_value                                      AS priority,
       type.field_value                                          AS type,
       component.field_value                                     AS component,
       state.field_value                                         AS state,
       (SELECT min(created) AS created
        FROM issue_comments
        WHERE issues.id = issue_comments.issue_id)               AS first_comment_date,
       (SELECT round(sum(time_spent) / 3600, 1) AS time_pent
        FROM issue_timeline
        WHERE issues.id = issue_timeline.issue_id
          AND transition_owner != 'YouTrackUser'
          AND issue_timeline.state_from_date <= (SELECT min(created) AS created
                                                 FROM issue_comments
                                                 WHERE issues.id = issue_comments.issue_id))
                                                                 AS time_spent_before_first_comment,
       CASE
         WHEN state IN ('Ожидает подтверждения', 'Подтверждена', 'Без подтверждения', 'Неполная')
           THEN (SELECT max(update_date_time)
                 FROM issue_history
                 WHERE issues.id = issue_history.issue_id
                   AND issue_history.field_name = 'Состояние'
                   AND new_value_string IN ('Ожидает ответа', 'Ожидает подтверждения', 'Incomplete'))
         ELSE NULL END                                           AS resolved_date,
       (SELECT round(sum(time_spent) / 3600, 1) AS time_pent
        FROM issue_timeline
        WHERE issues.id = issue_timeline.issue_id
          AND transition_owner != 'YouTrackUser'
          AND issue_timeline.state_from_date <= coalesce((SELECT max(update_date_time)
                                                          FROM issue_history
                                                          WHERE issues.id = issue_history.issue_id
                                                            AND issue_history.field_name = 'Состояние'
                                                            AND new_value_string IN
                                                                ('Ожидает ответа', 'Ожидает подтверждения', 'Incomplete')),
                                                         now())) AS time_spent_before_resolved
FROM issues
       LEFT JOIN custom_field_values priority ON issues.id = priority.issue_id AND priority.field_name = 'Priority'
       LEFT JOIN custom_field_values type ON issues.id = type.issue_id AND type.field_name = 'Type'
       LEFT JOIN custom_field_values component ON issues.id = component.issue_id AND component.field_name = 'Subsystem'
       LEFT JOIN custom_field_values state ON issues.id = state.issue_id AND state.field_name = 'State'
WHERE project_short_name  in ($p)"""

        val result = dsl.fetch(query).into(WorkDurationModel::class.java)
        /*result.forEach { println(it) }*/
        return result
    }
}
