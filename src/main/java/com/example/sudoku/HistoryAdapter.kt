package com.example.sudoku

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sudoku.data.SavedGameEntity
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onClick: (SavedGameEntity) -> Unit
) : ListAdapter<SavedGameEntity, HistoryAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<SavedGameEntity>() {
        override fun areItemsTheSame(a: SavedGameEntity, b: SavedGameEntity) = a.id == b.id
        override fun areContentsTheSame(a: SavedGameEntity, b: SavedGameEntity) = a == b
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvDate  = v.findViewById<TextView>(R.id.tv_game_date)
        private val tvInfo  = v.findViewById<TextView>(R.id.tv_game_info)
        private val fmt     = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())

        fun bind(game: SavedGameEntity) {
            tvDate.text = fmt.format(Date(game.timestamp))
            tvInfo.text = "${game.variant.replaceFirstChar{it.uppercase()}} • ${difficulty(game.difficulty)}"
            itemView.setOnClickListener { onClick(game) }
        }
        private fun difficulty(d:Int) = when(d){0->"Fácil";1->"Media";else->"Difícil"}
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_game, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
}
