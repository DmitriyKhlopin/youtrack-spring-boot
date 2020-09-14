package fsight.youtrack.api.fields

import com.google.gson.JsonObject
import fsight.youtrack.generated.jooq.tables.CustomFieldPresets.CUSTOM_FIELD_PRESETS
import fsight.youtrack.generated.jooq.tables.records.CustomFieldPresetsRecord
import fsight.youtrack.models.youtrack.CustomField
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class CustomFields(private val dsl: DSLContext) : ICustomFields {
    override fun attachField(): Any {
        return dsl.select().from(CUSTOM_FIELD_PRESETS).fetchInto(CustomFieldPresetsRecord::class.java).map { it.toJson() }.toString()
    }
}

fun CustomFieldPresetsRecord.toJson(): CustomField = CustomField(name = this.name, id = this.id, valueType = this.valueType)



