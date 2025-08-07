package com.example.sudoku.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.sudoku.R
import com.example.sudoku.generation.*
import kotlin.math.min

class BoardView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    /* Expuestas por SudokuActivity */
    var boardSize = 9
    var cages    : List<KillerSudokuGenerator.Cage>?    = null
    var thermos  : List<ThermoSudokuGenerator.Thermo>?  = null
    var arrows   : List<ArrowSudokuGenerator.Arrow>?    = null
    var whispers : List<GermanWhispersGenerator.Whisper>? = null

    /* Pinceles */
    private val thin  = Paint().apply { strokeWidth=1f;  style=Paint.Style.STROKE; color=Color.GRAY }
    private val thick = Paint().apply { strokeWidth=3f;  style=Paint.Style.STROKE; color=Color.BLACK }
    private val cagePaint = Paint(thin).apply { color=Color.DKGRAY }

    override fun onDraw(c:Canvas) {
        super.onDraw(c)
        val w = width  / boardSize.toFloat()
        val h = height / boardSize.toFloat()

        /* Fondo alterno */
        val even = Color.parseColor("#FFFDFDFD")
        val odd  = Color.parseColor("#FFF7F7F7")
        val box  = if (boardSize==16) 4 else 3
        val bg = Paint().apply{style=Paint.Style.FILL}
        for(r in 0 until boardSize)for(col in 0 until boardSize){
            bg.color = if(((r/box)+(col/box))%2==0) even else odd
            c.drawRect(col*w, r*h, (col+1)*w, (r+1)*h, bg)
        }

        /* Cuadrícula */
        val step = if(boardSize==16)4 else 3
        for(i in 0..boardSize){
            val p = if(i%step==0) thick else thin
            c.drawLine(i*w,0f,i*w,height.toFloat(),p)
            c.drawLine(0f,i*h,width.toFloat(),i*h,p)
        }

        drawKillerCages(c,w,h)
        drawThermos(c,w,h)
        drawArrows(c,w,h)
        drawWhispers(c,w,h)
    }

    /* ---------- extras ---------- */
    private fun drawKillerCages(c:Canvas,w:Float,h:Float){
        cages?.forEach { cage ->
            val l=cage.cells.minOf{it.second}; val t=cage.cells.minOf{it.first}
            val r=cage.cells.maxOf{it.second}+1; val b=cage.cells.maxOf{it.first}+1
            c.drawRect(l*w,t*h,r*w,b*h,cagePaint)
            cagePaint.textSize=min(w,h)*0.23f
            c.drawText("${cage.sum}", l*w+3, t*h+cagePaint.textSize, cagePaint)
        }
    }
    private fun drawThermos(c:Canvas,w:Float,h:Float){
        val light=Color.parseColor("#E3F2FD")
        val dark =Color.parseColor("#90CAF9")
        thermos?.forEach { th->
            th.cells.forEachIndexed { i,(r,col)->
                val f=if(th.cells.size>1) i/(th.cells.size-1f) else 0f
                val col=blend(light,dark,f)
                val p=Paint().apply{color=col;style=Paint.Style.FILL}
                c.drawRect(col*w,r*h,(col+1)*w,(r+1)*h,p)
            }
        }
    }
    private fun drawArrows(c:Canvas,w:Float,h:Float){
        val p=Paint().apply { color=Color.parseColor("#FFA726"); strokeWidth=6f }
        arrows?.forEach { a->
            val (sr,sc)=a.stem
            val (tr,tc)=a.cells.first()
            c.drawLine((sc+0.5f)*w,(sr+0.5f)*h,(tc+0.5f)*w,(tr+0.5f)*h,p)
        }
    }
    private fun drawWhispers(c:Canvas,w:Float,h:Float){
        val p=Paint().apply { color=Color.parseColor("#4CAF50"); strokeWidth=6f }
        whispers?.forEach { wisp->
            for(i in 0 until wisp.cells.size-1){
                val (r1,c1)=wisp.cells[i]; val (r2,c2)=wisp.cells[i+1]
                c.drawLine((c1+0.5f)*w,(r1+0.5f)*h,(c2+0.5f)*w,(r2+0.5f)*h,p)
            }
        }
    }
    private fun blend(a:Int,b:Int,f:Float)=Color.rgb(
        (Color.red(a)+(Color.red(b)-Color.red(a))*f).toInt(),
        (Color.green(a)+(Color.green(b)-Color.green(a))*f).toInt(),
        (Color.blue(a)+(Color.blue(b)-Color.blue(a))*f).toInt())
}
