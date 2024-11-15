package com.example.todolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ItemTodoBinding
import com.example.todolist.db.TodoEntity

class TodoRecyclerViewAdapter(private val todoList:ArrayList<TodoEntity>,
                              private val listener: OnItemLongClickListener)
    : RecyclerView.Adapter<TodoRecyclerViewAdapter.MyViewHolder>(){
    inner class MyViewHolder(binding :ItemTodoBinding) : RecyclerView.ViewHolder(binding.root){
        val tv_importance = binding.tvImportance
        val tv_title = binding.tvTitle

        //root 레이아웃을 의미
        val root = binding.root
    }

    //뷰 홀더를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding : ItemTodoBinding =
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder((binding))
    }

    //아이템 갯수 반환.
    override fun getItemCount(): Int {
        return todoList.size
    }

    //생성한 뷰 홀더 혹은 재활용 하는 뷰 홀더를 받아서 데이터와 연결
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val todoData = todoList[position]

        val red = ContextCompat.getColor(holder.itemView.context, R.color.red)
        val yellow = ContextCompat.getColor(holder.itemView.context, R.color.yellow)
        val green = ContextCompat.getColor(holder.itemView.context, R.color.green)

        when(todoData.importance){
            1 -> {
                holder.tv_importance.setBackgroundColor(red)
            }
            2 -> {
                holder.tv_importance.setBackgroundColor(yellow)
            }
            3 -> {
                holder.tv_importance.setBackgroundColor(green)
            }
        }
        holder.tv_importance.text=todoData.importance.toString()
        holder.tv_title.text = todoData.title

        holder.root.setOnLongClickListener{
            listener.onLongClick(position)
            false
        }
    }
}