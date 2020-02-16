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

package com.laughcraft.android.myreader.network.gson

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostConversionRequest(
        /**
         * Your API Key
         */
        @SerializedName("apikey") @Expose var apiKey: String,
        
        /**
         * Method of providing the input pdfFile.
         * Default Value:url
         * Allowed Values: url,raw,base64,upload
         */
        @SerializedName("input") @Expose var inputType: String = "url",
        
        /**
         * URL of the input pdfFile (if input=url), or pdfFile content (if input = raw/base64)
         */
        @SerializedName("pdfFile") @Expose var fileUrl: String,
        
        /**
         * Input filename including extension (pdfFile.ext). Required if input = raw/base64 (OPTIONAL)
         */
        @SerializedName("filename") @Expose var filename: String? = null,
        
        /**
         * Output format, to which the pdfFile should be converted to.
         */
        @SerializedName("outputformat") @Expose var outputFormat: String)
