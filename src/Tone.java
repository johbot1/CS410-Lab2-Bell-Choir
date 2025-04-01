package src;

import java.io.File;
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
 * Tone class - Main class to load and play a song using Members and a src.Conductor.
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

        // Ensure a file path is provided
        if (args.length == 0) {
            System.err.println("Error: No song file provided. Please specify a file path.");
            return;
        }

        String filePath = args[0];

        // Attempt to load the song
        t.loadSong(filePath);

        // Initialize Member threads (one per note, excluding REST).
        t.startMembers();

        // Create SourceDataLine for audio playback.
        try (SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            // Create and start Conductor thread.
            Conductor conductor = new Conductor(t.members, 120, t.loadedSong, line);
            conductor.start();
            conductor.join(); // Wait for Conductor to finish.

        } catch (LineUnavailableException e) {
            System.err.println("Line unavailable: " + e.getMessage());
        }
    }

    /**
     * Tone constructor.
     * @param af AudioFormat for playback.
     */
    Tone(AudioFormat af) {
        this.af = af;
    }

    /**
     * Initializes Member threads, one for each Note (excluding REST).
     */
    private void startMembers() {
        for (Note note : Note.values()) {
            if (note != Note.REST) { // Skip REST note.
                members.add(new Member(("Member " + note), null, note, null)); // Create Member for each Note.
            }
        }
    }

    /**
     * Validates a line from the song file and returns an error message if invalid.
     * @param line Line to validate.
     * @return Error message if invalid, null if valid.
     */
    private String validateLine(String line) {
        String[] parts = line.trim().split("\\s+");

        if (parts.length != 2) {
            return "Invalid format (must be 'NOTE DURATION'): " + line;
        }

        String noteStr = parts[0];
        String durationStr = parts[1];

        // Validate the note
        if (!isValidNote(noteStr)) {
            return "Invalid note '" + noteStr + "' in line: " + line;
        }

        // Validate the duration
        if (!isValidDuration(durationStr)) {
            return "Invalid duration '" + durationStr + "' in line: " + line;
        }

        return null; // Line is valid
    }

    /**
     * Checks if the given note exists in the Note enum.
     */
    private boolean isValidNote(String noteStr) {
        try {
            Note.valueOf(noteStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the given duration is a valid integer greater than zero.
     */
    private boolean isValidDuration(String durationStr) {
        try {
            int duration = Integer.parseInt(durationStr);
            return duration > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Loads a song from a file. If any line is invalid, the song is not loaded.
     * @param filePath Path to the song file.
     */
    public void loadSong(String filePath) {
        loadedSong.clear();
        List<String> errors = new ArrayList<>();

        File file = new File(filePath);

        // Check if the file exists and is readable
        if (!file.exists() || !file.isFile()) {
            System.err.println("Error: File not found or invalid path: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            List<BellNote> tempSong = new ArrayList<>(); // Temporary storage

            while ((line = reader.readLine()) != null) {
                String error = validateLine(line);
                if (error != null) {
                    errors.add(error);
                } else {
                    tempSong.add(parseBellNote(line));
                }
            }

            // If there were errors, print them and do NOT load the song
            if (!errors.isEmpty()) {
                System.err.println("Error loading song. The following issues were found:");
                for (String error : errors) {
                    System.err.println("  - " + error);
                }
                return;
            }

            // No errors, so load the song
            loadedSong.addAll(tempSong);
            System.out.println("Song loaded successfully.");

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }


    /**
     * Parses a valid line into a BellNote.
     */
    private BellNote parseBellNote(String line) {
        String[] parts = line.trim().split("\\s+");
        Note note = Note.valueOf(parts[0]);
        int duration = Integer.parseInt(parts[1]);
        NoteLength length = mapDurationToNoteLength(duration);
        return new BellNote(note, length);
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
 * BellNote class - Represents a musical note with length.
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
 * NoteLength enum - Represents the duration of a note.
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
 * Note enum - Represents musical notes.
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