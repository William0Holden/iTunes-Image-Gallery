package cs1302.gallery;

import javafx.scene.layout.HBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Pos;

/**
 * Component to hold the javafx object that are located at the bottom of the scene graph.
 */
public class GalleryBottomBar extends HBox {

    private ProgressBar progressBar = new ProgressBar(0);

    /**
     * Constructor for the GalleryBottomBar.
     * adds a {@link ProgressBar} and a {@link Label} to the GalleryBottomBar.
     */
    public GalleryBottomBar() {
        super();
        progressBar.setPrefWidth(230);
        Text text = new Text("Images provided by iTunes Search API.");
        text.setTextAlignment(TextAlignment.CENTER);
        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
        this.getChildren().addAll(progressBar, text);
    } //GalleryBottomBar (constructor)

    /**
     * Returns the progressBar variable.
     *
     * @return progressBar - the ProgressBar variable.
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    } //getProgressBar

} //GalleryBottomBar
