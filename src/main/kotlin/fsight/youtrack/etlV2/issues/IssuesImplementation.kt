package fsight.youtrack.etlV2.issues

import fsight.youtrack.models.v2.Issue
import org.springframework.stereotype.Service

@Service
class IssuesImplementation : IssuesServiceV2 {
    override fun getPaginated(size: Int) {

    }

    override fun getById(id: String): Issue {
        val res = IssuesFromYT.create().getById(id).execute()
        return res.body()!!
    }
}