# CHECKPOINTS.md

## 1. Setup and Data Handling ✅
- [✅] 1.1. Ensure `Tone` correctly loads and validates song files
- [✅] 1.2. Confirm `BellNote` and `NoteLength` correctly map notes and durations
- [✅] 1.3. Modify `Tone` to store parsed song data in a shared structure

## 2. Implement the `Member` Class (Threaded) ✅
- [✅] 2.1. Create a `Member` class that holds up to two assigned `BellNote`s
- [✅] 2.2. Implement a method for `Member` to play a note when signaled
- [✅] 2.3. Ensure `Member` runs as a separate thread and waits for cues

## 3. Implement the `Conductor` Class (Threaded) ✅
- [✅] 3.1. Create a `Conductor` class that maintains tempo
- [✅] 3.2. Implement a method to signal the correct `Member` to play
- [✅] 3.3. Use `Thread.sleep()` for tempo control and rests

## 4. Synchronization and Execution Flow ✅
- [ ✅] 4.1. Implement a shared queue or signaling mechanism for communication
  - [✅] 4.1.1. Implement and End-of-Performance Signal
  - [✅] 4.1.2. Ensure Members Process Notes Continuously
- [✅] 4.2. Ensure `Conductor` plays notes sequentially, one at a time
- [✅] 4.3. Test multiple members playing assigned notes without conflicts

## 5. Final Testing and Refinements
- [ ] 5.1. Test with multiple song files to ensure compatibility
- [ ] 5.2. Optimize thread synchronization for smoother execution
- [ ] 5.3. Implement error handling for misassigned notes or timing issues
- [ ] 5.4  Correctly configured ANT build.xml to be able to play multiple songs 

