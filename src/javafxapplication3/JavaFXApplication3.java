
package javafxapplication3;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.SerialPort;
import static jssc.SerialPort.MASK_RXCHAR;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class JavaFXApplication3 extends Application {

    SerialPort arduinoPort = null;
    ObservableList<String> portList;

    Label labelValue;
    final ProgressBar myProgressBar3 = new ProgressBar();
    final ProgressIndicator myProgressIndicator3 = new ProgressIndicator();

    private void detectPort() {

        portList = FXCollections.observableArrayList();

        String[] serialPortNames = SerialPortList.getPortNames();
        for (String name : serialPortNames) {
            System.out.println(name);
            portList.add(name);
        }
    }

    @Override
    public void start(Stage primaryStage) {

        labelValue = new Label();

        VBox root = new VBox();
        HBox hBox2 = new HBox();
        HBox hBox3 = new HBox();
        hBox2.setSpacing(3);
        hBox3.setSpacing(3);


        //With fixed progress
        ProgressBar myProgressBar2 = new ProgressBar();
        myProgressBar2.setProgress(0.3);
        ProgressIndicator myProgressIndicator2 = new ProgressIndicator();
        myProgressIndicator2.setProgress(myProgressBar2.getProgress());
        hBox2.getChildren().add(myProgressBar2);
        hBox2.getChildren().add(myProgressIndicator2);

        myProgressBar3.setProgress(0.1);
        myProgressIndicator3.setProgress(myProgressBar3.getProgress());

        detectPort();
        final ComboBox comboBoxPorts = new ComboBox(portList);
        comboBoxPorts.valueProperty()
                .addListener(new ChangeListener<String>() {

                    @Override
                    public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {

                        System.out.println(newValue);
                        disconnectArduino();
                        connectArduino(newValue);
                    }

                });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(
                comboBoxPorts, labelValue);

        hBox3.getChildren().add(myProgressBar3);
        hBox3.getChildren().add(myProgressIndicator3);

        root.getChildren().add(vBox);
        root.getChildren().add(hBox3);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    public boolean connectArduino(String port) {

        System.out.println("connectArduino");

        boolean success = false;
        SerialPort serialPort = new SerialPort(port);
        try {
            serialPort.openPort();
            serialPort.setParams(
                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setEventsMask(MASK_RXCHAR);
            serialPort.addEventListener((SerialPortEvent serialPortEvent) -> {
                if (serialPortEvent.isRXCHAR()) {
                    try {
                        String st = serialPort.readString(serialPortEvent.getEventValue());
                        
                        /***
                         *      VERİ GELİYOR
                        */

                        if (st.length() > 3 && !st.isEmpty()) {
                            System.out.println(st);
                            Platform.runLater(() -> {
                                String t = st.replaceAll("\\D+", "");

                                labelValue.setText("Sıcaklık değeri");

                                Integer x = Integer.parseInt(t);
                                float m = x;

                                System.out.println(st + " <<<<<-------");
                                myProgressBar3.setProgress(m / 100);
                                myProgressIndicator3.setProgress(myProgressBar3.getProgress());
                            });
                        }

                        //Update label in ui thread
                    } catch (SerialPortException ex) {
                        Logger.getLogger(JavaFXApplication3.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }

                }
            });

            arduinoPort = serialPort;
            success = true;
        } catch (SerialPortException ex) {
            Logger.getLogger(JavaFXApplication3.class.getName())
                    .log(Level.SEVERE, null, ex);
            System.out.println("SerialPortException: " + ex.toString());
        }

        return success;
    }

    public void disconnectArduino() {

        System.out.println("disconnectArduino()");
        if (arduinoPort != null) {
            try {
                arduinoPort.removeEventListener();

                if (arduinoPort.isOpened()) {
                    arduinoPort.closePort();
                }

            } catch (SerialPortException ex) {
                Logger.getLogger(JavaFXApplication3.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        disconnectArduino();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
