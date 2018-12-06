(
MIDIIn.connectAll;
g = LividControl("Code", "Controls");
g.bankChangeButton = 37;
g.addDestination(MIDIOut.newByName("IAC Driver", "Bus 1"));
g = LividControl("Code", "Controls");
g.bankChangeButton = 37;
g.addDestination(MIDIOut.newByName("IAC Driver", "Bus 1"));
g = LividControl("Code", "Controls");
g.bankChangeButton = 37;
g.addDestination(MIDIOut.newByName("IAC Driver", "Bus 1"));
g.buttonFuncs = [
  (
    24: [\toggle, { |on|
      if (on) { "hello there!".postln } { "goodbye".postln }
    } ],
    26: { "whatever".postln },
    23: \toggle,
    22: \toggle,
    21: \toggle
  ),
  (),
  (),
  ()
];
g.colors = [
  (1: \cyan, 2: \magenta, 3: \blue, 4: \magenta, 5: \cyan, 6: \cyan, 7: \blue, 8: \red, 9: \magenta, 10: \magenta, 11: \blue, 12: \red, 13: \green, 14: \magenta, 15: \blue, 16: \red, 17: \green, 18: \green, 19: \blue, 20: \blue, 21: \green, 22: \green, 23: \blue, 24: \blue, 25: \green, 26: \green, 27: \blue, 28: \blue, 29: \magenta, 30: \blue, 31: \blue, 32: \blue, 42: \blue, 43: \blue, 44: \blue, 45: \blue),
  (),
  (),
  ()
];
g.labels = [
  (1: "horror\ndrone", 2: " drone\n filter", 3: "  orch +\npenderecki", 4: "resonator\n pitch", 5: "horror\nmusic", 6: " love\ngrains", 7: "pno grains\n+ chimes", 8: "resonator", 9: " love\n delay", 10: " love\n filter", 11: "  love", 12: "church\n verb", 13: " piano", 14: " TEMPO +\ndhrup filt", 15: "dhrupad\n+ bell", 16: "crackle", 17: "reverse\n piano", 18: "granular\n  harp", 19: "  eno \n+ synths", 20: "  low\n wind", 21: "granular\n bells", 22: "granular\n guqin", 23: "bufdrone\n+ synths", 24: " high\n wind", 25: "horror\norgan", 26: "granular\nenigma", 27: " slow\n bach", 28: "pitched\n wind", 29: "lx pitch +\ncock verb", 30: "cocktail\nrecord", 31: "mather +\nwasteland", 32: "film fx", 33: " love 1", 34: " love 2", 35: " love 3", 36: " love 4", 42: "  film\n thunder", 43: "  film\n  fight", 44: "  film\n  crash", 45: "  film\n  laugh"),
  (),
  (),
  ()
];
g.makeWindow;
)


~knob1 = 2;
~knob2 = 18;
~knob3 = 1->5;
~knob4 = 1->6;
~page1 = [~knob3, ~knob4];
~page2 = [~knob1, ~knob2];

g.setKnob(0, 20, 10)
g.setKnobs(~knob1, 50)
g.setKnobs(~knob2, 35)
g.setKnobs(~page1, 100)
g.setKnobs([~page1, ~page2], 0)
g.setKnobs([2->(1..32), 3->(1..32), ~page1, ~page2], 100)
g.setKnobs(4.collect(_->(1..32)), 0)

g.pushButtons([2, 3, 4, 24, 42, 43, 44])
g.pushButtons(0->(21..24))
g.setButtons((21..24), \off)


g.fadeKnob(0, 32, 100, 5)
g.fadeKnob(0, 32, 0, 0.3)
g.setKnobs(32, 0)


r = fork { inf.do { |i| i.postln; 0.2.wait; }}

r.stop
nil.stop

(
a = (
  knobStates: g.knobStates.deepCopy,
  buttonStates: g.buttonStates.deepCopy,
  knobEnable: [true!32] ++ (false!34 ! 3),
  buttonEnable: true!45 ! 4,
  fadeTime: 4
);
b = (
  knobStates: g.knobStates.deepCopy,
  buttonStates: g.buttonStates.deepCopy,
  knobEnable: [false!32] ++ (true!34 ! 3),
  buttonEnable: true!45 ! 4,
  fadeTime: 20
);
)

a.postcs
g.setKnobs(4.collect(_->(1..32)), 0)

(
g.restoreSnapshot(a);
g.restoreSnapshot(b);
)

