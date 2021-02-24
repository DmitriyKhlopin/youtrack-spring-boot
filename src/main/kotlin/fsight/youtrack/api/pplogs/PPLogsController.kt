package fsight.youtrack.api.pplogs

import fsight.youtrack.LogType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


@RestController
class PPLogsController {
    @PostMapping("/api/pp_logs")
    fun uploadFile(@RequestParam("file") file: MultipartFile): Any? {
        /*try {
            val fileUploadVo = ObjectMapper().readValue(jsonFileVo, FileUploadVo::class.java);
        } catch (e: Exception) {
            e.printStackTrace();
        }*/

        val records = ArrayList<String>()
        var line: String? = null
        val stream: InputStream = file.inputStream
        val br = BufferedReader(InputStreamReader(stream))
        while (br.readLine().also { line = it } != null) {
            line?.let { records.add(it) }
        }
        val format = records.first()
        println(format)
        println(file.originalFilename.toString().substringAfterLast("."))
        val type = LogType.from(file.originalFilename.toString().substringAfterLast(".").toLowerCase())
        return type
    }
}
