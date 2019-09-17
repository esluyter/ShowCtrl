NewLividControl {
  var <device, <destinations, <banks, <customBanks, <currentBank, <bankChangeButton;

  *new { |deviceName, portName, numBanks = 4, channel = 0, makeDef = true|
    ^super.new.init(deviceName, portName, max(numBanks.floor, 1), channel, makeDef);
  }

  init { |deviceName, portName, numBanks, channel, makeDef|
    device = LividDevice(deviceName, portName, channel, makeDef);
    device.addDependant(this);

    destinations = [];

    banks = numBanks.collect { |i|
      var bank = LividBank(i);
      bank.addDependant(this);
      bank
    };
    this.setCurrentBank(0);
  }

  update { |obj, what, args|
    if (obj.class == LividDevice)  { // input from controller
      this.prUpdateFromDevice(obj, what, args);
    };
    if (obj.class == LividBank) { // internal value change
      this.prUpdateFromBank(obj, what, args);
    };
  }

  prUpdateFromDevice { |obj, what, args|
    var num = args[0];
    var val = args[1];
    if (what == \knob) {
      currentBank.knobs[num].value = val;
    };
    if (what == \button) {
      if (num == bankChangeButton) {
        this.incrementBank;
      } {
        currentBank.buttons[num].push;
      };
    }
  }

  prUpdateFromBank { |obj, what, args|
    var id = args[0];
    var bank = id.key;
    var num = id.value;
    var param = args[1];
    var val = args[2];

    this.prUpdateDestinations(what, param, bank, num, val);

    if (currentBank.id == bank) {
      if (what == \knob) {
        if (param == \value) {
          device.setKnob(num, val);
        };
      };
      if (what == \button) {
        if (param == \pushed) {
          device.pushButton(num);
        };
        if (param == \value) {
          device.setButton(num, val);
        };
      };
    };
  }

  prUpdateDestinations { |what, param, bank, num, val|
    if (what == \knob and: (param == \value)) {
      destinations.do(_.handleKnob(bank, num, val));
    };
    if (what == \button) {
      if (param == \pushed) {
        destinations.do(_.handlePushButton(bank, num));
      };
      if (param == \value) {
        destinations.do(_.handleToggleButton(bank, num, val));
      }
    };
  }

  setKnobs { |...pairs|

  }

  pushButtons { |...buttnums|

  }

  setButtons { |...pairs|

  }

  bankChangeButton_ { |val|
    bankChangeButton = val;
    device.setBankChangeButton(currentBank.id, bankChangeButton);
  }

  setCurrentBank { |bankid|
    if (bankid < banks.size) {
      currentBank = banks[bankid];
      device.loadBank(currentBank, bankChangeButton);
    };
    this.changed(\currentBank, currentBank);
  }

  incrementBank {
    this.setCurrentBank(currentBank.id + 1 % banks.size);
  }

  decrementBank {
    this.setCurrentBank(currentBank.id - 1 % banks.size);
  }

  addDestination { |outDevice, channel = 0|
    destinations = destinations.add(LividDestination.newWithDevice(outDevice, channel));
  }

  clearDestinations {
    destinations = [];
  }

  setStyles { |...pairs|

  }

  makeWindow {

  }
}
