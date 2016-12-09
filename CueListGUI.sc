CueListView : SCViewHolder {
  var <cueList;
  var <font, <gui, <leftPanelWidth, bottomPanelHeight = 50, <margin = 5, buttonWidth = 75;
  var dragStart, savedSelection;
  var <>parentWindow, <suppressEndFront = false, <>suppressToFront = false;
  var textBoxContainer;

  *new { |parent, bounds|
    ^super.new.init(parent, bounds);
  }

  init { |parent, bounds|

    bounds = bounds ?? Rect(0, 0, parent.bounds.width, parent.bounds.height);
    view = View(parent, bounds);

    leftPanelWidth = bounds.width / 3;

    textBoxContainer = View(view, Rect(leftPanelWidth, 45 + (2 * margin), bounds.width - leftPanelWidth, bounds.height - 35 - bottomPanelHeight - (5*margin)))
    .resize_(5);

    gui = (
      topBlackPanel: View(view, Rect(leftPanelWidth, 0, bounds.width - leftPanelWidth, 46 + (2 * margin)))
      .background_(Color.black)
      .resize_(2),

      curCue: StaticText(view, Rect(leftPanelWidth + margin, margin, bounds.width - leftPanelWidth - (3*margin), 45))
      .string_("Current cue")
      .stringColor_(Color.white)
      .resize_(2),

      cueList: ListView(view, Rect(-1, -1, leftPanelWidth - margin + 1, bounds.height - bottomPanelHeight - margin + 1))
      .resize_(4),

      textBox: CodeView(textBoxContainer, Rect(-1, 0, bounds.width - leftPanelWidth + 2, bounds.height - 35 - bottomPanelHeight - (5*margin)))
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

      resizePanel: View(view, Rect(leftPanelWidth - margin, 0, margin, bounds.height - bottomPanelHeight - margin))
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
        background: View(view, Rect(0, bounds.height - bottomPanelHeight - margin, bounds.width, bottomPanelHeight + margin))
        .background_(Color.gray(0.9))
        .resize_(8),

        updateButt: Button(view, Rect(bounds.width - buttonWidth - margin, bounds.height - bottomPanelHeight, buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["✎ Update"]])
        .resize_(9),

        backupsButt: Button(view, Rect(bounds.width - buttonWidth - margin, bounds.height - (bottomPanelHeight/2), buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["Backups..."]])
        .resize_(9),

        refreshButt: Button(view, Rect(margin, bounds.height - bottomPanelHeight, buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["Open..."]])
        .resize_(7),

        saveButt: Button(view, Rect(margin, bounds.height - (bottomPanelHeight/2), buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["Save"]])
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

    gui[\textBox].addDependant(this);

    this.makeStyle;

    this.font_(Font.monospace.size_(13));

    gui[\bottomPanel].keysValuesDo { |k, v| v.font_(Font.sansSerif(12)) };
    this.makeInteraction;
    gui[\textBox].focus;
  }

  handleKey { |view, chr, mod, uni, keycode, key|
    if (mod.isAlt && mod.isCmd.not) {
      if (keycode == 125) { // down
        cueList.incrementCueIndex;
        ^true;
      };
      if (keycode == 126) { // up
        cueList.decrementCueIndex;
        ^true;
      };
      if (keycode == 49) { //space
        cueList.executeCurrentCue;
        ^true;
      };
    };

    if (mod.isCtrl) { // ctrl:
      if (keycode == 32) { // u
        this.saveCue;
        ^true;
      };
      if (keycode == 13) { // w
        this.saveCueFuncs;
        ^true;
      };
    };

    if (mod.isCmd) {
      if (keycode == 1) { // s
        this.saveCue;
        this.saveCueFuncs;
        ^true;
      };
      if (keycode == 31) { // o
        this.openCueFuncs;
        ^true;
      };
      if (key == 78) { // n
        this.newCueFuncs;
      };
      if (keycode == 15) { // r
        this.renameCue;
        ^true;
      };
      if (keycode == 5) { // g
        this.gotoCue;
        ^true;
      };
      if (keycode == 51) { // delete
        this.deleteCue;
        ^true;
      };
      if (keycode == 126) { // up
        if (mod.isAlt) {
          this.addCueAbove;
        } {
          this.moveCurrentCueUp;
        };
        ^true;
      };
      if (keycode == 125) { // down
        if (mod.isAlt) {
          this.addCueBelow;
        } {
          this.moveCurrentCueDown;
        };
        ^true;
      };
    };
  }

  makeInteraction {
    var buttons = gui[\bottomPanel];
    buttons[\updateButt].action_({ this.saveCue });
    buttons[\backupsButt].action_({ CueListBackupBrowser.show(cueList) });
    buttons[\refreshButt].action_({ this.openCueFuncs });
    buttons[\saveButt].action_({ this.saveCueFuncs });
    buttons[\renameButt].action_({ this.renameCue });
    buttons[\deleteButt].action_({ this.deleteCue });
    buttons[\moveUpButt].action_({ this.moveCurrentCueUp });
    buttons[\moveDownButt].action_({ this.moveCurrentCueDown });
    buttons[\addBeforeButt].action_({ this.addCueAbove });
    buttons[\addAfterButt].action_({ this.addCueBelow });



    gui[\cueList].mouseUpAction_({ |v|
      cueList.currentCueIndex_(v.selection[0]);
      gui[\textBox].focus;
    })
    .keyDownAction_({ |view, chr, mod, uni, keycode, key|
      if (mod.isAlt || mod.isCtrl || mod.isCmd) { this.handleKey(view, chr, mod, uni, keycode, key) };
    });

    gui[\textBox].modKeyHandler_({ |view, chr, mod, uni, keycode, key|
      this.handleKey(view, chr, mod, uni, keycode, key)
    })
    .keyUpAction_({ |view, chr, mod, uni, keycode|
      if ((("{ |thisCueList|\n" ++ gui[\textBox].string ++ "\n}").asSymbol != cueList.currentCueFunc.def.sourceCode.asSymbol)) {
        cueList.unsavedCueChanges_(true);
      } {
        cueList.unsavedCueChanges_(false);
      };
    });
  }

  confirmBox { |header, action, cancelString = "Cancel", cancelAction|
    var button;
    var bounds = if (parentWindow.notNil) {
      Rect(
        parentWindow.bounds.left + (parentWindow.bounds.width / 2) - 200,
        parentWindow.bounds.top + (parentWindow.bounds.height / 3 * 2) - 50,
        400, 110);
    } {
      Rect(
        Window.screenBounds.width - 450,
        Window.screenBounds.height - 300,
        400, 110);
    };
    var win = Window(header, bounds, border: false)
    .onClose_({ suppressEndFront = false; suppressToFront = true; parentWindow.front; })
    .front;

    suppressEndFront = true; // don't get rid of completions window

    StaticText(win, Rect(10, 15, 380, 40))
    .string_(header)
    .font_(Font("Helvetica", 30));

    button = Button(win, Rect(10, 65, 185, 30)).states_([["OK"]])
    .action_({ action.value; win.close })
    .keyDownAction_({ |view, chr, mod, uni, keycode|
      if (keycode == 36) {
        button.doAction;
      };
      if (keycode == 53) {
        win.close;
      };
    });
    Button(win, Rect(205, 65, 185, 30)).states_([[cancelString]])
    .action_({ cancelAction.value; win.close });
  }

  dialogBox { |header, action, value=""|
    var field, button;
    var bounds = if (parentWindow.notNil) {
      Rect(
        parentWindow.bounds.left + (parentWindow.bounds.width / 2) - 200,
        parentWindow.bounds.top + (parentWindow.bounds.height / 3 * 2) - 65,
        400, 130);
    } {
      Rect(
        Window.screenBounds.width - 450,
        Window.screenBounds.height - 300,
        400, 130);
    };
    var win = Window(header, bounds, border: false)
    .alwaysOnTop_(true)
    .onClose_({ suppressEndFront = false; suppressToFront = true; parentWindow.front; })
    .front;

    suppressEndFront = true; // don't get rid of completions window

    StaticText(win, Rect(10, 10, 380, 40))
    .string_(header)
    .font_(Font("Helvetica", 30));

    field = TextField(win, Rect(10, 50, 380, 30))
    .string_(value)
    .keyUpAction_({ |view, chr, mod, uni, keycode|
      if (keycode == 36) {
        button.doAction;
      };
      if (keycode == 53) {
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
    var func = ("{ |thisCueList|\n" ++ gui[\textBox].string ++ "\n}").interpret;
    var restoreBackground = gui[\topBlackPanel].background;
    if (func.isNil) {
      gui[\topBlackPanel].background_(Color.red);
      AppClock.sched(0.1, { gui[\topBlackPanel].background_(restoreBackground); nil });
      AppClock.sched(0.2, { gui[\topBlackPanel].background_(Color.red); nil });
      AppClock.sched(0.3, { gui[\topBlackPanel].background_(restoreBackground); nil });
    } {
      gui[\topBlackPanel].background_(Color.green);
      AppClock.sched(0.2, { gui[\topBlackPanel].background_(restoreBackground); nil });
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

  newCueFuncs {
    if (cueList.unsavedListChanges || cueList.unsavedCueChanges) {
      this.confirmBox("Save first?", {
        {
          this.saveCue;
          this.saveCueFuncs {
            this.newCueFuncs
          };
        }.defer(0.2);
      }, "Don't save", {
        cueList.filepath = cueList.defaultfilepath;
        cueList.refreshCueFuncs;
      });
    } {
      cueList.filepath = cueList.defaultfilepath;
      cueList.refreshCueFuncs;
    };
  }

  moveCurrentCueDown {
    if (cueList.unsavedCueChanges) {
      this.confirmBox("Discard cue edits?", {
        cueList.unsavedCueChanges = false;
        cueList.moveCurrentCueDown;
      });
      ^nil;
    };
    cueList.moveCurrentCueDown;
  }

  moveCurrentCueUp {
    if (cueList.unsavedCueChanges) {
      this.confirmBox("Discard cue edits?", {
        cueList.unsavedCueChanges = false;
        cueList.moveCurrentCueUp;
      });
      ^nil;
    };
    cueList.moveCurrentCueUp;
  }

  addCueAbove {
    var action = { |string|
      cueList.addCueBeforeCurrent(string);
    };
    if (cueList.unsavedCueChanges) {
      this.confirmBox("Discard cue edits?", {
        cueList.unsavedCueChanges = false;
        this.dialogBox("New cue before", action);
      });
      ^nil;
    };
    this.dialogBox("New cue before", action);
  }

  addCueBelow {
    var action = { |string|
      cueList.addCueAfterCurrent(string);
      cueList.incrementCueIndex;
    };
    if (cueList.unsavedCueChanges) {
      this.confirmBox("Discard cue edits?", {
        cueList.unsavedCueChanges = false;
        this.dialogBox("New cue after", action);
      });
      ^nil;
    };
    this.dialogBox("New cue after", action);
  }

  leftPanelWidth_ { |newLeftPanelWidth|
    var bounds = view.bounds;
    leftPanelWidth = newLeftPanelWidth.max(margin);
    gui[\topBlackPanel].bounds_(Rect(leftPanelWidth, 0, bounds.width - leftPanelWidth, 46 + (2 * margin)));
    gui[\curCue].bounds_(Rect(leftPanelWidth + margin, 2*margin, bounds.width - leftPanelWidth - (3*margin), 35));
    gui[\cueList].bounds_(Rect(0, 0, leftPanelWidth - margin, bounds.height - bottomPanelHeight - margin));
    textBoxContainer.bounds_(Rect(leftPanelWidth, 45 + (2 * margin), bounds.width - leftPanelWidth, bounds.height - 35 - bottomPanelHeight - (5*margin)));
    gui[\resizePanel].bounds_(Rect(leftPanelWidth - margin, 0, margin, bounds.height - bottomPanelHeight - margin));
  }

  cueList_ { |newcueList|
    cueList = newcueList;
    gui[\textBox].interpretArgs_((thisCueList: cueList));
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
    var str = cueList.currentCueFunc.def.sourceCode.findRegexp("^\\{(\\s*\\|thisCueList\\|)?[\\n\\s]*(.*)\\}$")[2][1];
    if (str[str.size - 1] == $\n) { str = str[0..(str.size - 2)] };

    defer {
      gui[\curCue].string_(cueList.currentCueName);

      gui[\textBox].string_(str);
      gui[\textBox].select((gui[\textBox].string.find("*/") ?? -3) + 3, 0);

      gui[\cueList].selection_([cueList.currentCueIndex]);
    }
  }

  makeStyle {
    gui[\cueList].palette_(gui[\textBox].palette)
    .background_(gui[\textBox].palette.base.alpha_(0.97))
    .selectedStringColor_(Color.gray(gui[\textBox].palette.baseText.asHSV[2].round))
    .hiliteColor_(gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2));

    gui[\topBlackPanel].background_(gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2));
    gui[\curCue].stringColor_(Color.gray(gui[\textBox].palette.baseText.asHSV[2].round));
  }

  update { |obj, what, thing|
    switch (what)
    {\cueFuncs} { this.updateCues }
    {\unsavedChanges} { this.updateUnsaved }
    {\currentCueIndex} { this.updateCurrentCue }
    {\confirmForceCueIndex} {
      gui[\cueList].selection_([cueList.currentCueIndex]); // reset cuelist
      this.confirmBox("Discard cue edits?", { cueList.currentCueIndex_(thing, true) })
    }
    {\colorScheme} { this.makeStyle };
  }
}


CueListWindow : SCViewHolder {
  var <win, <isFront = false, <>toFrontAction, <>endFrontAction, <completeWindow;

  *new { |name="", bounds|
    ^super.new.init(name, bounds);
  }

  init { |name, bounds|
    win = Window(name, bounds)
    .background_(Color.clear)
    .acceptsMouseOver_(true)
    .toFrontAction_({
      toFrontAction.();
      if (completeWindow.notNil) {
        if (view.suppressToFront) { view.suppressToFront = false; } { completeWindow.visible = true; };
      };
      isFront = true;
    })
    .endFrontAction_({
      endFrontAction.();
      if (completeWindow.notNil) {
        if (completeWindow.isFront.not && view.suppressEndFront.not) { completeWindow.visible = false };
      };
      isFront = false;
    });

    view = CueListView(win.view)
    .parentWindow_(this.win)
    .resize_(5);

    this.makeCompleteWindow; // let the complete window bring cue list window to front :)
  }

  makeCompleteWindow {
    var bounds, cueListWindow = this;

    bounds = Rect(win.bounds.width + view.margin, 0, 500, 300);
    if (completeWindow.notNil) { completeWindow.close };

    completeWindow = view.gui[\textBox].makeCompleteWindow(bounds, win);

    completeWindow.endFrontAction = {
      if (cueListWindow.isFront.not) { cueListWindow.completeWindow.visible = false };
    };

    completeWindow.toFrontAction = {
      if (win.visible) { cueListWindow.view.suppressToFront = true; }
    };
  }

  cueList_ { |cueList|
    view.cueList_(cueList);
    view.cueList.addDependant(this);
  }

  cueList {
    ^view.cueList;
  }

  font {
    ^view.font;
  }

  font_ { |afont|
    view.font_(afont);
  }

  update { |obj, what|
    switch (what)
    {\filepath} { win.name_(view.cueList.filepath) };
  }
}


CueListBackupBrowser {
  classvar win, selectedfuncs, selectedfunc;

  *show { |cueList|
    var backupfolder = (cueList.filepath +/+ "backups/").standardizePath;
    var backupfuncs = ();
    var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    var gui = (), timesActions = [];
    var font = Font.monospace.size_(13);
    var selection = ();
    var displayCuefuncs;

    if (win.notNil) { win.close };

    PathName(backupfolder).files.do({ |filepath|
      var file;
      var year, month, day, hour, min, sec, timestring;
      var matches = filepath.fileName.findRegexp("^cue-data__(\\d{4})-(\\d{2})-(\\d{2})__(\\d{2})\\.(\\d{2})\\.(\\d{2})__\\.scd$");

      if (matches.size > 0) {
        year = matches[1][1].asInt;
        month = matches[2][1].asInt - 1;
        day = matches[3][1].asInt;
        hour = matches[4][1].asInt;
        min = matches[5][1].asSymbol;
        sec = matches[6][1].asSymbol;

        selection[\year] = year;

        if (backupfuncs[year].isNil) { backupfuncs[year] = () };
        if (backupfuncs[year][month].isNil) { backupfuncs[year][month] = () };
        if (backupfuncs[year][month][day].isNil) { backupfuncs[year][month][day] = () };
        if (backupfuncs[year][month][day][hour].isNil) { backupfuncs[year][month][day][hour] = () };
        if (backupfuncs[year][month][day][hour][min].isNil) { backupfuncs[year][month][day][hour][min] = () };

        file = File(filepath.fullPath, "r");

        backupfuncs[year][month][day][hour][min][sec] = file.readAllString.interpret;

        file.close;
      };
    });

    win = Window("Browse backups", Rect(100, 50, 800, Window.screenBounds.height - 150))
    .background_(Color.clear)
    .front;

    gui[\topPanel] = View(win, Rect(0, 0, win.bounds.width, 210))
    .background_(Color.gray(0.95, 0.9));

    gui[\menus] = View(win, Rect(0, 0, win.bounds.width, 200))
    .layout_(
      GridLayout.rows(
        [
          ListView(), ListView(), ListView(), ListView()
        ]
      )
      .hSpacing_(0)
      .vSpacing_(0)
      .margins_(0)
    )
    .resize_(2);
    gui[\years] = gui[\menus].children[0];
    gui[\months] = gui[\menus].children[1];
    gui[\days] = gui[\menus].children[2];
    gui[\times] = gui[\menus].children[3];

    gui[\cueList] = ListView(win, Rect(0, 210, 200, win.bounds.height - 255))
    .resize_(4);

    gui[\resizePanel] = View(win, Rect(200, 210, 5, win.bounds.height - 255))
    .background_(Color.gray(0.95, 0.9))
    .resize_(4);

    gui[\topBlackPanel] = View(win, Rect(205, 210, win.bounds.width - 205, 56))
    .background_(Color.black)
    .resize_(2);

    gui[\curCue] = StaticText(win, Rect(210, 215, win.bounds.width - 220, 45))
    .stringColor_(Color.white)
    .resize_(2);

    gui[\textBoxContainer] = View(win, Rect(205, 265, win.bounds.width - 205, win.bounds.height - 310)).resize_(5);

    gui[\textBox] = CodeView(gui[\textBoxContainer], Rect(-1, 0, win.bounds.width - 203, win.bounds.height - 310))
    .font_(font)
    .editable_(false)
    .resize_(5);

    gui[\bottomPanel] = View(win, Rect(0, win.bounds.height - 50, win.bounds.width, 50))
    .background_(Color.gray(0.9))
    .resize_(8)
    .layout_(
      GridLayout.rows(
        [
          Button().states_([["Restore entire CueList"]]).action_({
            cueList.cueFuncs_(selectedfuncs);
            cueList.saveCueFuncs;
          }),
          Button().states_([["Restore this function to current cue"]]).action_({
            cueList.currentCueFunc = selectedfunc[\func];
            cueList.currentCueName = selectedfunc[\name];
            cueList.saveCueFuncs;
          }),
          Button().states_([["Close this window"]]).action_({ win.close })
        ]
      )
      .hSpacing_(10)
      .vSpacing_(0)
      .margins_(10)
    );

    (gui[\menus].children ++ gui[\cueList]).do { |menu|
      menu.palette_(gui[\textBox].palette)
      .background_(gui[\textBox].palette.base.alpha_(0.97))
      .selectedStringColor_(Color.gray(gui[\textBox].palette.baseText.asHSV[2].round))
      .hiliteColor_(gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2))
      .font_(font.copy.size_(font.size * 1.2.reciprocal));
    };

    gui[\topBlackPanel].background_(gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2));

    gui[\curCue].stringColor_(Color.gray(gui[\textBox].palette.baseText.asHSV[2].round))
    .font_(font.copy.size_(20));



    // INTERACTION

    displayCuefuncs = { |cuefuncs|
      var oldindex = gui[\cueList].value ?? cueList.currentCueIndex;
      selectedfuncs = cuefuncs;

      gui[\cueList].items = cuefuncs.collect(_.name);
      gui[\cueList].action = { |view|
        selectedfunc = cuefuncs[view.value];

        gui[\curCue].string = cuefuncs[view.value][\name];
        gui[\textBox].string = cuefuncs[view.value][\func].def.sourceCode.findRegexp("^\\{(\\s*\\|thisCueList\\|)?[\\n\\s]*(.*)\\}$")[2][1];
      };
      gui[\cueList].valueAction = oldindex;
    };

    gui[\times].action = { |view|
      timesActions[view.value].();
    };

    gui[\days].action = { |view|
      var day = view.items[view.value];
      var month = months.indexOf(gui[\months].items[gui[\months].value]);
      var year = gui[\years].items[gui[\years].value];
      var dict = backupfuncs[year][month][day];
      var items = [];

      selection[\day] = day;

      timesActions = [];
      dict.keys.asArray.sort.do { |hour|
        dict[hour].keys.asArray.sort.do { |min|
          dict[hour][min].keys.asArray.sort.do { |sec|
            var cuefuncs = dict[hour][min][sec];
            var ampm = if (hour > 12) { "PM" } { "AM" };
            var timestring = "" ++ ((hour - 1) % 12 + 1) ++ ":" ++ min ++ " " ++ ampm;
            items = items.add(timestring);
            timesActions = timesActions.add({ displayCuefuncs.(cuefuncs) });
          };
        };
      };
      gui[\times].items = items;
      gui[\times].valueAction = gui[\times].items.size - 1;
    };

    gui[\months].action = { |view|
      var month = months.indexOf(view.items[view.value]);
      var year = gui[\years].items[gui[\years].value];

      selection[\month] = month;

      gui[\days].items = backupfuncs[year][month].keys.asArray.sort;
      gui[\days].valueAction = gui[\days].items.size - 1;
    };

    gui[\years].action = { |view|
      var year = view.items[view.value];

      selection[\year] = year;

      gui[\months].items = backupfuncs[year].keys.asArray.sort.collect(months[_]);
      gui[\months].valueAction = gui[\months].items.size - 1;
    };

    gui[\years].items = backupfuncs.keys.asArray.sort;
    gui[\years].valueAction = gui[\years].items.indexOf(selection[\year]);
  }
}
