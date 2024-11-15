package com.example.todolist

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.ActivityMainBinding
import com.example.todolist.db.AppDatabase
import com.example.todolist.db.TodoDao
import com.example.todolist.db.TodoEntity

class MainActivity : AppCompatActivity(), OnItemLongClickListner {

    private lateinit var binding :ActivityMainBinding

    //db 관련 변수
    private lateinit var db : AppDatabase
    private lateinit var todoDao : TodoDao
    private lateinit var todoList : ArrayList<TodoEntity>
    
    //어댑터 변수 생성
    private lateinit var adapter: TodoRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)!! //companion object
        todoDao = db.getTodoDao()

        getAllTodoList()

        //버튼을 눌렀을 때 AddTodoActivity로 이동
        binding.btnAddTodo.setOnClickListener{
            val intent = Intent(this,AddTodoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getAllTodoList(){
        Thread{
            todoList = ArrayList(todoDao.getAllTodo())
            setRecyclerView()
        }.start()
    }
    private fun setRecyclerView(){

        //아이템 뷰 생성
        //백그라운드 스레드라서 runOnUiThread사용
        runOnUiThread{
            adapter = TodoRecyclerViewAdapter(todoList,this)//this는 메인액티비티 객체로 리스너 구현해서 this로 하면 됨.
            binding.recyclerview.adapter = adapter
            binding.recyclerview.layoutManager = LinearLayoutManager(this)
        }
    }

    //액티비티의 생명주기 중 다른 액티비티로 갔다가 돌아오는 함수
    //메인에서 할일 추가 액티비티로 갔다가 다시 메인으로 돌아와서 내용 갱신
    override fun onRestart(){
        super.onRestart()
        getAllTodoList()
    }

    //삭제 기능 구현.
    override fun onLongClick(position: Int) {
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.alert_title))
        builder.setMessage(getString(R.string.alert_message))
        builder.setNegativeButton(getString(R.string.alert_no),null)
        builder.setPositiveButton(getString(R.string.alert_yes),
        object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                deleteTodo(position) }})
        builder.show()
    }
    private fun deleteTodo(position : Int){
        Thread{
            todoDao.deleteTodo(todoList[position])
            todoList.removeAt(position)

            runOnUiThread { adapter.notifyDataSetChanged()//어댑터 안에 데이터 바뀜 알려주고 자동 갱신
                Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

}