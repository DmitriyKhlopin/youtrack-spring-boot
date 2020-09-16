package fsight.youtrack.etl.fields

import fsight.youtrack.AUTH
import fsight.youtrack.Converter
import fsight.youtrack.ETLState
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.etl.IETLState
import fsight.youtrack.generated.jooq.tables.CustomFieldPresets.CUSTOM_FIELD_PRESETS
import fsight.youtrack.generated.jooq.tables.records.CustomFieldPresetsRecord
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CustomFieldsETL(private val dsl: DSLContext) : ICustomFieldsETL {
    @Autowired
    private lateinit var etlStateService: IETLState

    override fun getCustomFields(): Any {
        try {
            val res = YouTrackAPI.create(Converter.GSON).getListOfCustomFields(AUTH).execute()
            val records = res.body()?.map {
                CustomFieldPresetsRecord()
                    .setName(it.name)
                    .setType(null)
                    .setIsPrivate(null)
                    .setIsVisible(null)
                    .setIsAutoAttached(null)
                    .setDefault(null)
                    .setId(it.id)
                    .setTypeId(it.fieldType?.id)
                    .setValueType(it.fieldType?.valueType)
            }
            dsl.loadInto(CUSTOM_FIELD_PRESETS).onDuplicateKeyIgnore().loadRecords(records).fields(
                CUSTOM_FIELD_PRESETS.NAME,
                CUSTOM_FIELD_PRESETS.TYPE,
                CUSTOM_FIELD_PRESETS.IS_PRIVATE,
                CUSTOM_FIELD_PRESETS.IS_VISIBLE,
                CUSTOM_FIELD_PRESETS.IS_AUTO_ATTACHED,
                CUSTOM_FIELD_PRESETS.DEFAULT,
                CUSTOM_FIELD_PRESETS.ID,
                CUSTOM_FIELD_PRESETS.TYPE_ID,
                CUSTOM_FIELD_PRESETS.VALUE_TYPE
            ).execute()
            return res.body() ?: listOf<String>()
        } catch (e: Exception) {
            etlStateService.state = ETLState.DONE
            return ""
        }

    }
}
