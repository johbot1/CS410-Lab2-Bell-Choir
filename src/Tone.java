package src;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * src.Tone class - Main class to load and play a song using Members and a src.Conductor.
 */
public class Tone {

    // List to store loaded song notes.
    private List<BellNote> loadedSong = new ArrayList<>();

    // Audio format for playback.
    private final AudioFormat af;

    // List to store src.Member threads.
    private List<Member> members = new ArrayList<>();

    /**
     * Main method - Entry point of the program.
     * @param args Command line arguments (optional file path).
     * @throws Exception If an error occurs during execution.
     */
    public static void main(String[] args) throws Exception {
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Tone t = new Tone(af);

        // Load song from file (default or command-line argument).
        String filePath = "song.txt"; // Default song file.
        if (args.length > 0) {
            filePath = args[0]; // Use command-line argument if provided.
        }
        t.loadSong(filePath);

        // Initialize src.Member threads (one per note, excluding REST).
        t.startMembers();

        // Create SourceDataLine for audio playback.
        try (SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            // Create and start src.Conductor thread.
            Conductor conductor = new Conductor(t.members, 120, t.loadedSong, line);
            conductor.start();
            conductor.join(); // Wait for src.Conductor to finish.

        } catch (LineUnavailableException e) {
            System.err.println("Line unavailable: " + e.getMessage());
        }
    }

    /**
     * src.Tone constructor.
     * @param af AudioFormat for playback.
     */
    Tone(AudioFormat af) {
        this.af = af;
    }

    /**
     * Initializes src.Member threads, one for each src.Note (excluding REST).
     */
    private void startMembers() {
        for (Note note : Note.values()) {
            if (note != Note.REST) { // Skip REST note.
                members.add(new Member(("src.Member " + note), null, note, null)); // Create src.Member for each src.Note.
            }
        }
    }

    /**
     * Validates a line from the song file.
     * @param line Line to validate.
     * @return True if the line is valid, false otherwise.
     */
    private boolean validateLine(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2) {
            System.err.println("Invalid line format: " + line);
            return false;
        }

        String noteStr = parts[0];
        String durationStr = parts[1];

        try {
            Note.valueOf(noteStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid note: " + noteStr + " in line: " + line);
            return false;
        }

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
        return true;
    }

    /**
     * Loads a song from a file.
     * @param filePath Path to the song file.
     */
    public void loadSong(String filePath) {
        loadedSong.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (validateLine(line)) {
                    addNoteToSong(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Adds a note to the loaded song.
     * @param line Line from the song file.
     */
    private void addNoteToSong(String line) {
        String[] parts = line.trim().split("\\s+");
        Note note = Note.valueOf(parts[0]);
        int duration = Integer.parseInt(parts[1]);
        NoteLength length = mapDurationToNoteLength(duration);
        loadedSong.add(new BellNote(note, length));
    }

    /**
     * Maps duration to src.NoteLength enum.
     * @param duration Duration value.
     * @return src.NoteLength enum value.
     */
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

/**
 * src.BellNote class - Represents a musical note with length.
 */
class BellNote {
    final Note note;
    final NoteLength length;

    BellNote(Note note, NoteLength length) {
        if (note == null || length == null) {
            throw new IllegalArgumentException("Invalid BellNote: note or length is null");
        }
        this.note = note;
        this.length = length;
    }
}

/**
 * src.NoteLength enum - Represents the duration of a note.
 */
enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGTH(0.125f);

    private final int timeMs;

    NoteLength(float length) {
        timeMs = (int) (length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}

/**
 * src.Note enum - Represents musical notes.
 */
enum Note {
    REST, A4, A4S, B4, C4, C4S, D4, D4S, E4, F4, F4S, G4, G4S, A5;

    public static final int SAMPLE_RATE = 48 * 1024;
    public static final int MEASURE_LENGTH_SEC = 1;

    private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;
    private final double FREQUENCY_A_HZ = 440.0d;
    private final double MAX_VOLUME = 127.0d;
    private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

    Note() {
        int n = this.ordinal();
        if (n > 0) {
            final double halfStepUpFromA = n - 1;
            final double exp = halfStepUpFromA / 12.0d;
            final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);
            final double sinStep = freq * step_alpha;
            for (int i = 0; i < sinSample.length; i++) {
                sinSample[i] = (byte) (Math.sin(i * sinStep) * MAX_VOLUME);
            }
        }
    }

    public byte[] sample() {
        return sinSample;
    }
}