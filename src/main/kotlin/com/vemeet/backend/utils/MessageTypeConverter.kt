package com.vemeet.backend.utils

import com.vemeet.backend.model.MessageType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

// spring and enums worst combo ever
@Converter(autoApply = true)
class MessageTypeConverter : AttributeConverter<MessageType, String> {
    override fun convertToDatabaseColumn(attribute: MessageType): String {
        return attribute.name
    }

    override fun convertToEntityAttribute(dbData: String): MessageType {
        return MessageType.valueOf(dbData)
    }
}