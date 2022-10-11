package name.hennr.cctrayhub.cctray.controller

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import reactor.core.publisher.Mono

@Controller
class CctrayGenericController(val cctrayService: CctrayService) {

    @ResponseBody
    @GetMapping(value = ["/cctray/{githubGroup}/{githubRepo}/{githubWorkflowNameOrId}"],
        produces = [MediaType.APPLICATION_XML_VALUE])
    fun cctray(
        @PathVariable githubGroup: String,
        @PathVariable githubRepo: String,
        @PathVariable githubWorkflowNameOrId: String
    ): Mono<String> {
        return cctrayService.getLatestWorkflowRun(githubGroup, githubRepo, githubWorkflowNameOrId)
    }

}
