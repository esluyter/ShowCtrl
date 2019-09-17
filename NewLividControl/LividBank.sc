LividBank {
  var <id, <knobs, <buttons;

  *new { |id|
    ^super.new.init(id);
  }

  init { |argId|
    id = argId;
    knobs = ();
    buttons = ();
    32.do { |i|
      var knob = LividKnob(id, i + 1);
      knob.addDependant(this);
      knobs[i + 1] = knob;
    };
    45.do { |i|
      var button = LividButton(id, i + 1);
      button.addDependant(this);
      buttons[i + 1] = button;
    };
  }

  update { |obj, what, val|
    if (obj.class == LividKnob) {
      this.changed(\knob, [obj.id, what, val]);
    };
    if (obj.class == LividButton) {
      this.changed(\button, [obj.id, what, val]);
    };
  }
}
