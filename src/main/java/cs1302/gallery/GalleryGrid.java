package cs1302.gallery;

import javafx.scene.layout.TilePane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

/**
 * Compenent to represent a 5 by 4 grid of {@link ImageView} objects.
 */
public class GalleryGrid extends TilePane {

    protected static final String DEFAULT_IMG = "file:resources/default.png";
    protected static final int DEF_WIDTH = 100;
    protected static final int DEF_HEIGHT = 100;

    private ImageView[] imgViews = new ImageView[20]; //img views

    /**
     * Constructor for the GalleryGrid object.
     * creates 20 images and adds them to the GalleryGrid with a loop.
     */
    public GalleryGrid() {
        super();
        this.setPrefColumns(5);
        for (int i = 0; i < imgViews.length; i ++) {
            Image tempImg = new Image (DEFAULT_IMG, DEF_HEIGHT, DEF_WIDTH, false, false);
            imgViews[i] = new ImageView(tempImg);
            imgViews[i].setFitHeight(DEF_HEIGHT);
            imgViews[i].setFitWidth(DEF_WIDTH);
            imgViews[i].setPreserveRatio(false);
            this.getChildren().add(imgViews[i]);
        } //for
    } //GalleryGrid (constructor)

    /**
     * Returns the imgViews variable.
     *
     * @return imgViews - the list of ImageView objects.
     */
    public ImageView[] getImgViews() {
        return this.imgViews;
    } //getImageViews

} //GalleryGrid
