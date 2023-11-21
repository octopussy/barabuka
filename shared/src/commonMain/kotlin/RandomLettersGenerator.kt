import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RandomLettersGenerator {
    // Somewhere in your Kotlin code you define a suspend function
    // and annotate it with @NativeCoroutines
    @NativeCoroutines
    suspend fun getRandomLettersSuspend(): String {
        // Code to generate some random letters
        return "1234"
    }

    @NativeCoroutines
    fun getRandomLettersFlow(): Flow<String> {
        return flow {
            emit("123")
        }
    }
    fun getRandomLetters(): String {
        // Code to generate some random letters
        return "1234"
    }
}
