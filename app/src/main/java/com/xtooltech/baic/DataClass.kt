package com.xtooltech.baic

data class DataClass(val baic: List<BaicBean>)
  
data class BaicBean(
  val vehname: String,
val gwsupport: Int,
val buscount: Int,
val ecucount: Int,
val busset: List<BussetBean>,
val ecuset: List<EcusetBean>,
val connect: List<ConnectBean>)
    
data class BussetBean(val busname: String,val bustype: Int)
    
data class EcusetBean(val bustype: Int,val ecuname: String,val pos: String)
    
data class ConnectBean(val pos: String,val dest: Int)