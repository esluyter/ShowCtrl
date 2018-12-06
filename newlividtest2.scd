l = LividControl("Code", "Controls");
l.makeWindow;
l.addDestination(a);
l.latency = 0;
l.numBanks = 4;
l.bankButton = 37;

l.makeDef;

l.setKnob(5, 127); // defaults to bank 0
l.setKnob([1, 5], 127); // knob 5 in bank 1

StackLayout


(
MIDIIn.connectAll;
g = LividControl();
g.bankChangeButton = 37;
g.addDestination(MIDIOut.newByName("IAC Driver", "Bus 1"));
g.buttonFuncs = [
  (
    24: [\toggle, { |on|
      if (on) { "hello there!".postln } { "goodbye".postln }
    } ],
    26: { "whatever".postln }
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
w = Window.new("Livid Code", Rect(Window.screenBounds.width, 0, 640, 493));
v = LividControlView(w, Rect(0, 0, 640, 490), lividControl: g);
w.front
)


v.mainView.knobs[0].knobView.color


g.setKnob(0, 20, 0)


b = [[0, (1..32)], [[1, 2], [3, 4]], [0, 5], [0, 6]]
b.shape
b.collect(_.class)

[0, (1..32)].shape
a.rank

(
f = { |arr|
  arr.postln;
  if (arr.flop.size > 2) {
    arr.do { |item|
      item.postln;
      f.(item);
    }
  }
};
f.(a)
)




0->4
3->6

[`[0, 3], `[0, 5], [`[1, 6], `[3, (1..32)]]].flat
