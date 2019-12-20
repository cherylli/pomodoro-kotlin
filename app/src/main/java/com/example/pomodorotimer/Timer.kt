package com.example.pomodorotimer


class Timer{
    private var hourToCount: Int = 0
    private var minToCount: Int = 0
    private var secondToCount: Int = 0
    var isCounting = false
    var needResume = false  // turn to true when you clicked pause
    var workState = WorkState.Work //default to start with work timer

    // these two are both in minutes
    var breakTimer: Int = 0
    var workTimer: Int = 0


    fun loadBreakTimer() {

        hourToCount = 0
        minToCount = breakTimer
        secondToCount = 0
    }

    fun loadWorkTimer() {

        hourToCount = 0
        minToCount = workTimer
        secondToCount = 0

    }


    fun displayTime(): String {

        var result = ""

        var hr = hourToCount.toString()
        var min = minToCount.toString()
        var sec = secondToCount.toString()

        if (hourToCount > 0) {
            result += (if (hr.length < 2) hr.padStart(2, '0') else hr) + ":"
        }

        result += (if (min.length < 2) min.padStart(2, '0') else min) + ":"
        result += if (sec.length < 2) sec.padStart(2, '0') else sec

        return result
    }

    fun toSeconds(): Long {

        return hourToCount.toLong() * 60 * 60 + minToCount.toLong() * 60 + secondToCount.toLong()
    }

    fun restoreFromSeconds(s:Long){
        secondToCount = (s % 60).toInt()
        minToCount = (((s-secondToCount)/60) % 60).toInt()
        hourToCount = ((s-secondToCount-minToCount*60)/3600).toInt()
    }

    fun minusOneSecond() {

        if (hourToCount == 0 && minToCount == 0 && secondToCount == 0) {
            endTimer()
        }

        if (secondToCount == 0 && minToCount > 0) {
            secondToCount = 60
            minToCount--

        } else if (secondToCount == 0 && minToCount == 0) {
            secondToCount = 60
            minToCount = 60
            hourToCount--
        }
        secondToCount--

    }

    fun endTimer() {
        isCounting = false
        needResume = false
        //reset timer
        secondToCount=0
        minToCount= workTimer
        hourToCount=0
    }
}