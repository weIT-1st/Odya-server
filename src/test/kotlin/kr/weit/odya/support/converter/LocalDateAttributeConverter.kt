package kr.weit.odya.support.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId

@Converter(autoApply = true)
class LocalDateAttributeConverter : AttributeConverter<LocalDate?, Timestamp?> {
    override fun convertToDatabaseColumn(entityValue: LocalDate?): Timestamp? {
        return entityValue?.let { Timestamp.from(entityValue.atStartOfDay(ZoneId.systemDefault()).toInstant()) }
    }

    override fun convertToEntityAttribute(databaseValue: Timestamp?): LocalDate? {
        return databaseValue?.let { databaseValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() }
    }
}
