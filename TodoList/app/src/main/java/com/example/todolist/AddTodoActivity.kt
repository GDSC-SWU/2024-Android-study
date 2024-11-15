package com.example.todolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.todolist.databinding.ActivityAddTodoBinding
import com.example.todolist.databinding.ActivityMainBinding
import com.example.todolist.db.AppDatabase
import com.example.todolist.db.TodoDao
import com.example.todolist.db.TodoEntity

class AddTodoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddTodoBinding

    lateinit var db : AppDatabase
    lateinit var todoDao : TodoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)!! //메인에서 불러온 인스턴스와 같은 인스턴스
        todoDao = db.getTodoDao()

        binding.btnComplete.setOnClickListener{
            insertTodo()
        }
    }

    //라디오 그룹 안에서 체크된 버튼 아이디를 가져옴.
    private fun insertTodo()
    {
        val todoTitle = binding.edtTitle.text.toString()
        var todoImportance = binding.radioGroup.checkedRadioButtonId

        var impData =0
        when(todoImportance)
        {
            R.id.btn_high -> {impData=1}
            R.id.btn_high ->{impData=2}
            R.id.btn_high ->{impData=3}
        }

        if(impData ==0 || todoTitle.isBlank()){
            Toast.makeText(this,"모든 항목을 채워주세요.", Toast.LENGTH_SHORT).show()
        }else{
            Thread{
                todoDao.insertTodo(TodoEntity(null,todoTitle,impData))//자동으로 id는 null로 해줘도 추가되어서 들어감.
                runOnUiThread{
                    Toast.makeText(this,"할 일이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    finish() //종료 후 메인 액티비티로 화면 전환
                }
            }.start()
        }
    }
}