import marvin.image.MarvinImage;
import marvin.image.MarvinSegment;
import marvin.io.MarvinImageIO;

public class BodyPart {

    MarvinSegment bodyPartImage;
    String bodyPart;
    int leftParimeter;
    int rightParimeter;
    int topParimeter;
    int bottomParimeter;
    int width;
    int height;

    public BodyPart(MarvinSegment bodyPartImage, String bodyPart, int leftParimeter, int topParimeter) {
        this.bodyPartImage = bodyPartImage;
        this.bodyPart = bodyPart;
        this.leftParimeter = leftParimeter;
        this.rightParimeter = leftParimeter+bodyPartImage.width;
        this.topParimeter = topParimeter;
        this.bottomParimeter = topParimeter + bodyPartImage.height;
        this.width = bodyPartImage.width;
        this.height = bodyPartImage.width;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public int getLeftParimeter() {
        return leftParimeter;
    }

    public int getRightParimeter() {
        return rightParimeter;
    }

    public int getTopParimeter() {
        return topParimeter;
    }

    public int getBottomParimeter() {
        return bottomParimeter;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}