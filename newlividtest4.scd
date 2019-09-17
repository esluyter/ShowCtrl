MIDIIn.connectAll;
l = NewLividControl("Code", "Controls");
l.bankChangeButton = 37;
l.setCurrentBank(1)

a = MIDIOut.newByName("IAC Driver", "Bus 1");
l.addDestination(a)

l.currentBank.knobs[1].fadeTo(90)
l.currentBank.knobs[1].func = { |val| val.postln }

l.banks[0].knobs[1].value
l.banks[0].buttons[1].type = \toggle
l.banks[0].buttons[1].type = \frog
l.banks[0].buttons[1].value = true

l.currentBank.knobs.keysValuesDo { |num, knob| [num, knob].postln }
5.class
true.c
a


