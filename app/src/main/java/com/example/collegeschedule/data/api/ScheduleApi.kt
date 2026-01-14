package com.example.collegeschedule.data.api

import com.example.collegeschedule.data.dto.ScheduleByDateDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
interface ScheduleApi {
    @GET("schedule")
    suspend fun getSchedule(
        @Query("group") group: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): List<ScheduleByDateDto>
}