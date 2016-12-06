CueListView : SCViewHolder {
  var <cueList;
  var <font;
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

      curCue: StaticText(view, Rect(leftPanelWidth + margin, margin, bounds.width - leftPanelWidth - (3*margin), 45))
      .string_("Current cue")
      .stringColor_(Color.white)
      .resize_(2),

      cueList: ListView(view, Rect(margin, margin, leftPanelWidth - (2*margin), bounds.height - bottomPanelHeight - (2*margin)))
      .background_(Color(0.18, 0.21, 0.25))
      .stringColor_(Color.gray(0.65))
      .resize_(4),

      textBox: CodeView(view, Rect(leftPanelWidth, 35 + (4*margin), bounds.width - leftPanelWidth - margin, bounds.height - 35 - bottomPanelHeight - (5*margin)))
      .oneDarkColorScheme
      .customTokens_((
        cue: "Cue on.*?\\n",
        page: "Page #.*?\\n",
        remember: "\\*REMEMBER\\*.*?\\n"
      ))
      .customColors_((
        cue: Color(0.4, 0.7, 0.5),
        page: Color(0.4, 0.7, 0.5),
        remember: Color(1, 1, 0.7)
      ))
      .resize_(5),

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

    this.font_(Font("Input Sans", 12));

    gui[\bottomPanel].keysValuesDo { |k, v| v.font_(Font.sansSerif(12)) };
    this.makeInteraction;
    gui[\textBox].focus;
  }

  handleKey { |view, chr, mod, uni, key|
    if (mod.isAlt && mod.isCmd.not) {
      if (key == 125) { // down
        cueList.incrementCueIndex;
      };
      if (key == 126) { // up
        cueList.decrementCueIndex;
      };
      if (key == 49) { //space
        cueList.executeCurrentCue;
      };
    };

    if (mod.isCtrl) { // ctrl:
      if (key == 32) { // u
        this.saveCue;
      };
      if (key == 13) { // w
        this.saveCueFuncs;
      };
    };

    if (mod.isCmd) {
      if (key == 1) { // s
        this.saveCue;
        this.saveCueFuncs;
      };
      if (key == 31) { // o
        this.openCueFuncs;
      };
      if (key == 2) { // d
        HelpBrowser.openHelpFor(view.selectedString);
      };
      if (key == 15) { // r
        this.renameCue;
      };
      if (key == 5) { // g
        this.gotoCue;
      };
      if (key == 51) { // delete
        this.deleteCue;
      };
      if (key == 126) { // up
        if (mod.isAlt) {
          this.addCueAbove;
        } {
          cueList.moveCurrentCueUp;
        };
      };
      if (key == 125) { // down
        if (mod.isAlt) {
          this.addCueBelow;
        } {
          cueList.moveCurrentCueDown;
        };
      };
    };
  }

  makeInteraction {
    var buttons = gui[\bottomPanel];
    buttons[\updateButt].action_({ this.saveCue });
    buttons[\saveButt].action_({ this.saveCueFuncs });
    buttons[\renameButt].action_({ this.renameCue });
    buttons[\deleteButt].action_({ this.deleteCue });
    buttons[\moveUpButt].action_({ cueList.moveCurrentCueUp });
    buttons[\moveDownButt].action_({ cueList.moveCurrentCueDown });
    buttons[\addBeforeButt].action_({ this.addCueAbove });
    buttons[\addAfterButt].action_({ this.addCueBelow });



    gui[\cueList].mouseUpAction_({ |v|
      cueList.currentCueIndex_(v.selection[0]);
      gui[\textBox].focus;
    })
    .keyDownAction_({ |view, chr, mod, uni, key|
      if (mod.isAlt || mod.isCtrl || mod.isCmd) { this.handleKey(view, chr, mod, uni, key) };
    });

    gui[\textBox].modKeyHandler_({ |view, chr, mod, uni, key|
      this.handleKey(view, chr, mod, uni, key)
    })
    .keyUpAction_({ |view, chr, mod, uni, key|
      if ((("{" ++ gui[\textBox].string ++ "}").asSymbol != cueList.currentCueFunc.def.sourceCode.asSymbol)) {
        cueList.unsavedCueChanges_(true);
      } {
        cueList.unsavedCueChanges_(false);
      };
    });
  }

  confirmBox { |header, action, cancelString = "Cancel", cancelAction|
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
    Button(win, Rect(205, 60, 185, 30)).states_([[cancelString]])
    .action_({ cancelAction.value; win.close });
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

  renameCue {
    var oldname = cueList.currentCueName;
    var action = { |string|
      cueList.currentCueName = string.asSymbol;
    };
    this.dialogBox("Cue name", action, oldname);
  }

  gotoCue {
    var action = { |string|
      cueList.currentCueIndex_(string.asInteger);
    };
    this.dialogBox("Go to cue #", action);
  }

  deleteCue {
    var action = { cueList.deleteCurrentCue };
    this.confirmBox("Are you sure?", action);
  }

  saveCue {
    var func = ("{" ++ gui[\textBox].string ++ "}").interpret;
    if (func.isNil) {
      gui[\topBlackPanel].background_(Color.red);
      AppClock.sched(0.1, { gui[\topBlackPanel].background_(Color.black); nil });
      AppClock.sched(0.2, { gui[\topBlackPanel].background_(Color.red); nil });
      AppClock.sched(0.3, { gui[\topBlackPanel].background_(Color.black); nil });
    } {
      gui[\topBlackPanel].background_(Color.green);
      AppClock.sched(0.2, { gui[\topBlackPanel].background_(Color.black); nil });
      cueList.currentCueFunc_(func);
    };
  }

  saveCueFuncs { |action|
    if (cueList.saveCueFuncs.not) {
      FileDialog({ |path|
        path = path.asPathName;
        path = path.pathOnly +/+ path.fileNameWithoutExtension;
        ("rm -r " ++ path.copy.replace(" ", "\\ ")).unixCmd({ |exitcode|
          if (exitcode != 0) {
            { this.saveCueFuncs }.defer(0.2);
          } {
            ("mkdir " ++ path.copy.replace(" ", "\\ ")).unixCmd({ |exitcode|
              if (exitcode != 0) {
                { this.saveCueFuncs }.defer(0.2);
              } {
                ("cp -r " ++ cueList.defaultfilepath.copy.replace(" ", "\\ ") +/+ "* " ++ path.copy.replace(" ", "\\ ")).unixCmd({ |exitcode|
                  if (exitcode != 0) {
                    this.confirmBox("There was an error");
                    //{ this.saveCueFuncs }.defer(0.2);
                  } {
                    cueList.filepath = path;
                    cueList.saveCueFuncs;
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

  openCueFuncs {
    var openFile = {
      FileDialog({ |path|
        cueList.filepath = path;
        cueList.refreshCueFuncs;
        cueList.currentCueIndex = 0;
      }, fileMode: 2, acceptMode: 0, stripResult: true);
    };

    if (cueList.unsavedListChanges || cueList.unsavedCueChanges) {
      this.confirmBox("Save first?", {
        {
          this.saveCue;
          this.saveCueFuncs {
            this.openCueFuncs
          };
        }.defer(0.2);
      }, "Don't save", {
        openFile.();
      });
    } {
      openFile.();
    };
  }

  addCueAbove {
    var action = { |string|
      cueList.addCueBeforeCurrent(string);
    };
    this.dialogBox("New cue before", action);
  }

  addCueBelow {
    var action = { |string|
      cueList.addCueAfterCurrent(string);
      cueList.incrementCueIndex;
    };
    this.dialogBox("New cue after", action);
  }

  leftPanelWidth_ { |newLeftPanelWidth|
    var bounds = view.bounds;
    leftPanelWidth = newLeftPanelWidth.max(margin);
    gui[\topBlackPanel].bounds_(Rect(leftPanelWidth, margin, bounds.width - leftPanelWidth - margin, 35 + (2*margin)));
    gui[\curCue].bounds_(Rect(leftPanelWidth + margin, 2*margin, bounds.width - leftPanelWidth - (3*margin), 35));
    gui[\cueList].bounds_(Rect(margin, margin, leftPanelWidth - (2*margin), bounds.height - bottomPanelHeight - (2*margin)));
    gui[\textBox].bounds_(Rect(leftPanelWidth, 35 + (4*margin), bounds.width - leftPanelWidth - margin, bounds.height - 35 - bottomPanelHeight - (5*margin)));
    gui[\resizePanel].bounds_(Rect(leftPanelWidth - margin, margin, margin, bounds.height - bottomPanelHeight - (2*margin)));
  }

  cueList_ { |newcueList|
    cueList = newcueList;
    newcueList.addDependant(this);
    this.refresh;
  }

  font_ { |afont|
    font = afont.size_(afont.size ?? 12);

    gui[\textBox].font_(font);
    gui[\cueList].font_(font.copy.size_(font.size * 1.2.reciprocal));
    gui[\curCue].font_(font.copy.size_(20));
  }

  refresh {
    this.updateCues;
    this.updateCurrentCue;
    this.updateUnsaved;
  }

  updateUnsaved {
    defer {
      gui.bottomPanel[\saveButt].states_([[
        gui.bottomPanel[\saveButt].states[0][0],
        nil,
        if (cueList.unsavedListChanges) {Color.red} {nil}
      ]]);
      gui.bottomPanel[\updateButt].states_([[
        gui.bottomPanel[\updateButt].states[0][0],
        nil,
        if (cueList.unsavedCueChanges) {Color.green} {nil}
      ]]);
    };
  }

  updateCues {
    defer {
      gui[\cueList].items_(cueList.cueNames.collect { |name, i|
        i.asString.padLeft(cueList.cueFuncs.size.asString.size) ++ "  " ++ name
      });
      this.updateCurrentCue(true);
    };
  }

  updateCurrentCue {
    defer {
      gui[\curCue].string_(cueList.currentCueName);

      gui[\textBox].string_(cueList.currentCueFunc.def.sourceCode.findRegexp("^\\{[\\n\\s]*(.*)\\}$")[1][1]);
      gui[\textBox].select((gui[\textBox].string.find("*/") ?? -3) + 3, 0);

      gui[\cueList].selection_([cueList.currentCueIndex]);
      gui[\cueList].selection_([cueList.currentCueIndex]);
    }
  }

  update { |obj, what|
    switch (what)
    {\cueFuncs} { this.updateCues }
    {\unsavedChanges} { this.updateUnsaved }
    {\currentCueIndex} { this.updateCurrentCue };
  }
}


CueListWindow : SCViewHolder {
  var <win;

  *new { |name="", bounds|
    ^super.new.init(name, bounds);
  }

  init { |name, bounds|
    win = Window(name, bounds).front
    .acceptsMouseOver_(true);
    view = CueListView(win.view).resize_(5);
  }

  cueList_ { |cueList|
    view.cueList_(cueList);
    view.cueList.addDependant(this);
  }

  cueList {
    ^view.cueList;
  }

  update { |obj, what|
    switch (what)
    {\filepath} { win.name_(view.cueList.filepath) };
  }
}
