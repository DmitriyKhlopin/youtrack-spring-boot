package fsight.youtrack.api.fields

import fsight.youtrack.etl.fields.ICustomFieldsETL
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomFieldsController(private val etlService: ICustomFieldsETL, private val service: ICustomFields ) {
    @GetMapping("/api/fields")
    fun getCustomFields(): Any {
        return etlService.getCustomFields()
    }

    @GetMapping("/api/fields/attach")
    fun attachCustomField(): Any {
        return service.attachField()
    }
}
