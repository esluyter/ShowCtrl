# ShowCtrl

This repository contains miscellaneous SuperCollider classes I use to run theatrical shows.

## CueList

Interface for organizing SC functions into an ordered list of named cues, with functionality for nesting, linking, color coding, etc.

Related files:
- CueList.sc
- CueListGUI.sc
- ShowCtrlContextMenu.sc
- defaultCue/*

## CL1

Interface for controlling Yamaha CL series consoles over MIDI, including state memory for channel sends and DCA / fader positions, ability to set / fade individual or groups of sends / faders to target positions, restore / save EQs to console memory, and set individual EQ parameters.

Designed for ease/speed of use in real-time rehearsal situations using a code interface rather than GUI. As an example of common usage, to fade some of the sends on an arbitrarily large group of channels ~everyone over 2 seconds:

```
...
c.fadeSends(~everyone,
  \dur, 2,
  [~adat3, ~upas, ~mains, ~center],  0,
  ~delays, -3
);
```

Related files:
- YamahaBoard.sc
