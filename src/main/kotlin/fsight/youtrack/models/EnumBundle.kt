package fsight.youtrack.models

data class EnumBundle(
        var name: String,
        /*@SerializedName(value = "value")*/
        /*@Expose*/
        var value: List<Value>
)

