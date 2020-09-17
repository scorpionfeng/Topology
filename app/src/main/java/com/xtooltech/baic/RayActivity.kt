package com.xtooltech.baic

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class RayActivity : AppCompatActivity() {
    private lateinit var radarView: RayView
    private lateinit var radarSweepThread: Thread
    private var startRadar = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ray)
        radarView=findViewById(R.id.ray)
    }


    inner class RadarSweep() : Runnable {
        var i = 1
        override fun run() {
            // TODO Auto-generated method stub
            //无限判断当前线程状态，如果没有中断，就一直执行while内容
            //如果中断返回true，否则返回false
            while (!Thread.currentThread().isInterrupted && i == 1) {
                try {
                    radarView.postInvalidate() // 刷新radarView, 执行onDraw();
                    Thread.sleep(10) // 暂停当前线程，更新UI线程（注释掉貌似没影响）
                } catch (e: InterruptedException) {
                    i = 0 // 结束当前扫描线程标志符
                    break
                }
            }
        }
    }

    fun click_start(view: View) {
        if (startRadar) {
            radarView.visibility = View.VISIBLE // 设置可见
            val radarAnimEnter = AnimationUtils.loadAnimation(
                this@RayActivity, R.anim.radar_anim_enter
            ) // 初始化radarView进入动画
            radarView.startAnimation(radarAnimEnter) // 开始进入动画
            radarSweepThread = Thread(RadarSweep()) // 雷达扫描线程
            radarSweepThread.start()
            startRadar = false
        } else {
            view as Button
            view.setText("start")
            val radarAnimEnter = AnimationUtils.loadAnimation(
                this@RayActivity, R.anim.radar_anim_exit
            ) // 初始化radarView退出动画
            radarView.startAnimation(radarAnimEnter) // 开始进入动画
            radarView.visibility = View.INVISIBLE // 设置不可见
            radarSweepThread!!.interrupt() // 停止扫描更新
            startRadar = true
        }
    }
}