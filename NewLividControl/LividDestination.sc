LividDestination {
  var <outDevice, <>channel;

  *new { |deviceName, portName, channel = 0|
    ^super.new.init(deviceName, portName, channel);
  }

  *newWithDevice { |device, channel|
    ^super.new.initWithDevice(device, channel);
  }

  init { |deviceName, portName, argChannel|
    channel = argChannel;
    if (deviceName.notNil) {
      this.makeDevice(deviceName, portName);
    };
  }

  makeDevice { |deviceName, portName|
    outDevice = MIDIOut.newByName(deviceName, portName);
    outDevice.latency = 0;
  }

  initWithDevice { |device, argChannel|
    outDevice = device;
    channel = argChannel;
  }

  handleKnob { |bank, num, val|
    "knob! % % %".format(bank, num, val).postln;
  }

  handlePushButton { |bank, num|
    "push! % %".format(bank, num).postln;
  }

  handleToggleButton { |bank, num, val|
    "toggle! % % %".format(bank, num, val).postln;
  }
}
