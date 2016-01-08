package manatee.demo.switchsimulator.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import manatee.demo.switchsimulator.shared.SwitchConfiguration;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SwitchSimulator implements EntryPoint
{
    /**
     * The number of Ingress and Egress ports should be divisible by 8.
     */
    private static final int NUM_COLUMNS = 8;

    /**
     * Interface to the Google Web Toolkit (GWT) Remote Procedure Call (RPC) Service.
     */
    private SwitchSimulatorServiceAsync switchSimulatorService = GWT.create(SwitchSimulatorService.class);

    /**
     * Current Switch Configuration.
     */
    private SwitchConfiguration switchConfiguration = null;

    /**
     * Ingress Buttons.
     */
    private SwitchButton[] inputButtons;

    /**
     * Egress Buttons.
     */
    private SwitchButton[] outputButtons;

    /**
     * Currently-Selected Ingress Button.
     */
    private SwitchButton currentInputButton = null;

    /**
     * Currently-Selected Egress Button.
     */
    private SwitchButton currentOutputButton = null;

    /**
     * Button to connect an Ingress to an Egress.
     */
    private Button connectButton;

    /**
     * Button to disconnect an Ingress from an Egress.
     */
    private Button disconnectButton;

    /**
     * Button to disconnect ALL Ingress from Egress.
     */
    private Button disconnectAllButton;


    /**
     * Entry point method.
     */
    @Override
    public void onModuleLoad()
    {
        //
        // Fetch the initial Switch Configuration.
        //
        switchSimulatorService.getConfiguration(new AsyncCallback<SwitchConfiguration>()
        {
            @Override
            public void onSuccess(SwitchConfiguration result)
            {
                switchConfiguration = result;
                setupScreen();
            }


            @Override
            public void onFailure(Throwable caught)
            {
                System.err.println("Unable to fetch initial Switch Configuration");
                caught.printStackTrace();
            }
        });
    }


    /**
     * Perform initial drawing of the screen.
     */
    private void setupScreen()
    {
        //
        // Establish the overall layout.
        //

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.addStyleName("mainPanel");

        //
        // Create the ingress section.
        //

        Label inputLabel = new Label("Input");
        inputLabel.addStyleName("inputOutputLabel");
        mainPanel.add(inputLabel);

        int numInputButtons = switchConfiguration.getNumInputPorts();
        inputButtons = new SwitchButton[numInputButtons];
        int numInputRows = switchConfiguration.getNumInputPorts() / NUM_COLUMNS;
        Grid inputGrid = new Grid(numInputRows, NUM_COLUMNS);
        for (int row = 0; row < inputGrid.getRowCount(); row++)
        {
            for (int column = 0; column < inputGrid.getColumnCount(); column++)
            {
                int buttonNumber = (row * NUM_COLUMNS) + column + 1;
                final SwitchButton switchButton = new SwitchButton(buttonNumber);
                inputButtons[buttonNumber - 1] = switchButton;
                switchButton.setText("" + buttonNumber);
                switchButton.removeStyleName("gwt-Button switchButton");
                switchButton.setStyleName("switchButton");
                switchButton.addMouseOverHandler(new MouseOverHandler()
                {
                    @Override
                    public void onMouseOver(MouseOverEvent event)
                    {
                        if (switchButton.getPairedPort() != null)
                        {
                            switchButton.addStyleName("switchButtonHighlighted");
                            switchButton.getPairedPort().addStyleName("switchButtonHighlighted");
                        }
                    }
                });
                switchButton.addMouseOutHandler(new MouseOutHandler()
                {
                    @Override
                    public void onMouseOut(MouseOutEvent event)
                    {
                        if (switchButton.getPairedPort() != null)
                        {
                            if (switchButton != currentInputButton)
                            {
                                switchButton.removeStyleName("switchButtonHighlighted");
                            }
                            if (switchButton.getPairedPort() != currentOutputButton)
                            {
                                switchButton.getPairedPort().removeStyleName("switchButtonHighlighted");
                            }
                        }
                    }
                });
                switchButton.addClickHandler(new ClickHandler()
                {
                    @Override
                    public void onClick(ClickEvent event)
                    {
                        //
                        // Handle a De-Select.
                        //
                        if (currentInputButton == switchButton)
                        {
                            currentInputButton = null;
                            if (switchButton.getPairedPort() != null
                                    && switchButton.getPairedPort() == currentOutputButton)
                            {
                                currentOutputButton = null;
                            }
                        }

                        //
                        // Handle a Select.
                        //
                        else
                        {
                            //
                            // Select it.
                            //
                            currentInputButton = switchButton;

                            //
                            // If it is paired, select the mate.
                            //
                            if (switchButton.getPairedPort() != null)
                            {
                                currentOutputButton = outputButtons[switchButton.getPairedPort().getPortNumber() - 1];
                            }

                            //
                            // Un-select the previously-paired Output port.
                            //
                            else if (currentOutputButton != null
                                    && currentOutputButton.getPairedPort() != null
                                    && currentOutputButton.getPairedPort() != switchButton)
                            {
                                currentOutputButton = null;
                            }
                        }

                        //
                        // Update the button colors.
                        //
                        colorCodeAllButtons();
                    }
                });
                inputGrid.setWidget(row, column, switchButton);
            }
        }
        mainPanel.add(inputGrid);

        //
        // Create the Egress section.
        //

        Label outputLabel = new Label("Output");
        outputLabel.addStyleName("inputOutputLabel");
        mainPanel.add(outputLabel);

        int numOutputButtons = switchConfiguration.getNumOutputPorts();
        outputButtons = new SwitchButton[numOutputButtons];
        int numoutputRows = switchConfiguration.getNumOutputPorts() / NUM_COLUMNS;
        Grid outputGrid = new Grid(numoutputRows, NUM_COLUMNS);
        for (int row = 0; row < outputGrid.getRowCount(); row++)
        {
            for (int column = 0; column < outputGrid.getColumnCount(); column++)
            {
                int buttonNumber = (row * NUM_COLUMNS) + column + 1;
                final SwitchButton switchButton = new SwitchButton(buttonNumber);
                outputButtons[buttonNumber - 1] = switchButton;
                switchButton.setText("" + buttonNumber);
                switchButton.removeStyleName("gwt-Button switchButton");
                switchButton.setStyleName("switchButton");
                switchButton.addMouseOverHandler(new MouseOverHandler()
                {
                    @Override
                    public void onMouseOver(MouseOverEvent event)
                    {
                        if (switchButton.getPairedPort() != null)
                        {
                            switchButton.addStyleName("switchButtonHighlighted");
                            switchButton.getPairedPort().addStyleName("switchButtonHighlighted");
                        }
                    }
                });
                switchButton.addMouseOutHandler(new MouseOutHandler()
                {
                    @Override
                    public void onMouseOut(MouseOutEvent event)
                    {
                        if (switchButton.getPairedPort() != null)
                        {
                            if (switchButton != currentOutputButton)
                            {
                                switchButton.removeStyleName("switchButtonHighlighted");
                            }
                            if (switchButton.getPairedPort() != currentInputButton)
                            {
                                switchButton.getPairedPort().removeStyleName("switchButtonHighlighted");
                            }
                        }
                    }
                });
                switchButton.addClickHandler(new ClickHandler()
                {
                    @Override
                    public void onClick(ClickEvent event)
                    {
                        //
                        // Handle a De-Select.
                        //
                        if (currentOutputButton == switchButton)
                        {
                            currentOutputButton = null;
                            if (switchButton.getPairedPort() != null
                                    && switchButton.getPairedPort() == currentInputButton)
                            {
                                currentInputButton = null;
                            }
                        }

                        //
                        // Handle a Select.
                        //
                        else
                        {
                            //
                            // Select it.
                            //
                            currentOutputButton = switchButton;

                            //
                            // If it is paired, select the mate.
                            //
                            if (switchButton.getPairedPort() != null)
                            {
                                currentInputButton = inputButtons[switchButton.getPairedPort().getPortNumber() - 1];
                            }

                            //
                            // Un-select the previously-paired ooo port.
                            //
                            else if (currentInputButton != null
                                    && currentInputButton.getPairedPort() != null
                                    && currentInputButton.getPairedPort() != switchButton)
                            {
                                currentInputButton = null;
                            }
                        }

                        //
                        // Update the button colors.
                        //
                        colorCodeAllButtons();
                    }
                });
                outputGrid.setWidget(row, column, switchButton);
            }
        }
        mainPanel.add(outputGrid);

        //
        // Create the Connect/Disconnect/Disconnect-All buttons.
        //

        connectButton = new Button("Connect");
        connectButton.addStyleName("controlButton");
        connectButton.setEnabled(false);
        connectButton.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                //
                // Failsafe.
                //
                if (currentInputButton == null || currentOutputButton == null)
                {
                    System.err.println("Unable to Connect, Input/Output not selected");
                    return;
                }

                //
                // Perform the Connect.
                //
                switchSimulatorService.connect(currentInputButton.getPortNumber(), currentOutputButton.getPortNumber(),
                        new AsyncCallback<SwitchConfiguration>()
                {
                    @Override
                    public void onSuccess(SwitchConfiguration result)
                    {
                        switchConfiguration = result;
                        currentInputButton = null;
                        currentOutputButton = null;
                        establishPortMapping();
                    }


                    @Override
                    public void onFailure(Throwable caught)
                    {
                        System.err.println("Unable to Connect");
                        caught.printStackTrace();
                    }
                });
            }
        });

        disconnectButton = new Button("Disconnect");
        disconnectButton.addStyleName("controlButton");
        disconnectButton.setEnabled(false);
        disconnectButton.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                // Failsafe.
                //
                if (currentInputButton == null)
                {
                    System.err.println("Unable to Disconnect, Input not selected");
                    return;
                }

                //
                // Perform the Disconnect.
                //
                switchSimulatorService.disconnect(currentInputButton.getPortNumber(),
                        new AsyncCallback<SwitchConfiguration>()
                {
                    @Override
                    public void onSuccess(SwitchConfiguration result)
                    {
                        switchConfiguration = result;
                        currentInputButton = null;
                        currentOutputButton = null;
                        establishPortMapping();

                    }


                    @Override
                    public void onFailure(Throwable caught)
                    {
                        System.err.println("Unable to Disconnect");
                        caught.printStackTrace();
                    }
                });
            }
        });

        disconnectAllButton = new Button("Disconnect All");
        disconnectAllButton.addStyleName("controlButton");
        disconnectAllButton.setEnabled(true);
        disconnectAllButton.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                //
                // Perform the Disconnect.
                //
                switchSimulatorService.disconnectAll(new AsyncCallback<SwitchConfiguration>()
                {
                    @Override
                    public void onSuccess(SwitchConfiguration result)
                    {
                        switchConfiguration = result;
                        currentInputButton = null;
                        currentOutputButton = null;
                        establishPortMapping();

                    }


                    @Override
                    public void onFailure(Throwable caught)
                    {
                        System.err.println("Unable to Disconnect-All");
                        caught.printStackTrace();
                    }
                });
            }
        });

        HorizontalPanel controlButtonPanel = new HorizontalPanel();
        controlButtonPanel.setSpacing(10);
        controlButtonPanel.addStyleName("controlButtonPanel");

        controlButtonPanel.add(connectButton);
        controlButtonPanel.add(disconnectButton);
        controlButtonPanel.add(disconnectAllButton);

        mainPanel.add(controlButtonPanel);

        //
        // Establish the initial Switch Port Mapping.
        //
        establishPortMapping();

        //
        // Associate the Main panel with the HTML host page.
        //
        RootPanel.get("switchsimulatordiv").add(mainPanel);
    }


    private void establishPortMapping()
    {
        //
        // Failsafe.
        //
        if (switchConfiguration == null)
        {
            System.err.println("Switch Configuration is null");
            return;
        }
        if (switchConfiguration.getPortMap() == null)
        {
            System.err.println("Port Mapping is null");
            return;
        }
        if (inputButtons == null || inputButtons.length < 1)
        {
            System.err.println("Input Buttons are Null/Empty");
            return;
        }
        if (outputButtons == null || outputButtons.length < 1)
        {
            System.err.println("Output Buttons are Null/Empty");
            return;
        }

        //
        // First, clear the existing mapping.
        //
        for (SwitchButton switchButton : inputButtons)
        {
            switchButton.setPairedPort(null);
        }
        for (SwitchButton switchButton : outputButtons)
        {
            switchButton.setPairedPort(null);
            ;
        }

        //
        // Map each of the buttons.
        //
        for (Integer input : switchConfiguration.getPortMap().keySet())
        {
            int output = switchConfiguration.getPortMap().get(input);

            //
            // Failsafe.
            //
            if (input < 1 || input > switchConfiguration.getNumInputPorts())
            {
                System.err.println("Invalid Input mapping: " + input);
                continue;
            }
            if (output < 1 || output > switchConfiguration.getNumOutputPorts())
            {
                System.err.println("Invalid Output mapping: " + output);
                continue;
            }

            //
            // Bind the Ingress and Egress Ports.
            //
            inputButtons[input - 1].setPairedPort(outputButtons[output - 1]);
            outputButtons[output - 1].setPairedPort(inputButtons[input - 1]);
        }

        //
        // Color-code each of the buttons.
        //
        colorCodeAllButtons();
    }


    private void colorCodeAllButtons()
    {
        //
        // Color-Code the Ingress and Egress buttons.
        //
        for (SwitchButton switchButton : inputButtons)
        {
            colorCodeButton(switchButton, currentInputButton);
        }
        for (SwitchButton switchButton : outputButtons)
        {
            colorCodeButton(switchButton, currentOutputButton);
        }

        //
        // Enable/Disable the Connect/Disconnect buttons.
        //
        if (currentInputButton != null && currentOutputButton != null)
        {
            boolean connecting = false;
            boolean disconnecting = false;
            if (currentInputButton.getPairedPort() == currentOutputButton)
            {
                disconnecting = true;
            }
            else
            {
                connecting = true;
            }
            connectButton.setEnabled(connecting);
            disconnectButton.setEnabled(disconnecting);
        }
        else
        {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(false);
        }
    }


    private void colorCodeButton(SwitchButton switchButton, SwitchButton currentlySelectedButton)
    {
        //
        // First, remove ALL styling.
        //
        switchButton.removeStyleName("switchButtonHighlighted");
        switchButton.removeStyleName("switchButtonConnected");
        switchButton.removeStyleName("switchButtonDisconnected");

        if (switchButton == currentlySelectedButton)
        {
            switchButton.addStyleName("switchButtonHighlighted");
        }
        else if (switchButton.getPairedPort() != null)
        {
            switchButton.addStyleName("switchButtonConnected");
        }
        else
        {
            switchButton.addStyleName("switchButtonDisconnected");
        }
    }


    /**
     * A single Switch Button corresponding to an ingress/Egres Port. Each ingress Switch Button may be paired with a
     * corresponding egress Switch Button; and visa-versa.
     */
    class SwitchButton extends Button
    {
        /**
         * Label to be placed on the Port. (1 to NumPorts)
         */
        private int portNumber;

        /**
         * Corresponding Port to-which this port is mapped.
         */
        private SwitchButton pairedPort;


        SwitchButton()
        {
        }


        SwitchButton(int portNumber)
        {
            this();
            this.portNumber = portNumber;
        }


        public int getPortNumber()
        {
            return portNumber;
        }


        public void setPortNumber(int portNumber)
        {
            this.portNumber = portNumber;
        }


        public SwitchButton getPairedPort()
        {
            return pairedPort;
        }


        public void setPairedPort(SwitchButton pairedPort)
        {
            this.pairedPort = pairedPort;
        }
    }
}
