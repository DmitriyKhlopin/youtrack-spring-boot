package fsight.youtrack.api.hints

interface IHints {
    fun getAllRepositories(): List<Hints.CustomerRepository>
    fun getRepositoriesByCustomer(project: String?, customer: String?): List<Hints.CustomerRepository>
    fun postRepository(repository: Hints.CustomerRepository): Hints.CustomerRepository
}