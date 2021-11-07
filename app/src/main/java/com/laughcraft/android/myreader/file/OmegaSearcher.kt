package com.laughcraft.android.myreader.file

import com.laughcraft.android.myreader.file.abstr.Searcher
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OmegaSearcher @Inject constructor(): Searcher {
    
    enum class Conditions{ Extensions, MinSize, MaxSize, Name, AllFiles }
    
    private var conditions: EnumMap<Conditions, ((File) -> Boolean)> = EnumMap(Conditions::class.java)

    override fun setExtensions(vararg extensions: String): OmegaSearcher {
        val filteredExtensions = extensions
            .distinct()
            .filter { it.isNotBlank() }
            .map { it.replace(".", "").lowercase() }
    
        conditions[Conditions.Extensions] = { file ->
            when {
                file.isDirectory -> false
                file.extension in filteredExtensions -> true
                else -> false
            }
        }
        return this
    }
    
    override fun setName(name: String): OmegaSearcher {
        conditions[Conditions.Name] = { it.name.contains(name, true) }
        return this
    }
    
    override fun setMinSize(size: Long): OmegaSearcher {
        conditions[Conditions.MinSize] = { if (it.isDirectory) false else it.length() >= size }
        return this
    }
    
    override fun setMaxSize(size: Long): OmegaSearcher {
        conditions[Conditions.MaxSize] = { if (it.isDirectory) false else it.length() <= size }
        return this
    }

    fun setAllFilesMode(): OmegaSearcher {
        clearQuery()
        conditions[Conditions.AllFiles] = { it.isFile }
        return this
    }

    override fun clearQuery(): OmegaSearcher {
        conditions.clear()
        return this
    }

    override fun collectAllFiles(directory: File): List<File> {
        return setAllFilesMode().search(directory)
    }

    override fun search(directory: File): List<File> {
        val result: MutableList<File> = mutableListOf()
        if (conditions.isEmpty()){
            return result
        }

        directory.listFiles()?.toList()?.let { searchInDirectory(it, result) }

        return result
    }

    private fun searchInDirectory(directoryFiles: List<File>, results: MutableList<File>){
        val nextLevelFiles = mutableListOf<File>()
        directoryFiles.forEach {
            if (checkFile(it)) results.add(it)
            if (it.isDirectory) { it.listFiles()?.let { files -> if (files.isNotEmpty()) nextLevelFiles.addAll(files.toList()) } }
        }
        
        if (nextLevelFiles.isNotEmpty()) searchInDirectory(nextLevelFiles, results)
    }
    
    private fun checkFile(file: File): Boolean {
        conditions.values.forEach { if (!it.invoke(file)) return false }
        return true
    }
}