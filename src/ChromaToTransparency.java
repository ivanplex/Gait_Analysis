import static marvin.MarvinPluginCollection.*;

import com.github.sh0nk.matplotlib4j.NumpyUtils;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import com.github.sh0nk.matplotlib4j.builder.ContourBuilder;
import marvin.image.*;
import marvin.io.*;
import marvin.color.*;
import marvin.math.MarvinMath;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinAttributes;
import marvin.util.MarvinPluginLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ChromaToTransparency {

    public ChromaToTransparency(){

        //Load Image
        MarvinImage image = MarvinImageIO.loadImage("training/018z058pf.jpg");

        //Crop unwanted
        System.out.println("Cropping image.");
        MarvinImage croppedImage = cropImage(image, 300,30,520,350);
        MarvinImageIO.saveImage(croppedImage, "./res/crop.png");

        //Extract skin tone area
        System.out.println("Extracting person skin tone");
        MarvinImage skinImage = new MarvinImage(croppedImage.getWidth(), croppedImage.getHeight());
        extractSkintone(croppedImage, skinImage);
        MarvinImageIO.saveImage(skinImage, "./res/skin.png");

        //Segment identification
        System.out.println("Segment Identification");
        skinImage = segmentation(skinImage);

        //Outline analysis
        System.out.println("Foreground identification");
        MarvinImage foregroundImage = removeGreenScreen(croppedImage);
        MarvinImage outlineImage = foregroundImage.clone();
        outlineImage.clear(0xFF000000);
        roberts(croppedImage, outlineImage);
        MarvinImageIO.saveImage(outlineImage, "./res/edgeDetection.jpg");

        //Merge photo
        MarvinImage combinedImage = new MarvinImage(croppedImage.getWidth(), croppedImage.getHeight());
        combineByTransparency(outlineImage,skinImage,combinedImage,0,0,50);
        MarvinImageIO.saveImage(combinedImage, "./res/combined.jpg");

    }

    public MarvinImage segmentation(MarvinImage skinImage){

        int maxNumSegmentsWanted = 5;
        int minFeatureSize = 2500;    //Pixel squared
        int maxFeatureSize = (skinImage.getHeight()*skinImage.getWidth())/4;

        MarvinImage image = skinImage.clone();

        // 3. Use threshold to separate foreground and background.
        MarvinImage bin = MarvinColorModelConverter.rgbToBinary(image, 110);


        // 4. Morphological closing to group separated parts of the same object
        morphologicalClosing(bin.clone(), bin, MarvinMath.getTrueMatrix(30, 30));
        // 5. Use Floodfill segmention to get image segments
        image = MarvinColorModelConverter.binaryToRgb(bin);

        List<MarvinSegment> segments = Arrays.asList(floodfillSegmentation(image));
        // Show the largest 5 segments
        MarvinSegmentComparator segmentComparator = new MarvinSegmentComparator();
        Collections.sort(segments,segmentComparator);

        System.out.println(segments.size()+" segment(s) found:");

        //person profile identification variables
        int person_top = skinImage.getHeight();
        int person_left = skinImage.getWidth();
        int person_right = 0;
        int person_bottom = 0;

        List<MarvinSegment> bodySegments = new ArrayList<MarvinSegment>();

        //Process all fragments
        for (MarvinSegment seg: segments) {
            if (maxNumSegmentsWanted == 0 || seg.area < minFeatureSize) {
                break;
            }

            //Capture body segment and decrement count
            if (seg.area < maxFeatureSize) {
                bodySegments.add(seg);
                maxNumSegmentsWanted--;
            }
        }

        //Highlight body segments

        try {
            BufferedImage bufferedSkinImage = ImageIO.read(new File("./res/", "skin.png"));

            for (MarvinSegment seg : bodySegments) {
                System.out.println("Size: " + seg.area);
                drawBox(bufferedSkinImage,seg.x1,seg.y1,seg.width,seg.height,5,Color.yellow,"HEAD");


                //person profile identification
                if (seg.x1 < person_left) {
                    person_left = seg.x1;
                }
                if (seg.y1 < person_top) {
                    person_top = seg.y1;
                }
                if (seg.width + seg.x1 > person_right) {
                    person_right = seg.width + seg.x1;
                }
                if (seg.height + seg.y1 > person_bottom) {
                    person_bottom = seg.height + seg.y1;
                }
            }

            System.out.println("[" + person_left + " , " + person_top + "]");
            System.out.println("[" + person_right + " , " + person_bottom + "]");

            //Draw person profile
            drawBox(bufferedSkinImage,person_left,person_top,
                    person_right - person_left,person_bottom - person_top,
                    2,Color.red,"");

            skinImage = new MarvinImage(bufferedSkinImage);

            MarvinImageIO.saveImage(skinImage, "./res/segment_analysis.png");
        } catch (IOException e) {
            e.printStackTrace();
        }


        //TODO: Multimodel distribution
        //TODO: jama , wexter

        return skinImage;
    }

    public class MarvinSegmentComparator implements Comparator<MarvinSegment> {

        @Override
        public int compare(MarvinSegment firstSegment, MarvinSegment secondSegment) {
            return (secondSegment.area - firstSegment.area);
        }
    }


    public MarvinImage removeGreenScreen(MarvinImage image){
        //        Color greenScreenColor = new Color(56, 175, 93);

        MarvinImage imageOut = new MarvinImage(image.getWidth(), image.getHeight());

        // 1. Convert green to transparency
        greenToTransparency(image, imageOut);
        MarvinImageIO.saveImage(imageOut, "./res/person_chroma_out1.png");
        // 2. Reduce remaining green pixels
        reduceGreen(imageOut);
        MarvinImageIO.saveImage(imageOut, "./res/person_chroma_out2.png");
        // 3. Apply alpha to the boundary
        alphaBoundary(imageOut, 6);
        MarvinImageIO.saveImage(imageOut, "./res/person_chroma_out3.png");

        return imageOut;
    }

    /**
     * Crop image inwards
     * @param sourceImage
     * @param top
     * @param bottom
     * @param left
     * @param right
     * @return Cropped Marvin Image
     */
    private MarvinImage cropImage(MarvinImage sourceImage, int top, int bottom, int left, int right){
        MarvinImage croppedImage = new MarvinImage(sourceImage.getWidth(),sourceImage.getHeight());

        //Calculate crop parameter
        int origin_x = left;
        int origin_y = top;
        int length_x = sourceImage.getWidth() - left - right;
        int length_y = sourceImage.getHeight() - top - bottom;
        crop(sourceImage,croppedImage,origin_x,origin_y,length_x,length_y);

        return croppedImage;
    }

    private void extractSkintone(MarvinImage imageIn, MarvinImage imageOut){
        //https://arxiv.org/pdf/1708.02694.pdf
        for(int y=0; y<imageIn.getHeight(); y++){
            for(int x=0; x<imageIn.getWidth(); x++){

                int color = imageIn.getIntColor(x, y);
                int r = imageIn.getIntComponent0(x, y);
                int g = imageIn.getIntComponent1(x, y);
                int b = imageIn.getIntComponent2(x, y);

                double[] hsv = MarvinColorModelConverter.rgbToHsv(new int[]{color});

                if(hsv[0] >= 0 && hsv[0] <= 25 &&
                        hsv[1] >= 0.23 && hsv[1] <= 0.98
                        && hsv[2] >= 0.1){
                    imageOut.setIntColor(x, y, color);
                }
                else{
                    imageOut.setIntColor(x, y, 0, 127, 127, 127);
                }

            }
        }
    }

    private void greenToTransparency(MarvinImage imageIn, MarvinImage imageOut){
        for(int y=0; y<imageIn.getHeight(); y++){
            for(int x=0; x<imageIn.getWidth(); x++){

                int color = imageIn.getIntColor(x, y);
                int r = imageIn.getIntComponent0(x, y);
                int g = imageIn.getIntComponent1(x, y);
                int b = imageIn.getIntComponent2(x, y);

                double[] hsv = MarvinColorModelConverter.rgbToHsv(new int[]{color});

                if(hsv[0] >= 80 && hsv[0] <= 150 && hsv[1] >= 0.4 && hsv[2] >= 0.1){
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

    public void drawBox(BufferedImage image, int x, int y, int width, int height, int thickness, Color c, String text){

        Graphics g = image.getGraphics();
        g.setColor(c);
        //Draw inwards
        for(int i=0; i<thickness; i++) {
            g.drawRect(x+i, y+i, width-i*2, height-i*2);
        }

        Font f = new Font("Dialog", Font.PLAIN, 30);
        g.setFont(f);
        g.drawString(text,x+thickness,y+30);


        g.dispose();
    }

    public static void main(String[] args) {
        new ChromaToTransparency();
    }
}