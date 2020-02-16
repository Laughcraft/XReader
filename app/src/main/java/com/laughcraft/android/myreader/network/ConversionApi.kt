/*
 * Copyright (c) 2019.
 * Created by Vladislav Zraevskij
 *
 * This file is part of XReader.
 *
 *     XReader is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     XReader is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with XReader.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 *
 */

package com.laughcraft.android.myreader.network

import com.laughcraft.android.myreader.network.gson.*

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ConversionApi {
    @POST("convert")
    fun startConversion(@Body conversion: PostConversionRequest): Call<PostConversionResponse>
    
    @GET("convert/{id}/status")
    fun getConversionProgress(@Path("id") id: String): Call<GetProgressResponse>
    
    @GET("convert/{id}/dl)")
    fun getConvertedFile(@Path("id") id: String): Call<GetConvertedFileResponse>
    
    @Streaming
    @GET
    fun getFile(@Url url: String): Call<ResponseBody>
    
    @PUT("convert/{id}/{filename}")
    fun uploadFile(@Path("id") id: String, @Path(
            "filename") filename: String, @Body requestBody: RequestBody): Call<PutFileResponse>
}
