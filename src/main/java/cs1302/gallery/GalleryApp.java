package cs1302.gallery;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URL;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Random;

import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import cs1302.gallery.GalleryGrid;
import cs1302.gallery.GalleryTopBar;
import cs1302.gallery.GalleryBottomBar;

import cs1302.gallery.ItunesResponse;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents an iTunes Gallery App.
 */
public class GalleryApp extends Application {

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private Stage stage;
    private Scene scene;
    private VBox root;
    private GalleryTopBar topBar;
    private GalleryGrid grid;
    private GalleryBottomBar bottomBar; // creating all items in the scene graph (using components)
    protected static final String ITUNES_API = "https://itunes.apple.com/search";

    private Image[] imgs; //all the images download from the search
    private Random rand = new Random(); //random for randomReplacement()
    private boolean canRun = false; //canRun to stop randomReplacement thread outside of the thread
    private boolean alreadyRan = false; //prevents multiple randomReplacement threads
    private String webText = ""; //text of the URL display component
    private Alert alert = new Alert(AlertType.ERROR); //alert dialog
    private boolean skipGrid = false;

    /**
     * Constructs a {@code GalleryApp} object}.
     */
    public GalleryApp() {
        this.stage = null;
        this.scene = null;
        this.root = new VBox();
        alert.setResizable(true);
        topBar = new GalleryTopBar();
        grid = new GalleryGrid();
        bottomBar = new GalleryBottomBar(); //creating all components
        topBar.getGetImgs().setOnAction((e) -> this.runNow(() -> setGridImages()));
        topBar.getPlayButton().setOnAction(e -> playButtonPress()); //setting button actions
        topBar.getPlayButton().setDisable(true); //disabling play button (useless with default imgs)
        root.getChildren().addAll(topBar, grid, bottomBar);
    } // GalleryApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        // feel free to modify this method
        System.out.println("init() called");
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(this.root);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("GalleryApp!");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // feel free to modify this method
        System.out.println("stop() called");
    } // stop

    /**
     * Retrieves the JSON response string for a query to the iTunes Search API.
     *
     * @param query - the query string (should be a formatted itunes uri query string).
     * @return response - the JSON formatted string from the request.
     */
    public String getResponse(String query) {
        try {
            String val = this.topBar.getDropDown().getValue().toString(); //get selected medie type.
            String term = URLEncoder.encode(query, StandardCharsets.UTF_8); //encode query
            String media = URLEncoder.encode(val, StandardCharsets.UTF_8); //music(dropDown)
            String limit = URLEncoder.encode("200", StandardCharsets.UTF_8); // 200 results
            String endUri = String.format("?term=%s&media=%s&limit=%s", term, media, limit);
            String uri = ITUNES_API + endUri; //final request url
            webText = uri; // to be set to scene graph later.
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            } //if
            return response.body();
        } catch (IOException | InterruptedException e) {
            skipGrid = true;
            if (alreadyRan && this.topBar.getPlayButton().getText().equals("Pause")) {
                Platform.runLater(() -> playButtonPress()); //stop the play button if it is playing
            } //if
            this.topBar.getWebTxt().setText("Last attempt to get images failed...");
            String errorMessage = "URI: " + webText + "\n\nException: " + e.getMessage();
            TextArea textArea = new TextArea(errorMessage);
            textArea.setEditable(false);
            alert.getDialogPane().setContent(textArea);
            this.topBar.getGetImgs().setDisable(false); //re enabling buttons
            if (this.grid.getImgViews()[0].getImage().getUrl().equals(GalleryGrid.DEFAULT_IMG)) {
                this.topBar.getPlayButton().setDisable(true);
            } else {
                this.topBar.getPlayButton().setDisable(false);
            } //if
            Platform.runLater(() -> alert.show()); // show alert
        } //try
        return "";
    } //getResponse

    /**
     * Creates and returns a string list of all image urls found in the response parameter.
     *
     * @param response - the JSON formatted reponse string from iTunes API.
     * @return uriList - image uris parsed from the response parameter.
     */
    public String[] getURIList(String response) {
        String[] uriList = new String[0]; //list to return
        String jsonString = response;
        ItunesResponse itunesResponse = GSON
            .fromJson(jsonString, ItunesResponse.class);
        try {
            uriList = new String[itunesResponse.resultCount];
            for (int i = 0; i < itunesResponse.results.length; i++) {
                ItunesResult result = itunesResponse.results[i];
                uriList[i] = result.artworkUrl100;
            } //for
        } catch (NullPointerException e) {
            System.err.println("ERROR: alert dialog should appear");
            System.err.println(e.getMessage());
        } //try
        return uriList;
    }

    /**
     * Represents a push of the "Get images..." button.
     * This method will find >= 21 images from the iTunes API and set the grid of images acoordingly
     */
    public void setGridImages() {
        skipGrid = false;
        if (alreadyRan && this.topBar.getPlayButton().getText().equals("Pause")) {
            Platform.runLater(() -> playButtonPress()); //stop the play button if it is playing
        } //if
        this.topBar.getPlayButton().setDisable(true); //disable buttons during search
        this.topBar.getGetImgs().setDisable(true);
        this.topBar.getWebTxt().setText("Getting images...");
        try {
            String[] tempUris = this.getURIList(this.getResponse(topBar.getTextField().getText()));
            if (!skipGrid) {
                bottomBar.getProgressBar().setProgress(0);
                tempUris = removeDuplicates(tempUris);
                if (tempUris.length < 21) {
                    if (grid.getImgViews()[0].getImage().equals(GalleryGrid.DEFAULT_IMG)) {
                        this.topBar.getPlayButton().setDisable(true);
                    } //if
                    this.topBar.getGetImgs().setDisable(false);
                    throw new IllegalArgumentException(tempUris.length +
                    " distinct results found, but 21 or more are needed.");
                } else { //valid search path (more than 21 distinct images
                    ImageView [] imgViews = this.grid.getImgViews();
                    imgs = new Image[tempUris.length];
                    for (int i = 0; i < imgs.length; i++) { //download loop (all unique images)
                        imgs[i] = new Image(tempUris[i]);
                        bottomBar.getProgressBar().setProgress((i + 1.0) / (imgs.length * 1.0));
                    } //for
                    for (int i = 0; i < imgViews.length; i++) {
                        imgViews[i].setImage(imgs[i]); //setting images
                    } //for
                }
                this.topBar.getPlayButton().setDisable(false); //re enabling buttons
                this.topBar.getGetImgs().setDisable(false);
                this.topBar.getWebTxt().setText(webText); //updating url text component
            } //if
        } catch (IllegalArgumentException  e) {
            this.topBar.getWebTxt().setText("Last attempt to get images failed...");
            String errorMessage = "URI: " + webText + "\n\nException: " + e.getMessage();
            TextArea textArea = new TextArea(errorMessage);
            textArea.setEditable(false);
            alert.getDialogPane().setContent(textArea);
            Platform.runLater(() -> alert.show()); //alert dialog
            if (this.grid.getImgViews()[0].getImage().getUrl().equals(GalleryGrid.DEFAULT_IMG)) {
                this.topBar.getPlayButton().setDisable(true);
            } else {
                this.topBar.getPlayButton().setDisable(false);
            } //if
        } //try
    } //getGridImages

    /**
     * Thread creator: creates a thread and set daemon to true, then starts the thread.
     *
     * @param target - the runnable target (method intended to be ran by the thread).
     */
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    } //runNow

    /**
     * This method randomly replaces one image on the grid of 20 images.
     */
    public void randomReplacement() {
        if (canRun) {
            boolean isSame = true;
            String randomImg = "";
            int randomVal = 0;
            while (isSame) {
                isSame = false;
                randomVal = rand.nextInt(imgs.length);
                randomImg = imgs[randomVal].getUrl(); //random image
                for (int i = 0; i < this.grid.getImgViews().length; i ++) {
                    if (randomImg.equals(this.grid.getImgViews()[i].getImage().getUrl())) {
                        isSame = true; //if the image is already on the grid of image views
                    } //if
                } //for
            } //while
            Image newImage = imgs[randomVal]; // new image
            int tempInt = this.grid.getImgViews().length;
            this.grid.getImgViews()[rand.nextInt(tempInt)].setImage(newImage);
        } //while
    } //randomReplacement

    /**
     * Removes any duplicates from the String array, returns the new String array.
     *
     * @param arr - the String array to be processed.
     * @return String array with the removed duplicates.
     */
    public static String[] removeDuplicates(String[] arr) {
        if (arr.length == 0 || arr.length == 1) {
            return arr;
        }
        String[] temp = new String[arr.length];
        int tempLength = 0;
        for (int i = 0; i < arr.length; i++) {
            boolean isDuplicate = false;
            for (int j = 0; j < tempLength; j ++) {
                if (temp[j].equals(arr[i])) {
                    isDuplicate = true;
                } //if
            } //for
            if (!isDuplicate) {
                temp[tempLength] = arr[i];
                tempLength++;
            } //if
        } //for
        String[] newArr = new String[tempLength];
        for (int i = 0; i < newArr.length; i++) {
            newArr[i] = temp[i];
        } //for
        return newArr;
    } //removeDuplicates

    /**
     * Represents a push of the "Play" button.
     * Calls the randomReplacement method every 2 seconds and switches the text of the play button.
     */
    public void playButtonPress() {
        if (topBar.getPlayButton().getText().equals("Play")) { // adjusting the text acoordingly
            topBar.getPlayButton().setText("Pause");
            canRun = true;
        } else {
            topBar.getPlayButton().setText("Play");
            canRun = false;
        } //if
        if (!alreadyRan) { //first button press
            alreadyRan = true;
            EventHandler<ActionEvent> handler = event -> randomReplacement();
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
            Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play(); //randomReplacement every two seconds
        } //if
    } //playButtonPress
} // GalleryApp
