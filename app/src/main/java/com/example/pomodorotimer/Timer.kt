package com.example.pomodorotimer

class Timer {
    var hourToCount:Long = 0
    var minToCount:Long = 0
    var secondToCount:Long = 0
    var counting = false
    var resume = false  // turn to true when you clicked pause
    var workTimer: Long = 0
    var breakTimer: Long = 0
    var workState = WorkState.Work //default to start with work timer

    fun minusOneSecond(){
        secondToCount --
        if (secondToCount.toInt()==0 && minToCount.toInt()==0 && secondToCount.toInt()==0){
            //timer ends
        }
        if (secondToCount.toInt()==0){
            secondToCount = 60
            minToCount--
            if (minToCount.toInt()==0){
                minToCount = 60
                hourToCount --
            }
        }

    }
}