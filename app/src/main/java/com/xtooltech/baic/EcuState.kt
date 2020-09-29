package com.xtooltech.baic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.PixelFormat.OPAQUE
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

typealias  onClick=(EcuUnit)->Unit


class EcuState : View {


    /** 网关图片 */
    private var bitmapGate: Bitmap=BitmapFactory.decodeResource(resources, R.mipmap.ic_gateway);
    /** 最深的bus线Y */
    private var deepMax: Float=0.0f
    private var obdWidth: Float=130.0f
    /** bus 线延长线 */
    private val spacing=50.0f
    private var autoScanning: Boolean=false
    private var loadFlag: Boolean=false
    private var mheight: Int=0
    private var mWidth: Int=0
    private  var _canvas: Canvas?=null
    private val ANIMATION_DELAY: Long=300L
    private var sideTop: Float=0.0f
    private val SCAN_STEP=4.0f

    private var listener:onClick?=null
    private var widthMax=0.0f

    @Volatile
    private var moving: Boolean = false
    private var currentTimeDown: Long = 0L

    private var dragStarty: Float = 0.0f;
    private var dragStartx: Float = 0.0f
    private var distanceH: Float = 0.0f;
    private var distanceV: Float = 0.0f
    private var destMovex: Float = 0.0f
    private var destMovey: Float = 0.0f
    private var rawPointx = 0.0f
    private var rawPointy = 0.0f
    private var ecuScan:EcuUnit?=null
    private var textPadding=10.0f
    /** 前一个总线的开始点,用于画总线开始的连接线 */
    var preStartx=0.0f
    var preStarty=0.0f

    /** 当前缩放scale */
    private var destScale: Float = 1.0f
    private var data_ecubuss: MutableList<EcuBus> = mutableListOf()

    /** 连接线映射
     * id->名称
     * */
    private lateinit var busNameMap: Map<Int, String>

    /** ecu映射
     * pos->obj
     * */
    private var ecuMap = mutableMapOf<String, EcuUnit>()

    /** ecu映射
     * ecuname->obj
     * */
    private var ecunameMap = mutableMapOf<String, EcuUnit>()

    /** 总线映射
     * busid->obj
     * */
    private var busMap = mutableMapOf<Int, EcuBus>()

    /** 扩展线映射 */
    private lateinit var linkMap: Map<String, List<Int>>
    private var colorMap: Map<Int, String> = mapOf(
        0 to "#A16121",
        1 to "#15BABF",
        2 to "#97C215",
        3 to "#F22EEC",
        4 to "#2585F5",
        5 to "#F77C2F",
        6 to "#E5DB23",
        7 to "#224CD4",
        8 to "#D73B91",
        9 to "#8735E6"
    )

    /** 列宽度(默认) */
    private val widthPer: Float = 130.0f

    /** 单个ecu宽度(默认) */
    private val ecuWidth: Float = 50.0f

    /** 单个ecu高度(默认) */
    private val ecuHeight: Float = 30.0f

    /** 站立线高度(默认) */
    private val standLineHeight: Float = 21.0f

    private val minHeightBus=(ecuHeight+standLineHeight)*2.0f+10.0f
    /** ecu左右的空白距离 */
    val ecuMargin: Float = 10.0f

    /** 连接点偏移量 */
    private val linkPointOffset: Float = 5.0F

    private var scanning=false;


    /** can线画笔 */
    private var busPaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
        isDither = true
        strokeWidth=2.0f
        style = Paint.Style.STROKE

    }
    /** can线画笔 */
    private var startPointPaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        isDither = true
        strokeWidth=2.0f
        style = Paint.Style.STROKE
    }
    /** scan线画笔 */
    private var scanPaint = Paint().apply {
        color = Color.BLUE
        alpha= OPAQUE
        strokeWidth=3.0f
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL

    }


    /** ecu形状画笔 */
    private var ecuPaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL

    }

    var textPaint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = 4.0f
        textSize = 18.0f
        isAntiAlias=true
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    /** 获取 画笔的盒子模型 */
    val fontMeterics = textPaint.getFontMetrics()
    val textDistance = (fontMeterics.bottom - fontMeterics.top) / 2 - fontMeterics.bottom


    private lateinit var busSet: List<BussetBean>

    private lateinit var ecuSet: List<EcusetBean>

    private lateinit var connectSet: List<ConnectBean>

    private var rawData: BaicBean?=null


    var defaultBusDuration: Int = 100;


    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {


    }

    private fun supportData(data:BaicBean) {

        /** 创建布局对象 */
        /** 0.canbus 字典化 */
        busNameMap = data.busset.associateBy({ it.bustype }, { it.busname })
        data.connect?.apply {
            takeIf { isNotEmpty() }?.let {
                linkMap = associateBy({ it.pos }, { it.dest })
            }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

            if(_canvas==null){
                _canvas=canvas
            }

            canvas.save()
            canvas.scale(destScale, destScale)
            canvas.translate(destMovex, destMovey)

            if(loadFlag){
                loadFlag=false;
                mheight=height
                val contentHeight = mheight - paddingTop - paddingBottom
                val contentHeightShow = contentHeight * 1.0f
                rawData?.apply {
                    supportData(this)
                    //列宽度
                    val heightPer = Math.max(contentHeightShow / buscount,minHeightBus)

                    busset.filter { it.bustype>0 }.forEachIndexed { index, it ->
                        EcuBus(
                            obdWidth,
                                index * heightPer+minHeightBus,
                                obdWidth+width.toFloat(),
                                index * heightPer+minHeightBus,
                            Color.parseColor(colorMap[it.bustype]),
                            it.bustype,
                            findBusNameBy(it),
                            findEcuUintByBusId(it, index * heightPer+minHeightBus)
                        ).apply {
                            data_ecubuss.add(this)
                            busMap[busId] = this
                            endX=(ecus.size/2+ if(ecus.size%2==1) 2 else 1)*widthPer+spacing
                            endX= max(endX,widthMax)
                            /** 更新最大长度 */
                            widthMax=endX
                            /** 更新中心点 */
                            deepMax=endY
                        }
                    }
                    val centerY = (deepMax+minHeightBus) / 2.0f
                    takeIf { gwsupport==1 }?.let {
                        val gateRect = RectF(obdWidth / 2 - 25, centerY - standLineHeight - ecuHeight, obdWidth / 2 + 25, centerY - standLineHeight)
                        EcuUnit(gateRect.left, gateRect.right, gateRect.width(), gateRect.height(), Color.parseColor("#333333"), "GW", "0-1", 0, true, "GW", "0", 0).apply {
                            ecunameMap[title] = this
                        }
                    }
                }
            }


            /** 2.创建单元 */
            drawEcus(canvas)
            /** 3.创建扩展线条 */
            drawExt(canvas)
            /** 4.画OBD */
            drawObd(canvas)

            startScan(canvas)


            canvas.restore()

    }

    private fun drawObd(canvas: Canvas) {
        rawData?.run {
            val bitmapObd=BitmapFactory.decodeResource(resources,R.mipmap.ic_obd)
            val centerY = (deepMax+minHeightBus) / 2.0f
            val obdRect=RectF(0.0f,centerY-bitmapObd.height/2.0f,bitmapObd.width.toFloat(),centerY+bitmapObd.height/2.0f)
            /** obd图片 */
            canvas.drawBitmap(bitmapObd,null,obdRect,null)
            /** OBD横线 */
            canvas.drawLine(bitmapObd.width.toFloat()-2, centerY,obdWidth, centerY,startPointPaint)
            takeIf { gwsupport==1 }?.let {
                val gateRect=RectF(obdWidth/2-25,centerY-standLineHeight-ecuHeight,obdWidth/2+25,centerY-standLineHeight)
                val gateSrc=Rect(0, 0,bitmapGate.width,bitmapGate.height)
                val gateDest=Rect((obdWidth/2-bitmapGate.width/2).toInt(), (centerY-standLineHeight-ecuHeight/2-bitmapGate.height/2).toInt(), (obdWidth/2+bitmapGate.width/2).toInt(), (centerY-standLineHeight-ecuHeight/2+bitmapGate.height/2).toInt())
                canvas.drawRoundRect(gateRect,6.0f,6.0f,ecuPaint)
                canvas.drawBitmap(bitmapGate,gateSrc,gateDest,null)
                canvas.drawLine(obdWidth/2,centerY,obdWidth/2,centerY-standLineHeight,startPointPaint)
                ecunameMap["GW"]?.apply {
                    x=gateRect.left
                    y=gateRect.top
                    width=gateRect.width()
                    height=gateRect.height()
                }
            }
        }
    }
    private fun drawEcus(canvas: Canvas) {
        data_ecubuss.forEach {
            /** 画主线 bus横线*/
            canvas.drawLine(
                it.startX,
                it.startY,
                it.startX+widthMax,
                it.endY,
                busPaint.apply { color = it.color })
            /** 画主线 bus竖线*/
            if(preStartx!=0.0f || preStarty!=0.0f){
                canvas.drawLine(preStartx,preStarty,it.startX,it.startY,startPointPaint)
            }
            preStartx=it.startX
            preStarty=it.startY
            /** 画主线 bus竖线  end*/
            /** 画主线下面的ECU单元 */
            it.ecus.forEach { ecu ->
                val rect = RectF().apply {
                    left = ecu.x
                    top = ecu.y
                    right = ecu.x + ecu.width
                    bottom = ecu.y + ecu.height
                };
                canvas.drawRoundRect(rect,6.0f,6.0f, ecuPaint.apply { color=ecu.color })



                /** 画支柱线 */
                val endYSupport = if (ecu.above) rect.bottom else rect.top
                canvas.drawLine(
                    rect.centerX().toFloat(),
                    it.startY,
                    rect.centerX().toFloat(),
                    endYSupport.toFloat(),
                    busPaint
                )
                /** 画文字 */
                val textBaseLine: Float = rect.centerY() + textDistance
                canvas.drawText(ecu.title, rect.centerX().toFloat(), textBaseLine, textPaint)
            }
        }
    }

    private fun drawExt(canvas: Canvas) {
        rawData?.connect?.forEach {
            val ecuUnit = ecuMap[it.pos]
            it.dest.forEachIndexed { index, linkBusid ->
                ecuUnit?.apply {
                    val lineColor = colorMap[linkBusid]

                    val upon = busId < linkBusid
                    val horOffset = if (upon) -index * linkPointOffset else index * linkPointOffset
                    val sx = x + width
                    val sy: Float = y + height / 2

                    /** 位于总线下方时要翻转 */
                    val horRevel = if (upon) 0.0f else horOffset * 2

                    canvas.drawLine(
                        sx,
                        sy - horOffset + horRevel,
                        sx + (widthPer - ecuWidth) / 2 + horOffset,
                        sy - horOffset + horRevel,
                        busPaint.apply { color = Color.parseColor(lineColor) })

                    busMap[linkBusid]?.apply {
                        canvas.drawLine(
                            sx + (widthPer - ecuWidth) / 2 + horOffset,
                            sy - horOffset + horRevel,
                            sx + (widthPer - ecuWidth) / 2 + horOffset,
                            endY,
                            busPaint
                        )
                    }
                }


            }
        }
    }

    private fun findBusNameBy(bus: BussetBean): String {
        return busNameMap[bus.bustype].toString()
    }

    private fun findEcuUintByBusId(bus: BussetBean, positionY: Float): List<EcuUnit> {
        val ecuUnits = mutableListOf<EcuUnit>()
        var index = 1
        var sx = 0.0f
        var sy = 0.0f
        rawData?.ecuset?.forEachIndexed {ecuIndex,it->
            if (it.bustype == bus.bustype) {
                val above = index % 2 == 1
                val currWidth = max(ecuWidth, textPaint.measureText(it.ecuname) + textPadding * 2)
                sx = (index % 2 + index / 2) * widthPer + (widthPer - currWidth) / 2+widthPer/2
                sy = if (above) {
                    positionY - standLineHeight - ecuHeight
                } else {
                    positionY + standLineHeight
                }
                EcuUnit(
                    sx,
                    sy,
                        currWidth,
                    ecuHeight,
                    Color.parseColor("#333333"),
                    it.ecuname,
                    it.pos,
                    bus.bustype,
                    above,
                    bus.busname,
                        "0",ecuIndex
                ).apply {
                    ecuUnits.add(this)
                    ecuMap[position] = this
                    ecunameMap[title]= this
                }
                index++
            }
        }
        return ecuUnits
    }

    /** 放大 */
    fun zoomIn(scale: Float) {
        if(this.destScale>=2f) return
        this.destScale += scale
        invalidate()
    }

    /** 缩小  */
    fun zoomOut(scale: Float) {
        if(this.destScale<=0.4f) return
        this.destScale -= scale
        invalidate()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {


        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                this.dragStartx = event.x - this.distanceH
                this.dragStarty = event.y - this.distanceV

                currentTimeDown = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                moving=true
                destMovex = event.x - this.dragStartx
                destMovey = event.y - this.dragStarty
                if (Math.abs(destMovex) > 10 || Math.abs(destMovey.toInt()) > 10) {
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                moving = false
                this.distanceH = destMovex
                this.distanceV = destMovey
                val moveDuration = System.currentTimeMillis() - currentTimeDown
                rawPointx = (event.x - distanceH) / destScale
                rawPointy = (event.y - distanceV) / destScale
                Log.i("ken  ", "x=:${event.x}  rawx=${rawPointx}  y=${event.y} rawy=${rawPointy}")
                if (moveDuration > 200) {

                    if( scanning){
                        this._canvas?.let { startScan(it) }
                    }
                    return true
                } else {
                    Thread {
                        val ecuUnit = findEcyByPoint(rawPointx, rawPointy,event.x,event.y)
                        ecuUnit?.apply {
                            listener?.invoke(this)
                        }
                    }.start()
                }
                return true
            }
            else -> Log.i("ken", "onTouchEvent: ")

        }
        return super.onTouchEvent(event)
    }

    private fun startScan(canvas: Canvas) {

        if(!scanning || moving) return

        ecuScan?.apply {
            sideTop+=SCAN_STEP
            if(sideTop>=y+height-2){
                sideTop=y+3
            }
            val sx=(x-4.0f)
            val yy:Float =(sideTop)
            val ex:Float=(x+width+4.0f)
            canvas.drawLine(sx,yy,ex,yy,scanPaint)
                postInvalidateDelayed(ANIMATION_DELAY)



        }

    }



    private fun findEcyByPoint(px: Float, py: Float,rawx:Float=0.0f,rawy:Float=0.0f ): EcuUnit? {
        var result: EcuUnit? = null
        data_ecubuss.forEach  top@{
            it.ecus.forEach { ecu ->
//                Log.i("ken", "findEcyByPoint: px= ${px} x[ ${ecu.x} - ${ecu.x+ecu.width} ] py=${py} y [ ${ecu.y} - ${ecu.y+ecu.height} ]  contains x:${(ecu.x<px)and (px<ecu.x+ecu.width)} y:${(ecu.y<py)and (py<ecu.y+ecu.height)} title=${ecu.title}")
                if ((px in ecu.x..ecu.x + ecu.width) and (py in ecu.y..ecu.y + ecu.height)) {
                    Log.i("ken", "findEcyByPoint: ------=  raw [${px} : ${py} ] range: [ ${ecu.x} - ${ecu.x+ecu.width} ]  y [ ${ecu.y} - ${ecu.y+ecu.height} ]  contains x:${(ecu.x<px)and (px<ecu.x+ecu.width)} y:${(ecu.y<py)and (py<ecu.y+ecu.height)} title=${ecu.title} show [ ${rawx} : ${rawy} ] distance:[${distanceH} :${distanceV} ]")
                    result=ecu
                    return@top
                }
            }
        }
        return result
    }
/** 重置所有布局变量 */
    fun reset() {
        data_ecubuss.clear()
        rawData=null
        loadFlag=false
        this.destMovex = 0.0f
        this.destMovey = 0.0f
        this.destScale = 1.0f
        this.distanceH=0.0f
        this.distanceV=0.0f
        this.scanning=false
    /** 要改成移动,否则扫描动画不消失 */
        this.moving=true
    postInvalidate()
    }

    /** 刷新数据 */
    fun reload(data:BaicBean?){
        reset()
        data?.apply {
            rawData=this
            loadFlag=true
            scanning=false
            /** 要改成移动,否则扫描动画不消失 */
            moving=true
            postInvalidate()
        }
    }

    /** 添加点击回调 */
    fun setOnSelectListener(listener: onClick){
        this.listener=listener;
    }

    /** 更新ecuuinit */
    fun update(ecu:EcuUnit){
        val findEcyByPoint = findEcyByPoint(ecu.x, ecu.y)
        findEcyByPoint?.apply {
            color=ecu.color
        }
        postInvalidate()
    }

    fun updateByName(name:String,color:Int){
        val ecuUnit = ecunameMap[name]
        ecuUnit?.apply {
            this.color=color
            invalidate()
        }
    }
    fun updateEcu(name:String,newState:String){
        moving=false
        val ecuUnit = ecunameMap[name]
        ecuUnit?.apply {
            scanning=false

            color= when(newState){
                "0"-> Color.parseColor("#333333")
                "2"-> Color.parseColor("#34C759")
                "3"-> Color.parseColor("#FF4338")
                "4"-> Color.parseColor("#B8B8B8")
                else -> Color.BLACK.apply {
                        ecuScan=ecuUnit
                        sideTop=y+3
                        scanning=true
                }
            }
                state=newState
                ecuScan=this
                invalidate()

        }
    }
}
