package kamel.capstone.bootstrapnode.controller;

import kamel.capstone.bootstrapnode.data.model.Stats;
import kamel.capstone.bootstrapnode.service.BootstrapNodeService;
import kamel.capstone.bootstrapnode.util.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {
    private final BootstrapNodeService service;
    private final RestTemplate template;

    public HomeController(
            @Qualifier("default") BootstrapNodeService service,
            RestTemplate template
    ) {
        this.service = service;
        this.template = template;
    }

    @GetMapping("/stats")
    public String getStats(Model model) {
        List<Stats> stats = new LinkedList<>();
        service.getNodes().forEach(node -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", Constants.PRIVATE_KEY);
            HttpEntity<String> httpEntity = new HttpEntity<>("", headers);
            ResponseEntity<Stats> nodeStats = template.exchange(
                    node.getNodeAddress() + "/bootstrap/stats",
                    HttpMethod.GET,
                    httpEntity,
                    Stats.class
            );
            stats.add(nodeStats.getBody());
        });
        model.addAttribute("stats", stats);
        return "stats_page";
    }
}
