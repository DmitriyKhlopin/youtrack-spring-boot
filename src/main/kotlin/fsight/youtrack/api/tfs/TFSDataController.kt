package fsight.youtrack.api.tfs

import com.google.gson.Gson
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class TFSDataController(private val service: ITFSData) {
    @GetMapping("/api/tfs/count")
    fun getItemsCount() = service.getItemsCount()

    @GetMapping("/api/tfs")
    fun getItems(
            @RequestParam("offset", required = false) offset: Int? = null,
            @RequestParam("limit", required = false) limit: Int? = null,
            @RequestParam("action", required = false) action: String? = null,
            @RequestParam("iteration", required = false) iteration: String? = null,
            @RequestParam("build", required = false) build: String? = null,
            @RequestParam("changeRequestId", required = false) changeRequestId: Int? = null
    ): ResponseEntity<Any> {
        return when {
            action == "postToYT" && iteration != null -> service.postItemsToYouTrack(iteration)
            action == "postToYT" && iteration == null && offset != null && limit != null -> service.postItemsToYouTrack(
                    offset,
                    limit
            )
            action == "fixed" && iteration != null && build != null -> service.getDefectsByFixedBuildId(
                    iteration,
                    build
            )
            else -> ResponseEntity.status(HttpStatus.OK).body("Undefined set of parameters.")
        }
    }

    @PostMapping("/api/tfs")
    fun postChangeRequest(
            @RequestParam("action", required = false) action: String? = null,
            @RequestParam("changeRequestId", required = false) changeRequestId: Int? = null,
            @RequestBody(required = false) body: String? = null
    ): ResponseEntity<Any> {
        println(body)
        return when {
            action == "postChangeRequest" && changeRequestId != null -> service.postChangeRequestById(
                    changeRequestId,
                    body
            )
            else -> /*service.getItems(offset, limit)*/ ResponseEntity.status(HttpStatus.OK).body("Undefined set of parameters.")
        }
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

    @GetMapping("/api/tfs/iterations")
    fun getAllIterations(): ResponseEntity<Any> = service.getIterations()

    @GetMapping("/api/tfs/builds")
    fun getBuildsByIteration(@RequestParam("iteration") iteration: String? = null): ResponseEntity<Any> =
            when (iteration) {
                null -> service.getBuildsByIteration("\\P7\\PP9\\9.0\\1.0\\Update 1")
                else -> service.getBuildsByIteration(iteration)
            }

    @GetMapping("/api/tfs/serviceHooks")
    fun getHook(
            @RequestParam("limit", required = false) limit: Int? = null,
            @RequestParam("postable", required = false) postable: Boolean? = null
    ): ResponseEntity<Any> =
            when (postable) {
                true -> service.getPostableHooks(limit ?: 1)
                else -> service.getHook(limit ?: 1)
            }


    @PostMapping("/api/tfs/serviceHooks")
    fun postHook(
            @RequestParam("bugs", required = false) bugs: List<Int>? = null,
            @RequestBody body: String?
    ): ResponseEntity<Any> {
        return try {
            val jsonBody = Gson().fromJson(body, TFSData.Hook::class.java)
            service.postHook(jsonBody, bugs ?: listOf())
            /*if (InetAddress.getLocalHost().hostName != "hlopind") {
                println("*** Checking server ***")
                val status = API.create(environment = "TEST", converter = Converter.GSON).getStatus().execute()
                if (status.code() == 200) {
                    println("*** Redirecting ***")
                    val res = API.create(environment = "TEST", converter = Converter.GSON).postHook(body = jsonBody).execute()
                    ResponseEntity.status(res.code()).body(res.body())
                } else service.postHook(jsonBody)
            } else service.postHook(jsonBody)*/
        } catch (e: Exception) {
            println(e.message)
            ResponseEntity.status(HttpStatus.OK).body(e.message)
        }
    }

    @GetMapping("/api/tfs/serviceHooks/post/{id}")
    fun postCommand(@PathVariable("id") id: String? = null): ResponseEntity<Any> {
        return service.postCommand(id, "Состояние Открыта", "#{Направлена разработчику}")
    }
}


