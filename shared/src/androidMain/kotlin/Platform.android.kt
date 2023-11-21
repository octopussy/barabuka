import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual typealias PlatformBytes = ByteArray
actual fun PlatformBytes.toByteArray(): ByteArray = this
actual fun ByteArray.toPlatformBytes() : PlatformBytes = this
