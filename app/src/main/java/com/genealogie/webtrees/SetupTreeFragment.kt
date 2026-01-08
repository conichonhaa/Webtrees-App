package com.genealogie.webtrees

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

            // Arbre par défaut - Webtrees chargera "sorciers" automatiquement
            trees.addAll(listOf(
                FamilyTree("default", "Arbre par défaut (Webtrees choisira automatiquement)")
            ))

            // Sélectionner automatiquement le premier arbre
            selectedTree = trees[0]

            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        }, 800)
    }

    fun getSelectedTree(): FamilyTree? {
        // Retourner toujours un arbre par défaut
        return selectedTree ?: FamilyTree("default", "Arbre par défaut")
    }

    fun validateTree(): Boolean {
        // Toujours valide - on laisse Webtrees gérer
        return true
    }
}