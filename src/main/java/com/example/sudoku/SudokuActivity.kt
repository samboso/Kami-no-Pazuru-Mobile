package com.example.sudoku

import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.sudoku.data.UserPrefs
import com.example.sudoku.generation.*
import com.example.sudoku.model.SudokuBoard
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Pantalla principal de juego: muestra el tablero, controla teclados,
 * temporizador, pista, reinicio y menús.  Soporta todas las variantes
 * (Clásico, Killer, Thermo, Arrow, Whispers, Hexadoku 16×16, Fractal,
 * Cruz 4D y Daily offline).
 */
class SudokuActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    /* ---------- A)  UI ---------- */
    private lateinit var drawer: DrawerLayout
    private lateinit var nav   : NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var variantSpin: Spinner
    private lateinit var diffSpin   : Spinner
    private lateinit var hintBtn    : Button
    private lateinit var resetBtn   : Button
    private lateinit var exportBtn  : Button
    private lateinit var timerTv    : TextView
    private lateinit var keypad     : LinearLayout
    private lateinit var boardView  : BoardView
    private lateinit var gridLayout : GridLayout

    /* ---------- B)  Timer ---------- */
    private var startTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            updateTimerDisplay()
            handler.postDelayed(this, 1_000)
        }
    }

    /* ---------- C)  Estado de juego ---------- */
    private lateinit var board: SudokuBoard
    private lateinit var initialPuzzle: Array<IntArray>
    private lateinit var cellViews    : Array<Array<TextView?>>
    private lateinit var cellFrames   : Array<Array<FrameLayout?>>
    private var selR = -1
    private var selC = -1

    /* ---------- D)  Opciones ---------- */
    private var currentVariant   = "classic"
    private var currentDifficulty= 1
    private var hintCount        = 3
    private var showErrors       = true

    /* ---------- E)  Elementos decorativos ---------- */
    private var cages   : List<KillerSudokuGenerator.Cage>?   = null
    private var thermos : List<ThermoSudokuGenerator.Thermo>? = null
    private var arrows  : List<ArrowSudokuGenerator.Arrow>?   = null
    private var whispers: List<GermanWhispersGenerator.Whisper>? = null
    private val killerCells = mutableSetOf<Pair<Int,Int>>()
    private val thermoColors = mutableMapOf<Pair<Int,Int>,Int>()

    /* ----------------------------- onCreate ----------------------------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* -- Extras y prefs -- */
        currentVariant    = intent.getStringExtra("variant") ?: "classic"
        currentDifficulty = intent.getIntExtra("difficulty", 1)
        PreferenceManager.getDefaultSharedPreferences(this).also {
            showErrors = it.getBoolean("show_errors", true)
            hintCount  = it.getString("hint_count", "3")!!.toInt()
        }

        buildUi()
        generateBoard()
    }

    /* ----------------------------- UI builder --------------------------- */
    private fun buildUi() {
        drawer = DrawerLayout(this)
        setContentView(drawer)

        /* NavigationView */
        nav = NavigationView(this).apply {
            inflateMenu(R.menu.drawer_menu)
            setNavigationItemSelectedListener(this@SudokuActivity)
        }
        drawer.addView(nav, DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.WRAP_CONTENT,
            DrawerLayout.LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.START
        })

        /* Contenedor principal */
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        drawer.addView(content)

        /* Toolbar */
        toolbar = Toolbar(this).apply {
            title = getString(R.string.title_activity_sudoku)
            setBackgroundColor(ContextCompat.getColor(this@SudokuActivity, R.color.purple_500))
            setTitleTextColor(Color.WHITE)
        }
        content.addView(toolbar)
        setSupportActionBar(toolbar)
        ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.app_name, R.string.app_name).also {
            drawer.addDrawerListener(it); it.syncState()
        }

        /* Fila de controles */
        val ctrl = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(8,8,8,8)
        }
        // ▸ Variante
        ctrl.addView(TextView(this).apply { text = getString(R.string.variant_label) })
        variantSpin = Spinner(this).also { ctrl.addView(it) }
        // ▸ Dificultad
        ctrl.addView(TextView(this).apply {
            text = getString(R.string.difficulty_label); setPadding(16,0,0,0)
        })
        diffSpin = Spinner(this).also { ctrl.addView(it) }
        // ▸ Botones
        hintBtn  = Button(this).also { ctrl.addView(it) }
        resetBtn = Button(this).apply { text = getString(R.string.reset_all_label) }.also { ctrl.addView(it) }
        exportBtn= Button(this).apply { text = getString(R.string.export_label) }.also { ctrl.addView(it) }
        // ▸ Timer
        timerTv = TextView(this).apply { setPadding(16,0,0,0) }.also { ctrl.addView(it) }
        content.addView(ctrl)

        /* Spinners */
        resources.getStringArray(R.array.variants_entries).let { entries ->
            variantSpin.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, entries)
        }
        resources.getStringArray(R.array.variants_values).let { values ->
            variantSpin.setSelection(values.indexOf(currentVariant))
            variantSpin.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0:AdapterView<*>?,v:View?,pos:Int,id:Long){
                    currentVariant = values[pos]; generateBoard()
                }
                override fun onNothingSelected(p0:AdapterView<*>?) {}
            }
        }
        resources.getStringArray(R.array.difficulty_entries).let { entries ->
            diffSpin.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, entries)
        }
        resources.getStringArray(R.array.difficulty_values).let { values ->
            diffSpin.setSelection(values.indexOf(currentDifficulty.toString()))
            diffSpin.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0:AdapterView<*>?,v:View?,pos:Int,id:Long){
                    currentDifficulty = values[pos].toInt(); generateBoard()
                }
                override fun onNothingSelected(p0:AdapterView<*>?) {}
            }
        }

        /* BoardView + GridLayout dentro de Scroll */
        val sv = ScrollView(this); val sh = HorizontalScrollView(this)
        boardView  = BoardView(this)
        gridLayout = GridLayout(this)
        FrameLayout(this).apply {
            addView(boardView); addView(gridLayout)
            sh.addView(this)
        }
        sv.addView(sh)
        content.addView(sv,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1f))

        /* Keypad */
        keypad = LinearLayout(this).apply { gravity = Gravity.CENTER }
        content.addView(keypad)

        /* Click listeners */
        hintBtn.setOnClickListener { giveHint() }
        resetBtn.setOnClickListener { resetBoard() }
        exportBtn.setOnClickListener { exportCsv() }
    }

    /* ------------------------  GENERATE BOARD --------------------------- */
    private fun generateBoard() {

        fun clearExtras() { cages=null; thermos=null; arrows=null; whispers=null }

        when(currentVariant){
            "fractal"  -> { clearExtras(); board = SudokuFractalGenerator.generate(currentDifficulty,2) }
            "cross"    -> { clearExtras(); board = CrossSudoku4DGenerator.generate(currentDifficulty) }
            "thermo"   -> { clearExtras(); ThermoSudokuGenerator.generate(currentDifficulty).also{ board=it.board; thermos=it.thermos } }
            "killer"   -> { clearExtras(); KillerSudokuGenerator.generate(currentDifficulty).also{ board=it.board; cages=it.cages } }
            "arrow"    -> { clearExtras(); ArrowSudokuGenerator.generate(currentDifficulty).also{ board=it.board; arrows=it.arrows } }
            "whispers" -> { clearExtras(); GermanWhispersGenerator.generate(currentDifficulty).also{ board=it.board; whispers=it.whispers } }
            "hexadoku" -> { clearExtras(); board = HexadokuGenerator.generate(currentDifficulty) }
            "daily"    -> {
                clearExtras()
                val ent = runBlocking { DailyPuzzleManager.getToday() }
                board = SudokuBoard(
                    ent.puzzle.lines().map{it.split(',').map(String::toInt).toIntArray()}.toTypedArray(),
                    ent.solution.lines().map{it.split(',').map(String::toInt).toIntArray()}.toTypedArray()
                )
            }
            else       -> { clearExtras(); val (p,s,_) = SudokuGenerator.generate(currentDifficulty); board = SudokuBoard(p,s) }
        }

        initialPuzzle = Array(board.rows){ board.puzzle[it].clone() }
        computeThermoColours()
        killerCells.clear(); cages?.forEach{ it.cells.forEach(killerCells::add) }

        boardView.apply {
            boardSize = board.rows
            this.cages    = cages
            this.thermos  = thermos
            this.arrows   = arrows
            this.whispers = whispers
            invalidate()
        }

        renderGrid()
        buildKeypad()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        hintCount = prefs.getString("hint_count", "3")!!.toInt()
        hintBtn.text = getString(R.string.hint_label, hintCount)
        hintBtn.isEnabled = hintCount > 0
        selR=-1; selC=-1;

        if (::board.isInitialized) {
            lifecycleScope.launch {
                val encoded = board.encode()            // implementa encode() → String CSV
                UserPrefs.saveGame(this@SudokuActivity,
                    UserPrefs.SavedGame(encoded, 0L, System.currentTimeMillis()))
            }
        }

        startTimer()
    }

    /* ----------------------  GRID & KEYPAD ------------------------------ */
    private fun renderGrid() {
        gridLayout.removeAllViews()
        gridLayout.rowCount = board.rows; gridLayout.columnCount = board.cols
        cellViews  = Array(board.rows){ arrayOfNulls<TextView>(board.cols) }
        cellFrames = Array(board.rows){ arrayOfNulls<FrameLayout>(board.cols) }

        val sumMap = cages?.associateBy(
            keySelector = { c -> c.cells.minWith(compareBy<Pair<Int,Int>>{it.first}.thenBy{it.second}) },
            valueTransform = { it.sum }
        ) ?: emptyMap()

        val dp = resources.displayMetrics
        val cellPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            if(board.rows==16)30f else 40f, dp).toInt()

        for(r in 0 until board.rows) for(c in 0 until board.cols){
            val cont = FrameLayout(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellPx; height = cellPx
                    rowSpec = GridLayout.spec(r); columnSpec = GridLayout.spec(c)
                }
            }
            val lblSum  = TextView(this).apply { textSize=8f }
            val lblMain = TextView(this).apply { textSize=16f; gravity=Gravity.CENTER }
            cont.addView(lblSum); cont.addView(lblMain)

            // base color
            val base = if(!board.isActiveCell(r,c))
                R.color.inactive_cell_background
            else thermoColors[r to c] ?: R.color.cell_background
            val stroke = if(killerCells.contains(r to c))2 else 1
            cont.background = GradientDrawable().apply{
                setColor(ContextCompat.getColor(this@SudokuActivity, base)); setStroke(stroke,Color.BLACK)
            }

            // número inicial
            val v = board.puzzle[r][c]
            if(v!=0){
                lblMain.text = board.symbol(v)
                lblMain.setTextColor(ContextCompat.getColor(this, R.color.cell_fixed_text))
            }

            // suma killer
            sumMap[r to c]?.let { s ->
                lblSum.text = s.toString(); lblSum.visibility = View.VISIBLE
            }

            // click = seleccionar
            cont.setOnClickListener {
                if(board.isFixedCell(r,c)) return@setOnClickListener
                selR=r; selC=c; updateHighlight()
            }

            gridLayout.addView(cont)
            cellViews[r][c]=lblMain; cellFrames[r][c]=cont
        }
        updateHighlight()
    }

    private fun buildKeypad() {
        keypad.removeAllViews()
        val symbols = if(board.rows==16)
            resources.getStringArray(R.array.hex_symbols)
        else (1..9).map(Int::toString).toTypedArray()

        symbols.forEach { sym ->
            Button(this).apply {
                text = sym
                layoutParams = LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,1f).apply{
                    marginStart=4; marginEnd=4
                }
                setOnClickListener {
                    val value = if(board.rows==16) "0123456789ABCDEF".indexOf(sym) else sym.toInt()
                    onNumberSelected(value)
                }
                keypad.addView(this)
            }
        }
        // Botón borrar-todo
        Button(this).apply{
            text = getString(R.string.reset_all_label)
            setOnClickListener { resetBoard() }
            keypad.addView(this)
        }
    }

    /* ----------------------  INPUT & LOGIC ------------------------------ */
    private fun onNumberSelected(value:Int){
        if(selR<0||selC<0) return
        if(board.isFixedCell(selR,selC)) return
        val tv = cellViews[selR][selC] ?: return
        val cur = board.puzzle[selR][selC]
        if(value==cur){ board.puzzle[selR][selC]=0; tv.text=""; return }
        board.puzzle[selR][selC] = value
        tv.text = board.symbol(value)
        val ok  = board.solution[selR][selC]==value
        val color = when{
            ok -> R.color.cell_correct_text
            showErrors -> R.color.cell_wrong_text
            else -> R.color.cell_user_text
        }
        tv.setTextColor(ContextCompat.getColor(this,color))
        checkSolved()
    }

    private fun giveHint() {
        if(hintCount<=0) return
        outer@ for(r in 0 until board.rows) for(c in 0 until board.cols)
            if(board.isActiveCell(r,c)&& board.puzzle[r][c]!=board.solution[r][c]){
                board.puzzle[r][c]=board.solution[r][c]
                cellViews[r][c]?.apply{
                    text=board.symbol(board.solution[r][c])
                    setTextColor(ContextCompat.getColor(this@SudokuActivity,R.color.cell_user_text))
                }
                hintCount--; hintBtn.text=getString(R.string.hint_label,hintCount)
                if(hintCount==0) hintBtn.isEnabled=false
                break@outer
            }
        checkSolved()
    }

    private fun resetBoard(){
        for(r in 0 until board.rows) board.puzzle[r]=initialPuzzle[r].clone()
        renderGrid(); selR=-1; selC=-1
        PreferenceManager.getDefaultSharedPreferences(this).let {
            hintCount=it.getString("hint_count","3")!!.toInt()
        }
        hintBtn.text=getString(R.string.hint_label,hintCount); hintBtn.isEnabled=hintCount>0
        startTimer()
    }

    private fun updateHighlight(){
        for(r in 0 until board.rows) for(c in 0 until board.cols){
            val frame = cellFrames[r][c] ?: continue
            val base = if(!board.isActiveCell(r,c))
                R.color.inactive_cell_background
            else thermoColors[r to c] ?: R.color.cell_background
            val color = when{
                r==selR && c==selC -> R.color.selected_cell_background
                (r==selR || c==selC) && selR>=0 -> R.color.row_col_highlight
                else -> base
            }
            val stroke = if(killerCells.contains(r to c))2 else 1
            frame.background = GradientDrawable().apply{
                setColor(ContextCompat.getColor(this@SudokuActivity,color))
                setStroke(stroke,Color.BLACK)
            }
        }
    }

    private fun checkSolved(){
        for(r in 0 until board.rows) for(c in 0 until board.cols)
            if(board.isActiveCell(r,c) && board.puzzle[r][c]!=board.solution[r][c]) return
        handler.removeCallbacks(tick)
        lifecycleScope.launch {
            UserPrefs.addDailyTime(this@SudokuActivity, elapsed)
        }
        val time = System.currentTimeMillis()-startTime
        val mins = time/60000; val secs=(time/1000)%60
        Toast.makeText(this,"¡Resuelto en %d:%02d!".format(mins,secs),Toast.LENGTH_LONG).show()
    }

    /* ----------------------------- TIMER ------------------------------- */
    private fun startTimer(){
        startTime = System.currentTimeMillis()
        handler.removeCallbacks(tick)
        timerTv.visibility = View.VISIBLE
        timerTv.text = "00:00"
        handler.post(tick)
    }
    private fun updateTimerDisplay(){
        val t = System.currentTimeMillis()-startTime
        val m = (t/1000)/60; val s = (t/1000)%60
        timerTv.text="%02d:%02d".format(m,s)
    }

    /* -------------------- EXPORT CSV ---------------------- */
    private fun exportCsv(){
        val csv = buildString{
            for(r in board.puzzle) append(r.joinToString(","),"\n")
        }
        Intent(Intent.ACTION_SEND).apply{
            type="text/csv"; putExtra(Intent.EXTRA_TEXT,csv)
        }.also{ startActivity(Intent.createChooser(it,"Compartir CSV")) }
    }

    /* ---------------- Drawer ---------------- */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer.closeDrawer(GravityCompat.START)
        when(item.itemId){
            R.id.nav_new      -> generateBoard()
            R.id.nav_settings -> startActivity(Intent(this,SettingsActivity::class.java))
            R.id.nav_stats    -> startActivity(Intent(this,StatsActivity::class.java))
            R.id.nav_about    -> startActivity(Intent(this,AboutActivity::class.java))
        }
        return true
    }

    /**
     * Precompute a colour for each cell that belongs to a thermometer.
     * We assign a gradient along each thermometer from a pale colour
     * to a darker one so that the direction of increasing values is
     * visually apparent.  If no thermometers exist (for non‑thermo
     * variants) the map is cleared.
     */
    private fun computeThermoColours() {
        thermoColors.clear()
        val thermosList = thermos ?: return
        // Define two RGB colours for gradient start and end
        val startColour = Color.parseColor("#E3F2FD") // very light blue
        val endColour = Color.parseColor("#90CAF9")   // mid blue
        for (thermo in thermosList) {
            val cells = thermo.cells
            val length = cells.size
            for (i in cells.indices) {
                val (r, c) = cells[i]
                // Compute interpolation factor between 0 and 1
                val t = if (length > 1) i.toFloat() / (length - 1).toFloat() else 0f
                val sr = Color.red(startColour)
                val sg = Color.green(startColour)
                val sb = Color.blue(startColour)
                val er = Color.red(endColour)
                val eg = Color.green(endColour)
                val eb = Color.blue(endColour)
                val rr = (sr + t * (er - sr)).toInt()
                val gg = (sg + t * (eg - sg)).toInt()
                val bb = (sb + t * (eb - sb)).toInt()
                val colour = Color.rgb(rr.coerceIn(0, 255), gg.coerceIn(0, 255), bb.coerceIn(0, 255))
                thermoColors[r to c] = colour
            }
        }
    }
}
