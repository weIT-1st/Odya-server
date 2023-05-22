package kr.weit.odya

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class OdyaApplication

fun main(args: Array<String>) {
    runApplication<OdyaApplication>(*args)
}
