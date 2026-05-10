package com.jobmatch.util

/**
 * Sealed result wrapper — the single error-handling contract across all layers.
 *
 * Rules:
 *   - Repository functions ALWAYS return Result<T>; they never throw.
 *   - ViewModels unwrap Results and map them to UiState.
 *   - The UI layer never catches exceptions directly.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T)                           : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
}

/**
 * Executes [block] and wraps the return value; maps known exceptions to [Result.Error].
 *
 * Exception hierarchy handled (Ktor 2.x):
 *   ResponseException          → HTTP 4xx/5xx — extract status code + friendly message
 *   ConnectTimeoutException    → could not establish TCP connection
 *   SocketTimeoutException     → connection established but read timed out
 *   NoTransformationFoundException → response body could not be deserialized
 *     (e.g. server returned HTML error page instead of JSON)
 *   SerializationException     → JSON is valid but shape doesn't match the DTO
 *   CancellationException      → coroutine cancelled — MUST rethrow
 *   Exception (catch-all)      → UnresolvedAddressException (wrong IP/host),
 *                                 ConnectException (server not running), etc.
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
    Result.Success(block())

} catch (e: io.ktor.client.plugins.ResponseException) {
    val code = e.response.status.value
    Result.Error(httpErrorMessage(code), code)

} catch (e: io.ktor.client.network.sockets.ConnectTimeoutException) {
    Result.Error("Connection timed out — check your network and server address.")

} catch (e: io.ktor.client.network.sockets.SocketTimeoutException) {
    Result.Error("Request timed out — the server may be overloaded.")

} catch (e: io.ktor.serialization.JsonConvertException) {
    // Valid JSON but wrong shape — DTO mismatch
    Result.Error("Response format error: ${e.message?.take(120)}")

} catch (e: kotlinx.serialization.SerializationException) {
    Result.Error("Data parsing error: ${e.message?.take(120)}")

} catch (e: kotlinx.coroutines.CancellationException) {
    throw e   // NEVER swallow coroutine cancellation

} catch (e: Exception) {
    // Catch-all — log in debug builds, show generic message in UI
    Result.Error(e.message?.take(200) ?: "Network error — check your connection and retry.")
}

private fun httpErrorMessage(code: Int): String = when (code) {
    400  -> "Invalid request. Please check your input."
    401  -> "Session expired. Please log in again."
    403  -> "You don't have permission to do that."
    404  -> "Not found."
    409  -> "You have already applied to this job."
    422  -> "Validation failed. Check your input."
    429  -> "Too many requests. Please wait and retry."
    500  -> "Server error. Please try again in a moment."
    503  -> "Service unavailable. Please try again later."
    else -> "Something went wrong (HTTP $code)."
}

inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) block(data)
    return this
}

inline fun <T> Result<T>.onError(block: (String, Int?) -> Unit): Result<T> {
    if (this is Result.Error) block(message, code)
    return this
}

val <T> Result<T>.dataOrNull: T?
    get() = (this as? Result.Success)?.data

val <T> Result<T>.isSuccess: Boolean
    get() = this is Result.Success