package xl.iot.aces.flexua

import com.google.gson.GsonBuilder
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.security.cert.CertificateException
import javax.net.ssl.*

val BASE_URL      = "https://flexiot.xl.co.id/"
val SERVER_URL    = "http://52.221.141.22:8080"
val EVENT_URL     = "http://52.221.141.22:8080/api/pcs/Generic_brand_745GENERIC_DEVICEv3"
val X_SECRET      = "YUhGREQwZjNBbEdOUmZNSmNraFZ0dzJLVUlVYTpOakFyYzFBOEFNSzBoQzZTUnpxRDFBM1k1cVVh"
val USERNAME      = "ellianto@student.umn.ac.id"
val PASSWORD      = "MaRooN55"
val CONTENT_TYPE  = "application/json"
val DEVICE_ID     = 12627
val SERIAL        = "4407062520211518"


class UnsafeOkHttpClient {
    companion object {
        fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
            try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { _, _ -> true }

                return builder
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}

data class APIAuthorizationResponse(
        val access_token: String,
        val scope: String,
        val token_type: String,
        val expires_in: Int
)
interface APIAuthorizationService {
    @GET("/api/applicationmgt/authenticate")
    fun request(
            @Header("X-Secret") xSecret: String
    ) : Observable<APIAuthorizationResponse>

    companion object Factory {
        fun create(): APIAuthorizationService {
            val retrofit = Retrofit.Builder()
                    .client(UnsafeOkHttpClient.getUnsafeOkHttpClient().build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()!!
            return retrofit.create(APIAuthorizationService::class.java)
        }
    }
}

data class IOTUserRequestBody(
        val username: String,
        val password: String
)

data class IOTUserResponseData(
        val email: String,
        val phone: String,
        val userId: Int
)

data class IOTUserResponse(
        val auth: Boolean,
        val `X-IoT-JWT`: String,
        val data: IOTUserResponseData
)

interface IOTUserService {
    @POST("/api/usermgt/v1/authenticate")
    @Headers("Content-Type: application/json;charset=utf-8")
    fun authenticate(
            @Header("Authorization") authorization: String,
            @Body body: IOTUserRequestBody
    ) : Observable<IOTUserResponse>

    companion object Factory {
        fun create(): IOTUserService {
            val retrofit = Retrofit.Builder()
                    .client(UnsafeOkHttpClient.getUnsafeOkHttpClient().build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()!!
            return retrofit.create(IOTUserService::class.java)
        }
    }
}

data class DeviceResponse(
        val parameter: String,
        val state: String,
        val value: String
)

data class ExecuteActionResponse(
        val deviceResponse: List<DeviceResponse>
)

data class ExecuteActionBody(
        val operation: String,
        val deviceId: Int,
        val actionName: String,
        val userId: Int,
        val actionParameters: Any
)

data class ActionParameter(
        val mac: String
)

interface ExecuteActionService {
    @POST("/api/userdevicecontrol/v1/devices/executeaction")
    @Headers("Content-Type: application/json;charset=utf-8")
    fun execute(
            @Header("Authorization") authorization: String,
            @Header("X-IoT-JWT") xIotJwt: String,
            @Body body: ExecuteActionBody
    ) : Observable<ExecuteActionResponse>

    companion object Factory {
        fun create() : ExecuteActionService {
            val retrofit = Retrofit.Builder()
                    .client(UnsafeOkHttpClient.getUnsafeOkHttpClient().build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()!!
            return retrofit.create(ExecuteActionService::class.java)
        }
    }
}

data class IOTDataTestResponseArray(
        val stateName: String,
        val time_s: String,
        val timestamp_s: String,
        val benedict: Double,
        val biuret: Double,
        val dehydration: Double
)

data class IOTDataTestResponse(
        val device: List<IOTDataTestResponseArray>
)

interface IOTDataService {
    @GET("/api/datamgt/v1/user/devicehistory")
    @Headers("Content-Type: application/json;charset=utf-8")
    fun data(
            @Header("Authorization") authorization: String,
            @Header("X-IoT-JWT") xIotJwt: String,
            @Query("eventName") eventName: String,
            @Query("deviceIds") deviceIds: String,
            @Query("startDate") startDate: String,
            @Query("noOfEvents") noOfEvents: String,
            @Query("zoneId") zoneId: String,
            @Query("eventParams") eventParams: String
    ) : Observable<IOTDataTestResponse>

    companion object Factory{
        fun create() : IOTDataService {
            val retrofit = Retrofit.Builder()
                    .client(UnsafeOkHttpClient.getUnsafeOkHttpClient().build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()!!
            return retrofit.create(IOTDataService::class.java)
        }
    }
}