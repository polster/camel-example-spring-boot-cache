package sample.camel;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cache.CacheConstants;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class CachingRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration().host("localhost").port(8080);
        restConfiguration()
                .contextPath("/caching-router").apiContextPath("/api-doc")
                .apiProperty("api.title", "Camel REST API")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true")
                .apiContextRouteId("doc-api")
                .bindingMode(RestBindingMode.json);

        rest("/admin/tokens").description("Admin Token REST service")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .get("/{userId}").description("Get user ID specific admin token")
                .route().routeId("admin-token")
                .to("direct:get-admin-token")
                .endRest();

        from("direct:get-admin-token").routeId("get-admin-token")
                .setProperty("userId", simple("${header.userId}"))
                // Prepare headers
                .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_GET))
                .setHeader(CacheConstants.CACHE_KEY, exchangeProperty("userId"))
                .to("cache://AdminTokenCache")
                .choice().when(header(CacheConstants.CACHE_ELEMENT_WAS_FOUND).isNull())
                    .to("direct:ep-admin-token")
                    .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_ADD))
                    .setHeader(CacheConstants.CACHE_KEY, exchangeProperty("userId"))
                    .to("cache://AdminTokenCache")
                .end();

        // cache configuration
        from("cache://AdminTokenCache" +
                "?maxElementsInMemory=1000" +
                "&memoryStoreEvictionPolicy=" +
                "MemoryStoreEvictionPolicy.LFU" +
                "&overflowToDisk=false" +
                "&eternal=true" +
                "&timeToLiveSeconds=300" +
                "&timeToIdleSeconds=300" +
                "&diskPersistent=false").routeId("admin-token-cache")
                .log(LoggingLevel.DEBUG, "Get admin token from cache");

        // Admin token REST end point
        from("direct:ep-admin-token").routeId("ep-admin-token")
                .removeHeaders("*")
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .setHeader(Exchange.ACCEPT_CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .to("rest:get:tokens/new")
                .convertBodyTo(String.class);
    }

}
