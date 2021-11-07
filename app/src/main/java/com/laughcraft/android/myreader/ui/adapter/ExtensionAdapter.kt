package com.laughcraft.android.myreader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laughcraft.android.myreader.R

class ExtensionAdapter(var extensions: Array<String>): RecyclerView.Adapter<ExtensionAdapter.ExtensionHolder>() {

    var onExtensionClicked = { extension: String -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtensionHolder {
        return ExtensionHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_extension, parent, false))
    }

    override fun onBindViewHolder(holder: ExtensionHolder, position: Int) {
        holder.bind(extensions[position])
    }

    override fun getItemCount(): Int {
        return extensions.size
    }

    inner class ExtensionHolder(v: View): RecyclerView.ViewHolder(v){
        var textView: TextView = v.findViewById(R.id.tvExtension)

        fun bind(extension: String){
            textView.text = extension
            textView.setOnClickListener { onExtensionClicked.invoke(extension) }
        }
    }
}