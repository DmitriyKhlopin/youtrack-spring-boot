package fsight.youtrack.etl.bundles

import com.google.gson.Gson
import fsight.youtrack.AUTH
import fsight.youtrack.Converter
import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.generated.jooq.tables.BundleValues.BUNDLE_VALUES
import fsight.youtrack.generated.jooq.tables.records.BundleValuesRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class BundleV2(private val dslContext: DSLContext) : IBundle {
    override fun getBundles() {
        val arr = arrayListOf<BundleValue>()
        val res = YouTrackAPI.create(Converter.GSON).getBundleValues(AUTH).execute()
        println(Gson().toJson(res.body()?.firstOrNull()))
        res.body()?.forEach { item: CustomField ->
            item.instances?.forEach { instance: FieldInstance ->
                /*println(instance.bundle?.`$type`)*/
                instance.bundle?.values?.forEach { value ->
                    arr.add(value.apply {
                        projectId = instance.project?.id
                        projectName = instance.project?.shortName
                        fieldId = instance.id
                        fieldName = item.name
                        `$type` = instance.bundle.`$type`
                    })
                }
            }
        }
        dslContext.deleteFrom(BUNDLE_VALUES).execute()
        val i = arr.map { item -> BundleValuesRecord().setId(item.id).setName(item.name).setProjectId(item.projectId).setProjectName(item.projectName).setFieldId(item.fieldId).setFieldName(item.fieldName).setType(item.`$type`) }
        dslContext.loadInto(BUNDLE_VALUES).loadRecords(i).fields(BUNDLE_VALUES.ID, BUNDLE_VALUES.NAME, BUNDLE_VALUES.PROJECT_ID, BUNDLE_VALUES.PROJECT_NAME, BUNDLE_VALUES.FIELD_ID, BUNDLE_VALUES.FIELD_NAME, BUNDLE_VALUES.TYPE).execute()
    }
}