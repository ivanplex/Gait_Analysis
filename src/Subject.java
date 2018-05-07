import java.util.ArrayList;
import java.util.List;

public class Subject {

    String filename;
    int numOfBodyParts;
    List<BodyPart> bodyParts;

    public Subject(String filename){
        this.filename = filename;
        numOfBodyParts = 0;
        bodyParts = new ArrayList<BodyPart>();
    }

    public void addBodyPart(BodyPart limb){
        bodyParts.add(limb);
        numOfBodyParts++;
    }

    public String getFilename() {
        return filename;
    }

    public int getNumOfBodyParts() {
        return numOfBodyParts;
    }

    public List<BodyPart> getBodyParts() {
        return bodyParts;
    }
}
