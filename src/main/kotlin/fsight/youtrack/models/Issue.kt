package fsight.youtrack.models

data class Issue(var id: String, var entityId: String, var field: List<Field>, var comment: List<Comment>, var tag: List<Tag>)

