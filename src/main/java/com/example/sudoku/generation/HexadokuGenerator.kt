package com.example.sudoku.generation
import com.example.sudoku.model.SudokuBoard
import kotlin.random.Random

object HexadokuGenerator {
    private const val SIZE = 16
    private const val BOX  = 4
    fun generate(diff:Int): SudokuBoard {
        val g = Array(SIZE){IntArray(SIZE)}
        fill(g)
        val sol = Array(SIZE){ g[it].clone() }
        val remove = when(diff){0->50;1->80;2->120 else->140}
        var removed=0
        while(removed<remove){
            val r=Random.nextInt(SIZE); val c=Random.nextInt(SIZE)
            if(g[r][c]!=0){ g[r][c]=0; removed++ }
        }
        return SudokuBoard(g,sol)
    }
    private fun fill(g:Array<IntArray>):Boolean{
        for(r in 0 until SIZE) for(c in 0 until SIZE) if(g[r][c]==0){
            (1..SIZE).shuffled().forEach{v->
                if(valid(g,r,c,v)){ g[r][c]=v; if(fill(g)) return true; g[r][c]=0 }
            }; return false
        }; return true
    }
    private fun valid(g:Array<IntArray>,r:Int,c:Int,v:Int):Boolean{
        for(i in 0 until SIZE) if(g[r][i]==v||g[i][c]==v) return false
        val br=r/BOX*BOX; val bc=c/BOX*BOX
        for(i in 0 until BOX) for(j in 0 until BOX)
            if(g[br+i][bc+j]==v) return false
        return true
    }
}
