package com.votredomaine.webtrees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SetupTreeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noTreesText: TextView
    private lateinit var adapter: TreeAdapter
    
    private var selectedTree: FamilyTree? = null
    private val trees = mutableListOf<FamilyTree>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup_tree, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.treesRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        noTreesText = view.findViewById(R.id.noTreesText)
        
        setupRecyclerView()
        loadTrees()
    }

    private fun setupRecyclerView() {
        adapter = TreeAdapter(trees) { tree ->
            selectedTree = tree
        }
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadTrees() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        noTreesText.visibility = View.GONE
        
        view?.postDelayed({
            trees.clear()
            trees.addAll(listOf(
                FamilyTree("tree1", "Famille Dupont"),
                FamilyTree("tree2", "Famille Martin"),
                FamilyTree("tree3", "Arbre Principal")
            ))
            
            val prefsManager = PreferencesManager(requireContext())
            val defaultTree = prefsManager.getDefaultTree()
            if (defaultTree != null) {
                selectedTree = trees.find { it.name == defaultTree }
            }
            
            progressBar.visibility = View.GONE
            
            if (trees.isEmpty()) {
                noTreesText.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            }
        }, 1500)
    }

    fun getSelectedTree(): FamilyTree? {
        return selectedTree
    }

    fun validateTree(): Boolean {
        if (selectedTree == null) {
            noTreesText.text = getString(R.string.error_no_tree_selected)
            noTreesText.visibility = View.VISIBLE
            return false
        }
        return true
    }
}
