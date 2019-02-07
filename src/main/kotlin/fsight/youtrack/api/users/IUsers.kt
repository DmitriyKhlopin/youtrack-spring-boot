package fsight.youtrack.api.users

import fsight.youtrack.models.UserDetails

interface IUsers {
    fun getAll(): List<UserDetails>
}
