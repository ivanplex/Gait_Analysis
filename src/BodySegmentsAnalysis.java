import marvin.image.MarvinImage;
import marvin.image.MarvinSegment;
import marvin.io.MarvinImageIO;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.util.*;

import static marvin.MarvinPluginCollection.crop;

public class BodySegmentsAnalysis {

    MarvinImage image;
    String targetDirectory;
    Map<String,Set> classifiedSegments;

    int leftParimeter;
    int rightParimeter;
    int topParimeter;
    int bottomParimeter;

    public BodySegmentsAnalysis(MarvinImage img, String targetDirectory){
        this.image = img;
        this.targetDirectory = targetDirectory;

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

        exportToJSON(classifiedSegments);
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
        MarvinImageIO.saveImage(personCrop, "./"+targetDirectory+"/person_cropped.png");
    }

    private void exportToJSON(Map<String,Set> classifiedSegments){

        ObjectMapper mapper = new ObjectMapper();
        Subject subject = new Subject(new File(targetDirectory).getName());

        Iterator it = classifiedSegments.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,Set<MarvinSegment>> pair = (Map.Entry)it.next();
            for(MarvinSegment seg: pair.getValue()){
                subject.addBodyPart(new BodyPart(seg,pair.getKey(),seg.x1,seg.y1));
            }
        }

        try {
            //Object to JSON in file
            mapper.writeValue(new File("./"+targetDirectory+"/features.json"), subject);

            //Object to JSON in String
            String jsonInString = mapper.writeValueAsString(subject);
        }catch (Exception e){

        }

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
