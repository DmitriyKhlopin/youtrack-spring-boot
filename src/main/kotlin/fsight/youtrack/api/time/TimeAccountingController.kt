package fsight.youtrack.api.time

import fsight.youtrack.models.TimeAccountingDictionaryItem
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class TimeAccountingController(private val service: ITimeAccounting) {
    @GetMapping("/api/wi_today")
    fun getTimeAccountingToday() = service.getWorkItemsToday("")

    @GetMapping("/api/wi_yesterday")
    fun getTimeAccountingYesterday() = service.getWorkItemsYesterday("")

    @GetMapping("/api/time")
    fun getWorkItems(
            @RequestParam("dateFrom", required = false) dateFrom: String? = null,
            @RequestParam("dateTo", required = false) dateTo: String? = null
    ) = service.getWorkItems(dateFrom, dateTo)

    @GetMapping("/api/time/dictionary")
    fun getDictionary() = service.getDictionary()

    @GetMapping("/api/time/work/fact")
    fun getFactWork(
            @RequestParam("emails", required = false) emails: String? = null,
            @RequestParam("dateFrom", required = false) dateFrom: String? = null,
            @RequestParam("dateTo", required = false) dateTo: String? = null
    ) = service.getFactWork(emails, dateFrom, dateTo)


    @PostMapping("/api/time/dictionary")
    fun postDictionaryItem(@RequestBody body: TimeAccountingDictionaryItem): ResponseEntity<Any> {
        return service.postDictionaryItem(body)
    }

    @PostMapping("/api/time/dictionary/{id}")
    fun toggleDictionaryItem(@PathVariable("id") id: Int): ResponseEntity<Any> {
        return service.toggleDictionaryItemById(id)
    }

    @DeleteMapping("/api/time/dictionary/{id}")
    fun deleteDictionaryItem(@PathVariable("id") id: Int): ResponseEntity<Any> {
        return service.deleteDictionaryItemById(id)
    }
}
