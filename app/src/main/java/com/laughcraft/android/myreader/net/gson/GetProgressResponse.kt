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

package com.laughcraft.android.myreader.net.gson

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GetProgressResponse {
    /**
     * HTTP Status Code
     */
    @SerializedName("code") @Expose var code: Int? = null
    
    /**
     * Always 'ok' on Success
     */
    @SerializedName("status") @Expose var status: String? = null
    
    @SerializedName("error") @Expose var error: String? = null
    
    /**
     * Result data
     */
    @SerializedName("data") @Expose var data: Data? = null
    
    class Data {
        /**
         * Your Conversion ID
         */
        @SerializedName("id") @Expose var id: String? = null
        
        /**
         * Conversion Step
         * Allowed Values: wait,finish,convert,upload
         */
        @SerializedName("step") @Expose var step: String? = null
        
        /**
         * Step Progress in %
         */
        @SerializedName("step_percent") @Expose var stepPercent: String? = null
        
        /**
         * API Minutes used by this conversion
         */
        @SerializedName("minutes") @Expose var minutes: Int? = null
        
        /**
         * Output pdfFile information
         */
        @SerializedName("output") @Expose var output: Output? = null
        
        class Output {
            /**
             * URL of the pdfFile to download
             * If there are multiple output files it will contain a link to a ZIP pdfFile,
             * which find all output files
             */
            @SerializedName("url") @Expose var url: String? = null
            
            /**
             * Size of the pdfFile in bytes
             */
            @SerializedName("size") @Expose var size: Int? = null
        }
    }
}
