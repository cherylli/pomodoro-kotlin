package com.example.pomodorotimer

enum class WorkState(val value: Int){
    Work(0),
    Break(1);

    companion object{
        fun getValueFromInt(value: Int) = WorkState.values().first{
            it.value == value
        }
    }
}