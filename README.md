# Bell Choir Lab

## What is this?

With this second lab in our OS course, we are tasked with designing a Bell Choir to continue our
understanding of Multi-threaded programming. In this program, the Tone class sets up our environment, 
and starts Members on seperate threads. These Member Threads are in charge of a specific note, able to
play it when it's told to by the Conductor. The Conductor is on his own seperate thread, reading in the 
music text file, and assigning notes to each of the Members, and signalling to them to play at the right time.

## Requirements:

### Apache Ant:

Apache Ant is a build tool to quickly run java files from the command line.
Instructions for install found at: https://ant.apache.org/manual/install.html  
To ensure you have Ant installed and properly running use the following command  
to check your installation version: `ant -v` or `ant --version`

## How to Run:

- Download the files
- Navigate to the directory you have the files, and run
  the following Ant build command `ant -Dsong.file={name of your song}.txt`
  - **NOTE:** To load in songs through a USB the command changes:
    - If using a Windows machine:  `ant -Dsong.file={name of the USB}:\song.txt`
    - If using a Linux/macOS machine: `ant -Dsong.file=/Volumes/{name of the USB}/song.txt`

# Explanation of the lab
## Technical Overview
This Lab simulates a bell choir performance, demonstrating multi-threading and concurrency. 
Acting as the entry point, Tone.java acts as the setup for the file by reading in a musical
text file, validating the notes, and setting up the audio. The Conductor class is the director,
with his own seperate thread, iterating through the loaded in file, and signalling the right Member
using commands like wait() and notify(). Each Member thread, plays it's note in the shared 
SourceDataLine. It all comes together to show how multiple threads work together via signalling
to manage a shared resource (The SourceDataLine), and put on a great show!

## Concurrency:
Each Member acts as an independent musician, responsible for a singular note. This concurrent execution 
by multiple Member threads within their own run() methods is essential for producing a kinda layered sound, 
mirroring a real bell choir. If the note playing wasn't handled properly by a Member,
the simulation would go back to playback controlled solely by the Conductor, even if 
multiple Members exist. The Conductor enables this concurrency by signaling the specific
Member threads at the correct musical times, allowing their run() methods to execute 
the audio playback based on the song's structure.

## Thread Coordination / Resource Management:
Apart from concurrency, the lab enphasizes thread coordination and resource management. The Conductor acts as, 
well, an ochestral conductor, ensuring each Member plays their notes in the right time and duration, synchronizing 
the performance. The coordination, using wait()/notify(), is vital to avoid a garbage mess of chaotic audio output 
and produce a coherent musical performance. While multiple Member threads could write to the SourceDataLine 
concurrently if the music requires (like chords signaled close together), the Conductor ensures this happens only 
at the musically appropriate times. Without the Conductor, the concurrent Member threads would play an unorganized 
garbled audio stream. And believe me, it sounds exactly as awful as you think it does.

## Thread Safety:

Thread saftey is addressed through internal Member state variables. Things like wait/notify signaling  are 
protected using techniques like synchronized blocks, volatile variables and atomic booleans. The Conductor on the other
hand, signals the Members safely, and at the right time. The shared SourceDataLine is a resource that can be accessed
by multiple Members, but the coordination done by the Conductor ensures Members who have been instructed can write to
it.

# Sources:

- Google Gemini was used for idea organization (Helping setup CHECKPOINTS.md, guiding my comment process, and 
how to word things a bit better than simply "It work good" within my PDF)