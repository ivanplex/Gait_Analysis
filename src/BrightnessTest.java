import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;

public class BrightnessTest {

    public BrightnessTest(){
        MarvinImage imageIn = MarvinImageIO.loadImage("./analysis/017z054pf.jpg/skin.png");
        MarvinImage imageOut = new MarvinImage(imageIn.getWidth(),imageIn.getHeight());
        SubjectGaitExtraction sge = new SubjectGaitExtraction("./analysis/017z054pf.jpg/skin.png");
        sge.extractSkintone(imageIn,imageOut);
        MarvinImageIO.saveImage(imageOut, "./analysis/017z054pf.jpg/skin.png");
    }

    public static void main(String[] args){
        new BrightnessTest();
    }
}

