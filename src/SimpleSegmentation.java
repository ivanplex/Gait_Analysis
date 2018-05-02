import static marvin.MarvinPluginCollection.*;
import marvin.image.*;
import marvin.io.*;
import marvin.color.*;
import marvin.math.MarvinMath;

import java.awt.*;


public class SimpleSegmentation {
    public SimpleSegmentation(){
        // 1. Load image
        MarvinImage original = MarvinImageIO.loadImage("./res/robocup.jpg");
        MarvinImage image = original.clone();
        // 2. Change green pixels to white
        filterGreen(image);
        // 3. Use threshold to separate foreground and background.
        MarvinImage bin = MarvinColorModelConverter.rgbToBinary(image, 127);
        // 4. Morphological closing to group separated parts of the same object
        morphologicalClosing(bin.clone(), bin, MarvinMath.getTrueMatrix(30, 30));
        // 5. Use Floodfill segmention to get image segments
        image = MarvinColorModelConverter.binaryToRgb(bin);
        MarvinSegment[] segments = floodfillSegmentation(image);
        // 6. Show the segments in the original image
        for(int i=1; i<segments.length; i++){
            MarvinSegment seg = segments[i];
            original.drawRect(seg.x1, seg.y1, seg.width, seg.height, Color.yellow);
//            original.drawRect(seg.x1+1, seg.y1+1, seg.width, seg.height, Color.yellow);
        }
        MarvinImageIO.saveImage(original, "./res/robocup_segmented.png");
    }
    private void filterGreen(MarvinImage image){
        int r,g,b;
        for(int y=0; y<image.getHeight(); y++){
            for(int x=0; x<image.getWidth(); x++){
                r = image.getIntComponent0(x, y);
                g = image.getIntComponent1(x, y);
                b = image.getIntComponent2(x, y);
                if(g > r*1.5 && g > b*1.5){
                    image.setIntColor(x, y, 255,255,255);
                }}}
    }
    public static void main(String[] args) { new SimpleSegmentation();  }
}