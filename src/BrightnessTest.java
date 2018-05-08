import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;

public class BrightnessTest {

    public BrightnessTest(){

        String path = "./analysis/017z054pf.jpg/skin.png";
        MarvinImage imageIn = MarvinImageIO.loadImage(path);
        MarvinImage imageOut = new MarvinImage(imageIn.getWidth(),imageIn.getHeight());
        SubjectGaitExtraction sge = new SubjectGaitExtraction(path);
        sge.extractSkintone(imageIn,imageOut);
        MarvinImageIO.saveImage(imageOut, "./analysis/017z054pf.jpg/skin.png");
    }

    public static void main(String[] args){
        new BrightnessTest();
    }
}

