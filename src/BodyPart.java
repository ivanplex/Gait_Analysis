import marvin.image.MarvinImage;

public class BodyPart {

    MarvinImage bodyPartImage;
    String bodyPart;
    int left_distance;
    int right_distance;
    int top_distance;
    int bottom_distance;
    int width;
    int height;

    public BodyPart(MarvinImage bodyPartImage, String bodyPart, int left_distance, int right_distance, int top_distance, int bottom_distance) {
        this.bodyPartImage = bodyPartImage;
        this.bodyPart = bodyPart;
        this.left_distance = left_distance;
        this.right_distance = right_distance;
        this.top_distance = top_distance;
        this.bottom_distance = bottom_distance;
        this.width = bodyPartImage.getWidth();
        this.height = bodyPartImage.getHeight();
    }
}
