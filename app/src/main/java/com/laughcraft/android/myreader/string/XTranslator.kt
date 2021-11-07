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

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XTranslator @Inject constructor(private val applicationContext: Context) : Translator {
    
    private var russianResources: Resources = createResources(Translator.RUSSIAN_LANGUAGE)
    private var englishResources: Resources = createResources(Translator.ENGLISH_LANGUAGE)
    
    override fun getString(language: String?, @StringRes id: Int): String {
        return when (language) {
            Translator.ENGLISH_LANGUAGE -> englishResources.getString(id)
            Translator.RUSSIAN_LANGUAGE -> russianResources.getString(id)
            else -> applicationContext.resources.getString(id)
        }
    }
    
    override fun getString(@StringRes id: Int): String {
        return getString(null, id)
    }
    
    override fun getString(language: String?, @StringRes id: Int, vararg arguments: Any): String {
        return when (language) {
            Translator.ENGLISH_LANGUAGE -> englishResources.getString(id, arguments)
            Translator.RUSSIAN_LANGUAGE -> russianResources.getString(id, arguments)
            else -> applicationContext.resources.getString(id, arguments)
        }
    }
    
    override fun getStringArray(language: String?, @ArrayRes id: Int): Array<String>{
        return when (language) {
            Translator.ENGLISH_LANGUAGE -> englishResources.getStringArray(id)
            Translator.RUSSIAN_LANGUAGE -> russianResources.getStringArray(id)
            else -> applicationContext.resources.getStringArray(id)
        }
    }
    
    private fun createResources(language: String): Resources {
        var conf: Configuration = applicationContext.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(Locale(language))
        return applicationContext.createConfigurationContext(conf).resources
    }
    
    fun findViews(v: View) {
        try {
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) {
                    val child: View = v.getChildAt(i)
                    // recursively call this method
                    findViews(child)
                }
            } else if (v is TextView) {
                //do whatever you want ...
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getCurrentLanguage(): String {
        return Locale.getDefault().language
    }

    fun getElementsString(elements: Int): String{
        return when (getCurrentLanguage()){
            Translator.ENGLISH_LANGUAGE -> getEnglishString(elements)
            Translator.RUSSIAN_LANGUAGE -> getRussianString(elements)
            else -> getEnglishString(elements)
        }
    }

    private fun getRussianString(elements: Int): String {
        return "$elements " + when {
            elements in 5..20 -> "элементов"
            elements.toString().endsWith('1') -> "элемент"
            Integer.parseInt(elements.toString().last().toString()) in 2..4 -> "элемента"
            else -> "элементов"
        }
    }

    private fun getEnglishString(elements: Int): String {
        return "$elements " + if (elements == 1) "element" else "elements"
    }
}