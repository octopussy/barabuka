class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual typealias PlatformBytes = ByteArray
actual fun PlatformBytes.toByteArray(): ByteArray = this
actual fun ByteArray.toPlatformBytes() : PlatformBytes = this
