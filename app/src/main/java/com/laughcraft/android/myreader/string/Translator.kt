/*
 * Copyright (c) 2021.
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

package com.laughcraft.android.myreader.string

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

interface Translator {
    
    companion object {
        const val RUSSIAN_LANGUAGE = "ru"
        const val ENGLISH_LANGUAGE = "en"
    }

    fun getCurrentLanguage(): String
    fun getString(@StringRes id: Int): String
    fun getString(language: String? = null, @StringRes id: Int): String
    fun getString(language: String? = null, @StringRes id: Int, vararg arguments: Any): String
    fun getStringArray(language: String? = null, @ArrayRes id: Int): Array<String>
}