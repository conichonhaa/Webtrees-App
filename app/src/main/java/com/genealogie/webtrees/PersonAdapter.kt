package com.genealogie.webtrees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PersonAdapter(
    private val persons: List<Person>,
    private val onClick: (Person) -> Unit
) : RecyclerView.Adapter<PersonAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textName)
        val info: TextView = view.findViewById(R.id.textInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = persons[position]
        holder.name.text = person.fullName.ifEmpty { person.xref }
        holder.info.text = buildString {
            if (person.birthDate.isNotEmpty()) append("Né(e) ${person.birthDate}")
            if (person.birthPlace.isNotEmpty()) append(" à ${person.birthPlace}")
            if (person.deathDate.isNotEmpty()) {
                if (isNotEmpty()) append("  •  ")
                append("Décédé(e) ${person.deathDate}")
            }
        }.ifEmpty { person.xref }
        holder.itemView.setOnClickListener { onClick(person) }
    }

    override fun getItemCount() = persons.size
}
