package fsight.youtrack.api.tfs.hooks


import com.google.gson.Gson
import fsight.youtrack.models.hooks.Hook
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class TFSHooksController(private val service: ITFSHooks) {
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
            val jsonBody = Gson().fromJson(body, Hook::class.java)
            service.postHook(jsonBody)
            /*return if (InetAddress.getLocalHost().hostName != "SPB-FSIGHT11") {
                println("*** Checking server ***")
                val status = API.create(environment = "TEST", converter = Converter.GSON).getStatus().execute()
                if (status.code() == 200) {
                    println("*** Redirecting ***")
                    val res = API.create(environment = "TEST", converter = Converter.GSON).postHook(body = jsonBody).execute()
                    ResponseEntity.status(res.code()).body(res.body())
                } else service.postHook(jsonBody, listOf())
            } else service.postHook(jsonBody, listOf())*/
        } catch (e: Exception) {
            println(e.message)
            ResponseEntity.status(HttpStatus.OK).body(e.message)
        }
    }

    @GetMapping("/api/tfs/serviceHooks/post/{id}")
    fun postCommand(@PathVariable("id") id: String? = null): ResponseEntity<Any> {
        return service.postCommand(id, "Состояние Открыта")
    }

}


