package com.example.sudoku
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.sudoku.generation.*

class BoardView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var boardSize = 9
    var cages : List<KillerSudokuGenerator.Cage>? = null
    var thermos: List<ThermoSudokuGenerator.Thermo>? = null
    var arrows : List<ArrowSudokuGenerator.Arrow>? = null
    var whispers: List<GermanWhispersGenerator.Whisper>? = null

    private val thin  = Paint().apply { strokeWidth = 1f ; style = Paint.Style.STROKE }
    private val thick = Paint().apply { strokeWidth = 4f ; style = Paint.Style.STROKE }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width / boardSize.toFloat()
        val h = height / boardSize.toFloat()

        /* 1. Cuadrícula */
        for (i in 0..boardSize) {
            val p = if (i % (if(boardSize==16)4 else 3) == 0) thick else thin
            c.drawLine(i*w,0f,i*w,height.toFloat(),p)
            c.drawLine(0f,i*h,width.toFloat(),i*h,p)
        }

        /* 2. Jaulas Killer */
        cages?.forEach { cage ->
            val l=cage.cells.minOf{it.second}; val t=cage.cells.minOf{it.first}
            val r=cage.cells.maxOf{it.second}+1; val b=cage.cells.maxOf{it.first}+1
            val p=Paint(thin).apply{color=Color.GRAY}
            c.drawRect(l*w,t*h,r*w,b*h,p)
            p.textSize=w*0.25f
            c.drawText("${cage.sum}", l*w+4, t*h+p.textSize, p)
        }

        /* 3. Termómetros */
        thermos?.forEach { th ->
            val light=Color.parseColor("#E3F2FD")
            val dark =Color.parseColor("#90CAF9")
            th.cells.forEachIndexed { idx,(r,co) ->
                val f= if(th.cells.size>1) idx/(th.cells.size-1f) else 0f
                val col=blend(light,dark,f)
                val p=Paint().apply{color=col; style=Paint.Style.FILL}
                c.drawRect(co*w,r*h,co*w+w,r*h+h,p)
            }
        }

        /* 4. Arrows */
        arrows?.forEach { a ->
            val head=a.stem; val tail=a.cells.first()
            val p=Paint().apply{color=Color.parseColor("#FF9800");strokeWidth=6f}
            c.drawLine((head.second+0.5f)*w,(head.first+0.5f)*h,
                (tail.second+0.5f)*w,(tail.first+0.5f)*h,p)
            val tri=Path().apply{
                moveTo((head.second+0.5f)*w,(head.first+0.5f)*h)
                lineTo((head.second+0.25f)*w,(head.first+0.25f)*h)
                lineTo((head.second+0.75f)*w,(head.first+0.25f)*h)
                close()
            }
            c.drawPath(tri,p)
        }

        /* 5. German Whispers */
        whispers?.forEach { wisp ->
            val p=Paint().apply{color=Color.parseColor("#4CAF50");strokeWidth=8f}
            val path=Path()
            wisp.cells.forEachIndexed { i,(r,co)->
                if(i==0) path.moveTo((co+0.5f)*w,(r+0.5f)*h)
                else path.lineTo((co+0.5f)*w,(r+0.5f)*h)
            }
            c.drawPath(path,p)
        }
    }
    private fun blend(a:Int,b:Int,f:Float)=Color.rgb(
        (Color.red(a)+f*(Color.red(b)-Color.red(a))).toInt(),
        (Color.green(a)+f*(Color.green(b)-Color.green(a))).toInt(),
        (Color.blue(a)+f*(Color.blue(b)-Color.blue(a))).toInt()
    )
}
