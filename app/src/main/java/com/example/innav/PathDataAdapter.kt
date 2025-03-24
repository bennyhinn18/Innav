//package com.example.innav
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//class PathDataAdapter(private val dataList: List<Map<String, Any>>) :
//    RecyclerView.Adapter<PathDataAdapter.PathDataViewHolder>() {
//
//    class PathDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val dataTextView: TextView = view.findViewById(R.id.dataTextView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathDataViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_path_data, parent, false)
//        return PathDataViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: PathDataViewHolder, position: Int) {
//        val data = dataList[position]
//        holder.dataTextView.text = data.toString()
//    }
//
//    override fun getItemCount() = dataList.size
//}
