package controller;

import dao.DAOAppointments;
import dao.DBQuery;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Keith A Graham
 * Controls the New Appointment screen and all functionality therein.
 *
 */

public class AppointmentController implements Initializable {

    @FXML
    private ComboBox ComboBoxAppointmentType;
    @FXML
    private DatePicker DatePickerStartDate;
    @FXML
    private DatePicker DatePickerEndDate;
    @FXML
    private ComboBox ComboBoxStartTime;
    @FXML
    private ComboBox ComboBoxEndTime;
    @FXML
    private TextField TextFieldFirstName;
    @FXML
    private TextField TextFieldLastName;
    @FXML
    private TextField TextFieldPhone;
    @FXML
    private TextField TextFieldTitle;
    @FXML
    private TextField TextFieldDescription;
    @FXML
    private TextField TextFieldLocation;
    @FXML
    private ComboBox ComboBoxContact;
    @FXML
    private TextField TextFieldCustomerID;
    @FXML
    private TextField TextFieldUserID;
    @FXML
    private Button ButtonSave;
    @FXML
    private Button ButtonCancel;
    @FXML
    private Button ButtonExit;



    public static ObservableList options = DBQuery.options;


    /**
     * Initializes the controller class and sets the initial options in comboboxes.
     *
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {


        ComboBoxAppointmentType.getItems().addAll("Scrum", "Presentation", "Planning Session", "De-Briefing");

        ComboBoxContact.getItems().addAll(DBQuery.contacts());
        LocalDateTime easternStart = LocalDateTime.of(LocalDate.now(), LocalTime.of(8,0));
        ZonedDateTime easternZDT = easternStart.atZone(ZoneId.of("America/New_York"));
        ZonedDateTime localZDT = easternZDT.withZoneSameInstant(ZoneId.systemDefault());
        LocalTime startLocal = localZDT.toLocalTime();

        LocalDateTime easternEnd = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0));
        ZonedDateTime easternZDTE = easternEnd.atZone(ZoneId.of("America/New_York"));
        ZonedDateTime localZDTE = easternZDTE.withZoneSameInstant(ZoneId.systemDefault());
        LocalTime endLocal = localZDTE.toLocalTime();

        LocalTime lts = startLocal;
        while (lts.isBefore(endLocal.plusSeconds(1))) {
            ComboBoxStartTime.getItems().add(lts);
            ComboBoxEndTime.getItems().add(lts);
            lts = lts.plusMinutes(15);
        }

    }

    @FXML
    private void onComboBoxAppointmentType(ActionEvent event) {

    }

    /**
     * When the date picker is selected, the local variable setEndDate is set from the value chosen.
     * @param event
     */
    @FXML
    private void onDatePickerStartDate(ActionEvent event) {
        LocalDate setEndDate = DatePickerStartDate.getValue();
        DatePickerEndDate.setValue(setEndDate);
    }

    /**
     * The End Date date picker is set to the value set in the Start Date date picker.
     * @param event
     */
    @FXML
    private void onDatePickerEndDate(ActionEvent event) {
        onDatePickerStartDate(event);
    }

    /**
     * The End Time combo box value is captured and alerts pop if the end time is earlier than the start time. Also in use
     * is a lambda expression to display the alert in the event this error occurs.
     * @param event
     */
    @FXML
    private void onComboBoxEndTime(ActionEvent event) {

        LocalTime s = (LocalTime) ComboBoxStartTime.getValue();
        LocalTime e = (LocalTime) ComboBoxEndTime.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kk:mm");
        String stext = s.format(formatter);
        String etext = e.format(formatter);
        LocalTime sParsedTime = LocalTime.parse(stext);
        LocalTime eParsedTime = LocalTime.parse(etext);


/********************************************************************************************************************
 * lambda
 ********************************************************************************************************************/
        if (sParsedTime.isAfter(eParsedTime)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "End time must be after start time.");
            // the following lambda expression causes "alert" to display, looks for the OK button to be pressed,
            // and upon being pressed will cause the items in the End Time combo box to show
            alert.showAndWait()
                   .filter(response -> response == ButtonType.OK)
                   .ifPresent(response -> ComboBoxEndTime.show());
        }
    }

    /**
     * When the save button is pressed, all of the entered values are checked for correctness. A query runs to determine there are no other
     * meetings scheduled for the customer during the selected timeframe and alerts if one is found. Then, if none found, a database update
     * occurs using the information collected in the form.
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void onButtonSave(ActionEvent event) throws IOException {

        LocalTime s = (LocalTime) ComboBoxStartTime.getValue();
        LocalTime e = (LocalTime) ComboBoxEndTime.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kk:mm");
        String stext = s.format(formatter);
        String etext = e.format(formatter);
        LocalTime sParsedTime = LocalTime.parse(stext);
        LocalTime eParsedTime = LocalTime.parse(etext);

        if (sParsedTime.isAfter(eParsedTime)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "End time must be after start time.");
            // the following lambda expression causes "alert" to display, looks for the OK button to be pressed,
            // and upon being pressed will cause the items in the End Time combo box to show
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> ComboBoxEndTime.show());
        } else {

            if (DatePickerStartDate.getValue() == null ||
                    DatePickerEndDate.getValue() == null) {
                Alert blank = new Alert(Alert.AlertType.ERROR);
                blank.setHeaderText(null);
                blank.setContentText("Select a date.");
                blank.showAndWait();
            }

            if (ComboBoxStartTime.getSelectionModel().isEmpty() || ComboBoxEndTime.getSelectionModel().isEmpty()) {
                Alert blank = new Alert(Alert.AlertType.ERROR);
                blank.setHeaderText(null);
                blank.setContentText("Select a time.");
                blank.showAndWait();
            } else {
                LocalTime lt = (LocalTime) ComboBoxStartTime.getValue();
                LocalTime ltend = (LocalTime) ComboBoxEndTime.getValue();

                LocalDate ld = DatePickerStartDate.getValue();
                LocalDate ldend = DatePickerEndDate.getValue();

                LocalDateTime ldt = LocalDateTime.of(ld, lt);
                LocalDateTime ldtend = LocalDateTime.of(ldend, ltend);

                Timestamp start = Timestamp.valueOf(ldt);
                Timestamp end = Timestamp.valueOf(ldtend);



                if (TextFieldTitle.getText().isEmpty() || TextFieldDescription.getText().isEmpty() || TextFieldLocation.getText().isEmpty() ||
                        ComboBoxAppointmentType.getSelectionModel().isEmpty() || TextFieldCustomerID.getText().isEmpty() || TextFieldUserID.getText().isEmpty() ||
                        ComboBoxContact.getSelectionModel().isEmpty()) {
                    Alert blank = new Alert(Alert.AlertType.ERROR);
                    blank.setHeaderText(null);
                    blank.setContentText("You must complete all fields!");
                    blank.showAndWait();
                } else {

                    String titleField = TextFieldTitle.getText();
                    String descriptionField = TextFieldDescription.getText();
                    String locationField = TextFieldLocation.getText();
                    String typeField = ComboBoxAppointmentType.getValue().toString();
                    int customerIDField = Integer.valueOf(TextFieldCustomerID.getText());
                    int userIDField = Integer.valueOf(TextFieldUserID.getText());
                    String contactNameField = ComboBoxContact.getValue().toString();
                    int contactId = (int) DBQuery.contactIDLookup(contactNameField);

                    LocalDateTime startA = start.toLocalDateTime();
                    ZonedDateTime startB = startA.atZone(ZoneId.systemDefault());
                    ZonedDateTime startC = startB.withZoneSameInstant(ZoneId.of("UTC"));
                    LocalDateTime startD = startC.toLocalDateTime();
                    Timestamp startE = Timestamp.valueOf(startD);

                    LocalDateTime endA = end.toLocalDateTime();
                    ZonedDateTime endB = endA.atZone(ZoneId.systemDefault());
                    ZonedDateTime endC = endB.withZoneSameInstant(ZoneId.of("UTC"));
                    LocalDateTime endD = endC.toLocalDateTime();
                    Timestamp endE = Timestamp.valueOf(endD);

                    System.out.println(startE + " " + endE);
                    boolean result = DBQuery.existingApptSearch(customerIDField, startE, endE);



                    if(!result) {

                        // create object

                        DAOAppointments apptObject = new DAOAppointments(titleField, descriptionField, locationField, typeField, start, end,
                                customerIDField, userIDField, contactId);

                        DBQuery.apptInsert(apptObject);

                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment created successfully.");
/* *********************************************************************************************************************************
lambda Alert
 ***********************************************************************************************************************************/
                        alert.showAndWait()
                                .filter(response -> response == ButtonType.OK)
                                .ifPresent(response -> ((Stage) (((Button) event.getSource()).getScene().getWindow())).close());

                        Parent root = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));

                        Stage newStage = new Stage();
                        newStage.setTitle(null);
                        Scene scene = new Scene(root);
                        newStage.setScene(scene);
                        newStage.show();
                    } else {
                            Alert blank = new Alert(Alert.AlertType.ERROR);
                            blank.setHeaderText(null);
                            blank.setContentText("This customer already has a meeting during that timeframe! Choose different times.");
                            blank.showAndWait();
                        }
                }
            }
        }
    }

    /**
     * If the cancel button is pressed, the form is closed and the user is returned to the main menu.
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void onButtonCancel(ActionEvent event) throws IOException {
        
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();

        Parent root = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));

        Stage newStage = new Stage();
        newStage.setTitle(null);
        Scene scene = new Scene(root);
        newStage.setScene(scene);
        newStage.show();
        
    }

    public void onComboBoxStartTime(ActionEvent actionEvent) {
    }


    public void onComboBoxContact(ActionEvent actionEvent) {

    }


}
