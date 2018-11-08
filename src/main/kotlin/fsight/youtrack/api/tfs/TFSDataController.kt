package fsight.youtrack.api.tfs

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class TFSDataController(private val service: TFSDataService) {
    @GetMapping("/api/tfs/count")
    fun getItemsCount() = service.getItemsCount()

    @GetMapping("/api/tfs")
    fun getItems(
            @RequestParam("offset", required = false) offset: Int? = null,
            @RequestParam("limit", required = false) limit: Int? = null,
            @RequestParam("action", required = false) action: String? = null,
            @RequestParam("iteration", required = false) iteration: String? = null
    ): ResponseEntity<Any> = when {
        action == "postToYT" && iteration != null -> service.postItemsToYouTrack(iteration)
        action == "postToYT" && iteration == null && offset != null && limit != null -> service.postItemsToYouTrack(offset, limit)
        else -> service.getItems(offset, limit)
    }

    @GetMapping("/api/tfs/{id}")
    fun postItemToYouTrack(
            @PathVariable("id") id: Int,
            @RequestParam("action", required = false) action: String? = null
    ): ResponseEntity<Any> = when (action) {
        "postToYT" -> service.postItemToYouTrack(id)
        "toJSON" -> service.toJson(id)
        else -> ResponseEntity.status(HttpStatus.OK).body(service.getItemById(id))
    }
}