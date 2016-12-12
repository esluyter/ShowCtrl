ShowCtrlContextMenu {
  classvar <>font;
  classvar rout;
  classvar menu;
  classvar thisview;

  *initClass {
    Class.initClassTree(Font);
    font = Font.default;
  }

  *remove { thisview.remove }

  *create { |cueListView, x, y ...pairs|
    var items = [], functions = [];
    var height, width;
    var fontheight = " ".bounds(font).height * 1.1;
    var maxitemwidth = 10;
    var parentview = cueListView;
    var cueList = cueListView.cueList;

    menu.remove; // only have one open at a time;
    thisview.remove;

    pairs.pairsDo { |item, func|
      var testwidth;
      items = items.add(item);
      functions = functions.add(func);

      testwidth = item.asString.bounds(font).width * 1.2;
      if (testwidth > maxitemwidth) {
        maxitemwidth = testwidth;
      };
    };
    height = items.size * (fontheight) + 5;
    width = max(maxitemwidth, 200);

    thisview = UserView(parentview, Rect(x, y, width, height + 120))
    .background_(cueListView.gui[\textBox].palette.base.blend(cueListView.gui[\textBox].palette.base.complementary, 0.1).alpha_(0.9))
    .mouseLeaveAction_({ |view| rout = fork { 0.6.wait; defer { view.remove; } } })
    .mouseOverAction_({ rout.stop })
    .focusLostAction_({ |view| view.remove; });

    menu =
    if (items.size > 0) {
      ListView(thisview, Rect(0, 0, width, height))
      .font_(font)
      .items_(items)
      .mouseOverAction_({|menu, x, y|
        var idx = (y/(fontheight)).floor;
        if (items[idx].asSymbol == '') {
          menu.selection_(-1);
        } {
          menu.selection_(idx);
        };

        rout.stop;
      })
      .mouseUpAction_({ |menu, x, y|
        functions.do { |func, i|
          if (menu.selection[0] == i) { func.value() };
        };
        thisview.remove;
      });
    } { nil };

    StaticText(thisview, Rect(5, height, width - 10, 20)).string_("Cue color").stringColor_(cueListView.gui[\textBox].palette.baseText);

    Button(thisview, Rect(5, height + 20, width - 10, 20))
    .states_([[" ", nil, thisview.background]])
    .action_({ cueList.currentCueColor = nil; });

    3.do { |j|
      8.do { |i|
        var bgcolor = case
        { true } { Color.hsv(
          i.linlin(0, 8, 0, 1),
          case
          { j == 0 } { 0.3 }
          { j == 1 } { 1 }
          { j == 2 } { 1 },
          case
          { j == 0 } { 0.8 }
          { j == 1 } { 1 }
          { j == 2 } { 0.5 }
        )};

        Button(thisview, Rect(5 + (((width - 5) / 8) * i), height + 45 + (25 * j), ((width - 5) / 8) - 5, 20))
        .states_([[" ", nil, bgcolor]])
        .action_({ cueList.currentCueColor = bgcolor; });
      };
    };
    if (menu.notNil) { menu.focus };
  }
}
