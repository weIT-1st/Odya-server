package kr.weit.odya.support.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.math.BigDecimal

@Converter(autoApply = true)
class BooleanAttributeConverter : AttributeConverter<Boolean, BigDecimal> {
    override fun convertToDatabaseColumn(entityValue: Boolean?): BigDecimal? {
        return entityValue?.let { if (entityValue) BigDecimal.ONE else BigDecimal.ZERO }
    }

    override fun convertToEntityAttribute(databaseValue: BigDecimal?): Boolean? {
        return databaseValue?.let { databaseValue == BigDecimal.ONE }
    }
}
