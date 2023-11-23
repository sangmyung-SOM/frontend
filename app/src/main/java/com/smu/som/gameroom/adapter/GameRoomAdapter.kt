package com.smu.som.gameroom.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smu.som.R
import com.smu.som.databinding.ActivityGameroomListBinding
import com.smu.som.gameroom.model.GameRoom


class GameRoomAdapter(val context: Context)
    : RecyclerView.Adapter<GameRoomAdapter.Holder>()
{

    private lateinit var onItemClickListener: GameRoomAdapter.OnItemClickListener
    private lateinit var binding: ActivityGameroomListBinding
    var gameRoomList = ArrayList<GameRoom>()
    val bundle: Bundle = Bundle()
    val selectItems = HashSet<Int>()


    fun addItem(item: GameRoom){
        gameRoomList.add(item)
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(v: Holder, data: GameRoom, pos: Int)
    }

    fun setOnItemClickListener(onItemClickListener: GameRoomAdapter.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }



    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView){


        val gameRoomNm = itemView.findViewById<TextView>(R.id.gameroom_nm)
        val category = itemView.findViewById<ImageView>(R.id.game_setting_category)
        val adultView = itemView.findViewById<ImageView>(R.id.game_setting_adult)
        val roomItem = itemView.findViewById<View>(R.id.room_list)


        @SuppressLint("NotifyDataSetChanged")
        fun bind(gameRoom: GameRoom, context: Context){
            roomItem.isActivated = false

            gameRoomNm.text = gameRoom.roomName // 우선 유저 닉네임으로 설정 추후에 방이름, 유저이름 등록
            settingCategory(gameRoom.category, gameRoom.adult) // 카테고리 설정에 따른 UI 변경


            itemView.setOnClickListener(View.OnClickListener {
                onItemClickListener.onItemClick(this, gameRoom, adapterPosition)
            })

        }

        // 카테고리 설정에 따른 UI 변경
        private fun settingCategory(category: String?, adult: String?) {
            when(category) {
                "COUPLE" -> setImage(R.drawable.couple)
                "MARRIED" -> setImage(R.drawable.married)
                "PARENT" -> setImage(R.drawable.parent)
            }
            adultView.isEnabled = adult == "ON"
        }

        private fun setImage(image: Int) {
            category.setImageResource(image)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameRoomAdapter.Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.gameroom_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return gameRoomList.size
    }

    override fun onBindViewHolder(holder: GameRoomAdapter.Holder, position: Int) {
        holder.bind(gameRoomList[position], context)
    }
}