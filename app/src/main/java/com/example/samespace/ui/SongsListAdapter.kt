package com.example.samespace.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.samespace.R
import com.example.samespace.databinding.RvSongItemBinding
import com.example.samespace.models.Song

class SongsListAdapter : RecyclerView.Adapter<SongsListAdapter.ViewHolder>() {
    private var onClickListener: OnClickListener? = null
    private val diffUtil =
        object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(
                oldItem: Song,
                newItem: Song,
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Song,
                newItem: Song,
            ): Boolean {
                return oldItem == newItem
            }
        }
    private val listDiffer = AsyncListDiffer(this, diffUtil)

    fun saveData(songs: List<Song>) {
        listDiffer.submitList(songs)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = RvSongItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val song = listDiffer.currentList[position]
        holder.binding.apply {
            tvTitle.text = song.name
            tvSinger.text = song.artist
            Glide.with(holder.itemView.context)
                .load("https://cms.samespace.com/assets/${song.cover}")
                .placeholder(R.drawable.bg_gradient)
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .into(ivSong)
        }
        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position, song)
        }
    }

    override fun getItemCount() = listDiffer.currentList.size

    class ViewHolder(val binding: RvSongItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(
            position: Int,
            song: Song,
        )
    }
}
