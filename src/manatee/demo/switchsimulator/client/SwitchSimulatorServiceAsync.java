package manatee.demo.switchsimulator.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import manatee.demo.switchsimulator.shared.SwitchConfiguration;


public interface SwitchSimulatorServiceAsync
{
    void getConfiguration(AsyncCallback<SwitchConfiguration> callback);


    void connect(int inputPort, int outputPort, AsyncCallback<SwitchConfiguration> callback);


    void disconnect(int inputPort, AsyncCallback<SwitchConfiguration> callback);


    void disconnectAll(AsyncCallback<SwitchConfiguration> callback);
}
