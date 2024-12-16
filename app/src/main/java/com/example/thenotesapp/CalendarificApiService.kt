import com.example.thenotesapp.HolidayResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarificApiService {
    @GET("holidays")
    fun getHolidays(
        @Query("api_key") apiKey: String,
        @Query("country") country: String,
        @Query("year") year: Int,
        @Query("language") language: String
    ): Call<HolidayResponse>
}