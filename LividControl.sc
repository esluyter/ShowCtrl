LividControl {
    var <>a; // additional midi destinations
    var <>outDevice, <>inDevice;
    var <>knobFuncs, <>buttonFuncs, <knobStates, <buttonStates, <knobRouts;
    var <>numBanks, <currentBank = 0, <>bankChangeButton;
    var <>inChannel = 0, <>outChannel = 9;
    var <colors, <labels;


    currentBank_ { |newCurrentBank|
        currentBank = newCurrentBank;
        if (outDevice.notNil) {
            knobStates[currentBank].do { |val, num|
                outDevice.control(inChannel, num + 1, val);
            };
            if (bankChangeButton.notNil) {
                outDevice.noteOn(0, 37, 127/3 * currentBank);
            };
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

    setStyles { |...pairs|
        pairs.pairsDo { |num, val|
            var color = false, label = false;
            val.asArray.do { |item|
                if (item.class == String) {
                    label = item;
                };
                if (item.class == Color || (item.class == Symbol)) {
                    color = item;
                };
                if (item.isNil) {
                    color = nil;
                    label = nil;
                };
            };
            num.asArray.flat.do { |n|
                if (n.class == Association) {
                    if (n.value.isArray) {
                        n.value.do { |value|
                            // case like num = 2->(1..32)
                            if (color != false) {
                                colors[n.key][value] = color;
                            };
                            if (label != false) {
                                labels[n.key][value] = label;
                            };
                        };
                    } {
                        // case like num = 2->1
                        if (color != false) {
                            colors[n.key][n.value] = color;
                        };
                        if (label != false) {
                            labels[n.key][n.value] = label;
                        };
                    };
                } {
                    // case like num = 5
                    if (color != false) {
                        colors[0][n] = color;
                    };
                    if (label != false) {
                        labels[0][n] = label;
                    };
                };
            };
            this.changed(\styles);
        }
    }



    *new { |deviceName, portName, numBanks = 4, makeDef = true|
        ^super.new.init(deviceName, portName, max(numBanks.floor, 1), makeDef);
    }

    init { |deviceName, portName, argNumBanks, makeDef|
        numBanks = argNumBanks;
        knobFuncs = () ! numBanks;
        buttonFuncs = () ! numBanks;
        knobStates = 0!32 ! numBanks;
        knobRouts = nil!32 ! numBanks;
        buttonStates = false!45 ! numBanks;
        labels = () ! numBanks;
        colors = () ! numBanks;
        a = [];

        if (deviceName.notNil) {
            this.makeDevice(deviceName, portName, makeDef);
        };
    }

    makeDevice { |deviceName, portName, makeDef = true|
        outDevice = MIDIOut.newByName(deviceName, portName);
        inDevice = MIDIIn.findPort(deviceName, portName);
        outDevice.latency = 0;
        if (makeDef) { this.makeDef };
    }

    makeDef {
        MIDIdef.cc(\lividknobs, { |val, num|
            knobRouts[currentBank][num - 1].stop;
            knobRouts[currentBank][num - 1] = nil;
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
            this.handleToggle(bank, num, buttonStates[bank][num - 1].not);
        } {
            this.handlePushButton(bank, num, vel);
        }
    }

    handleToggle { |bank, num, on|
        buttonStates[bank][num - 1] = on;
        buttonFuncs[bank][num].asArray[1].value(on);
        // visual display
        if (bank == currentBank && (outDevice.notNil)) {
            outDevice.noteOn(inChannel, num, 127 * on.asInt)
        };
        this.changed(\button, [bank, num, on]);
    }

    handlePushButton { |bank, num, vel|
        if (buttonFuncs[bank][num].value(vel) != false) {
            a.do(_.noteOn(outChannel + bank, num, vel))
        };
        // update light
        if (bank == currentBank && (outDevice.notNil)) {
            {
                outDevice.noteOn(0, num, 127);
                0.1.wait;
                outDevice.noteOn(0, num, 0);
            }.fork(AppClock);
        };
        this.changed(\buttonPush, [bank, num, vel]);
    }



    setKnob { |bank, num, val|
        if (outDevice.notNil && (bank == currentBank)) {
            outDevice.control(inChannel, num, val);
        };
        this.handleKnob(bank, num, val);
    }

    fadeKnob { |bank, num, val, dur = 1, hz = 30|
        knobRouts[bank][num - 1].stop;
        knobRouts[bank][num - 1] = nil;
        if (dur == 0) {
            this.setKnob(bank, num, val);
        } {
            var waittime = hz.reciprocal;
            var iterations = (dur * hz).floor;
            var preVal = knobStates[bank][num - 1];
            var difference = val - preVal;
            var increment = difference / iterations;
            knobRouts[bank][num - 1] = fork {
                iterations.do { |i|
                    var value = increment * (i + 1) + preVal;
                    this.setKnob(bank, num, value.floor);
                    waittime.wait;
                };
            };
        };
    }

    setKnobs { |...pairs|
        pairs.pairsDo { |num, val|
            val = val.asArray.flat;
            num.asArray.flat.do { |n, i|
                if (n.class == Association) {
                    if (n.value.isArray) {
                        n.value.do { |value|
                            // case like num = 2->(1..32)
                            this.fadeKnob(n.key, value, val.wrapAt(i), 0);
                        };
                    } {
                        // case like num = 2->1
                        this.fadeKnob(n.key, n.value, val.wrapAt(i), 0);
                    };
                } {
                    // case like num = 5
                    this.fadeKnob(0, n, val.wrapAt(i), 0);
                };
            };
        };
    }

    pushButton { |bank, num|
        this.handleButton(bank, num, 127);
    }

    pushButtons { |...buttnums|
        buttnums.do { |num|
            num.asArray.flat.do { |n|
                if (n.class == Association) {
                    if (n.value.isArray) {
                        n.value.do { |value|
                            this.handleButton(n.key, value, 127);
                        };
                    } {
                        this.handleButton(n.key, n.value, 127);
                    };
                } {
                    this.handleButton(0, n, 127);
                };
            };
        };
    }

    setButton { |bank, num, on|
        var button = buttonFuncs[bank][num];
        var state = buttonStates[bank][num - 1];

        if (button.asArray[0] == \toggle) {
            if (state == on) {
                // toggle without change -- pass
            } {
                // toggle with change
                this.handleToggle(bank, num, on);
            };
        } {
            // not a toggle
            if (on) {
                this.handleButton(bank, num, 127);
            };
        }
    }

    setButtons { |...pairs|
        pairs.pairsDo { |num, on|
            if (on.class == Symbol) {
                on = (on == \on);
            };

            on = on.asArray.flat;
            num.asArray.flat.do { |n, i|
                if (n.class == Association) {
                    if (n.value.isArray) {
                        n.value.do { |value|
                            this.setButton(n.key, value, on.wrapAt(i))
                        };
                    } {
                        this.setButton(n.key, n.value, on.wrapAt(i));
                    };
                } {
                    this.setButton(0, n, on.wrapAt(i));
                };
            };
        };
    }



    restoreSnapshot { |snapshot|
        var dur = snapshot.fadeTime;
        snapshot.knobStates.do { |bankStates, bank|
            bankStates.do { |value, i|
                if (snapshot.knobEnable[bank][i]) {
                    this.fadeKnob(bank, i + 1, value, dur);
                };
            };
        };
        snapshot.buttonStates.do { |bankStates, bank|
            bankStates.do { |on, i|
                if (snapshot.buttonEnable[bank][i]) {
                    this.setButton(bank, i + 1, on)
                };
            };
        };
    }



    makeWindow {
        var win, view;
        win = Window("Livid Code", Rect(Window.screenBounds.width, 0, 640, 493)).front;
        view = LividControlView(win, Rect(0, 0, 640, 490), lividControl: this);
        ^win;
    }
}
