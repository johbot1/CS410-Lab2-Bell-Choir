import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Tone {
    // List to store loaded
    private final ConcurrentLinkedQueue<BellNote> songQueue = new ConcurrentLinkedQueue<>();
    private final AudioFormat af;

    public static void main(String[] args) throws Exception {
        final AudioFormat af =
                new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Tone t = new Tone(af);
        String filePath = "song.txt"; //default Mary Had A Little Lamb
        if (args.length > 0) {
            filePath = args[0]; //get file from command line args
        }
        t.loadSong(filePath);
        t.playSong();
    }
    

    Tone(AudioFormat af) {
        this.af = af;
    }

    void playSong() throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();
            BellNote bn;

            while ((bn = songQueue.poll()) != null) { // Poll ensures thread safety
                playNote(line, bn);
            }
            line.drain();
        }
    }

    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }

    private boolean validateLine(String line) {
        // Split by whitespace
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2) {
            System.err.println("Invalid line format: " + line);
            return false;
        }

        String noteStr = parts[0];
        String durationStr = parts[1];

        // Check if note is valid enum value
//        System.out.println("Starting note check");
        try {
            Note.valueOf(noteStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid note: " + noteStr + " in line: " + line);
            return false;
        }

        // Check if duration is positive
//        System.out.println("End note check");
//        System.out.println("Starting duration check");
        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                System.err.println("Invalid duration: " + duration + " in line: " + line);
                return false;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid duration format: " + durationStr + " in line: " + line);
            return false;
        }
        // Line is valid
        return true;
    }

    // Method to read a text file and print its contents
    private void loadSong(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (validateLine(line)) {
                    addNoteToQueue(line); // Add valid note to the song
                    // Print only if valid
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file! Cannot load song from file:"+ filePath);
        }
    }

    private void addNoteToQueue(String line) {
        String[] parts = line.trim().split("\\s+");
        Note note = Note.valueOf(parts[0]);
        int duration = Integer.parseInt(parts[1]);
        NoteLength length = mapDurationToNoteLength(duration);
        songQueue.offer(new BellNote(note, length)); // Enqueue instead of adding to list
    }

//    private void addNoteToSong(String line) {
//        String[] parts = line.trim().split("\\s+");
//        Note note = Note.valueOf(parts[0]);
//        int duration = Integer.parseInt(parts[1]);
//        NoteLength length = mapDurationToNoteLength(duration);
//        loadedSong.add(new BellNote(note, length));
//    }

    private NoteLength mapDurationToNoteLength(int duration) {
        return switch (duration) {
            case 1 -> NoteLength.WHOLE;
            case 2 -> NoteLength.HALF;
            case 4 -> NoteLength.QUARTER;
            case 8 -> NoteLength.EIGTH;
            default -> null;
        };
    }

}

class BellNote {
    final Note note;
    final NoteLength length;

    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }
}

enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGTH(0.125f);

    private final int timeMs;

    private NoteLength(float length) {
        timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}

enum Note {
    // REST Must be the first 'Note'
    // A4 MUST be the second 'Note'
    REST,
    A4,
    A4S,
    B4,
    C4,
    C4S,
    D4,
    D4S,
    E4,
    F4,
    F4S,
    G4,
    G4S,
    A5;

    public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
    public static final int MEASURE_LENGTH_SEC = 1;

    // Circumference of a circle divided by # of samples
    private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

    private final double FREQUENCY_A_HZ = 440.0d;
    private final double MAX_VOLUME = 127.0d;

    private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

    private Note() {
        int n = this.ordinal();
        if (n > 0) {
            // Calculate the frequency!
            final double halfStepUpFromA = n - 1;
            final double exp = halfStepUpFromA / 12.0d;
            final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

            // Create sinusoidal data sample for the desired frequency
            final double sinStep = freq * step_alpha;
            for (int i = 0; i < sinSample.length; i++) {
                sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
            }
        }
    }

    public byte[] sample() {
        return sinSample;
    }
}