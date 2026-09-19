package com.ban.formtree.form.data.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
fun createFormJson(): Json = Json {
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        polymorphicDefaultDeserializer(FormNodeDto::class) { FormNodeDto.UnknownDto.serializer() }
    }
}
