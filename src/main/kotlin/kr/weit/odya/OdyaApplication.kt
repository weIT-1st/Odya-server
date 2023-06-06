package kr.weit.odya

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication(exclude = [ElasticsearchDataAutoConfiguration::class])
class OdyaApplication

fun main(args: Array<String>) {
    runApplication<OdyaApplication>(*args)
}
