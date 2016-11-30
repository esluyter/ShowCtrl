ShowCtrlView : SCViewHolder {
  var <showCtrl;
  var <gui, <leftPanelWidth, bottomPanelHeight = 50, margin = 5, buttonWidth = 75;
  var dragStart;
  var savedSelection;

  *new { |parent, bounds|
    ^super.new.init(parent, bounds);
  }

  init { |parent, bounds|
    bounds = bounds ?? Rect(0, 0, parent.bounds.width, parent.bounds.height);
    view = View(parent, bounds);

    leftPanelWidth = bounds.width / 3;

    gui = (
      topBlackPanel: View(view, Rect(leftPanelWidth, margin, bounds.width - leftPanelWidth - margin, 35 + (2*margin)))
      .background_(Color.black)
      .resize_(2),

      curScene: StaticText(view, Rect(leftPanelWidth + margin, margin, bounds.width - leftPanelWidth - (3*margin), 45))
      .string_("Current scene")
      .font_(Font("Input Sans", 20))
      .stringColor_(Color.white)
      .resize_(2),

      sceneList: ListView(view, Rect(margin, margin, leftPanelWidth - (2*margin), bounds.height - bottomPanelHeight - (2*margin)))
      .background_(Color(0.18, 0.21, 0.25))
      .stringColor_(Color.gray(0.65))
      .font_(Font("Input Sans", 10))
      .resize_(4),

      textBox: TextView(view, Rect(leftPanelWidth, 35 + (4*margin), bounds.width - leftPanelWidth - margin, bounds.height - 35 - bottomPanelHeight - (5*margin)))
      .palette_(QPalette.dark)
      .string_("{\nstuff\n}")
      .font_(Font("Input Sans", 10))
      .background_(Color(0.18, 0.21, 0.25))
      .stringColor_(Color.white)
      .resize_(5)
      .enterInterpretsSelection_(true),

      resizePanel: View(view, Rect(leftPanelWidth - margin, margin, margin, bounds.height - bottomPanelHeight - (2*margin)))
      .background_(Color.gray(0, 0))
      .resize_(4)
      .mouseEnterAction_({ |v, x, y|
        v.background_(Color.gray(0, 0.3))
      })
      .mouseLeaveAction_({ |v|
        v.background_(Color.gray(0, 0))
      })
      .mouseDownAction_({ |v, x, y|
        dragStart = x@y;
      })
      .mouseMoveAction_({ |v, x, y|
        this.leftPanelWidth_(x - dragStart.x + leftPanelWidth);
      }),

      bottomPanel: (
        updateButt: Button(view, Rect(bounds.width - buttonWidth - margin, bounds.height - bottomPanelHeight, buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["✎ Update"]])
        .resize_(9),

        refreshButt: Button(view, Rect(margin, bounds.height - bottomPanelHeight, buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["↻ Refresh"]])
        .resize_(7),

        saveButt: Button(view, Rect(margin, bounds.height - (bottomPanelHeight/2), buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["✍ Save"]])
        .resize_(7),

        renameButt: Button(view, Rect(buttonWidth + (2*margin), bounds.height - bottomPanelHeight, buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["✎ Rename"]])
        .resize_(7),

        deleteButt: Button(view, Rect(buttonWidth + (2*margin), bounds.height - (bottomPanelHeight/2), buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["⌫ Delete"]])
        .resize_(7),

        moveUpButt: Button(view, Rect((2*buttonWidth) + (3*margin), bounds.height - bottomPanelHeight, buttonWidth - 12, bottomPanelHeight / 2 - margin))
        .states_([["↑ Move"]])
        .resize_(7),

        moveDownButt: Button(view, Rect((2*buttonWidth) + (3*margin), bounds.height - (bottomPanelHeight/2), buttonWidth - 12, bottomPanelHeight / 2 - margin))
        .states_([["↓ Move"]])
        .resize_(7),

        addBeforeButt: Button(view, Rect((3*buttonWidth) + (4*margin) - 12, bounds.height - bottomPanelHeight, buttonWidth - 12, bottomPanelHeight / 2 - margin))
        .states_([["+↑ Add"]])
        .resize_(7),

        addAfterButt: Button(view, Rect((3*buttonWidth) + (4*margin) - 12, bounds.height - (bottomPanelHeight/2), buttonWidth - 12, bottomPanelHeight / 2 - margin))
        .states_([["+↓ Add"]])
        .resize_(7)
      )
    );

    gui[\bottomPanel].keysValuesDo { |k, v| v.font_(Font.sansSerif(12)) };
    this.makeInteraction;
    gui[\textBox].focus;

  }

  handleKey { |view, chr, mod, uni, key|
    if (mod.isAlt && mod.isCmd.not) {
      if (key == 125) { // down
        showCtrl.incrementSceneIndex;
      };
      if (key == 126) { // up
        showCtrl.decrementSceneIndex;
      };
      if (key == 49) { //space
        showCtrl.executeCurrentScene;
      };
    };

    if (mod.isCtrl) { // ctrl:
      if (key == 32) { // u
        this.saveScene;
      };
      if (key == 13) { // w
        this.saveSceneFuncs;
      };
    };

    if (mod.isCmd) {
      if (key == 1) { // s
        this.saveScene;
        this.saveSceneFuncs;
      };
      if (key == 31) { // o
        this.openSceneFuncs;
      };
      if (key == 2) { // d
        HelpBrowser.openHelpFor(view.selectedString);
      };
      if (key == 15) { // r
        this.renameScene;
      };
      if (key == 5) { // g
        this.gotoScene;
      };
      if (key == 51) { // delete
        this.deleteScene;
      };
      if (key == 126) { // up
        if (mod.isAlt) {
          this.addSceneAbove;
        } {
          this.moveSceneUp;
        };
      };
      if (key == 125) { // down
        if (mod.isAlt) {
          this.addSceneBelow;
        } {
          this.moveSceneDown;
        };
      };
    };
  }

  makeInteraction {
    var buttons = gui[\bottomPanel];
    buttons[\updateButt].action_({ this.saveScene });
    buttons[\saveButt].action_({ this.saveSceneFuncs });
    buttons[\renameButt].action_({ this.renameScene });
    buttons[\deleteButt].action_({ this.deleteScene });
    buttons[\moveUpButt].action_({ this.moveSceneUp });
    buttons[\moveDownButt].action_({ this.moveSceneDown });
    buttons[\addBeforeButt].action_({ this.addSceneAbove });
    buttons[\addAfterButt].action_({ this.addSceneBelow });



    gui[\sceneList].mouseUpAction_({ |v|
      showCtrl.currentSceneIndex_(v.selection[0]);
      gui[\textBox].focus;
    })
    .keyDownAction_({ |view, chr, mod, uni, key|
      if (mod.isAlt || mod.isCtrl || mod.isCmd) { this.handleKey(view, chr, mod, uni, key) };
    });

    gui[\textBox].keyDownAction_({ |view, chr, mod, uni, key|
      //[chr.asCompileString, mod, uni, key].postln;
      //if (/*(key == 36) || */(key == 41)) { this.colorizeText; }; // return or ;
      if (mod.isAlt || mod.isCtrl || mod.isCmd) { this.handleKey(view, chr, mod, uni, key) };
      if ((key == 36) && mod.isCmd && (view.selectionSize == 0)) { // cmd-enter
        savedSelection = [view.selectionStart, view.selectionSize];
        view.select(0, view.string.size);
        { view.select(*savedSelection) }.defer(0.1);
        ("-> " ++ view.string.interpret).postln;
        true; // don't do c++ evaluation
      };
    })
    .keyUpAction_({ |view, chr, mod, uni, key|
      var beginningOfPrevLine, prevLine, indent;

      if (key == 36) { // return
        beginningOfPrevLine = view.string.reverse[view.string.size - view.selectionStart + 1..].find("\n");
        beginningOfPrevLine = if (beginningOfPrevLine.notNil) {view.selectionStart - beginningOfPrevLine - 1} {0};
        prevLine = view.string[beginningOfPrevLine..view.selectionStart];
        indent = prevLine.reject({|item| item == $\n}).findRegexp("^(\\s+)?.*?")[1][1].size;
        view.setString(String.newFrom($ !indent), view.selectionStart, 0);
      };

      if (key == 48) { // tab
        view.setString("  ", view.selectionStart - 1, 1);
      };

      this.colorizeText;

      if ((("{" ++ gui[\textBox].string ++ "}").asSymbol != showCtrl.currentSceneFunc.def.sourceCode.asSymbol)) {
        showCtrl.unsavedSceneChanges_(true);
      } {
        showCtrl.unsavedSceneChanges_(false);
      };
    });
  }

  confirmBox { |header, action|
    var button;
    var win = Window(header, Rect(Window.screenBounds.width - 450, Window.screenBounds.height - 300, 400, 100)).front;
    StaticText(win, Rect(10, 10, 380, 40))
    .string_(header)
    .font_(Font("Helvetica", 30));

    button = Button(win, Rect(10, 60, 185, 30)).states_([["OK"]])
    .action_({ action.value; win.close })
    .keyDownAction_({ |view, chr, mod, uni, key|
      if (key == 36) {
        button.doAction;
      };
      if (key == 53) {
        win.close;
      };
    });
    Button(win, Rect(205, 60, 185, 30)).states_([["Cancel"]])
    .action_({ win.close });
  }

  dialogBox { |header, action, value=""|
    var field, button;
    var win = Window(header, Rect(Window.screenBounds.width - 450, Window.screenBounds.height - 300, 400, 130)).front;
    StaticText(win, Rect(10, 10, 380, 40))
    .string_(header)
    .font_(Font("Helvetica", 30));

    field = TextField(win, Rect(10, 50, 380, 30))
    .string_(value)
    .keyUpAction_({ |view, chr, mod, uni, key|
      if (key == 36) {
        button.doAction;
      };
      if (key == 53) {
        win.close;
      };
    });

    button = Button(win, Rect(10, 90, 185, 30)).states_([["OK"]])
    .action_({ action.value(field.string); win.close });
    Button(win, Rect(205, 90, 185, 30)).states_([["Cancel"]])
    .action_({ win.close });
  }

  renameScene {
    var oldname = showCtrl.currentSceneName;
    var action = { |string|
      showCtrl.currentSceneName = string.asSymbol;
    };
    this.dialogBox("Scene name", action, oldname);
  }

  gotoScene {
    var action = { |string|
      showCtrl.currentSceneIndex_(string.asInteger);
    };
    this.dialogBox("Go to scene #", action);
  }

  deleteScene {
    var action = { showCtrl.deleteCurrentScene };
    this.confirmBox("Are you sure?", action);
  }

  saveScene {
    var func = ("{" ++ gui[\textBox].string ++ "}").interpret;
    if (func.isNil) {
      gui[\topBlackPanel].background_(Color.red);
      AppClock.sched(0.1, { gui[\topBlackPanel].background_(Color.black); nil });
      AppClock.sched(0.2, { gui[\topBlackPanel].background_(Color.red); nil });
      AppClock.sched(0.3, { gui[\topBlackPanel].background_(Color.black); nil });
    } {
      gui[\topBlackPanel].background_(Color.green);
      AppClock.sched(0.2, { gui[\topBlackPanel].background_(Color.black); nil });
      showCtrl.currentSceneFunc_(func);
    };
  }

  saveSceneFuncs { |action|
    if (showCtrl.saveSceneFuncs.not) {
      FileDialog({ |path|
        path = path.asPathName;
        path = path.pathOnly +/+ path.fileNameWithoutExtension;
        ("rm -r " ++ path.copy.replace(" ", "\\ ")).unixCmd({ |exitcode|
          if (exitcode != 0) {
            { this.saveSceneFuncs }.defer(0.2);
          } {
            ("mkdir " ++ path.copy.replace(" ", "\\ ")).unixCmd({ |exitcode|
              if (exitcode != 0) {
                { this.saveSceneFuncs }.defer(0.2);
              } {
                ("cp -r " ++ showCtrl.defaultfilepath.copy.replace(" ", "\\ ") +/+ "* " ++ path.copy.replace(" ", "\\ ")).unixCmd({ |exitcode|
                  if (exitcode != 0) {
                    this.confirmBox("There was an error");
                    //{ this.saveSceneFuncs }.defer(0.2);
                  } {
                    showCtrl.filepath = path;
                    showCtrl.saveSceneFuncs;
                    { action.value }.defer(0.2);
                  }
                }, false);
              };
            }, false);
          };
        }, false);
      }, fileMode: 0, acceptMode: 1, stripResult: true);
    };
  }

  openSceneFuncs {
    if (showCtrl.unsavedListChanges || showCtrl.unsavedSceneChanges) {
      this.confirmBox("Save first?", {
        {
          this.saveScene;
          this.saveSceneFuncs {
            this.openSceneFuncs
          };
        }.defer(0.2);
      });
    } {
      FileDialog({ |path|
        showCtrl.filepath = path;
        showCtrl.refreshSceneFuncs;
        showCtrl.currentSceneIndex = 0;
      }, fileMode: 2, acceptMode: 0, stripResult: true);
    };
  }

  moveSceneUp {
    var index = showCtrl.currentSceneIndex;
    var newIndex = if (index == 0) { 0 } { index - 1 };
    var func = showCtrl.currentSceneFunc;
    var name = showCtrl.currentSceneName;
    showCtrl.deleteCurrentScene;
    showCtrl.addScene(newIndex, name, func);
    showCtrl.currentSceneIndex_(newIndex);
  }

  moveSceneDown {
    var index = showCtrl.currentSceneIndex;
    var newIndex = if (index == (showCtrl.sceneFuncs.size - 1)) { index } { index + 1 };
    var func = showCtrl.currentSceneFunc;
    var name = showCtrl.currentSceneName;
    showCtrl.deleteCurrentScene;
    showCtrl.addScene(newIndex, name, func);
    showCtrl.currentSceneIndex_(newIndex);
  }

  addSceneAbove {
    var action = { |string|
      showCtrl.addSceneBeforeCurrent(string);
    };
    this.dialogBox("New scene before", action);
  }

  addSceneBelow {
    var action = { |string|
      showCtrl.addSceneAfterCurrent(string);
      showCtrl.incrementSceneIndex;
    };
    this.dialogBox("New scene after", action);
  }

  leftPanelWidth_ { |newLeftPanelWidth|
    var bounds = view.bounds;
    leftPanelWidth = newLeftPanelWidth.max(margin);
    gui[\topBlackPanel].bounds_(Rect(leftPanelWidth, margin, bounds.width - leftPanelWidth - margin, 35 + (2*margin)));
    gui[\curScene].bounds_(Rect(leftPanelWidth + margin, 2*margin, bounds.width - leftPanelWidth - (3*margin), 35));
    gui[\sceneList].bounds_(Rect(margin, margin, leftPanelWidth - (2*margin), bounds.height - bottomPanelHeight - (2*margin)));
    gui[\textBox].bounds_(Rect(leftPanelWidth, 35 + (4*margin), bounds.width - leftPanelWidth - margin, bounds.height - 35 - bottomPanelHeight - (5*margin)));
    gui[\resizePanel].bounds_(Rect(leftPanelWidth - margin, margin, margin, bounds.height - bottomPanelHeight - (2*margin)));
  }

  showCtrl_ { |newShowCtrl|
    showCtrl = newShowCtrl;
    newShowCtrl.addDependant(this);
    this.refresh;
  }



  colorizeText { |wholething=false|
    var view = gui[\textBox];
    var commentregexp = "//.*?\\n";
    var longcommentregexp = "/\\*.*?\\*/";
    var bracketregexp = "[<>\\&\\{\\}\\(\\)\\[\\]\\.\\,\\;!\\~\\=\\+\\-\\*\\/\\%]";
    var cueregexp = "Cue on.*?\\n";
    var pageregexp = "Page #.*?\\n";
    var rememberregexp = "\\*REMEMBER\\*.*?\\n";
    var numberregexp = "(\\d+(\\.\\d+)?)|(pi)";
    var classregexp = "\\s|^[A-Z]\\w+";
    var envvarregexp = "\\~\\w+";
    var varregexp = "var\\s";
    var symbolregexp = "((')((\\\\{2})*|(.*?[^\\\\](\\\\{2})*))\\2)|(\\\\\\w+)";
    var stringregexp =  "([" ++ ('"'.asString) ++ "])((\\\\{2})*|(.*?[^\\\\](\\\\{2})*))\\1";

    var stringForward, stringBack, pos, start, end, endOfLastComment, beginningOfNextComment, startOfThisComment, endOfThisComment;

    if (wholething) {
      start = 0;
      end = view.string.size;
    } {
      pos = view.selectionStart;

      stringForward = view.string[pos..];
      stringBack = view.string.reverse[view.string.size - pos..];

      end = stringForward.find("\n");
      end = if (end.notNil) { pos + end } { view.string.size };
      start = stringBack.find("\n");
      start = if (start.notNil) { pos - start } { 0 };


      stringForward = view.string[end..];
      stringBack = view.string.reverse[view.string.size - start..];

      endOfLastComment = (stringBack.find("/*") ?? inf) + (pos - start);
      beginningOfNextComment = (stringForward.find("/*") ?? inf) + (end - pos);

      stringForward = view.string[start..];
      stringBack = view.string.reverse[view.string.size - end..];

      startOfThisComment = (stringBack.find("*/") ?? inf) - (end - pos);
      endOfThisComment = (stringForward.find("*/") ?? inf) - (pos - start);

      if (startOfThisComment >= endOfLastComment) { startOfThisComment = nil };
      if (endOfThisComment >= beginningOfNextComment) { endOfThisComment = nil };

      end = if (endOfThisComment.notNil) { pos + endOfThisComment + 2 } { end };
      start = if (startOfThisComment.notNil) { pos - startOfThisComment - 2 } { start };
    };

    view.setStringColor(Color.gray(0.85), start, end - start);

    view.string.findRegexp(bracketregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.6, 0.7, 0.8), result[0], result[1].size);
    };
    view.string.findRegexp(numberregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(1, 0.8, 0.9), result[0], result[1].size);
    };
    view.string.findRegexp(classregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.6, 0.95, 0.95), result[0], result[1].size);
    };
    view.string.findRegexp(envvarregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.9, 0.9, 0.6), result[0], result[1].size);
    };
    view.string.findRegexp(varregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.2, 0.4, 0.7), result[0], result[1].size);
    };
    view.string.findRegexp(symbolregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.5, 0.9, 0.6), result[0], result[1].size);
    };
    view.string.findRegexp(stringregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(1, 0.6, 0.7), result[0], result[1].size);
    };
    view.string.findRegexp(commentregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.7, 0.4, 0.5), result[0], result[1].size);
    };
    view.string.findRegexp(longcommentregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.7, 0.4, 0.5), result[0], result[1].size);
    };
    view.string.findRegexp(cueregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.4, 0.7, 0.5), result[0] + 7, result[1].size - 7);
    };
    view.string.findRegexp(pageregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(0.4, 0.7, 0.5), result[0] + 7, result[1].size - 7);
    };
    view.string.findRegexp(rememberregexp, 0).select({ |item|
      (item[0] >= start) && (item[0] < end)
    }).do { |result|
      view.setStringColor(Color(1, 1, 0.7), result[0], result[1].size);
    };
  }

  refresh {
    this.updateScenes;
    this.updateCurrentScene;
    this.updateUnsaved;
  }

  updateUnsaved {
    defer {
      gui.bottomPanel[\saveButt].states_([[
        gui.bottomPanel[\saveButt].states[0][0],
        nil,
        if (showCtrl.unsavedListChanges) {Color.red} {nil}
      ]]);
      gui.bottomPanel[\updateButt].states_([[
        gui.bottomPanel[\updateButt].states[0][0],
        nil,
        if (showCtrl.unsavedSceneChanges) {Color.green} {nil}
      ]]);
    };
  }

  updateScenes {
    defer {
      gui[\sceneList].items_(showCtrl.sceneNames.collect { |name, i|
        i.asString.padLeft(showCtrl.sceneFuncs.size.asString.size) ++ "  " ++ name
      });
      this.updateCurrentScene(true);
    };
  }

  updateCurrentScene { |preserveCursor=false|
    defer {
      var selectionStart = gui[\textBox].selectionStart;
      var selectionSize = gui[\textBox].selectionSize;

      gui[\curScene].string_(showCtrl.currentSceneName);

      if (preserveCursor) {
        //gui[\textBox].select(selectionStart, selectionSize);
      } {
        gui[\textBox].string_(showCtrl.currentSceneFunc.def.sourceCode.findRegexp("^\\{[\\n\\s]*(.*)\\}$")[1][1]);
        this.colorizeText(true);
        gui[\textBox].select((gui[\textBox].string.find("*/") ?? -3) + 3, 0);
      };

      gui[\textBox].enterInterpretsSelection_(true);

      gui[\sceneList].selection_([showCtrl.currentSceneIndex]);
      gui[\sceneList].selection_([showCtrl.currentSceneIndex]);
    }
  }

  update { |obj, what|
    switch (what)
    {\sceneFuncs} { this.updateScenes }
    {\unsavedChanges} { this.updateUnsaved }
    {\currentSceneIndex} { this.updateCurrentScene };
  }
}


ShowCtrlWindow : SCViewHolder {
  var <win;

  *new { |name="", bounds|
    ^super.new.init(name, bounds);
  }

  init { |name, bounds|
    win = Window(name, bounds).front
    .acceptsMouseOver_(true);
    view = ShowCtrlView(win.view).resize_(5);
  }

  cueList_ { |cueList|
    view.showCtrl_(cueList);
    view.showCtrl.addDependant(this);
  }

  cueList {
    ^view.showCtrl;
  }

  update { |obj, what|
    switch (what)
    {\filepath} { win.name_(view.showCtrl.filepath) };
  }
}