interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect class PlatformBytes
expect fun PlatformBytes.toByteArray(): ByteArray
expect fun ByteArray.toPlatformBytes() : PlatformBytes
