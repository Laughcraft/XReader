package com.laughcraft.android.myreader.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.color.FilePainter
import com.laughcraft.android.myreader.file.FileLengthFormatter
import com.laughcraft.android.myreader.string.Translator
import com.laughcraft.android.myreader.string.XTranslator
import java.io.File
import javax.inject.Inject

class FileAdapter @Inject constructor(private val formatter: FileLengthFormatter,
                                      private val painter: FilePainter,
                                      private val translator: Translator): RecyclerView.Adapter<FileAdapter.FileHolder>() {

    lateinit var files: List<File>

    var onClick: ((file: File)->Unit)? = null
    var onLongClick: ((open: Boolean)->Unit)? = null

    var onSelection: ((selected: Int)->Unit)? = null

    val selectedFiles: MutableSet<File> = mutableSetOf()

    inner class FileHolder(private val v: View): RecyclerView.ViewHolder(v) {
        private val image: ImageView =      v.findViewById(R.id.ivFilePic)
        private val filename: TextView =    v.findViewById(R.id.tvFilename)
        private val filepath: TextView =    v.findViewById(R.id.tvFilePath)
        private val length: TextView =      v.findViewById(R.id.tvFileLength)
        private val extension: TextView =   v.findViewById(R.id.tvExtension)

        fun updateViews(file: File){
            selectHolderIfNeeded(v, file)
            itemView.setOnClickListener {
                if (selectedFiles.isEmpty()) onClick?.invoke(file) else {
                    selectFile(v, file)
                    if (selectedFiles.isEmpty()) onLongClick?.invoke(false)
                }
            }
            itemView.setOnLongClickListener {
                if (selectedFiles.isEmpty()) {
                    onLongClick?.invoke(true)
                    selectFile(v, file)
                } else {
                    onLongClick?.invoke(false)
                    clearSelectedFiles()
                }
                true
            }

            filepath.text = file.parentFile?.absolutePath?.removePrefix("/storage/emulated/0") ?: ""
            filename.text = file.name
            extension.text = if (file.isDirectory) "" else file.extension

            if (file.isDirectory){
                length.text = (translator as XTranslator).getElementsString(file.listFiles()?.size ?: 0)
                image.setImageResource(R.drawable.ic_doc_folder)
            } else {
                length.text = formatter.humanReadableByteCount(file.length())
                image.setImageResource(R.drawable.ic_file_outline)
            }
            image.setColorFilter(painter.getColorForFile(file))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        return FileHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false))
    }

    override fun onBindViewHolder(holder: FileHolder, position: Int) { holder.updateViews(files[position]) }

    override fun getItemCount(): Int = files.size

    private fun selectFile(itemView: View, file: File){
        if (file !in selectedFiles) {
            selectedFiles.add(file)
            onSelection?.invoke(selectedFiles.size)
            selectHolder(itemView)
        } else {
            selectedFiles.remove(file)
            onSelection?.invoke(selectedFiles.size)
            unselectHolder(itemView)
        }
    }

    private fun selectHolderIfNeeded(itemView: View, file: File){
        if (file in selectedFiles) selectHolder(itemView) else unselectHolder(itemView)
    }

    private fun selectHolder(itemView: View){
        itemView.setBackgroundColor(painter.getColor(R.color.selected_file_tint))
    }

    private fun unselectHolder(itemView: View){
        itemView.setBackgroundColor(painter.getColor(android.R.color.transparent))
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelectedFiles(){
        selectedFiles.clear()
        notifyDataSetChanged()
    }

    fun updateFileHolder(file: File){
        val i = files.indexOf(file)
        if (i > 0) notifyItemChanged(i)
    }

    fun updateFileHolders(files: List<File>){ files.forEach { updateFileHolder(it) } }
}