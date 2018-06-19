package cz.jkremlacek.ib053.controllers;

import cz.jkremlacek.ib053.CrossroadManager;
import cz.jkremlacek.ib053.models.Crossroad;
import cz.jkremlacek.ib053.models.CrossroadCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @author Jakub Kremláček
 */
@RestController
@RequestMapping ("/crossroad")
public class CrossroadController {

    @RequestMapping (method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map> getCrossroad() {
        return ResponseEntity.ok(CrossroadManager.getInstance().getCurrentCrossroadState());
    }

    @RequestMapping (method = RequestMethod.POST)
    @ResponseBody
    public Response requestChange(
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "number", required = false) Integer number
    ) {
        if ((state != null && number != null) || (state == null && number == null)) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Invalid type of command! Use either tram state change or crossing number for pedestrian request.")
                    .build();
        }

        CrossroadCommand cmd;

        if (state != null) {
            Crossroad.CrossroadState stateEnum;

            try {
                stateEnum = Crossroad.CrossroadState.valueOf(state.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("State invalid value: " + state + " use one of: " + Crossroad.CrossroadState.values()).build();
            }

            cmd = new CrossroadCommand(stateEnum);
        } else {
            cmd = new CrossroadCommand(number);
        }

        //non-blocking variant - not used due to MS Azure thread limit
        //new Thread(() -> CrossroadManager.getInstance().addCommand(cmd)).start();

        CrossroadManager.getInstance().addCommand(cmd);
        return Response.ok().build();
    }
}
