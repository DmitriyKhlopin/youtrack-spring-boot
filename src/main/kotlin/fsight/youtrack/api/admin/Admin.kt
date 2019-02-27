package fsight.youtrack.api.admin

import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class Admin : IAdmin {
    //TODO implement
    override fun addBuild(name: String, date: Timestamp): Boolean {
        return true
    }
}
