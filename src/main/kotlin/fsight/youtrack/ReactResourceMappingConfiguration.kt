package fsight.youtrack

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.ResourceResolver
import org.springframework.web.servlet.resource.ResourceResolverChain
import java.io.IOException
import javax.servlet.http.HttpServletRequest


@Configuration
class ReactResourceMappingConfiguration : WebMvcConfigurer {
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/").setViewName("forward:/index.html")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val resolver = ReactResourceResolver()


        registry.addResourceHandler("/**")
                .resourceChain(true)
                .addResolver(resolver)
        //.addResourceHandler("/resources/**")
        //.addResourceLocations("/resources/")
    }

    inner class ReactResourceResolver : ResourceResolver {
        private val index = ClassPathResource(REACT_DIR + "index.html")
        private val rootStaticFiles = listOf("favicon.io", "asset-manifest.json", "manifest.json", "service-worker.js")

        override fun resolveResource(
                request: HttpServletRequest?, requestPath: String, locations: List<Resource>, chain: ResourceResolverChain
        ): Resource? {
            println(requestPath)
            return resolve(requestPath)
        }

        override fun resolveUrlPath(
                resourcePath: String,
                locations: List<Resource>,
                chain: ResourceResolverChain
        ): String? {
            val resolvedResource = resolve(resourcePath) ?: return null
            return try {
                resolvedResource.url.toString()
            } catch (e: IOException) {
                resolvedResource.filename
            }
        }

        private fun resolve(requestPath: String?): Resource? {
            if (requestPath == null) return null
            return if (rootStaticFiles.contains(requestPath) || requestPath.startsWith(REACT_STATIC_DIR)) {
                ClassPathResource(REACT_DIR + requestPath)
            } else
                index
        }
    }
}
