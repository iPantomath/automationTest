package com.dhl.demp.dmac.data.network.adapter

import com.dhl.demp.dmac.model.BBfiedApproveState
import com.google.gson.*
import java.lang.reflect.Type

class BBfiedApproveStateAdapter : JsonSerializer<BBfiedApproveState>,
    JsonDeserializer<BBfiedApproveState> {
    override fun serialize(
        src: BBfiedApproveState,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.value)
    }

    override fun deserialize(
        element: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): BBfiedApproveState {
        return BBfiedApproveState.from(element.asJsonPrimitive.asString)
    }
}