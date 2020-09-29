package com.xtooltech.baic


data class EcuBus(var startX:Float, val startY:Float,var endX:Float,val endY:Float,val color:Int,val busId: Int,val busName: String,var ecus:List<EcuUnit>)

data class EcuUnit(var x:Float,var y:Float,var width:Float,var height:Float,var color:Int,val title:String,val position:String,val busId:Int,val above:Boolean ,val busName:String,var state:String,var index:Int)

data class  Point(val x:Float,val y:Float)

fun EcuUnit.anchorTop():Point{
    return Point(this.x+this.width/2,this.y)
}
fun EcuUnit.anchorBottom():Point{
    return Point(this.x+this.width/2,this.y+this.height)
}
fun EcuUnit.anchorLeft():Point{
    return Point(this.x,this.y+this.height/2)
}

fun EcuUnit.anchorRight():Point{
    return Point(this.x+this.width,this.y+this.height/2)
}

fun EcuUnit.isTouchInSide(point:Point):Boolean{
    return point.x>=this.x && point.x<=this.x+this.width && point.y>=this.y && point.y<=this.y+this.height
}