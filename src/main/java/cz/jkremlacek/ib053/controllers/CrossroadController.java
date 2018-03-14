package cz.jkremlacek.ib053.controllers;

import cz.jkremlacek.ib053.CrossroadManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Jakub Kremláček
 */
@RestController
@RequestMapping ("/crossroad")
public class CrossroadController {

    @RequestMapping (method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<Map> getCrossroad() {
        return ResponseEntity.ok(CrossroadManager.getInstance().getCurrentCrossroadState());
    }
}
