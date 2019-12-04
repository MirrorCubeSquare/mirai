@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.message.internal

import kotlinx.io.core.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.io.*

internal fun IoBuffer.parseMessageFace(): Face {
    //00  01  AF  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  F0
    //00  01  0C  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  4D
    discardExact(1)

    // FIXME: 2019/11/20  EMOJI 表情会解析失败
    val id1 = FaceId(readLVNumber().toInt().toUByte())//可能这个是id, 也可能下面那个
    discardExact(readByte().toLong()) // -1
    readLVNumber()//某id?
    return Face(id1)
}

internal fun IoBuffer.parsePlainTextOrAt(): Message {
    // 06 00 0D 00 01 00 00 00 08 00 76 E4 B8 DD 00 00
    discardExact(1)// 0x01 whenever plain or at
    val msg = readUShortLVString()
    return if (this.readRemaining == 0) {
        PlainText(msg)
    } else {
        discardExact(10)
        At(readUInt())
    }
}

internal fun IoBuffer.parseLongText0x19(): PlainText {
    discardExact(1)//0x01
    discard()
    //AA 02 33 50 00 60 00 68 00 9A 01 2A 08 0A 20 C1 50 78 A7 C0 04 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 00 B0 03 00 C0 03 00 D0 03 00 E8 03 00
    //AA 02 33 50 00 60 00 68 00 9A 01 2A 08 09 20 CB 50 78 80 80 04 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 D3 02 A0 03 10 B0 03 00 C0 03 AF 9C 01 D0 03 00 E8 03 00
    //AA 02 30 50 00 60 00 68 00 9A 01 27 08 0A 78 A7 C0 04 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 00 B0 03 00 C0 03 00 D0 03 00 E8 03 00
    // 应该是手机发送时的字体或气泡之类的
    // println("parseLongText0x19.raw=${raw.toUHexString()}")
    return PlainText("")
}

internal fun IoBuffer.parseMessageImage0x06(): Image {
    discardExact(1)
    //MiraiLogger.debug(this.toUHexString())
    val filenameLength = readShort()

    discardExact(filenameLength.toInt())
    discardExact(8)//03 00 04 00 00 02 9C 04
    val length = readShort()//=27
    return Image(ImageId0x06(readString(length)))
    //return Image("{${readString(length)}}.$suffix")
}


//00 1B filenameLength
// 43 37 46 29 5F 34 32 34 4E 33 55 37 7B 4C 47 36 7D 4F 25 5A 51 58 51 2E 6A 70 67 get suffix
// 03 00 04 00 00 02 9C 04
// 00 25 2F 32 65 37 61 65 33 36 66 2D 61 39 31 63 2D 34 31 32 39 2D 62 61 34 32 2D 37 65 30 31 32 39 37 37 35 63 63 38 14
// 00 04 03 00 00 00 18
// 00 25 2F 32 65 37 61 65 33 36 66 2D 61 39 31 63 2D 34 31 32 39 2D 62 61 34 32 2D 37 65 30 31 32 39 37 37 35 63 63 38 19
// 00 04 00 00 00 2E 1A 00 04 00 00 00 2E FF
// 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20 36 36 38 65 35 43 36 38 45 36 42 44 32 46 35 38 34 31 42 30 39 37 39 45 37 46 32 35 34 33 38 38 31 33 43 33 2E 6A 70 67 66 2F 32 65 37 61 65 33 36 66 2D 61 39 31 63 2D 34 31 32 39 2D 62 61 34 32 2D 37 65 30 31 32 39 37 37 35 63 63 38 41

internal fun IoBuffer.parseMessageImage0x03(): Image {
    //discardExact(1)
    val tlv = readTLVMap(expectingEOF = true, tagSize = 1)
    //tlv.printTLVMap("parseMessageImage0x03")
    // 02 [     ] IMAGE ID
    // 04 [00 04] 8B 88 95 C1
    // 05 [00 04] 3B 24 59 FC
    // 06 [00 04] 00 00 01 BB
    // 07 [00 01] 42
    // 08 [00 10] 32 55 6D 52 33 4E 53 64 41 68 44 78 71 56 79 41
    // 09 [00 01] 01
    // 0A [00 10] F5 C3 E6 86 70 EA 03 3E D3 30 1D 2A B5 2E D6 5C
    // 15 [00 04] 00 00 03 76
    // 16 [00 04] 00 00 07 80
    // 17 [00 02] 00 65
    // 18 [00 04] 00 01 A6 F2
    // 19 [00 01] 00
    // 1A [00 04] 00 00 81 5C
    // 1B [00 04] 00 01 3D 27
    // 1C [00 04] 00 00 03 EB
    // FF [00 56] 15 36 20 38 36 65 41 31 42 38 62 38 38 39 35 63 31 33 62 32 34 35 39 66 63 20 20 20 20 20 31 62 62 32 55 6D 52 33 4E 53 64 41 68 44 78 71 56 79 41 46 35 43 33 45 36 38 36 37 30 45 41 30 33 33 45 44 33 33 30 31 44 32 41 42 35 32 45 44 36 35 43 2E 6A 70 67 41
    // return if (tlv.containsKey(0x0Au)) {
    return Image(
        ImageId0x03(
            String(tlv[0x02u]!!).adjustImageId(),
            //  tlv[0x0Au],
            uniqueId = tlv[0x04u]!!.read { readUInt() },
            height = tlv[0x16u]!!.toUInt().toInt(),
            width = tlv[0x15u]!!.toUInt().toInt()
        )//.also { debugPrintln("ImageId: $it") }
    )
    //} else {
    //    Image(
    //        ImageId0x06(
    //            String(tlv[0x02u]!!).adjustImageId()
    //        )
    //    )
    //}

}

private operator fun String.get(range: IntRange) = this.substring(range)

// 有些时候会得到 724D95122B54EEAC1E214AAAC37259DF.gif
// 需要调整      {724D9512-2B54-EEAC-1E21-4AAAC37259DF}.gif

private fun String.adjustImageId() =
    if (this.first() == '{') this
    else "{${this[0..7]}-${this[8..11]}-${this[12..15]}-${this[16..19]}-${this[20..31]}}.${this.substringAfterLast(".")}"

internal fun ByteReadPacket.readMessage(): Message? {
    val messageType = this.readByte().toInt()
    val sectionData = this.readIoBuffer(this.readShort().toInt())

    return try {
        when (messageType) {
            0x01 -> sectionData.parsePlainTextOrAt()
            0x02 -> sectionData.parseMessageFace()
            0x03 -> sectionData.parseMessageImage0x03()
            0x06 -> sectionData.parseMessageImage0x06()


            0x19 -> {
                //19 00  5C / 01  00  59  AA  02  56  30  01  3A  40  6E  35  46  4F  62  68  75  4B  6F  65  31  4E  63  45  41  6B  77  4B  51  5A  5A  4C  47  54  57  43  68  30  4B  56  7A  57  44  38  67  58  70  37  62  77  6A  67  51  69  66  66  53  4A  63  4F  69  78  4F  75  37  36  49  49  4F  37  48  32  55  63  9A  01  0F  80  01  01  C8  01  00  F0  01  00  F8  01  00  90  02  00  14  01  75  01  01  6B  01  78  9C  CD  92  BB  4E  C3  30  14  86  77  9E  C2  32  73  DA  A4  21  24  48  4E  AA  F4  06  A5  B4  51  55  A0  A8  0B  4A  5D  27  35  E4  82  72  69  4B  B7  6E  08  06  C4  C0  06  42  48  30  20  21  60  62  EB  E3  34  F4  31  70  4A  11  23  23  FC  96  2C  F9  D8  BF  CF  F1  77  8C  F2  23  D7  01  03  12  84  D4  F7  54  28  64  78  08  88  87  FD  1E  F5  6C  15  C6  91  C5  29  30  AF  AD  00  26  E4  86  36  E8  06  94  58  2A  CC  FC  73  41  E0  1E  5A  D4  21  0D  D3  25  2A  2C  55  0A  1B  D2  BA  5E  E0  24  91  D7  B9  B5  72  41  E1  74  B9  5C  E0  78  25  27  8B  92  28  14  45  45  FF  76  B4  E8  98  39  18  05  13  47  0B  24  03  4A  86  F5  D8  89  68  3D  B4  21  B0  1C  93  71  11  21  08  49  30  A0  98  54  4B  6C  25  A5  E6  80  84  B4  A7  42  4F  AA  18  DD  7E  5C  F3  89  D0  C0  65  FD  78  58  6B  76  3A  3B  9B  BB  ED  62  9F  AF  ED  8F  DB  25  C5  3E  38  91  BB  C3  23  BB  49  2D  AB  B5  8D  0D  3A  32  62  79  BD  5A  35  E4  AD  DC  1E  86  40  03  88  46  C4  05  8E  79  EA  C7  11  EB  09  64  91  88  46  0E  D1  C0  5F  73  FD  4D  00  65  97  95  02  D4  0F  34  94  65  D3  B2  78  80  7D  C7  0F  54  B8  AA  F0  E9  60  8F  4A  EE  1E  3F  6E  2E  84  E4  F6  7E  3E  7D  9E  5D  5E  25  EF  67  C9  E4  15  FC  DC  81  B2  29  08  0D  85  7E  1C  60  02  BC  45  33  E7  93  F3  D9  C3  D3  FC  E5  6D  36  BD  86  2C  C3  D7  66  7A  98  FD  4F  ED  13  9B  C7  C1  78  02  00  04  00  00  00  23  0E  00  07  01  00  04  00  00  00  09
                //1000个 "." 被压缩为上面这条消息?

                //bot手机自己跟自己发消息会出这个
                //似乎手机发消息就会有这个?
                //sectionData: 01 00 1C AA 02 19 08 00 88 01 00 9A 01 11 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00
                //             01 00 1C AA 02 19 08 00 88 01 00 9A 01 11 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00
                //return null

                return null
                // sectionData.parseLongText0x19()
            }

            // XML
            /*
             * 19 [01 AD] 01 01 AA 9A 03 A6 03 0A A3 03 01 78 9C A5 91 CB 4E E3 30 14 86 5F A5 F2 BA 4A 93 A6 49 DA EE 59 B1 06 69 34 42 95 9B B8 25 D0 D8 C6 71 5A 31 A8 52 C5 02 CA C0 86 E1 BA E0 22 16 08 21 2A 71 D9 50 31 C3 D3 10 37 E5 2D 38 4E 87 D9 CC 12 59 B2 74 FE F3 FB 3B FF 91 37 10 E6 1C D5 91 CF 22 43 12 EA 13 2A 8D 58 8A C4 97 51 DC 46 45 D0 69 2B 6C A3 FA 06 C2 89 64 71 F8 83 A0 3A B4 09 74 64 18 41 61 39 9E 63 7B AE 57 AE 14 51 8B 89 1E 16 C1 A7 43 B2 55 42 81 6D D7 1C DF B3 3C DB 2A BB 2D AB EA 60 D7 0D AA 15 B3 59 29 37 2B 6E 15 9B 4D 18 23 D7 39 B0 10 65 22 C2 1D D4 2F A2 80 C4 3E 08 93 E3 87 F7 93 DF 60 88 88 C4 3A 05 25 BD 38 4F 03 E3 99 86 43 0F D3 40 B0 30 68 F0 D5 76 83 62 1D 2A 57 39 6F CC B0 56 5E 84 90 CB 32 CD 9A 63 79 9E FB 6F 80 7A 7E 9A DE 6C 66 67 BB B6 63 9A 46 2D 1D 6F 4F 46 57 F0 7A 25 89 F8 82 E8 80 63 59 4A 5E 2F 95 12 D1 31 7C 5A 72 F8 5C 3C 9F AC 80 83 0B D2 0D 49 EF 7F 87 DB B5 BF 2D AE 81 23 66 89 F0 49 23 F4 3F 83 FE 15 92 9C AB B7 C6 6D 1D E1 E0 50 1D DC CE 6E 2D 86 B2 A3 57 78 1B FC 54 C3 81 DA D9 CF AE 2F D4 E5 65 21 1B 3E A6 E3 D7 74 7C 9E DD 6F 17 D4 9F CD EC 68 6F 32 3C 4D 5F 76 DE 06 BB D9 68 54 9D DE 9F 17 A6 57 03 F5 6B 4F ED BF A2 7E 5F 27 64 11 97 80 FA AE 86 5B E9 CB DD D2 D7 90 45 D4 25 02 68 A6 A1 8F A5 EB D9 FE F9 9F F4 3F 00 FB FE EF 17
             *
             * 01 00 89 01 00 86 5B E5 88 86 E4 BA AB 5D E3 80 8A E5 88 80 E5 89 91 E7 A5 9E E5 9F 9F 20 E7 88 B1 E4 B8 BD E4 B8 9D E7 AF 87 20 E5 BC 82 E7 95 8C E6 88 98 E4 BA 89 E3 80 8B E7 AC AC 38 E8 AF 9D 20 E8 A1 80 E5 92 8C E5 91 BD 0A E5 B7 B2 E8 A7 82 E7 9C 8B 33 35 30 30 2E 39 E4 B8 87 E6 AC A1 0A 68 74 74 70 3A 2F 2F 75 72 6C 2E 63 6E 2F 35 70 45 73 4B 75 6A 0A E6 9D A5 E8 87 AA 3A 20 E5 93 94 E5 93 A9 E5 93 94 E5 93 A9
             *
             * 19 00 42 01 00 3F AA 02 3C 08 00 50 03 60 00 68 00 88 01 00 9A 01 2E 08 09 20 BF 50 78 00 A0 01 81 DC 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 90 04 80 0B 0E 00 0E 01 00 04 00 00 00 09 0A 00 04 00 00 00 00 12 00 25 05 00 04 00 00 00 01 08 00 04 00 00 00 01 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 10
             */


            0x14 -> {//长文本的后一部分? 总长度 0x0175=373, body长度=0x016B=363
//14  01  75  01  01  6B  01  78  9C  CD  92  4D  4F  C2  30  18  C7  EF  7E  8A  A6  1E  C9  64  83  B1  CD  A4  1B  E1  4D  19  8A  C6  20  11  BC  98  39  3A  A8  EE  C5  74  1D  20  37  6E  46  0F  C6  83  37  8D  31  D1  83  89  51  4F  DE  F8  38  4C  3E  86  1D  62  3C  7A  D4  7F  93  26  7D  DA  7F  9F  A7  BF  A7  28  3F  F4  5C  D0  C7  34  24  81  AF  43  69  45  84  00  FB  76  D0  21  7E  57  87  11  73  04  0D  E6  8D  25  C0  85  BC  B0  0B  0E  29  C1  8E  0E  57  FE  B9  20  F0  0E  1C  E2  E2  2D  CB  C3  3A  D4  A4  D5  72  A9  58  58  13  24  AD  A4  0A  B2  22  E5  84  42  59  91  85  9C  52  C8  2A  62  46  14  65  59  FD  76  34  C8  88  3B  38  05  CB  66  73  24  7D  82  07  F5  C8  65  A4  1E  76  21  70  5C  8B  73  C9  42  10  62  DA  27  36  36  CB  7C  95  4B  CC  14  87  A4  A3  C3  AA  B4  CE  82  63  B5  46  54  8D  A4  EC  F6  20  F4  68  D8  21  C5  ED  7D  B5  B9  A1  0D  46  4D  33  E5  8F  86  47  AD  DA  6E  73  2F  D3  3B  91  2B  55  53  A9  6C  B6  76  AC  96  DD  20  38  DB  36  21  30  00  22  0C  7B  C0  B5  4E  83  88  F1  9E  40  1E  61  84  B9  D8  00  7F  CD  F5  37  01  94  5E  54  0A  50  8F  1A  28  CD  A7  45  F1  C0  0E  DC  80  EA  70  59  13  93  C1  1F  15  DF  3D  7E  DC  5C  48  F1  ED  FD  6C  F2  3C  BD  BC  8A  DF  CF  E2  F1  2B  F8  B9  03  A5  13  10  06  0A  83  88  DA  18  F8  F3  66  CE  C6  E7  D3  87  A7  D9  CB  DB  74  72  0D  79  86  AF  CD  E4  30  FF  9F  C6  27  23  A2  C1  36  02  00  04  00  00  00  23  0E  00  07  01  00  04  00  00  00  09

                discardExact(1)
                val value = readUShortLVByteArray()
                println(value.size)
                println("0x14的未知压缩的data=" + value.toUHexString())
                //todo 未知压缩算法

                return PlainText("")

                //后面似乎还有一节?
                //discardExact(7)//02  00  04  00  00  00  23
                //return PlainText(value.toUHexString())
            }

            0x0E -> {
                null
            }

            else -> {
                println("未知的messageType=0x${messageType.toByte().toUHexString()}")
                println("后文=${this.readBytes().toUHexString()}")
                null
            }
        }
    } finally {
        sectionData.release(IoBuffer.Pool)
    }
}

fun ByteReadPacket.readMessageChain(): MessageChain {
    val chain = MessageChain()
    do {
        if (this.remaining == 0L) {
            return chain
        }
    } while (this.readMessage().takeIf { it != null }?.let { chain.followedBy(it) } != null)
    return chain
}

fun MessageChain.toPacket(): ByteReadPacket = buildPacket {
    this@toPacket.forEach { message ->
        writePacket(with(message) {
            when (this) {
                is Face -> buildPacket {
                    writeUByte(MessageType.FACE.value)

                    writeShortLVPacket {
                        writeShort(1)
                        writeUByte(id.value)

                        writeHex("0B 00 08 00 01 00 04 52 CC F5 D0 FF")

                        writeShort(2)
                        writeByte(0x14)//??
                        writeUByte((id.value + 65u).toUByte())
                    }
                }

                is At -> buildPacket {
                    writeUByte(MessageType.AT.value)

                    writeShortLVPacket {
                        writeByte(0x01)
                        writeShortLVString(stringValue) // 这个应该是 "@群名", 手机上面会显示这个消息, 电脑会显示下面那个
                        // 06 00 0D 00 01 00 00 00 08 00 76 E4 B8 DD 00 00
                        writeHex("06 00 0D 00 01 00 00 00 08 00")
                        writeUInt(target)
                        writeZero(2)
                    }
                }

                is Image -> buildPacket {
                    when (id.value.length) {
                        //   "{F61593B5-5B98-1798-3F47-2A91D32ED2FC}.jpg"
                        // 41 ???
                        41, 42 -> {
                            writeUByte(MessageType.IMAGE_42.value)

                            //00 00 03 00 CB 02 00 2A 7B 46 36 31 35 39 33 42 35 2D 35 42 39 38 2D 31 37 39 38 2D 33 46 34 37 2D 32 41 39 31 44 33 32 45 44 32 46 43 7D 2E 6A 70 67
                            // 04 00 04 87 E5 09 3B 05 00 04 D2 C4 C0 B7 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 01 ED 16 00 04 00 00 02 17 18 00 04 00 00 EB 34 FF 00 5C 15 36 20 39 32 6B 41 31 43 38 37 65 35 30 39 33 62 64 32 63 34 63 30 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20
                            // 7B 46 36 31 35 39 33 42 35 2D 35 42 39 38 2D 31 37 39 38 2D 33 46 34 37 2D 32 41 39 31 44 33 32 45 44 32 46 43 7D 2E 6A 70 67 41
                            /*
                             * 00 00 06 00 F3 02 00 1B 28 52 49 5F 36 31 28 32 52 59 4B 59 43 40 37 59 29 58 29 39 29 42 49 2E 67 69 66 03 00 04 00 00 11 90 04 00 25 2F 35 37 34 63 34 32 34 30 2D 30 31 33 66 2D 34 35 39 38 2D 61 37 32 34 2D 30 36 65 66 35 36 39 39 30 64 30 62 14 00 04 03 00 00 00 0B 00 00 18 00 25 2F 35 37 34 63 34 32 34 30 2D 30 31 33 66 2D 34 35 39 38 2D 61 37 32 34 2D 30 36 65 66 35 36 39 39 30 64 30 62 19 00 04 00 00 00 2D 1A 00 04 00 00 00 2D FF 00 63 16 20 20 39 39 31 30 20 38 38 31 44 42 20 20 20 20 20 20 34 34 39 36 65 33 39 46 37 36 35 33 32 45 31 41 42 35 43 41 37 38 36 44 37 41 35 31 33 38 39 32 32 35 33 38 35 2E 67 69 66 66 2F 35 37 34 63 34 32 34 30 2D 30 31 33 66 2D 34 35 39 38 2D 61 37 32 34 2D 30 36 65 66 35 36 39 39 30 64 30 62 41
                             * 00 00 06 00 F3 02 00 1B 46 52 25 46 60 30 59 4F 4A 5A 51 48 31 46 4A 53 4C 51 4C 4A 33 46 31 2E 6A 70 67 03 00 04 00 00 02 A2 04 00 25 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66 14 00 04 03 00 00 00 0B 00 00 18 00 25 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66 19 00 04 00 00 00 4E 1A 00 04 00 00 00 23 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20 36 37 34 65 31 46 42 34 43 32 35 45 42 34 46 45 31 32 45 34 46 33 42 42 38 31 39 31 33 37 42 44 39 39 30 39 2E 6A 70 67 66 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66 41
                             */

                            writeShortLVPacket {
                                writeUByte(0x02u)
                                writeShortLVString(id.value)
                                writeHex("04 00 04 87 E5 09 3B 05 00 04 D2 C4 C0 B7 06 00 04 00 00 00 50 07 00 01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 00 00 00 00 15 00 04 00 00 01 ED 16 00 04 00 00 02 17 18 00 04 00 00 EB 34 FF 00 5C 15 36 20 39 32 6B 41 31 43 38 37 65 35 30 39 33 62 64 32 63 34 63 30 62 37 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20")
                                writeStringUtf8(id.value)
                                writeUByte(0x41u)
                            }
                        }

                        //   "/01ee6426-5ff1-4cf0-8278-e8634d2909ef"
                        37 -> {
                            writeUByte(MessageType.IMAGE_37.value)

                            // 00 00 06 00 F3 02
                            // 00 1B 24 5B 56 54 4A 38 60 4C 5A 4E 46 7D 53 39 4F 52 36 25 45 60 42 55 53 2E 6A 70 67
                            // 03 00 04 00 01 41 1B 04
                            // 00 25 2F 65 61 37 30 30 61 38 33 2D 38 38 38 62 2D 34 66 37 31 2D 61 62 64 31 2D 63 33 38 64 63 62 64 31 61 65 36 31
                            // 14 00 04 00 00 00 00 0B 00 00 18
                            // 00 25 2F 65 61 37 30 30 61 38 33 2D 38 38 38 62 2D 34 66 37 31 2D 61 62 64 31 2D 63 33 38 64 63 62 64 31 61 65 36 31
                            // 19 00 04 00 00 03 74 1A 00 04 00 00 02 BE FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20
                            ///38 32 32 30 33 65 39 36 30 46 42 35 44 37 46 42 33 39 46 34 39 39 31 37 46 34 37 33 44 41 45 31 37 30 32 46 44 31 2E 6A 70 67 66
                            // 2F 65 61 37 30 30 61 38 33 2D 38 38 38 62 2D 34 66 37 31 2D 61 62 64 31 2D 63 33 38 64 63 62 64 31 61 65 36 31 41

                            // 00 00 06 00 F3 02
                            // 00 1B 7B 48 29 47 52 53 31 29 50 24 5A 42 28 4F 35 43 44 4B 45 31 35 7B 37 2E 70 6E 67
                            // 03 00 04 00 00 6F 36 04
                            // 00 25 2F 35 65 63 32 34 63 37 62 2D 34 30 32 39 2D 34 61 39 33 2D 62 63 66 35 2D 34 31 38 34 35 32 65 39 32 33 31 64
                            // 14 00 04 00 00 00 00 0B 00 00 18
                            // 00 25 2F 35 65 63 32 34 63 37 62 2D 34 30 32 39 2D 34 61 39 33 2D 62 63 66 35 2D 34 31 38 34 35 32 65 39 32 33 31 64
                            // 19 00 04 00 00 04 14 1A 00 04 00 00 02 77 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 41 42 20 20 20 20 20
                            ///32 38 34 37 30 65 35 42 37 44 45 37 36 41 34 41 44 44 31 37 43 46 39 32 39 38 33 43 46 30 41 43 45 35 42 34 33 39 2E 70 6E 67 66
                            // 2F 35 65 63 32 34 63 37 62 2D 34 30 32 39 2D 34 61 39 33 2D 62 63 66 35 2D 34 31 38 34 35 32 65 39 32 33 31 64 41
                            /*
                             * 00 00 06 00 F3 02
                             * 00 1B 46 52 25 46 60 30 59 4F 4A 5A 51 48 31 46 4A 53 4C 51 4C 4A 33 46 31 2E 6A 70 67
                             * 03 00 04 00 00 02 A2 04
                             * 00 25 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66
                             * 14 00 04 03 00 00 00 0B 00 00 18
                             * 00 25 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66
                             * 19 00 04 00 00 00 4E 1A 00 04 00 00 00 23 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20
                             **36 37 34 65 31 46 42 34 43 32 35 45 42 34 46 45 31 32 45 34 46 33 42 42 38 31 39 31 33 37 42 44 39 39 30 39 2E 6A 70 67 66
                             * 2F 37 33 38 33 35 38 36 37 2D 38 64 65 31 2D 34 65 30 66 2D 61 33 36 35 2D 34 39 62 30 33 39 63 34 61 39 31 66 41
                             */
                            writeShortLVPacket {
                                writeByte(0x02)
                                //"46 52 25 46 60 30 59 4F 4A 5A 51 48 31 46 4A 53 4C 51 4C 4A 33 46 31 2E 6A 70 67".hexToBytes().encodeToString()
                                //   writeShortLVString(filename)//图片文件名 FR%F`0YOJZQH1FJSLQLJ3F1.jpg
                                writeShortLVString(id.value.substring(1..24) + ".gif")// 图片文件名. 后缀不影响. 但无后缀会导致 PC QQ 无法显示这个图片
                                writeHex("03 00 04 00 00 02 A2 04")
                                writeShortLVString(id.value)
                                writeHex("14 00 04 03 00 00 00 0B 00 00 18")
                                writeShortLVString(id.value)

                                writeHex("19 00 04 00 00 00 4E 1A 00 04 00 00 00 23 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20 ")

                                writeStringUtf8("674e")// 没有 "e" 服务器就不回复

                                writeStringUtf8(id.value.substring(1..id.value.lastIndex - 4))//这一串文件名决定手机 QQ 保存的图片. 可以随意
                                writeStringUtf8(".gif")// 后缀似乎必须要有
                                writeUByte(0x66u)

                                //有时候 PC QQ 看不到发这些消息, 但手机可以. 可能是 ID 过期了, 手机有缓存而电脑没有

                                /*
                                * writeHex("19 00 04 00 00 00 4E 1A 00 04 00 00 00 23 FF 00 63 16 20 20 39 39 31 30 20 38 38 31 43 42 20 20 20 20 20 20 20 ")

                                writeStringUtf8(id.value.subSequence(1..id.value.lastIndex))//674e1FB4C25EB4FE12E4F3BB819137BD9909.jpg
                                writeStringUtf8(".jpg")// "36 37 34 65 31 46 42 34 43 32 35 45 42 34 46 45 31 32 45 34 46 33 42 42 38 31 39 31 33 37 42 44 39 39 30 39 2E 6A 70 67

                                writeUByte(0x66u)
                                * */

                                //36 37 34 65 31 46 42 34 43 32 35 45 42 34 46 45 31 32 45 34 46 33 42 42 38 31 39 31 33 37 42 44 39 39 30 39 2E 6A 70 67 66
                                writeStringUtf8(id.value)
                                writeUByte(0x41u)
                            }
                        }
                        else -> error("Illegal ImageId: ${id.value}")
                    }
                }

                is PlainText -> buildPacket {
                    writeUByte(MessageType.PLAIN_TEXT.value)

                    writeShortLVPacket {
                        writeByte(0x01)
                        writeShortLVString(stringValue)
                    }
                }

                else -> throw UnsupportedOperationException("${this::class.simpleName} is not supported. Do NOT implement Message manually. Full MessageChain=${this@toPacket}")
            }
        })
    }
}