package manatee.demo.switchsimulator.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import manatee.demo.switchsimulator.shared.SwitchConfiguration;


@RemoteServiceRelativePath("simulatorControl")
public interface SwitchSimulatorService extends RemoteService
{
    SwitchConfiguration getConfiguration();


    SwitchConfiguration connect(int inputPort, int outputPort);


    SwitchConfiguration disconnect(int inputPort);


    SwitchConfiguration disconnectAll();
}
