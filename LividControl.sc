LividControl {
    var <>a; // additional midi destinations
    var <>outDevice, <>inDevice;
    var <>knobFuncs, <>buttonFuncs, <>knobStates, <>buttonStates;
    var <>numBanks, <currentBank = 0, <>bankChangeButton;
    var <>inChannel = 0, <>outChannel = 9;
    var <>colors, <>labels;


    currentBank_ { |newCurrentBank|
        currentBank = newCurrentBank;
        knobStates[currentBank].do { |val, num|
            outDevice.control(inChannel, num + 1, val);
        };
        if (bankChangeButton.notNil) {
            outDevice.noteOn(0, 37, 127/3 * currentBank);
        };
        this.changed(\currentBank, newCurrentBank);
    }

    incrementBank {
        this.currentBank_(currentBank + 1 % numBanks);
    }

    decrementBank {
        this.currentBank_(currentBank - 1 % numBanks);
    }

    addDestination { |destination|
        destination.latency_(0);
        a = a.add(destination);
    }



    *new { |deviceName = "Code", portName = "Controls", numBanks = 4, makeDef = true|
        ^super.new.init(deviceName, portName, max(numBanks.floor, 1), makeDef);
    }

    init { |deviceName, portName, argNumBanks, makeDef|
        outDevice = MIDIOut.newByName(deviceName, portName);
        inDevice = MIDIIn.findPort(deviceName, portName);

        outDevice.latency = 0;

        numBanks = argNumBanks;
        knobFuncs = () ! numBanks;
        buttonFuncs = () ! numBanks;
        knobStates = 0!32 ! numBanks;
        buttonStates = false!45 ! numBanks;
        labels = () ! numBanks;
        colors = () ! numBanks;


        a = [];

        if (makeDef) { this.makeDef };
    }

    makeDef {
        MIDIdef.cc(\lividknobs, { |val, num|
            this.handleKnob(currentBank, num, val);
        }, chan: inChannel, srcID: inDevice.uid);

        MIDIdef.noteOn(\lividbuttons, { |vel, num|
            this.handleButton(currentBank, num, vel);
        }, chan: inChannel, srcID: inDevice.uid);
    }

    handleKnob { |bank, num, val|
        if (knobFuncs[bank][num].value(val) != false) {
            a.do(_.control(outChannel + bank, num, val));
        };
        knobStates[bank][num - 1] = val;
        this.changed(\knob, [bank, num, val]);
    }

    handleButton { |bank, num, vel|
        var button = buttonFuncs[bank][num];

        if (num == bankChangeButton) {
            this.incrementBank;
            ^false;
        };

        if (button.asArray[0] == \toggle) {
            // handle toggle
            var state;
            buttonStates[bank][num - 1] = buttonStates[bank][num - 1].not;
            state = buttonStates[bank][num - 1];
            button[1].value(state);
            // visual display
            if (bank == currentBank) {
                //if (state) { "% light on".format(num).postln; } { "% light off".format(num).postln; };
                outDevice.noteOn(inChannel, num, 127 * state.asInt)
            };
            this.changed(\button, [bank, num, state]);
        } {
            // handle push button
            if (button.value(vel) != false) {
                a.do(_.noteOn(outChannel + bank, num, vel))
            };
            // update light
            if (bank == currentBank) {
                {
                    //"% light on".format(num).postln;
                    outDevice.noteOn(0, num, 127);
                    0.1.wait;
                    //"% light off".format(num).postln;
                    outDevice.noteOn(0, num, 0);
                }.fork(AppClock);
            };
            this.changed(\buttonPush, [bank, num, vel]);
        }
    }



    setKnob { |bank, num, val|
        outDevice.control(inChannel, num, val);
        this.handleKnob(bank, num, val);
    }
}
