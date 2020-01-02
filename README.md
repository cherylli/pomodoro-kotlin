
[![Kotlin Version](https://img.shields.io/badge/kotlin-1.3.50-blue.svg)](http://kotlinlang.org/)  
[![Gradle](https://img.shields.io/badge/gradle-5.4.1-blue.svg)](https://lv.binarybabel.org/catalog/gradle/latest)  
[![API](https://img.shields.io/badge/API-19%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=21)  
# Pomodoro Timer:  
  
## About the project  
  
  
#### How to use  
  
Set the duration for the work session and let the app do the counting!  
  
#### Technologies  
  
- **Language**: Kotlin  
- **Deployment**: Android Studio  
  
#### Known Issue  
  
1. There is a [rare chance](https://stackoverflow.com/questions/52647496/foreground-app-killed-by-os-after-1-hour-in-standby-mode) the Android Operating System will terminate the App after extremely long session.  (Tested on my Xiaomi Redmi 7, app was terminated after 10 hours, which is acceptable for this project)
  
#### Challenges  
  
1. The built in [CountDownTimer](https://developer.android.com/reference/android/os/CountDownTimer) we use do not provide a stop method.  
Resolution: Destroy the CountDownTimer on a stop timer request, save the remaining time, and start a new CountDownTimer with the remaining time.  
  
3. The CountDownTimer stops counting when app is running in the background. (user clicked home, or back, but not kill app)  
Resolution: Move the CountDownTimer into a service, which can perform long-running operations in the background  
  
4. The CountDownTimer will also stop when the phone goes into sleep mode. (CPU is turned off)  
[Resolution](https://stackoverflow.com/questions/25613395/when-the-screen-turns-off-the-countdowntimer-doesnt-work-properly): Keep the CPU on when timer is counting by acquiring the wakelock  
  
5. [The Android Operating System can kill the entire app to reclaim resources.](https://stackoverflow.com/questions/46435406/when-memory-is-low-does-android-destroy-individual-activities-or-the-entire-sta)  
[Resolution](https://stackoverflow.com/questions/16651009/android-service-stops-when-app-is-closed): Move the timer into a foreground service, which means the user is aware of the service running. Hence reduce the chance of the app being killed.  
Noted this cannot 100% prevent the app from being kill. Normal service are meant to be kill and restart from previous state when resource is available again, but this is not an option for timer purpose  
  
### Tasks  
  
- [x] Have a timer that count down  
- [x] Handle timer stop and cancel  
- [x] Keep counting when app run in background  
- [x] Send Notification on complete  
- [x] Handle orientation change  
- [x] Automatically switch from work and break sessions  
- [ ] Store session duration setting
- [ ] Include time stop sound effect
