import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable, UICallback{

    SpaceManager manager;

    @FXML
    ListView listViewEnvs;
    @FXML
    ListView listViewDevs;
    @FXML
    Button buttonAddEnv;
    @FXML
    Button buttonAddDev;
    @FXML
    Button buttonMoveDev;
    @FXML
    Button buttonDelDev;
    @FXML
    Button buttonDelEnv;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        manager = new SpaceManager(this);
        manager.initSpace();

        refreshUI();

        listViewEnvs.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                ObservableList<String> items = FXCollections.observableArrayList (
                        manager.listDevicesFromEnvironment(newSelection.toString()));
                listViewDevs.setItems(items);
            }
        });

        buttonAddEnv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                manager.createEnvironment();
            }
        });

        buttonAddDev.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                manager.createDevice(listViewEnvs.getSelectionModel().getSelectedItem().toString());
            }
        });

        buttonDelEnv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                manager.deleteEnvironments(listViewEnvs.getSelectionModel().getSelectedItem().toString());
            }
        });

        buttonDelDev.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                manager.deleteDeviceFromEnvironment(listViewEnvs.getSelectionModel().getSelectedItem().toString(),listViewDevs.getSelectionModel().getSelectedItem().toString());
            }
        });
    }

    @Override
    public void refreshUI() {
        ObservableList<String> items = FXCollections.observableArrayList (
                manager.listEnvironments());
        listViewEnvs.setItems(items);
    }
}
