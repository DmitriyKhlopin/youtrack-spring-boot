package fsight.youtrack.models

data class ProjectCustomFieldParameters(
        val name: String,
        val type: String,
        val emptyText: String,
        val canBeEmpty: Boolean,
        val param: Any,
        val defaultValue: Any
)