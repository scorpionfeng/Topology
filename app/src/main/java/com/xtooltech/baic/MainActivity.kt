package com.xtooltech.baic

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private var count: Int=0;
    private var data: BaicBean?=null
    private  var baics :Baics?=null
    lateinit var  ecuState:EcuState
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ecuState=findViewById(R.id.ecustate)
        convertBaicData{
            data=findDataByname("C32BF05")
            ecuState.reload(data)
            ecustate.setOnSelectListener{
                Log.i("ken", "selected : ${it}")
                it.color= Color.RED
                ecustate.update(it)
            }
        }

    }

    fun findDataByname(name: String):BaicBean?{
        var filter = baics?.baic?.filter { it.vehname.equals(name) }
        return filter?.first()
    }

    private fun convertBaicData(entity: (Baics) -> Unit){
        Thread {
            val input: InputStream
            try {
                input = resources.assets.open("ecudata.json")
                val data: String? = convertToString(input)
                data?.apply {
                    baics = Gson().fromJson(data, Baics::class.java)
                    baics?.apply{ entity.invoke(this) }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (ej: JsonSyntaxException) {
                ej.printStackTrace()
            }
        }.start()
    }

    private fun convertToString(`is`: InputStream): String? {
        var s: String? = null
        try {
            //格式转换
            val scanner = Scanner(`is`, "UTF-8").useDelimiter("\\A")
            if (scanner.hasNext()) {
                s = scanner.next()
            }
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return s
    }

    fun click_reset(view: View) {
        ecustate.reset()
    }
    fun click_zout(view: View) {
        ecuState.zoomOut(0.2F)
    }
    fun click_zin(view: View) {
        ecuState.zoomIn(0.2F)
    }



    private fun mockData2(): BaicBean {

        val busSet = listOf(
            BussetBean("PCAN", 1),
            BussetBean("BCAN", 2)
        )


        val ecuSet = listOf(
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
        val connectSet = listOf(
            ConnectBean("1-3", listOf(8, 7, 4)),
            ConnectBean("1-7", listOf(2)),
            ConnectBean("4-5", listOf(5)),
            ConnectBean("3-2", listOf(1, 2))
        )

        return BaicBean(
            vehname = "N60",
            gwsupport = 1,
            buscount = 8,
            ecucount = 44,
            busset = busSet,
            ecuset = ecuSet,
            connect = connectSet
        )
    }
    private fun mockData(): BaicBean {

        val busSet = listOf(
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


        val ecuSet = listOf(
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
        val connectSet = listOf(
            ConnectBean("1-3", listOf(8, 7, 4)),
            ConnectBean("1-7", listOf(2)),
            ConnectBean("4-5", listOf(5)),
            ConnectBean("3-2", listOf(1, 2))
        )

        return BaicBean(
            vehname = "N60",
            gwsupport = 1,
            buscount = 8,
            ecucount = 44,
            busset = busSet,
            ecuset = ecuSet,
            connect = connectSet
        )
    }


    fun click_reload(view: View) {
         data = mockData()
        ecuState.reload(data)
    }

    fun click_update(view: View) {
        ecuState.updateByName("VCU", Color.CYAN)

    }

    fun click_scan(view: View) {
        ecustate.updateEcu(if(count%2==0)"EMS_V" else "EPS", if(count%2==0)"1" else "2")
        count++;
    }
}