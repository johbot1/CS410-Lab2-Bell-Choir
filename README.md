# Bell Choir Lab

## What is this?

With this second lab in our OS course, we are tasked with designing a Bell Choir to continue our
understanding of Multi-threaded programming. In this program, Conductor objects begin on 
their own threads, and spawn Members, who each have a singular note to play. Getting passed in a 
song text file, the Conductor doles out the commands at specific times to play a song.

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
  - **NOTE:** Please ensure your song text file is in the same directory.
    - (See the included song files for how to format your song to play properly)
  - **NOTE:** Please replace "{name of your song}" with the actual name of your song text file.

# Explanation of the lab

## Technical Overview

This Lab simulates a bell choir performance, demonstrating multi-threading and concurrency. 
Acting as our entry point, Tone.java acts as the setup for the file by reading in a musical
text file, and validating the notes. The Member class are representative of a musician, running
as an independent thread and responsible for playing a singular, specific note. The Conductor acts
as the director, iterating through the musical sheet, and instructing the Member threads to play
their assigned notes, at a specific time. Java's SourceDataLine acts as a shared resource, allowing
the member threads to produce the audio itself. This sim demonstrates how multiple threads can work
together, concurrently, to produce a musical performance, using thread coordination and resource management.

## Concurrency:

Each Member acts as an independent musician, responsible for a singular note. This concurrent execution
allows the choir to produce a layered sound, mirroring a REAL Bell Choir. If these Member threads
did not run concurrently, the sim is more or less just sequential playback of notes. The Conductor
contributes to this concurrency by initiating and timing each of the Member threads, allowing them to play
notes in parallel.

## Thread Coordination / Resource Management:

Apart from concurrency, the lab enphasizes thread coordination and resource management mainly though
the Conductor thread. The Conductor acts as, well, an ochestral conductor, ensuring each Member plays their
notes in the right time and duration, synchronizing the performance. The coordination is vital to avoid a 
garbage mess of chaotic audio output and produce a coherent musical performance. Additionally, the Conductor
manages the shared SourceDataLine resource, ensuring that the audio samples from each Member are written in
a controlled way, preventing race conditions and ensuring nice audio. Without the Conductor, 
the concurrent Member threads would play an unorganized garbled audio stream. And believe me, it sounded scary.

## Thread Safety:

Thread saftey is addressed mainly through the access to the SharedDataLine resource by the Conductor,
ensuring Member threads don't end up causing a race condition during audio output.

# Sources:

- Google Gemini was used for idea organization (Helping setup CHECKPOINTS.md, guiding my comment process, and 
how to word things a bit better than simply "It work good")