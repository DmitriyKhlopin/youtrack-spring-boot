package fsight.youtrack.models

data class StringFieldValue(
    override val id: String?,
    override val `$type`: String?,
    override val value: String?
) : FieldValueBase(id, `$type`, value)

data class SingleFieldValue(
    override val id: String?,
    override val `$type`: String?,
    override val value: ActualValue?
) : FieldValueBase(id, `$type`, value)

data class MultiFieldValue(
    override val id: String?,
    override val `$type`: String?,
    override val value: List<ActualValue>?
) : FieldValueBase(id, `$type`, value)

open class FieldValueBase(
    open val id: String?,
    @Suppress("unused")
    open val `$type`: String?,
    open val value: Any?
)
