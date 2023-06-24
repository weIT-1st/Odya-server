package kr.weit.odya.client

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kr.weit.odya.OdyaApplication
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.env.Environment
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

private const val DEFAULT_TIME_OUT_MS: Long = 5000

@Component
class HttpInterfaceFactoryBeanFactoryPostProcessor : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val beanDefinitions =
            HttpInterfaceClassFinder().findBeanDefinitions(beanFactory.getBean(Environment::class.java))
        beanDefinitions.stream().filter {
            StringUtils.hasText(it.beanClassName)
        }.forEach {
            findClassAndRegisterAsSingletonBean(beanFactory, HttpInterfaceFactory(), it)
        }
    }

    private fun findClassAndRegisterAsSingletonBean(
            beanFactory: ConfigurableListableBeanFactory,
            factory: HttpInterfaceFactory,
            beanDefinition: BeanDefinition
    ) {
        val beanClassName = getBeanClassName(beanDefinition)
        beanFactory.registerSingleton(
                beanClassName,
                factory.create(findHttpInterfaceClass(beanDefinition))
        )
    }

    private fun findHttpInterfaceClass(beanDefinition: BeanDefinition): Class<*> {
        try {
            val beanClassName = getBeanClassName(beanDefinition)
            return ClassUtils.forName(beanClassName, this::class.java.classLoader)
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(e)
        }
    }

    private fun getBeanClassName(beanDefinition: BeanDefinition): String =
            beanDefinition.beanClassName ?: throw IllegalStateException("beanClassName is null")
}

class HttpInterfaceClassFinder {
    fun findBeanDefinitions(environment: Environment): MutableSet<BeanDefinition> {
        val scanner = object : ClassPathScanningCandidateComponentProvider(false, environment) {
            override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
                return beanDefinition.metadata.isInterface && beanDefinition.metadata.hasAnnotation(HttpExchange::class.java.name)
            }
        }
        scanner.addIncludeFilter(AnnotationTypeFilter(HttpExchange::class.java))
        return scanner.findCandidateComponents(OdyaApplication::class.java.`package`.name)
    }
}

class HttpInterfaceFactory {
    fun <S> create(clientClass: Class<S>): S {
        val httpExchange = AnnotationUtils.findAnnotation(clientClass, HttpExchange::class.java)
                ?: throw IllegalStateException("HttpExchange annotation not found")
        require(StringUtils.hasText(httpExchange.url)) { "HttpExchange url is empty" }
        val webClient = getWebClient(httpExchange.url)
        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build().createClient(clientClass)
    }

    private fun getWebClient(baseUrl: String) = WebClient
            .builder()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient()))
            .build()

    private fun httpClient() = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_TIME_OUT_MS.toInt())
            .responseTimeout(Duration.ofMillis(DEFAULT_TIME_OUT_MS))
            .doOnConnected { connection ->
                connection
                        .addHandlerFirst(ReadTimeoutHandler(DEFAULT_TIME_OUT_MS, TimeUnit.MILLISECONDS))
                        .addHandlerLast(WriteTimeoutHandler(DEFAULT_TIME_OUT_MS, TimeUnit.MILLISECONDS))
            }
}
