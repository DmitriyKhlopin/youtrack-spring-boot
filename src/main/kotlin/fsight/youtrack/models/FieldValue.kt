package fsight.youtrack.models

data class FieldValue(
    override val id: String?,
    override val `$type`: String?,
    override val value: ActualValue?
) : FieldValueBase(id, `$type`, value)

data class FieldValue2(
    override val id: String?,
    override val `$type`: String?,
    override val value: List<ActualValue>?
) : FieldValueBase(id, `$type`, value)

open class FieldValueBase(
    open val id: String?,
    open val `$type`: String?,
    open val value: Any?
)
