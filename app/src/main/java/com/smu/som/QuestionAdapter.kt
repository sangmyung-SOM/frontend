package com.smu.som

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


// 질문 배열을 목록 형태로 보여주는 Adapter
class QuestionAdapter(
    var questionList: ArrayList<Question>,
    val inflater: LayoutInflater
) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val question: TextView
        val category: TextView

        init {
            question = itemView.findViewById(R.id.question)
            category = itemView.findViewById(R.id.category)
        }
    }

    // ViewHolder 객체를 생성해서 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.question_item_view, parent, false)
        return ViewHolder(view)
    }

    // 전체 데이터 수 반환
    override fun getItemCount(): Int {
        return questionList.size
    }

    // ViewHolder 안의 내용을 position 에 해당하는 데이터로 교체
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.question.setText(questionList.get(position).question)
        holder.category.setText(questionList.get(position).category)
    }
}