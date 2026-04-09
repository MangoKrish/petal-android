package com.petal.app.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")
    data object Onboarding : Screen("onboarding")
    data object ImportData : Screen("import_data")
    data object Dashboard : Screen("dashboard")
    data object Calendar : Screen("calendar")
    data object QuickLog : Screen("quick_log?date={date}&entryId={entryId}") {
        const val baseRoute = "quick_log"

        fun createRoute(
            date: String? = null,
            entryId: String? = null
        ): String {
            val params = buildList {
                if (date != null) add("date=$date")
                if (entryId != null) add("entryId=$entryId")
            }

            return if (params.isEmpty()) baseRoute else "$baseRoute?${params.joinToString("&")}"
        }
    }
    data object Partner : Screen("partner")
    data object PartnerSetup : Screen("partner_setup")
    data object Caregiver : Screen("caregiver")
    data object PartnerNotifications : Screen("partner_notifications")
    data object DailyInsights : Screen("daily_insights")
    data object Recommendations : Screen("recommendations")
    data object Charts : Screen("charts")
    data object CycleTrends : Screen("cycle_trends")
    data object Education : Screen("education")
    data object HealthQA : Screen("health_qa")
    data object Article : Screen("article/{articleId}") {
        fun createRoute(articleId: String) = "article/$articleId"
    }
    data object Settings : Screen("settings")
    data object NotificationSettings : Screen("notification_settings")
    data object PrivacySettings : Screen("privacy_settings")
    data object ShareSettings : Screen("share_settings")
    data object DataExport : Screen("data_export")
}
