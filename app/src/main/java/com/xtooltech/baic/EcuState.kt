package com.xtooltech.baic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * TODO: document your custom view class.
 */
class EcuState : View {


    private lateinit var data_ecubuss: MutableList<EcuBus>
    /** 连接线映射
     * id->名称
     * */
    private lateinit var busNameMap: Map<Int, String>
    /** ecu映射
     * pos->obj
     * */
    private  var ecuMap= mutableMapOf<String,EcuUnit>()
    /** 总线映射
     * busid->obj
     * */
    private var busMap= mutableMapOf<Int,EcuBus>()
    /** 扩展线映射 */
    private lateinit var linkMap:Map<String,List<Int>>
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
    val widthPer:Float=150.0f
    /** 单个ecu宽度(默认) */
    val ecuWidth:Float=100.0f
    /** 单个ecu高度(默认) */
    val ecuHeight:Float=50.0f
    /** 站立线高度(默认) */
    val standLineHeight:Float=10.0f
    /** ecu左右的空白距离 */
    val ecuMargin:Float=20.0f



    /** can线画笔 */
    private var busPaint=Paint().apply {
        color=Color.RED
        isAntiAlias=true
        isDither=true
        style=Paint.Style.STROKE

    }


    /** ecu形状画笔 */
    private var ecuPaint=Paint().apply {
        color=Color.BLACK
        isAntiAlias=true
        isDither=true
        style=Paint.Style.FILL

    }

    var textPaint=Paint().apply {
        style=Paint.Style.FILL
        strokeWidth=5.0f
        textSize=10.0f
        textAlign=Paint.Align.CENTER
        color=Color.WHITE
    }
    /** 获取 画笔的盒子模型 */
    val fontMeterics=textPaint.getFontMetrics()
    val textDistance=(fontMeterics.bottom-fontMeterics.top)/2-fontMeterics.bottom


    private lateinit var busSet: List<BussetBean>

    private  lateinit var ecuSet:List<EcusetBean>

    private lateinit var connectSet:List<ConnectBean>

    private lateinit var rawData:BaicBean



    var defaultBusDuration:Int=100;


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

        rawData=BaicBean(
            vehname = "N60",
            gwsupport = 1,
            buscount = 8,
            ecucount = 44,
            busset = busSet,
            ecuset = ecuSet,
            connect = connectSet
        )

    }

    private fun mockData() {

         busSet= listOf(
            BussetBean("Gw",0),
            BussetBean("EVBUS",1),
            BussetBean("CBUS",2),
            BussetBean("BodyBUS",3),
            BussetBean("IBUS1",4),
            BussetBean("IBUS2",5),
            BussetBean("TBUS",6),
            BussetBean("ADASBUS",7),
            BussetBean("Thermal CAN",8)
        )


         ecuSet= listOf(
            EcusetBean(0,"GW","0-1"),
            EcusetBean(1,"BMS","1-1"),
            EcusetBean(1,"CCU","1-2"),
            EcusetBean(1,"ECC","1-3"),
            EcusetBean(1,"MCUF","1-4"),
            EcusetBean(1,"MCUR","1-5"),
            EcusetBean(1,"PDU","1-6"),
            EcusetBean(1,"VCU","1-7"),
            EcusetBean(2,"SDM","2-1"),
            EcusetBean(2,"ESP","2-2"),
            EcusetBean(2,"EPS","2-3"),
            EcusetBean(2,"PCU","2-4"),
            EcusetBean(2,"IBOOSTER","2-5"),
            EcusetBean(3,"BCM","3-1"),
            EcusetBean(3,"ADB","3-2"),
            EcusetBean(3,"CIM","3-3"),
            EcusetBean(3,"OHC","3-4"),
            EcusetBean(3,"PEPS","3-5"),
            EcusetBean(3,"PKC","3-6"),
            EcusetBean(3,"DSMC","3-7"),
            EcusetBean(3,"PSM","3-8"),
            EcusetBean(3,"PASC","3-9"),
            EcusetBean(3,"PLGM","3-10"),
            EcusetBean(3,"TPMS","3-11"),
            EcusetBean(3,"VSP","3-12"),
            EcusetBean(3,"RAC","3-13"),
            EcusetBean(4,"PWC","4-1"),
            EcusetBean(4,"AMP","4-2"),
            EcusetBean(4,"PAS","4-3"),
            EcusetBean(4,"MFS","4-4"),
            EcusetBean(4,"ICC","4-5"),
            EcusetBean(5,"HUD","5-1"),
            EcusetBean(6,"TBOX","6-1"),
            EcusetBean(7,"ADAS","7-1"),
            EcusetBean(7,"AVAP","7-2"),
            EcusetBean(7,"MPC","7-3"),
            EcusetBean(7,"MRR","7-4"),
            EcusetBean(7,"CMRR FL","7-5"),
            EcusetBean(7,"CMRR FR","7-6"),
            EcusetBean(7,"CMRR RL","7-7"),
            EcusetBean(7,"CMRR RR","7-8"),
            EcusetBean(8,"WTC_H","8-1"),
            EcusetBean(8,"WTC_B","8-2"),
            EcusetBean(8,"EAS","8-3")
        )
         connectSet= listOf(
            ConnectBean("1-3", listOf(8,7)),
            ConnectBean("1-7", listOf(2)),
            ConnectBean("4-5", listOf(5))
        )



    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        val contentHeightShow=contentHeight*.80f
        //列宽度
        val heightPer=contentHeightShow/rawData.buscount



        /** 创建布局对象 */

        /** 0.canbus 字典化 */


        busNameMap=rawData.busset.associateBy({it.bustype},{it.busname})

        linkMap=rawData.connect.associateBy({it.pos},{it.dest})

        /** 1.创建总线 */
         data_ecubuss= mutableListOf<EcuBus>()

        rawData.busset.forEachIndexed{index, it ->
            EcuBus(0.0f,index*heightPer,width.toFloat(),index*heightPer,Color.parseColor(colorMap[index]),it.bustype,findBusNameBy(it),findEcuUintByBusId(it,index*heightPer)).apply {
                data_ecubuss.add(this)
                busMap[busId] = this
            }
        }

        /** 2.创建单元 */
        drawEcus(canvas)
        /** 3.创建扩展线条 */
        drawExt(canvas)

    }

    private fun drawEcus(canvas:Canvas){
        data_ecubuss.forEach {
            /** 画主线 */
            canvas.drawLine(it.startX,it.startY,it.endX,it.endY,busPaint.apply { color=it.color })
            /** 画主线下面的ECU单元 */
            it.ecus.forEach{ ecu ->
                val rect=Rect().apply {
                    left=ecu.x.toInt()
                    top=ecu.y.toInt()
                    right=(ecu.x+ecu.width).toInt()
                    bottom=(ecu.y+ecu.height).toInt()
                };
                canvas.drawRect(rect,ecuPaint)
                /** 画支柱线 */
                val endYSupport=if(ecu.above) rect.bottom else rect.top
                canvas.drawLine(rect.centerX().toFloat(),it.startY,rect.centerX().toFloat(),endYSupport.toFloat(),busPaint)
                /** 画文字 */
                val textBaseLine:Float=rect.centerY()+textDistance
                canvas.drawText(ecu.title,rect.centerX().toFloat(),textBaseLine,textPaint)
            }
        }
    }

    private fun drawExt(canvas: Canvas) {
        rawData.connect.forEach {
            val ecuUnit=ecuMap[it.pos]
            it.dest.forEachIndexed{
                index,linkBusid->
                ecuUnit?.apply {
                    val lineColor=colorMap[linkBusid]
                    val sx=x+width
                    val sy:Float=y+height/2
                    canvas.drawLine(sx,sy,sx+(widthPer-ecuWidth)/2,sy,busPaint.apply { color=Color.parseColor(lineColor) })
                    busMap[linkBusid]?.apply {
                        canvas.drawLine(sx+(widthPer-ecuWidth)/2,sy,sx+(widthPer-ecuWidth)/2,endY,busPaint)
                    }
                }


            }
        }
    }

    private fun findBusNameBy(bus: BussetBean): String {
        return busNameMap[bus.bustype].toString()
    }

    private fun findEcuUintByBusId(bus: BussetBean,positionY:Float): List<EcuUnit> {
        val ecuUnits= mutableListOf<EcuUnit>()
        var index=1
        var sx=0.0f
        var sy=0.0f
        rawData.ecuset.forEach {
            if(it.bustype==bus.bustype){
                val above = index % 2 == 1

                sx=(index%2+index/2)*widthPer+ (widthPer-ecuWidth)/2
                sy = if(above){
                    positionY-standLineHeight-ecuHeight
                }else{
                    positionY+standLineHeight
                }
                EcuUnit(sx,sy,ecuWidth,ecuHeight,Color.BLACK,it.ecuname,it.pos,bus.bustype,above,bus.busname).apply {
                    ecuUnits.add(this)
                    ecuMap[position] = this
                }
                index++
            }
        }
        return ecuUnits
    }


}
