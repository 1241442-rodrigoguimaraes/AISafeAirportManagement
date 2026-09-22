package eapli.alsafe.remoteaccess;

import eapli.alsafe.remoteaccess.server.requesthandlers.AuthenticationRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.AddAircraftRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.AddPilotRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.CreateFlightRouteRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.DecommissionAircraftRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.GetAirportsRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.ListActiveAircraftRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.ListAircraftModelsRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.ListAircraftRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.ListPilotRosterRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.RemovePilotRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.CancelFlightPlanRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.CreateFlightPlanRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.InsertWeatherDataInFlightRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.ListAvailableWeatherDataRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.ListPilotAirControlAreasRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.ListPilotAircraftRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.ListPilotFlightPlansRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.ListPilotRoutesRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.SubmitFlightPlanRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.pilot.ValidateFlightPlanRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.weatherperson.ConsultWeatherDataRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.weatherperson.ImportBulkWeatherDataRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.weatherperson.ListAirControlAreasRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.weatherperson.RegisterWeatherDataRequestHandler;
import eapli.alsafe.remoteaccess.server.requesthandlers.atcc.DeleteFlightRouteRequestHandler;

import java.util.HashMap;
import java.util.Map;

public final class RemoteAccessHandlerRegistry {

    private RemoteAccessHandlerRegistry() {}

    public static Map<Byte, RemoteAccessRequestHandler> handlers() {
        Map<Byte, RemoteAccessRequestHandler> handlers = new HashMap<>();

        handlers.put((byte) 1, new AuthenticationRequestHandler());
        handlers.put((byte) 2, new ListAircraftModelsRequestHandler());
        handlers.put((byte) 3, new AddAircraftRequestHandler());
        handlers.put((byte) 4, new ListActiveAircraftRequestHandler());
        handlers.put((byte) 5, new DecommissionAircraftRequestHandler());
        handlers.put((byte) 6, new ListAircraftRequestHandler());
        handlers.put((byte) 7, new GetAirportsRequestHandler());
        handlers.put((byte) 8, new CreateFlightRouteRequestHandler());
        handlers.put((byte) 9, new DeleteFlightRouteRequestHandler());
        handlers.put((byte) 10, new AddPilotRequestHandler());
        handlers.put((byte) 11, new ListPilotRosterRequestHandler());
        handlers.put((byte) 12, new RemovePilotRequestHandler());
        handlers.put((byte) 13, new ImportBulkWeatherDataRequestHandler());
        handlers.put((byte) 14, new RegisterWeatherDataRequestHandler());
        handlers.put((byte) 15, new ListAirControlAreasRequestHandler());
        handlers.put((byte) 16, new ConsultWeatherDataRequestHandler());
        handlers.put((byte) 20, new ListPilotRoutesRequestHandler());
        handlers.put((byte) 21, new ListPilotAircraftRequestHandler());
        handlers.put((byte) 22, new CreateFlightPlanRequestHandler());
        handlers.put((byte) 23, new ListPilotFlightPlansRequestHandler());
        handlers.put((byte) 24, new SubmitFlightPlanRequestHandler());
        handlers.put((byte) 25, new CancelFlightPlanRequestHandler());
        handlers.put((byte) 26, new ValidateFlightPlanRequestHandler());
        handlers.put((byte) 28, new ListPilotAirControlAreasRequestHandler());
        handlers.put((byte) 29, new ListAvailableWeatherDataRequestHandler());
        handlers.put((byte) 30, new InsertWeatherDataInFlightRequestHandler());

        return handlers;
    }
}
