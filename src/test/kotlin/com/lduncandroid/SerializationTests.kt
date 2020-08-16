package com.lduncandroid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.nullable
import kotlinx.serialization.stringify
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("java.util.Date", PrimitiveKind.STRING)

    // Consider wrapping in ThreadLocal if serialization may happen in multiple threads
    private val df: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").apply {
        timeZone = TimeZone.getTimeZone("GMT+2")
    }

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(df.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        return df.parse(decoder.decodeString())
    }
}

class NullableIntSerializer : KSerializer<Int?> {

    override val descriptor: SerialDescriptor = PrimitiveDescriptor("NullableIntSerializer", PrimitiveKind.INT).nullable
    private val delegate = Int.serializer().nullable

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) {
            delegate.serialize(encoder, 0)
        } else {
            delegate.serialize(encoder, value)
        }
    }

    override fun deserialize(decoder: Decoder): Int? {
        return delegate.deserialize(decoder) ?: 0
    }
}

@Serializable
data class ClassWithDate(@Serializable(with = DateSerializer::class) val date: Date)

@Serializable
data class ClassWithNullableInt(@Serializable(with = NullableIntSerializer::class) val value: Int?)

@UnstableDefault
@ImplicitReflectionSerializer
class SerializeJavaClassTest : FunSpec({
    test("Parsed Date should equal Date initialized from instant") {
        val date = ClassWithDate(Date(1538636400000L))
        val jsonStringDate = Json.stringify(date)

        jsonStringDate shouldBe """{"date":"04/10/2018 09:00:00.000"}"""

        val date2 = Json.parse(ClassWithDate.serializer(), jsonStringDate)

        date2 shouldBe date
    }
})

@UnstableDefault
@ImplicitReflectionSerializer
class NullableIntSerializerTests : FunSpec({
    val json = Json(JsonConfiguration.Default)

    test("0 value should deserialize as equal to 0") {
        val zeroInt = ClassWithNullableInt(0)
        val jsonStringZeroInt = json.stringify(ClassWithNullableInt.serializer(), zeroInt)

        jsonStringZeroInt shouldBe """{"value":0}"""

        val zeroInt2 = json.parse(ClassWithNullableInt.serializer(), jsonStringZeroInt)

        zeroInt2 shouldBe zeroInt
    }

    test("null value should deserialize as equal to 0") {
        val nullInt = ClassWithNullableInt(null)
        val zeroInt = ClassWithNullableInt(0)

        val jsonStringNullInt = json.stringify(ClassWithNullableInt.serializer(), nullInt)

        jsonStringNullInt shouldBe """{"value":0}"""

        val zeroInt2 = json.parse(ClassWithNullableInt.serializer(), jsonStringNullInt)

        zeroInt2 shouldBe zeroInt
    }
})
