package manatee.demo.switchsimulator.server;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import manatee.demo.switchsimulator.client.SwitchSimulatorService;
import manatee.demo.switchsimulator.shared.SwitchConfiguration;
import manatee2.prototype.common.logger.shared.LogMessageSeverity;
import manatee2.prototype.common.logger.shared.LogReporter;


//
// TODO: Karl, Your stuff goes in this class.
//

public class SwitchSimulatorServiceImpl extends RemoteServiceServlet implements SwitchSimulatorService, LogReporter
{
    private static final long serialVersionUID = 1L;

    private static final int NUM_INPUT_PORTS = 32;
    private static final int NUM_OUTPUT_PORTS = 64;

    private static Map<Integer, Integer> portMap = new HashMap<Integer, Integer>();


    public SwitchSimulatorServiceImpl()
    {
        //
        // Start with a random initial mapping.
        //
        portMap.put(2, 5);
        portMap.put(12, 15);
        portMap.put(22, 25);
        portMap.put(32, 35);
    }


    @Override
    public SwitchConfiguration getConfiguration()
    {
        return new SwitchConfiguration(NUM_INPUT_PORTS, NUM_OUTPUT_PORTS, portMap);
    }


    @Override
    public SwitchConfiguration connect(int inputPort, int outputPort)
    {
        //
        // Failsafe.
        //
        if (inputPort < 1 || inputPort > NUM_INPUT_PORTS)
        {
            logError("Unable to connect: Invalid Input-Port " + inputPort);
            return getConfiguration();
        }
        if (outputPort < 1 || outputPort > NUM_OUTPUT_PORTS)
        {
            logError("Unable to connect: Invalid Output-Port " + outputPort);
            return getConfiguration();
        }

        logInfo("Connecting: " + inputPort + " to " + outputPort);

        portMap.put(inputPort, outputPort);

        return getConfiguration();
    }


    @Override
    public SwitchConfiguration disconnect(int inputPort)
    {
        //
        // Failsafe.
        //
        if (inputPort < 1 || inputPort > NUM_INPUT_PORTS)
        {
            logError("Unable to disconnect: Invalid Input-Port " + inputPort);
            return getConfiguration();
        }

        logInfo("Disconnecting: " + inputPort + " from " + portMap.get(inputPort));

        portMap.remove(inputPort);

        return getConfiguration();
    }


    @Override
    public SwitchConfiguration disconnectAll()
    {
        logInfo("Disconnecting All");

        portMap.clear();

        return getConfiguration();
    }


    @Override
    public String logReporter()
    {
        return "SwitchSimulatorService";
    }


    //
    // TODO - Remove this override before the final delivery.
    //
    @Override
    public void logMessage(LogMessageSeverity severity, String text)
    {
        System.out.println("Logger:" + logReporter() + ":" + severity + ":" + text);
    }
}
