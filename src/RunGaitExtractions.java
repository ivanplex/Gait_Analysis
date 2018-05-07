import java.io.File;

public class RunGaitExtractions {

    String directory;


    public RunGaitExtractions(String directory){
        this.directory = directory;

        traverseDirectory();
    }

    private void traverseDirectory(){
        File dir = new File(directory);
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                performExtraction(directory+child.getName());
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
    }

    private void performExtraction(String path){
        System.out.println(".......Processing image: "+path+" ........");

        new SubjectGaitExtraction(path);
        System.out.println(".......              END               ........");
    }

    public static void main(String[] args){
        new RunGaitExtractions("./test/");
    }
}
