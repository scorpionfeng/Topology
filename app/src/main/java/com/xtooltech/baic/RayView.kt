package com.xtooltech.baic

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View

class RayView:View{
    private lateinit var circlePaint: Paint // 圆形画笔
    private lateinit var linePaint: Paint // 线形画笔
    private lateinit var sweepPaint: Paint // 扫描画笔
    lateinit var sweepGradient: SweepGradient // 扇形渐变Shader
    var degree = 0.0f

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, att: AttributeSet?) : super(context, att) {
        initPaint()
    }

    /** * @param * @return void * @Description //初始化定义的画笔  */
    private fun initPaint() {
        val r: Resources = this.getResources()
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG) // 圆形画笔，设置Paint为抗锯齿
        circlePaint.setARGB(255, 50, 57, 74) // 设置透明度和RGB颜色
        circlePaint.setStrokeWidth(3.0f) // 轮廓宽度
        circlePaint.setStyle(Paint.Style.STROKE)
        linePaint = Paint(Paint.ANTI_ALIAS_FLAG) // 线性画笔
        linePaint.setStrokeCap(Paint.Cap.ROUND)
        linePaint.setARGB(150, 50, 57, 74)
        linePaint.setStrokeWidth(2.0f)
        sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG) // 雷达Shader画笔
        sweepPaint.setStrokeCap(Paint.Cap.ROUND)
        sweepPaint.setStrokeWidth(4.0f)
        sweepGradient = SweepGradient(0.0f, 0.0f,   Color.GREEN, Color.BLUE)
        sweepPaint.setShader(sweepGradient) //绘制梯度渐变，这里是渐变设置的方法
    }

    override fun onMeasure(wMeasureSpec: Int, hMeasureSpec: Int) {
        val width = MeasureSpec.getSize(wMeasureSpec)
        val height = MeasureSpec.getSize(hMeasureSpec)
        val d = if (width >= height) height else width // 获取最短的边作为直径
        setMeasuredDimension(d, d) // 重写测量方法，保证获得的画布是正方形
    }

    override fun onDraw(canvas: Canvas) {
        val Width: Int = getMeasuredWidth() // //计算控件的中心位置
        val Height: Int = getMeasuredHeight()
        val pointX = Width / 2.0f // 获得圆心坐标
        val pointY = Height / 2.0f
        var radius = if (pointX >= pointY) pointY else pointX // 设置半径
        radius -= 10 // 设置半径
        canvas.save() // 保存Canvas坐标原点
        degree += 5 // 扫描旋转增量度数
        canvas.translate(pointX, pointY) // 设置旋转的原点
        canvas.rotate(270 + degree)
        canvas.drawCircle(0.0f, 0.0f, radius, sweepPaint) // 绘制扫描区域
        canvas.restore() // 恢复原Canvas坐标(0,0)
        canvas.drawCircle(pointX, pointY, radius, circlePaint) // 绘制3个嵌套同心圆形，使用circlePaint画笔
        circlePaint.setAlpha(100) // 降低内部圆形的透明度
        circlePaint.setStrokeWidth(2.0f) // 轮廓宽度
        canvas.drawCircle(pointX, pointY, radius * 2 / 3, circlePaint)
        canvas.drawCircle(pointX, pointY, radius / 3, circlePaint)
        canvas.drawLine(pointX, 10.0f, pointX, 2 * radius + 10, linePaint) // 绘制十字分割线
        // ， 竖线
        canvas.drawLine(10.0f, pointY, 2 * radius + 10, pointY, linePaint)
        canvas.save() // 保存Canvas坐标原点
        canvas.translate(10.0f, radius + 10) // 设置相对横线起始坐标
        val s = radius / 12f // 刻度间距
        val minlength = s / 2 // 短刻度线长度
        for (i in 0..23) {
            var fromX: Float
            var toX: Float
            toX = s * i
            fromX = toX
            if (i % 4 != 0) { // 与圆形重叠处不画刻度
                if (i % 2 != 0) {
                    canvas.drawLine(
                        fromX, -minlength, toX, minlength,
                        linePaint
                    ) // 绘制X轴短刻度
                } else {
                    canvas.drawLine(
                        fromX, -s, toX, s,
                        linePaint
                    ) // 绘制X轴长刻度
                }
            }
        }
        canvas.restore()
        canvas.translate(pointX, 10.0f) // 设置相对竖线起始坐标
        for (i in 0..23) {
            var fromY: Float
            var toY: Float
            toY = s * i
            fromY = toY
            if (i % 4 != 0) {
                if (i % 2 != 0) {
                    canvas.drawLine(
                        -minlength, fromY, minlength, toY,
                        linePaint
                    ) // 绘制Y短轴刻度
                } else {
                    canvas.drawLine(
                        -s, fromY, s, toY,
                        linePaint
                    ) // 绘制Y长轴刻度
                }
            }
        }
    }
}