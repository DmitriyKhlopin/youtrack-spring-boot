package fsight.youtrack.models.youtrack

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable

@Serializable
data class SimpleIssueCustomField(
        var projectCustomField: ProjectCustomField?,
        @ContextualSerialization
        var value: Any?,
        var name: String?,
        var id: String?,
        var `$type`: String?
)/*{
        @Serializer(forClass = SimpleIssueCustomField::class)
        companion object: KSerializer<SimpleIssueCustomField>{
                override val descriptor: SerialDescriptor =
                        StringDescriptor.withName("SimpleIssueCustomField")

                override fun deserialize(decoder: Decoder): SimpleIssueCustomField {
                      return SimpleIssueCustomField(
                              decoder.context
                      )
                }
        }
}*/