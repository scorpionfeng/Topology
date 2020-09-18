package com.xtooltech.baic


data class EcuBus(val startX:Float, val startY:Float,val endX:Float,val endY:Float,val color:Int,val busId: Int,val busName: String,var ecus:List<EcuUnit>)

data class EcuUnit(val x:Float,val y:Float,val width:Float,val height:Float,var color:Int,val title:String,val position:String,val busId:Int,val above:Boolean ,val busName:String)

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