package com.example.sudoku.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("user_prefs")

object UserPrefs {

    /* ---------- claves ---------- */
    private val NAME         = stringPreferencesKey("name")
    private val AVATAR       = stringPreferencesKey("avatar_uri")
    private val THEME        = stringPreferencesKey("theme")           // light | dark | hc
    private val SOLVED       = intPreferencesKey("solved_total")
    private val TOTAL_TIME   = longPreferencesKey("total_time")
    private val VARIANT_SOLVED = stringPreferencesKey("variant_solved")
    private val BEST_CLASSIC = longPreferencesKey("best_classic")
    private val ACHIEVEMENTS = stringSetPreferencesKey("achievements")
    private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    private val HISTORY       = stringPreferencesKey("history_json")   // máx 5
    private val DAILY_AVG     = stringPreferencesKey("daily_avg")      // yyyy-MM-dd:ms

    /* ---------- modelos ---------- */
    data class Profile(val name:String, val avatar:String?, val theme:String)
    data class Stats(val solved:Int, val total:Long,
                     val byVariant:Map<String,Int>, val bestClassic:Long)

    /* ---------- flujos ---------- */
    fun profile(ctx:Context): Flow<Profile> =
        ctx.ds.data.map {
            Profile(
                it[NAME] ?: "Jugador",
                it[AVATAR],
                it[THEME] ?: "light"
            )
        }

    data class SavedGame(val puzzle:String, val elapsed:Long, val date:Long)

    fun history(ctx:Context): Flow<List<SavedGame>> =
        ctx.ds.data.map { parseGames(it[HISTORY]) }

    suspend fun saveGame(ctx:Context, game:SavedGame){
        ctx.ds.edit {
            val list = (parseGames(it[HISTORY]) + game).takeLast(5)
            it[HISTORY] = gamesToString(list)
        }
    }

    suspend fun addDailyTime(ctx:Context, elapsed:Long){
        val today = java.time.LocalDate.now().toString()
        ctx.ds.edit {
            val map = parseMapLong(it[DAILY_AVG]).toMutableMap()
            val pair = map[today] ?: (0L to 0)
            map[today] = pair.first + elapsed to pair.second + 1
            it[DAILY_AVG] = mapToStringLong(map)
        }
    }

    fun dailyAverages(ctx:Context): Flow<Map<String,Long>> =
        ctx.ds.data.map { parseMapLong(it[DAILY_AVG]).mapValues{ it.value.first/it.value.second } }

    /* helpers (añádelos al final del objeto) */
    private fun gamesToString(l:List<SavedGame>) =
        l.joinToString("|"){ "${it.puzzle},${it.elapsed},${it.date}" }
    private fun parseGames(s:String?) = s?.split('|')?.filter{it.contains(',')}?.map{
        val (p,e,d)=it.split(','); SavedGame(p,e.toLong(),d.toLong())
    } ?: emptyList()
    private fun parseMapLong(s:String?) = s?.split(';')?.associate{
        val(k,v)=it.split(':'); k to v.toLong()
    } ?: emptyMap()
    private fun mapToStringLong(m:Map<String,Pair<Long,Int>>) =
        m.entries.joinToString(";"){ "${it.key}:${it.value.first},${it.value.second}" }

    fun stats(ctx:Context): Flow<Stats> =
        ctx.ds.data.map {
            Stats(
                it[SOLVED] ?: 0,
                it[TOTAL_TIME] ?: 0L,
                parseMap(it[VARIANT_SOLVED]),
                it[BEST_CLASSIC] ?: 0L
            )
        }

    fun achievements(ctx:Context): Flow<Set<String>> =
        ctx.ds.data.map { it[ACHIEVEMENTS] ?: emptySet() }

    fun onboardingDone(ctx:Context): Flow<Boolean> =
        ctx.ds.data.map { it[ONBOARDING_DONE] ?: false }

    /* ---------- operaciones ---------- */
    suspend fun saveProfile(ctx:Context,name:String,avatar:String?,theme:String){
        ctx.ds.edit {
            it[NAME] = name
            if (avatar!=null) it[AVATAR]=avatar else it.remove(AVATAR)
            it[THEME]=theme
        }
    }

    suspend fun recordSolve(ctx:Context, variant:String, elapsed:Long){
        ctx.ds.edit {
            it[SOLVED]=(it[SOLVED]?:0)+1
            it[TOTAL_TIME]=(it[TOTAL_TIME]?:0L)+elapsed
            val map=parseMap(it[VARIANT_SOLVED]); val new=(map[variant]?:0)+1
            it[VARIANT_SOLVED]=mapToString(map+(variant to new))
            if (variant=="classic"){
                val best=it[BEST_CLASSIC]?:0L
                if (best==0L||elapsed<best) it[BEST_CLASSIC]=elapsed
            }
            val set=(it[ACHIEVEMENTS]?: emptySet()).toMutableSet()
            when(new){10->set.add("bronze_$variant")
                25->set.add("silver_$variant")
                50->set.add("gold_$variant")
                100->set.add("platinum_$variant")}
            it[ACHIEVEMENTS]=set
        }
    }

    suspend fun resetStats(ctx:Context)= ctx.ds.edit {
        it.remove(SOLVED);it.remove(TOTAL_TIME)
        it.remove(VARIANT_SOLVED);it.remove(BEST_CLASSIC);it.remove(ACHIEVEMENTS)
    }

    suspend fun setOnboardingDone(ctx:Context)= ctx.ds.edit { it[ONBOARDING_DONE]=true }

    /* ---------- helpers ---------- */
    private fun parseMap(s:String?)= s?.split(';')?.associate{
        val(k,v)=it.split(':');k to v.toInt()
    } ?: emptyMap()
    private fun mapToString(m:Map<String,Int>)=m.entries.joinToString(";"){ "${it.key}:${it.value}"}
}
