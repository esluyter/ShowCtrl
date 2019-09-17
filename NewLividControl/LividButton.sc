LividButton {
  var <id, <type = \push, <>func, <value = false, <color, <label;

  *new { |bank, num|
    ^super.new.init(bank, num);
  }

  init { |bank, num|
    id = bank -> num;
  }

  type_ { |val|
    val = val.asSymbol;
    if (val == \push) {
      this.value_(false);
      type = \push;
      ^this;
    };
    if (val == \toggle) {
      type = \toggle;
      ^this;
    };
    "Invalid type! Must be \\toggle or \\push.".warn;
  }

  value_ { |val|
    if (type == \toggle) {
      val = val.asBoolean;
      if (value != val) {
        value = val;
        func.(value);
        this.changed(\value, value);
      };
    } {
      if (val.asBoolean == true) {
        func.(true);
        this.changed(\pushed);
      };
    }
  }

  push {
    if (type == \toggle) {
      this.value_(value.not);
    } {
      this.value_(true);
    }
  }
}
