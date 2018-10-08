package fsight.youtrack.models

data class ProjectCustomField(var projectShortName: String = "",
                              val name: String,
                              val url: String,
                              var fieldType: String = "",
                              var emptyText: String = "",
                              var canBeEmpty: Boolean = false,
                              var param: String = "",
                              var defaultValue: String = "")