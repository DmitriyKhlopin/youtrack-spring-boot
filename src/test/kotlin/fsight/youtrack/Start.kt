package fsight.youtrack


/*

object TestLauncher {
    var listener = SummaryGeneratingListener()
@Test
    fun main(args: Array<String>) { //@formatter:off
        val request: LauncherDiscoveryRequest = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass("fsight.youtrack.DatabaseTest"))
                */
/*.configurationParameter("junit.conditions.deactivate", "com.baeldung.extensions.*")*//*

                */
/*.configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")*//*

                .build()
        //@formatter:on
        val plan: TestPlan = LauncherFactory.create()
                .discover(request)
        val launcher: Launcher = LauncherFactory.create()
        val summaryGeneratingListener = SummaryGeneratingListener()
        launcher.execute(request, listener)
        launcher.execute(request)
        summaryGeneratingListener.getSummary()
                .printTo(PrintWriter(System.out))
    }
}*/
