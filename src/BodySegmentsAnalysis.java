import marvin.image.MarvinImage;
import marvin.image.MarvinSegment;
import marvin.io.MarvinImageIO;

import java.util.*;

import static marvin.MarvinPluginCollection.crop;

public class BodySegmentsAnalysis {

    MarvinImage image;
    Map<String,Set> classifiedSegments;

    int leftParimeter;
    int rightParimeter;
    int topParimeter;
    int bottomParimeter;

    public BodySegmentsAnalysis(MarvinImage img){
        this.image = img;

        topParimeter=img.getHeight();
        leftParimeter=img.getWidth();
        rightParimeter = 0;
        bottomParimeter = 0;
    }

    public Map<String,Set> classifySegments(List<MarvinSegment> segments){

        //Sort segments in their vertical position
        segments = verticalSegmentSort(segments);

        classifiedSegments = new HashMap<String, Set>();

        Set<MarvinSegment> bodySet = new HashSet<MarvinSegment>();
        int localTop = 0;
        int localBottom = 0;
        for (MarvinSegment seg: segments) {

            bodyParimeterCheck(seg);

            if(bodySet.isEmpty()){
                //Add segment to the set
                bodySet.add(seg);
                localTop = seg.y1;
                localBottom = seg.y1 + seg.height;
                System.out.println("BODYSET: New body set found! ["+localTop+", "+localBottom+"]");
            }else{
                //See if the segment is vertically close to other segments in the set
                int midPoint = seg.y1 + (int) Math.floor(seg.height/2);
                if(localTop < midPoint && midPoint < localBottom){
                    System.out.println("BODYSET: Similar body set found! Midpoint: "+midPoint+" SET_ENV: ["+localTop+", "+localBottom+"]");
                    bodySet.add(seg);
                    if(seg.y1 < localTop){ localTop = seg.y1; }
                    if(seg.y1+seg.height > localBottom){ localBottom = seg.y1+seg.height; }
                }else{
                    //Segment belongs to new body part
                    commitBodySet(bodySet);

                    bodySet = new HashSet<MarvinSegment>(); //Reset
                    bodySet.add(seg);
                    localTop = seg.y1;
                    localBottom = seg.y1 + seg.height;
                    System.out.println("BODYSET: New body set found! ["+localTop+", "+localBottom+"]");
                }
            }
        }
        if(!bodySet.isEmpty()){
            commitBodySet(bodySet);
        }

        return classifiedSegments;
    }

    private List<MarvinSegment> verticalSegmentSort(List<MarvinSegment> segments){
        //Sort in vertical position
        SegmentVerticalPositionComparator comparator = new SegmentVerticalPositionComparator();
        Collections.sort(segments,comparator);

        return segments;
    }

    private void commitBodySet(Set<MarvinSegment> bodySet){
        if(classifiedSegments.isEmpty()){
            classifiedSegments.put("HEAD",bodySet);
            System.out.println("BODYSET: HEAD identified, num of segments: "+bodySet.size());
        }else if(classifiedSegments.containsKey("ARM")){
            classifiedSegments.put("LEG",bodySet);
            System.out.println("BODYSET: LEG identified, num of segments: "+bodySet.size());
        }else if(classifiedSegments.containsKey("HEAD")){
            classifiedSegments.put("ARM",bodySet);
            System.out.println("BODYSET: ARM identified, num of segments: "+bodySet.size());
        }else{
            System.out.println("BODYSET: ERROR");
        }
    }

    private void bodyParimeterCheck(MarvinSegment seg){
        if(seg.y1 < topParimeter){ topParimeter = seg.y1;}
        if(seg.x1 < leftParimeter){ leftParimeter = seg.x1; }
        if(seg.y1+seg.height > bottomParimeter){ bottomParimeter = seg.y1+seg.height; }
        if(seg.x1+seg.width > rightParimeter){ rightParimeter = seg.x1+seg.width; }
    }

    private void generateCroppedPerson(){
        //Create person crop
        MarvinImage personCrop = new MarvinImage(image.getWidth(), image.getHeight());
        crop(image,personCrop,
                this.getLeftParimeter(),
                this.getTopParimeter(),
                this.getRightParimeter() - this.getLeftParimeter(),
                this.getBottomParimeter() - this.getTopParimeter()
        );
        MarvinImageIO.saveImage(personCrop, "./res/person_cropped.png");
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

    public int getPersonWidth() {
        return this.rightParimeter - this.leftParimeter;
    }

    public int getPersonHeight() {
        return this.bottomParimeter - this.topParimeter;
    }

    public class SegmentVerticalPositionComparator implements Comparator<MarvinSegment> {

        @Override
        public int compare(MarvinSegment firstSegment, MarvinSegment secondSegment) {
            return (firstSegment.y1 - secondSegment.y1);
        }
    }
}
