LividSnapshot {
    var <>knobStates, <>buttonStates, <>knobEnable, <>buttonEnable, <>fadeTime;

    *new { |knobStates, buttonStates, knobEnable, buttonEnable, fadeTime|
        ^super.newCopyArgs(knobStates, buttonStates, knobEnable, buttonEnable, fadeTime);
    }

    storeArgs {
        ^[knobStates, buttonStates, knobEnable, buttonEnable, fadeTime];
    }

    setKnob { |bank, num, val|
        knobStates[bank][num - 1] = val;
    }

    enableKnob { |bank, num, on|
        knobEnable[bank][num - 1] = on;
    }

    setButton { |bank, num, on|
        buttonStates[bank][num - 1] = on;
    }

    enableButton { |bank, num, on|
        buttonEnable[bank][num - 1] = on;
    }

    setKnobs { |...pairs|
        pairs.pairsDo { |num, val|
            val = val.asArray.flat;
            num.asArray.flat.do { |n, i|
                if (n.class == Association) {
                    if (n.value.isArray) {
                        n.value.do { |value|
                            // case like num = 2->(1..32)
                            this.setKnob(n.key, value, val.wrapAt(i));

                        };
                    } {
                        // case like num = 2->1
                        this.setKnob(n.key, n.value, val.wrapAt(i));
                    };
                } {
                    // case like num = 5
                    this.setKnob(0, n, val.wrapAt(i));
                };
            };
        };
    }

    enableKnobs { |...pairs|
        pairs.pairsDo { |num, val|
            val = val.asArray.flat;
            num.asArray.flat.do { |n, i|
                if (n.class == Association) {
                    if (n.value.isArray) {
                        n.value.do { |value|
                            // case like num = 2->(1..32)
                            this.enableKnob(n.key, value, val.wrapAt(i));
                        };
                    } {
                        // case like num = 2->1
                        this.enableKnob(n.key, n.value, val.wrapAt(i));
                    };
                } {
                    // case like num = 5
                    this.enableKnob(0, n, val.wrapAt(i));
                };
            };
        };
    }

    enableAllKnobs { |on|
        knobEnable.size.do { |bank|
            32.do { |i|
                knobEnable[bank][i] = on;
            };
        };
    }

    setButtons { |...pairs|
        pairs.pairsDo { |num, val|
            val = val.asArray.flat;
            num.asArray.flat.do { |n, i|
                if (n.class == Association) {
                    if (n.value.isArray) {
                        n.value.do { |value|
                            // case like num = 2->(1..32)
                            this.setButton(n.key, value, val.wrapAt(i));

                        };
                    } {
                        // case like num = 2->1
                        this.setButton(n.key, n.value, val.wrapAt(i));
                    };
                } {
                    // case like num = 5
                    this.setButton(0, n, val.wrapAt(i));
                };
            };
        };
    }

    enableButtons { |...pairs|
        pairs.pairsDo { |num, val|
            val = val.asArray.flat;
            num.asArray.flat.do { |n, i|
                if (n.class == Association) {
                    if (n.value.isArray) {
                        n.value.do { |value|
                            // case like num = 2->(1..32)
                            this.enableButton(n.key, value, val.wrapAt(i));
                        };
                    } {
                        // case like num = 2->1
                        this.enableButton(n.key, n.value, val.wrapAt(i));
                    };
                } {
                    // case like num = 5
                    this.enableButton(0, n, val.wrapAt(i));
                };
            };
        };
    }

    enableAllButtons { |on|
        buttonEnable.size.do { |bank|
            45.do { |i|
                buttonEnable[bank][i] = on;
            };
        };
    }
}

LividSnapshots {
    var <lividControl, <filePath;
    var <snapshots;

    *new { |lividControl, filePath|
        ^super.new.init(lividControl, filePath);
    }

    init { |arglividControl, argfilePath|
        lividControl = arglividControl;
        filePath = argfilePath;
        snapshots = ();
    }

    capture { |name, knobEnable, buttonEnable, fadeTime=1|
        var numBanks = lividControl.numBanks;
        var knobStates = lividControl.knobStates.deepCopy;
        var buttonStates = lividControl.buttonStates.deepCopy;
        if (knobEnable.isNil) {
            knobEnable = true ! 32 ! numBanks;
        };
        if (buttonEnable.isNil) {
            buttonEnable = true ! 45 ! numBanks;
        };
        name = name.asSymbol;
        snapshots[name] = LividSnapshot(knobStates, buttonStates, knobEnable, buttonEnable, fadeTime);
    }

    at { |name|
        name = name.asSymbol;
        ^snapshots[name];
    }

    restore { |name|
        lividControl.restoreSnapshot(snapshots[name]);
    }
}
