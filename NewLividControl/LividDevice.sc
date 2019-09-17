LividDevice {
  var <outDevice, <inDevice, <>channel, <knobMidiFunc, <buttonMidiFunc;

  *new { |deviceName, portName, channel = 0, makeDef = true|
    ^super.new.init(deviceName, portName, channel, makeDef);
  }

  init { |deviceName, portName, argChannel, makeDef|
    channel = argChannel;
    if (deviceName.notNil) {
      this.makeDevice(deviceName, portName);
    };
    if (makeDef) { this.makeDef };
  }

  makeDevice { |deviceName, portName|
    outDevice = MIDIOut.newByName(deviceName, portName);
    inDevice = MIDIIn.findPort(deviceName, portName);
    outDevice.latency = 0;
  }

  makeDef {
    knobMidiFunc.free;
    buttonMidiFunc.free;
    if (inDevice.notNil) {
      knobMidiFunc = MIDIFunc.cc({ |val, num|
        this.changed(\knob, [num, val]);
      }, chan: channel, srcID: inDevice.uid);

      buttonMidiFunc = MIDIFunc.noteOn({ |vel, num|
        this.changed(\button, [num]);
      }, chan: channel, srcID: inDevice.uid);
    };
  }

  setKnob { |num, val|
    outDevice.control(channel, num, val);
  }

  setButton { |num, val|
    if (val.isInteger) {
      outDevice.noteOn(channel, num, val);
    } {
      outDevice.noteOn(channel, num, val.asInteger * 127);
    }
  }

  pushButton { |num|
    {
      this.setButton(num, true);
      0.1.wait;
      this.setButton(num, false);
    }.fork(AppClock);
  }

  loadBank { |bank, bankChangeButton|
    bank.knobs.keysValuesDo { |num, knob|
      this.setKnob(num, knob.value);
    };
    bank.buttons.keysValuesDo { |num, button|
      this.setButton(num, button.value);
    };
    this.setBankChangeButton(bank.id, bankChangeButton);
  }

  setBankChangeButton { |banknum, bankChangeButton|
    if (bankChangeButton.notNil) {
      this.setButton(bankChangeButton, (127/3 * banknum).asInteger);
    };
  }
}
