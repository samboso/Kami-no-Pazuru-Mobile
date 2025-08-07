package com.example.sudoku

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.sudoku.data.UserPrefs
import kotlinx.coroutines.launch

class ProfileActivity : BaseDrawerActivity() {

    private lateinit var imgAvatar: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var etName   : EditText
    private lateinit var spTheme  : Spinner
    private lateinit var tvStats  : TextView
    private lateinit var badgesLay: LinearLayout
    private var pickedUri : Uri? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){ uri ->
        uri?.let {
            pickedUri = it
            imgAvatar.load(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initDrawer(R.id.toolbar_profile)

        imgAvatar  = findViewById(R.id.img_avatar)
        etName     = findViewById(R.id.et_name)
        spTheme    = findViewById(R.id.sp_theme)
        tvStats    = findViewById(R.id.tv_stats)
        badgesLay  = findViewById(R.id.layout_badges)

        /* Tema spinner */
        val themes = arrayOf("Claro","Oscuro","Alto contraste")
        val values = arrayOf("light","dark","hc")
        spTheme.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,themes)

        /* Cargar datos */
        lifecycleScope.launch {
            UserPrefs.profile(this@ProfileActivity).collect{ p ->
                etName.setText(p.name)
                if (pickedUri==null) imgAvatar.load(p.avatar)
                spTheme.setSelection(values.indexOf(p.theme))
            }
        }
        lifecycleScope.launch {
            UserPrefs.stats(this@ProfileActivity).collect{ s ->
                val mins = s.totalTime/60000
                tvStats.text = "${s.solved} sudokus • $mins min totales"
            }
        }
        lifecycleScope.launch {
            UserPrefs.achievements(this@ProfileActivity).collect { set ->
                badgesLay.removeAllViews()
                set.forEach { id ->
                    ImageView(this@ProfileActivity).apply {
                        setImageResource(badgeRes(id))
                        layoutParams = LinearLayout.LayoutParams(64,64).apply { marginEnd=8 }
                        badgesLay.addView(this)
                    }
                }
            }
        }

        /* Botones */
        findViewById<Button>(R.id.btn_change_pic).setOnClickListener {
            pickImage.launch("image/*")
        }
        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val name = etName.text.toString().ifBlank{"Jugador"}
            val themeVal = values[spTheme.selectedItemPosition]
            lifecycleScope.launch {
                UserPrefs.saveProfile(this@ProfileActivity,name,pickedUri?.toString(),themeVal)
            }
            if (themeVal != UserPrefs.profile(applicationContext).value.theme) {
                recreate()                       // aplica tema al vuelo
            } else finish()
        }
        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            lifecycleScope.launch {
                UserPrefs.resetStats(this@ProfileActivity)
            }
            Toast.makeText(this,"Estadísticas reiniciadas",Toast.LENGTH_SHORT).show()
        }
    }
    private fun badgeRes(id:String)= when {
        id.startsWith("bronze")   -> R.drawable.ic_badge_bronze
        id.startsWith("silver")   -> R.drawable.ic_badge_silver
        id.startsWith("gold")     -> R.drawable.ic_badge_gold
        else                      -> R.drawable.ic_badge_platinum
    }
}
