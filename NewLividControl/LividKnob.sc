LividKnob {
  var <id, <>func, <value = 0, fadeRout, <color, <label;

  *new { |bank, num|
    ^super.new.init(bank, num);
  }

  init { |bank, num|
    id = bank -> num;
  }

  value_ { |val|
    value = val.asInteger;
    func.(value);
    this.changed(\value, value);
  }

  fadeTo { |val, dur = 1, refreshHz = 30|
    fadeRout.stop;
    fadeRout = nil;

    if (dur == 0) {
      ^this.value_(val);
    };

    fadeRout = fork {
      var waittime = refreshHz.reciprocal;
      var iterations = (dur * refreshHz).floor;
      var preVal = value;
      var difference = val - preVal;
      var increment = difference / iterations;

      iterations.do { |i|
        this.value_((increment * (i + 1) + preVal).floor);
        waittime.wait;
      };
    };
  }
}
