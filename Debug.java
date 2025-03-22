public class Debug {
    public static void printMessage(Integer i, String name, Note note){
        switch(i){
            case 1: // Receive Note
                System.out.println("[MSG] " + name + " has recieved a note: " + note);
            case 2: // Finish Playing
                System.out.println("[MSG] " + name + " has finished playing a note: " + note);
            case 3: // Attempt to Play
                System.out.println("[MSG] " + name + " is attempting to play: " + note);
            case 4: // Writing Audio Data
                System.out.println("[MSG] " + name + " writing audio data for: " + note);
            case 5: // Adding a note to Queue
                    System.out.println("Adding note to queue: " + note);
        }
    }

    public static void printError(Integer i, String name){
        switch(i){
            case 1:
                System.out.println("[ERROR] " + name + " has been interrupted!");
            case 2:
                System.out.println("[ERROR] " + name + " is already assigned a note in their hand!");

        }
    }
}
