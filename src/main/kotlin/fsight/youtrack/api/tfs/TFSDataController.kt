package fsight.youtrack.api.tfs

import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000", "http://10.0.172.42:3000"])
@RestController
class TFSDataController(private val service: TFSDataService) {
    @GetMapping("/api/tfs/count")
    fun getItemsCount() = service.getItemsCount()

    @GetMapping("/api/tfs")
    fun getItems(
            @RequestParam("offset", required = false) offset: Int? = null,
            @RequestParam("limit", required = false) limit: Int? = null
    ) = service.getItems(offset, limit)

    @GetMapping("/api/tfs/item/{id}")
    fun postItemToYouTrack(
            @PathVariable("id") id: Int,
            @RequestParam("action", required = false) action: String? = null
    ) = when (action) {
        "postToYT" -> service.postItemToYouTrack(id)
        else -> service.getItemById(id)
    }
}