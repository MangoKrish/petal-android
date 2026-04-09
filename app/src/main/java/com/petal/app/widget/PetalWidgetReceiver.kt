package com.petal.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import com.petal.app.data.local.PetalDatabase
import com.petal.app.data.model.CyclePhase
import com.petal.app.domain.CycleCalculator
import com.petal.app.domain.DailyInsightsEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * BroadcastReceiver for the Petal home screen widget.
 * Handles widget creation, updates, and background data refresh.
 * Registered in AndroidManifest.xml with the APPWIDGET_UPDATE action.
 */
class PetalWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = PetalWidget()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        refreshWidgetData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Handle custom refresh action from WorkManager or alarm
        if (intent.action == ACTION_REFRESH_WIDGET) {
            refreshWidgetData(context)
        }
    }

    /**
     * Fetches latest cycle data from the local Room database and updates
     * the widget's GlanceStateDefinition (DataStore-backed preferences).
     * This runs on a background coroutine scope so it does not block the main thread.
     */
    private fun refreshWidgetData(context: Context) {
        coroutineScope.launch {
            try {
                val database = PetalDatabase.getInstance(context)
                val cycleEntryDao = database.cycleEntryDao()
                val calculator = CycleCalculator()
                val insightsEngine = DailyInsightsEngine()

                // Retrieve the current user's ID from shared preferences
                val prefs = context.getSharedPreferences("petal_auth", Context.MODE_PRIVATE)
                val userId = prefs.getString("current_user_id", null) ?: return@launch
                val userName = prefs.getString("current_user_name", "there") ?: "there"

                val entries = cycleEntryDao.getEntriesForUserOnce(userId)
                val cycles = entries.map { it.toCycleLog() }

                val cycleDay = calculator.getCurrentCycleDay(cycles)
                val cycleLength = calculator.getAverageCycleLength(cycles)
                val phase = calculator.getCurrentPhase(cycles)
                val nextPeriod = calculator.getNextPeriodDate(cycles)
                val daysUntilPeriod = calculator.getDaysUntil(nextPeriod)
                val insights = insightsEngine.getDailyInsights(cycleDay, cycleLength, userName)

                val tipCard = insights.cards.firstOrNull()

                // Compute weekly forecast
                val weeklyPhases = mutableMapOf<Int, CyclePhase>()
                val weeklyEnergies = mutableMapOf<Int, Int>()
                val weeklyTips = mutableMapOf<Int, String>()

                for (offset in 0..6) {
                    val futureDate = LocalDate.now().plusDays(offset.toLong())
                    val futureCycleDay = calculator.getCurrentCycleDay(cycles, futureDate)
                    val futurePhase = calculatePhaseForDay(futureCycleDay, cycleLength)
                    weeklyPhases[offset] = futurePhase
                    weeklyEnergies[offset] = energyForPhase(futurePhase)

                    val dayInsights = insightsEngine.getDailyInsights(futureCycleDay, cycleLength, userName)
                    weeklyTips[offset] = dayInsights.cards.firstOrNull()?.tip ?: ""
                }

                // Update all widget instances
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(PetalWidget::class.java)

                for (glanceId in glanceIds) {
                    updateAppWidgetState(context, glanceId) { mutablePrefs ->
                        mutablePrefs[PetalWidget.KEY_CYCLE_DAY] = cycleDay
                        mutablePrefs[PetalWidget.KEY_CYCLE_LENGTH] = cycleLength
                        mutablePrefs[PetalWidget.KEY_PHASE] = phase.display
                        mutablePrefs[PetalWidget.KEY_TIP_HEADLINE] = tipCard?.headline ?: ""
                        mutablePrefs[PetalWidget.KEY_TIP_BODY] = tipCard?.tip ?: ""
                        mutablePrefs[PetalWidget.KEY_DAYS_UNTIL_PERIOD] = daysUntilPeriod
                        mutablePrefs[PetalWidget.KEY_USER_NAME] = userName
                        mutablePrefs[PetalWidget.KEY_HORMONE_NOTE] = insights.hormoneNote
                        mutablePrefs[PetalWidget.KEY_LAST_UPDATED] = LocalDate.now().toString()

                        // Weekly data
                        for (offset in 0..6) {
                            mutablePrefs[PetalWidget.weekDayPhaseKey(offset)] =
                                weeklyPhases[offset]?.display ?: CyclePhase.Follicular.display
                            mutablePrefs[PetalWidget.weekDayEnergyKey(offset)] =
                                weeklyEnergies[offset] ?: 2
                            mutablePrefs[PetalWidget.weekDayTipKey(offset)] =
                                weeklyTips[offset] ?: ""
                        }
                    }

                    glanceAppWidget.update(context, glanceId)
                }
            } catch (e: Exception) {
                // Widget update failed silently -- data will refresh on next cycle
                android.util.Log.w("PetalWidget", "Widget refresh failed", e)
            }
        }
    }

    private fun calculatePhaseForDay(dayInCycle: Int, cycleLength: Int): CyclePhase {
        val follicularEnd = maxOf(6, (cycleLength * 0.46).toInt())
        val ovulationEnd = minOf(cycleLength, follicularEnd + 2)
        return when {
            dayInCycle <= 5 -> CyclePhase.Menstrual
            dayInCycle <= follicularEnd -> CyclePhase.Follicular
            dayInCycle <= ovulationEnd -> CyclePhase.Ovulation
            else -> CyclePhase.Luteal
        }
    }

    private fun energyForPhase(phase: CyclePhase): Int = when (phase) {
        CyclePhase.Menstrual -> 1
        CyclePhase.Follicular -> 2
        CyclePhase.Ovulation -> 3
        CyclePhase.Luteal -> 1
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.petal.app.action.REFRESH_WIDGET"
    }
}
