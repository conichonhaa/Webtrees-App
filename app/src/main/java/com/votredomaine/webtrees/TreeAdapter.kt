package com.votredomaine.webtrees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TreeAdapter(
    private val trees: List<FamilyTree>,
    private val onTreeSelected: (FamilyTree) -> Unit
) : RecyclerView.Adapter<TreeAdapter.TreeViewHolder>() {

    private var selectedPosition = -1

    inner class TreeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val treeName: TextView = view.findViewById(R.id.treeName)
        val treeTitle: TextView = view.findViewById(R.id.treeTitle)
        val selectedIndicator: ImageView = view.findViewById(R.id.selectedIndicator)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previousPosition = selectedPosition
                    selectedPosition = position
                    
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    
                    onTreeSelected(trees[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_family_tree, parent, false)
        return TreeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
        val tree = trees[position]
        
        holder.treeName.text = tree.title
        holder.treeTitle.text = tree.name
        
        holder.selectedIndicator.visibility = 
            if (position == selectedPosition) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = trees.size
}
