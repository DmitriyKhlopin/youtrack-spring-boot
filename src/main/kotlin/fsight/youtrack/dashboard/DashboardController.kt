package fsight.youtrack.dashboard

import fsight.youtrack.api.TimeAccountingService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

/*@RestController
class DashboardController(private val service: TimeAccountingService) {
    @GetMapping("/dashboard")
    fun getWorkItemsBad(@RequestParam("token") token: String?) = service.getWorkItemsBad(token)

}*/

@Controller
class DashboardController(private val service: TimeAccountingService) {
    @GetMapping("/dashboard")
    fun getDashboard(@RequestParam("token") token: String?, model: Model): ModelAndView {
        val items = service.getWorkItemsBad(token)
        /*model.addAttribute(" items", items)*/
        return ModelAndView("dashboard", "items", arrayListOf<String>())
    }

    @GetMapping("/wi_today")
    fun getWorkItemsToday(@RequestParam("token") token: String?, model: Model): ModelAndView {
        val items = service.getWorkItemsToday(token)
        println(items.size)
        /*model.addAttribute(" items", items)*/
        return ModelAndView("work_items_today", "items", arrayListOf<String>())
    }

    @GetMapping("/wi_yesterday")
    fun getWorkItemsYesterday(@RequestParam("token") token: String?, model: Model): ModelAndView {
        val items = service.getWorkItemsYesterday(token)
        println(items.size)
        /*model.addAttribute(" items", items)*/
        return ModelAndView("work_items_yesterday", "items", arrayListOf<String>())
    }

    @GetMapping("/wi_bad")
    fun getWorkItemsBad(@RequestParam("token") token: String?, model: Model): ModelAndView {
        val items = service.getWorkItemsBad(token)
        println(items.size)
        /*model.addAttribute(" items", items)*/
        return ModelAndView("work_items_bad", "items", arrayListOf<String>())
    }

}