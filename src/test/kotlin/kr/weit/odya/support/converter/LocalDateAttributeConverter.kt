package kr.weit.odya.support.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate

@Converter(autoApply = true)
class LocalDateAttributeConverter : AttributeConverter<LocalDate?, Timestamp?> {
    override fun convertToDatabaseColumn(entityValue: LocalDate?): Timestamp? {
        return entityValue?.let { Timestamp.from(Instant.from(it)) }
    }

    override fun convertToEntityAttribute(databaseValue: Timestamp?): LocalDate? {
        return databaseValue?.let { LocalDate.parse(it.toInstant().toString()) }
    }
}
