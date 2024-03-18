package com.dhl.demp.dmac.data.network.adapter

import com.dhl.demp.dmac.model.InstallationRoutineType
import com.google.gson.*
import java.lang.reflect.Type

class InstallationRoutineTypeAdapter : JsonSerializer<InstallationRoutineType>,
    JsonDeserializer<InstallationRoutineType> {
    override fun serialize(
        src: InstallationRoutineType,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.value)
    }

    override fun deserialize(
        element: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): InstallationRoutineType {
        return InstallationRoutineType.from(element.asJsonPrimitive.asString)
    }
}