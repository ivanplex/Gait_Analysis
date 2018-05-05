import static marvin.MarvinPluginCollection.*;
import marvin.image.*;
import marvin.io.*;
import marvin.color.*;

import java.awt.*;

public class EdgeDetectionTest {

    public EdgeDetectionTest(){
        MarvinImage image = MarvinImageIO.loadImage("training/023z082ps.jpg");
        MarvinImage imageOut = new MarvinImage(image.getWidth(), image.getHeight());

        Color greenScreenColor = new Color(56, 175, 93);
        image.fillRect(0, 0, image.getWidth(), 300, greenScreenColor);  //TOP
        image.fillRect(0, 0, 520, image.getHeight(), greenScreenColor);  //LEFT
        image.fillRect(image.getWidth()-350, 0, image.getWidth(), image.getHeight(), greenScreenColor);  //RIGHT
        image.fillRect(0, image.getHeight()-30, image.getWidth(), image.getHeight(), greenScreenColor);  //BOTTOM
        greenToTransparency(image, imageOut);
        reduceGreen(imageOut);
        alphaBoundary(imageOut, 6);


        //Sobel
        imageOut.clear(0xFF000000);
        sobel(image, imageOut);
        MarvinImageIO.saveImage(imageOut, "./experiments/EdgeDetection/sobel.jpg");

        //Reberts
        imageOut.clear(0xFF000000);
        roberts(image, imageOut);
        MarvinImageIO.saveImage(imageOut, "./experiments/EdgeDetection/roberts.jpg");

        //Prewitt
        imageOut.clear(0xFF000000);
        prewitt(image, imageOut);
        MarvinImageIO.saveImage(imageOut, "./experiments/EdgeDetection/prewitt.jpg");

    }

    private void greenToTransparency(MarvinImage imageIn, MarvinImage imageOut){
        for(int y=0; y<imageIn.getHeight(); y++){
            for(int x=0; x<imageIn.getWidth(); x++){

                int color = imageIn.getIntColor(x, y);
                int r = imageIn.getIntComponent0(x, y);
                int g = imageIn.getIntComponent1(x, y);
                int b = imageIn.getIntComponent2(x, y);

                double[] hsv = MarvinColorModelConverter.rgbToHsv(new int[]{color});

                if(hsv[0] >= 80 && hsv[0] <= 150 && hsv[1] >= 0.4 && hsv[2] >= 0.3){
                    imageOut.setIntColor(x, y, 0, 127, 127, 127);
                }
                else{
                    imageOut.setIntColor(x, y, color);
                }

            }
        }
    }

    private void reduceGreen(MarvinImage image){
        for(int y=0; y<image.getHeight(); y++){
            for(int x=0; x<image.getWidth(); x++){
                int r = image.getIntComponent0(x, y);
                int g = image.getIntComponent1(x, y);
                int b = image.getIntComponent2(x, y);
                int color = image.getIntColor(x, y);
                double[] hsv = MarvinColorModelConverter.rgbToHsv(new int[]{color});

                if(hsv[0] >= 60 && hsv[0] <= 130 && hsv[1] >= 0.15 && hsv[2] > 0.15){
                    if((r*b) !=0 && (g*g) / (r*b) >= 1.5){
                        image.setIntColor(x, y, 255, (int)(r*1.4), (int)g, (int)(b*1.4));
                    } else{
                        image.setIntColor(x, y, 255, (int)(r*1.2), g, (int)(b*1.2));
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        new EdgeDetectionTest();
    }
}
