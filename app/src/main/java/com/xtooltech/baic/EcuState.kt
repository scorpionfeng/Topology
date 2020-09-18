package com.xtooltech.baic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.PixelFormat.OPAQUE
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class EcuState : View {


    private var mheight: Int=0
    private var mWidth: Int=0
    private  var _canvas: Canvas?=null
    private val ANIMATION_DELAY: Long=300L
    private var sideTop: Float=0.0f
    private val SCAN_STEP=5.0f

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

    /** 当前缩放scale */
    private var destScale: Float = 1.0f
    private lateinit var data_ecubuss: MutableList<EcuBus>

    /** 连接线映射
     * id->名称
     * */
    private lateinit var busNameMap: Map<Int, String>

    /** ecu映射
     * pos->obj
     * */
    private var ecuMap = mutableMapOf<String, EcuUnit>()

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
    private val widthPer: Float = 150.0f

    /** 单个ecu宽度(默认) */
    private val ecuWidth: Float = 100.0f

    /** 单个ecu高度(默认) */
    private val ecuHeight: Float = 50.0f

    /** 站立线高度(默认) */
    private val standLineHeight: Float = 10.0f

    /** ecu左右的空白距离 */
    val ecuMargin: Float = 20.0f

    /** 连接点偏移量 */
    private val linkPointOffset: Float = 5.0F

    private var scanning=false;


    /** can线画笔 */
    private var busPaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE

    }
    /** scan线画笔 */
    private var scanPaint = Paint().apply {
        color = Color.BLUE
        alpha= OPAQUE
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
        strokeWidth = 5.0f
        textSize = 20.0f
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    /** 获取 画笔的盒子模型 */
    val fontMeterics = textPaint.getFontMetrics()
    val textDistance = (fontMeterics.bottom - fontMeterics.top) / 2 - fontMeterics.bottom


    private lateinit var busSet: List<BussetBean>

    private lateinit var ecuSet: List<EcusetBean>

    private lateinit var connectSet: List<ConnectBean>

    private lateinit var rawData: BaicBean


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
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.EcuState, defStyle, 0
        )

        a.recycle()


        mockData()

        initData()

    }

    private fun mockData() {

        busSet = listOf(
            BussetBean("Gw", 0),
            BussetBean("EVBUS", 1),
            BussetBean("CBUS", 2),
            BussetBean("BodyBUS", 3),
            BussetBean("IBUS1", 4),
            BussetBean("IBUS2", 5),
            BussetBean("TBUS", 6),
            BussetBean("ADASBUS", 7),
            BussetBean("Thermal CAN", 8)
        )


        ecuSet = listOf(
            EcusetBean(0, "GW", "0-1"),
            EcusetBean(1, "BMS", "1-1"),
            EcusetBean(1, "CCU", "1-2"),
            EcusetBean(1, "ECC", "1-3"),
            EcusetBean(1, "MCUF", "1-4"),
            EcusetBean(1, "MCUR", "1-5"),
            EcusetBean(1, "PDU", "1-6"),
            EcusetBean(1, "VCU", "1-7"),
            EcusetBean(2, "SDM", "2-1"),
            EcusetBean(2, "ESP", "2-2"),
            EcusetBean(2, "EPS", "2-3"),
            EcusetBean(2, "PCU", "2-4"),
            EcusetBean(2, "IBOOSTER", "2-5"),
            EcusetBean(3, "BCM", "3-1"),
            EcusetBean(3, "ADB", "3-2"),
            EcusetBean(3, "CIM", "3-3"),
            EcusetBean(3, "OHC", "3-4"),
            EcusetBean(3, "PEPS", "3-5"),
            EcusetBean(3, "PKC", "3-6"),
            EcusetBean(3, "DSMC", "3-7"),
            EcusetBean(3, "PSM", "3-8"),
            EcusetBean(3, "PASC", "3-9"),
            EcusetBean(3, "PLGM", "3-10"),
            EcusetBean(3, "TPMS", "3-11"),
            EcusetBean(3, "VSP", "3-12"),
            EcusetBean(3, "RAC", "3-13"),
            EcusetBean(4, "PWC", "4-1"),
            EcusetBean(4, "AMP", "4-2"),
            EcusetBean(4, "PAS", "4-3"),
            EcusetBean(4, "MFS", "4-4"),
            EcusetBean(4, "ICC", "4-5"),
            EcusetBean(5, "HUD", "5-1"),
            EcusetBean(6, "TBOX", "6-1"),
            EcusetBean(7, "ADAS", "7-1"),
            EcusetBean(7, "AVAP", "7-2"),
            EcusetBean(7, "MPC", "7-3"),
            EcusetBean(7, "MRR", "7-4"),
            EcusetBean(7, "CMRR FL", "7-5"),
            EcusetBean(7, "CMRR FR", "7-6"),
            EcusetBean(7, "CMRR RL", "7-7"),
            EcusetBean(7, "CMRR RR", "7-8"),
            EcusetBean(8, "WTC_H", "8-1"),
            EcusetBean(8, "WTC_B", "8-2"),
            EcusetBean(8, "EAS", "8-3")
        )
        connectSet = listOf(
            ConnectBean("1-3", listOf(8, 7, 4)),
            ConnectBean("1-7", listOf(2)),
            ConnectBean("4-5", listOf(5)),
            ConnectBean("3-2", listOf(1, 2))
        )
    }


    private fun initData() {

        rawData = BaicBean(
            vehname = "N60",
            gwsupport = 1,
            buscount = 8,
            ecucount = 44,
            busset = busSet,
            ecuset = ecuSet,
            connect = connectSet
        )
        /** 创建布局对象 */
        /** 0.canbus 字典化 */
        busNameMap = rawData.busset.associateBy({ it.bustype }, { it.busname })
        linkMap = rawData.connect.associateBy({ it.pos }, { it.dest })
        /** 1.创建总线 */
        data_ecubuss = mutableListOf<EcuBus>()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

            if(_canvas==null){
                _canvas=canvas
            }

            Log.i("ken","255:  = ");
            canvas.save()
            canvas.scale(destScale, destScale)
            canvas.translate(destMovex, destMovey)

        if (mheight==0) {
            mheight=height
            val contentHeight = mheight - paddingTop - paddingBottom
            val contentHeightShow = contentHeight * .80f
            //列宽度
            val heightPer = contentHeightShow / rawData.buscount

            rawData.busset.forEachIndexed { index, it ->
                EcuBus(
                    0.0f,
                    index * heightPer,
                    width.toFloat(),
                    index * heightPer,
                    Color.parseColor(colorMap[index]),
                    it.bustype,
                    findBusNameBy(it),
                    findEcuUintByBusId(it, index * heightPer)
                ).apply {
                    data_ecubuss.add(this)
                    busMap[busId] = this
                }
            }
        }



            /** 2.创建单元 */
            drawEcus(canvas)
            /** 3.创建扩展线条 */
            drawExt(canvas)

            scanStart(canvas)


            canvas.restore()

    }

    private fun drawEcus(canvas: Canvas) {
        data_ecubuss.forEach {
            /** 画主线 */
            canvas.drawLine(
                it.startX,
                it.startY,
                it.endX,
                it.endY,
                busPaint.apply { color = it.color })
            /** 画主线下面的ECU单元 */
            it.ecus.forEach { ecu ->
                val rect = RectF().apply {
                    left = ecu.x
                    top = ecu.y
                    right = ecu.x + ecu.width
                    bottom = ecu.y + ecu.height
                };
                canvas.drawRect(rect, ecuPaint)
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
        rawData.connect.forEach {
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
        rawData.ecuset.forEach {
            if (it.bustype == bus.bustype) {
                val above = index % 2 == 1

                sx = (index % 2 + index / 2) * widthPer + (widthPer - ecuWidth) / 2
                sy = if (above) {
                    positionY - standLineHeight - ecuHeight
                } else {
                    positionY + standLineHeight
                }
                EcuUnit(
                    sx,
                    sy,
                    ecuWidth,
                    ecuHeight,
                    Color.BLACK,
                    it.ecuname,
                    it.pos,
                    bus.bustype,
                    above,
                    bus.busname
                ).apply {
                    ecuUnits.add(this)
                    ecuMap[position] = this
                }
                index++
            }
        }
        return ecuUnits
    }

    /** 放大 */
    fun zoomIn(scale: Float) {
        if(this.destScale>=5f) return
        this.destScale += scale
        invalidate()
    }

    /** 缩小  */
    fun zoomOut(scale: Float) {
        if(this.destScale<=0.2f) return
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
                if ((moveDuration > 200) and moving) {

                    if( scanning){
                        this._canvas?.let { scanStart(it) }
                    }
                    return true
                } else {

                    Thread {

                        val ecuUnit = findEcyByPoint(rawPointx, rawPointy,event.x,event.y)
                        ecuUnit?.apply {
                            ecuScan=this
                            scanning=true
                            sideTop=y+10
                            postInvalidate(x.toInt(),y.toInt(),(x+width).toInt(),(y+height).toInt())
                            Log.i("ken", "388:  = " + this.title);
                        }
                    }.start()
                }
                return true
            }
            else -> Log.i("ken", "onTouchEvent: ")

        }
        return super.onTouchEvent(event)
    }

    private fun scanStart(canvas: Canvas) {
        ecuScan?.apply {
            sideTop+=SCAN_STEP
            if(sideTop>=y+height){
                sideTop=y+6
            }
            Thread{

                val rl = (x + 10.0f + distanceH) * destScale
                val rt = (sideTop + distanceV).toFloat() * destScale
                val rr = (x + width - 5.0f + distanceH) * destScale
                val rb = (sideTop - 6.0f + distanceV) * destScale
                canvas.drawOval(
                    rl,rt,rr,rb,
                    scanPaint.apply {
                        shader = RadialGradient(
                            x + width / 2,
                            y + height / 2,
                            Math.min(x + width / 2, y + height / 2),
                            intArrayOf(
                                if (moving) Color.TRANSPARENT else Color.GREEN,
                                Color.TRANSPARENT
                            ),
                            null,
                            Shader.TileMode.MIRROR
                        )
                    })
                if(!moving) {
                    postInvalidateDelayed(ANIMATION_DELAY)
                }

            }.start()


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
        this.destMovex = 0.0f
        this.destMovey = 0.0f
        this.destScale = 1.0f
        this.distanceH=0.0f
        this.distanceV=0.0f
        invalidate()
    }


}
