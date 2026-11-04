package com.abhik.paisatrack.data.network

import android.content.Context
import com.abhik.paisatrack.data.AuthManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ─── Network Models ────────────────────────────────────────────────

data class SignupRequest(
    val googleId: String,
    val email: String,
    val name: String,
    val image: String,
    val idToken: String? = null
)

data class SignupResponse(
    val userId: String,
    val isFirstLogin: Boolean,
    val token: String? = null
)

data class NetworkCollection(
    val id: String,
    val userId: String,
    val title: String,
    val amount: Double,
    val color: String?,
    val icon: String?,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val isPrebuilt: Boolean? = false
)

data class NetworkTransaction(
    val id: String,
    val user_id: String?,
    val collectionId: String?,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String?,
    val time: String?,
    val createdAt: Long?,
    val updatedAt: Long? = null,
    val collectionName: String?
)

data class CreateCollectionRequest(
    val id: String,
    val userId: String,
    val title: String,
    val color: String,
    val icon: String
)

data class UpdateCollectionRequest(
    val title: String,
    val color: String,
    val icon: String
)

data class CreateTransactionRequest(
    val id: String,
    val userId: String,
    val collectionId: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String?,
    val time: String?
)

data class UpdateTransactionRequest(
    val title: String,
    val amount: Double,
    val category: String,
    val collectionId: String
)

data class UpdatePushTokenRequest(
    val userId: String,
    val pushToken: String,
    val unregister: Boolean = false
)

// ─── API Endpoints Service ──────────────────────────────────────────

interface PaisaTrackApi {
    @GET("users/current")
    suspend fun getCurrentUser(
        @Query("userId") userId: String,
        @Query("noCache") noCache: String = "false"
    ): Response<Any?>

    @POST("users/signup")
    suspend fun signupUser(@Body body: SignupRequest): Response<SignupResponse>

    @POST("users/push-token")
    suspend fun updatePushToken(@Body body: UpdatePushTokenRequest): Response<Map<String, Any>>

    @GET("collections/{userId}")
    suspend fun getCollections(
        @Path("userId") userId: String,
        @Query("noCache") noCache: String = "false",
        @Query("since") since: Long? = null
    ): Response<List<NetworkCollection>>

    @POST("collections/create")
    suspend fun createCollection(@Body body: CreateCollectionRequest): Response<NetworkCollection>

    @PUT("collections/{collectionId}")
    suspend fun updateCollection(
        @Path("collectionId") collectionId: String,
        @Body body: UpdateCollectionRequest
    ): Response<NetworkCollection>

    @DELETE("collections/{collectionId}")
    suspend fun deleteCollection(
        @Path("collectionId") collectionId: String,
        @Query("userId") userId: String
    ): Response<Map<String, String>>

    @POST("collection-transactions/create")
    suspend fun createTransaction(@Body body: CreateTransactionRequest): Response<NetworkTransaction>

    @PUT("collection-transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body body: UpdateTransactionRequest
    ): Response<NetworkTransaction>

    @DELETE("collection-transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String,
        @Query("userId") userId: String
    ): Response<Map<String, String>>

    @GET("transactions/{userId}")
    suspend fun getTransactions(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0,
        @Query("noCache") noCache: String = "false",
        @Query("since") since: Long? = null
    ): Response<List<NetworkTransaction>>

    @DELETE("users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): Response<Map<String, String>>
}

// ─── API Client Singleton ───────────────────────────────────────────

object ApiClient {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Dynamic URL selector based on build variant (DEBUG vs RELEASE)
    private val BASE_URL = if (com.abhik.paisatrack.BuildConfig.DEBUG) {
        // Use "127.0.0.1" for emulator, or physical device via wireless/USB debugging (requires: adb reverse tcp:8080 tcp:8080)
        // Alternatively, use your host PC's local network IP (e.g., "http://192.168.1.XX:8080/api/")
        "http://192.168.31.219:8080/api/"
    } else {
        "https://paisa-track.redsider.com/api/"
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = appContext?.let { AuthManager.getAuthToken(it) }
        val authenticatedRequest = if (!token.isNullOrEmpty() && originalRequest.header("Authorization") == null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        chain.proceed(authenticatedRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (com.abhik.paisatrack.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: PaisaTrackApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PaisaTrackApi::class.java)
    }
}
