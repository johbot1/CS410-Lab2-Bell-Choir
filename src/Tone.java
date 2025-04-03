package src;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     */
    public static void main(String[] args) { //
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Tone t = new Tone(af);

        if (args.length == 0) {
            System.err.println("Error: No song file provided. Please specify a file path.");
            return;
        }
        String filePath = args[0];

        try {
            t.loadSong(filePath);
            if (t.loadedSong == null) {
                System.err.println("Error: Song loading failed! Path is null");
                return;
            }
            if (t.loadedSong.isEmpty()) {
                System.err.println("Error: Song loading failed! Check your pathing, cause there's nothing in here");
                return;
            }

            t.startMembers(); // Creates and starts Member threads
            System.out.println("Begin performance!");

            Conductor conductor = null;
            try (SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
                line.open(af, Note.SAMPLE_RATE); // Specify buffer size?
                line.start();

                conductor = new Conductor(t.members, 120, t.loadedSong, line); // Example BPM: 120
                conductor.start(); // Start the Conductor thread

                conductor.join(); // Wait for Conductor thread to complete its run() method

                // Drain the line to ensure all buffered audio plays
                line.drain();

            } catch (LineUnavailableException e) {
                System.err.println("Audio line unavailable");
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted while waiting for Conductor.");
                if (conductor != null) {
                    conductor.interrupt(); // Signal conductor to stop if main is interrupted
                }
                Thread.currentThread().interrupt();
            } catch (Exception e) { // Catch other potential exceptions
                System.err.println("Wow.\nThis goofed up so bad I don't have an error for it.\nHere's the message though: " + e.getMessage());
            } finally {
                // --- Cleanup: Ensure all Member threads are stopped and joined ---
                if (t.members != null) {
                    for (Member member : t.members) {
                        if (member != null) {
                            member.stopPlaying();
                        }
                    }
                }

                if (t.members != null) {
                    for (Member member : t.members) {
                        if (member != null && member.isAlive()) { // Only join live threads
                            member.joinThread(); // Wait for the member thread to finish
                        }
                    }
                }
                System.out.println("All Member threads joined. The performance has now concluded!");
            }

        } catch (Exception e) { // Catch errors during setup (loading, starting members)
            System.err.println("Something went wrong during setup.\n I don't know what, but here's the message: " + e.getMessage());
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
        System.out.println("Initializing your choir...!");
        // Assuming Note enum has values like C4, D4 etc.
        for (Note note : Note.values()) {
            if (note != Note.REST) { // Create a member for each playable note
                Member member = new Member("Member-" + note.name(), note);
                members.add(member);
                member.start(); // START THE THREAD!
            }
        }
    }

    /**
     * Validates a line from the song file and returns an error message if invalid.txt.
     * @param line Line to validate.
     * @return Error message if invalid.txt, null if valid.
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
     * Loads a song from a file. If any line is invalid.txt, the song is not loaded.
     * @param filePath Path to the song file.
     */
    public void loadSong(String filePath) {
        loadedSong.clear();
        List<String> errors = new ArrayList<>();

        File file = new File(filePath);

        // Check if the file exists and is readable
        if (!file.exists() || !file.isFile()) {
            System.err.println("Error: File not found or invalid.txt path: " + filePath);
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