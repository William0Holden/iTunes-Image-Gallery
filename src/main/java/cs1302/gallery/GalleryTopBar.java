package cs1302.gallery;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.text.TextAlignment;

/**
 * Component holding the javafx objects located at the top of the scene graph.
 */
public class GalleryTopBar extends VBox {

    private Button playButton = new Button("Play"); //play button
    private TextField textField = new TextField("daft punk"); //default search of daft punk
    private ComboBox<String> dropDown = new ComboBox<>(); //combo box
    private Button getImgs = new Button("Get Images"); //get images button
    private Text webTxt = new Text("Type in a term, select a media type, then click the button.");

    /**
     * Constructor for the GalleryTopBar.
     * Adds various javafx objects to the GalleryTopBar.
     */
    public GalleryTopBar() {
        super();
        HBox hBox = new HBox();
        Separator line = new Separator(Orientation.VERTICAL);
        Text searchText = new Text("Search:");
        dropDown.getItems().addAll("movie", "podcast", "music", "musicVideo", "audiobook");
        dropDown.getItems().addAll("shortFilm", "tvShow", "software", "ebook", "all"); //selections
        dropDown.setValue("music"); //default selection
        textField.setPrefWidth(150);
        dropDown.setPrefWidth(100); //sizing
        hBox.setHgrow(textField, Priority.ALWAYS);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(5);
        hBox.getChildren().addAll(playButton, line, searchText,textField, dropDown, getImgs);
        this.getChildren().addAll(hBox, webTxt);
    } //GalleryTopBar (constructor)

    /**
     * Returns the playButton variable.
     *
     * @return playButton - Button variable.
     */
    public Button getPlayButton() {
        return playButton;
    } //getPlayButton

    /**
     * Returns the textField variable.
     *
     * @return textField - TextField variable.
     */
    public TextField getTextField() {
        return textField;
    } //getTextField

    /**
     * Returns the dropDown variable.
     *
     * @return dropDown - ComboBox variable.
     */
    public ComboBox getDropDown() {
        return dropDown;
    } //getDropDown

    /**
     * Returns the getImgs variable.
     *
     * @return getImgs - Button Variable.
     */
    public Button getGetImgs() {
        return getImgs;
    } //getGetImgs

    /**
     * Returns the webTxt variable.
     *
     * @return webTxt - Label variable.
     */
    public Text getWebTxt() {
        return webTxt;
    } //getWebTxt

} //GalleryTopBar
