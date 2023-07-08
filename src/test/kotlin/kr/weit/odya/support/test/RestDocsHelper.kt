package kr.weit.odya.support.test

import kr.weit.odya.support.test.RestDocsHelper.Companion.EXAMPLE
import kr.weit.odya.support.test.RestDocsHelper.Companion.field
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.RequestFieldsSnippet
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.snippet.Attributes.Attribute
import org.springframework.restdocs.snippet.Snippet
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultHandlersDsl
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

private const val EXAMPLE_NULL = "null"

class RestDocsHelper {
    companion object {
        const val EXAMPLE = "example"

        fun generateRestDocMockMvc(
            webApplicationContext: WebApplicationContext,
            restDocumentationContextProvider: RestDocumentationContextProvider,
        ): MockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
            .apply<DefaultMockMvcBuilder>(
                MockMvcRestDocumentation.documentationConfiguration(restDocumentationContextProvider)
                    .operationPreprocessors()
                    .withRequestDefaults(Preprocessors.prettyPrint())
                    .withResponseDefaults(Preprocessors.prettyPrint()),
            )
            .build()

        fun requestBody(vararg fields: RestDocsField): RequestFieldsSnippet =
            PayloadDocumentation.requestFields(fields.map { it.descriptor })

        fun responseBody(vararg fields: RestDocsField): ResponseFieldsSnippet =
            PayloadDocumentation.responseFields(fields.map { it.descriptor })

        fun field(key: String, value: String?): Attribute {
            return Attribute(key, value)
        }

        fun MockMvcResultHandlersDsl.createDocument(identifier: String, vararg snippets: Snippet) {
            return handle(MockMvcRestDocumentation.document("{class-name}/$identifier", *snippets))
        }

        fun createPathDocument(value: Any, vararg snippets: Snippet): RestDocumentationResultHandler =
            MockMvcRestDocumentation.document("{class-name}/$value", *snippets)
    }
}

infix fun String.type(
    type: JsonFieldType,
): RestDocsField = createField(this, type)

private fun createField(
    path: String,
    type: JsonFieldType,
): RestDocsField = RestDocsField(PayloadDocumentation.fieldWithPath(path).type(type))

class RestDocsField(
    val descriptor: FieldDescriptor,
) {
    infix fun isOptional(value: Boolean): RestDocsField {
        if (value) descriptor.optional()
        return this
    }

    infix fun description(value: String): RestDocsField {
        descriptor.description(value)
        return this
    }

    infix fun example(value: Any?): RestDocsField {
        descriptor.attributes(field(EXAMPLE, if (value is String) value else value?.toString() ?: EXAMPLE_NULL))
        return this
    }
}

infix fun String.headerDescription(value: String): HeaderDescriptor {
    return HeaderDocumentation.headerWithName(this).description(value)
}

infix fun String.pathDescription(value: String): ParameterDescriptor {
    return RequestDocumentation.parameterWithName(this).description(value)
}

infix fun String.parameterDescription(value: String): ParameterDescriptor {
    return RequestDocumentation.parameterWithName(this).description(value)
}

infix fun ParameterDescriptor.example(value: Any): ParameterDescriptor {
    return this.attributes(field(EXAMPLE, if (value is String) value else value.toString()))
}

infix fun ParameterDescriptor.isOptional(value: Boolean): ParameterDescriptor {
    if (value) this.optional()
    return this
}
