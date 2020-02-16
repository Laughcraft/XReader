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

package com.laughcraft.android.myreader.model

import android.content.Context
import android.os.Build
import java.io.File
import java.util.*

object Natives {
    var TAG = Natives::class.java.simpleName
    val ARCH = Build.CPU_ABI
    fun loadLibraries(context: Context, vararg libs: String) {
        try {
            for (l in libs) System.loadLibrary(l) // API16 failed to find dependencies
        } catch (e: ExceptionInInitializerError) { // API15 crash
            for (l in libs) loadLibrary(context, l)
        } catch (e: UnsatisfiedLinkError) {
            for (l in libs) loadLibrary(context, l)
        }
    }
    
    /**
     * API15 crash while loading wrong arch native libraries. We need to find and load them manually.
     *
     *
     * Caused by: java.lang.UnsatisfiedLinkError: Cannot load library: reloc_library[1286]:  1823 cannot locate '__aeabi_idiv0'...
     * at java.lang.Runtime.loadLibrary(Runtime.java:370)
     * at java.lang.System.loadLibrary(System.java:535)
     */
    fun loadLibrary(context: Context, libname: String) {
        val file = search(context, System.mapLibraryName(libname)) ?: throw UnsatisfiedLinkError(
                "pdfFile not found: $libname")
        System.load(file)
    }
    
    fun search(context: Context, filename: String): String? {
        val dir = context.applicationInfo.nativeLibraryDir
        if (dir.endsWith(ARCH)) {
            var f = File(dir)
            f = f.parentFile
            val lib = search(f, filename)
            if (lib != null) return lib
        }
        return search(File(dir), filename)
    }
    
    fun list(f: File, filename: String): ArrayList<File> {
        val ff = ArrayList<File>()
        val aa = f.listFiles()
        if (aa != null) {
            for (a in aa) {
                if (a.isDirectory) {
                    val mm = list(a, filename)
                    ff.addAll(mm)
                }
                if (a.name == filename) ff.add(a)
            }
        }
        return ff
    }
    
    fun search(f: File, filename: String): String? {
        val ff: List<File> = list(f, filename)
        Collections.sort(ff, ArchFirst())
        return if (ff.size == 0) null else ff[0].absolutePath
    }
    
    class ArchFirst : Comparator<File> {
        override fun compare(o1: File, o2: File): Int {
            val p1 = o1.path
            val p2 = o2.path
            val b1 = p1.contains(ARCH)
            val b2 = p2.contains(ARCH)
            if (b1 && b2) return p1.compareTo(p2)
            if (b1) return -1
            return if (b2) 1 else p1.compareTo(p2)
        }
    }
}