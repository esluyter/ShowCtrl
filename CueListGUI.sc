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
      .background_(Color.clear)
      .resize_(2),

      curCue: StaticText(view, Rect(leftPanelWidth + margin, margin, bounds.width - leftPanelWidth - (3*margin) - buttonWidth, 45))
      .string_("Current cue")
      .stringColor_(Color.white)
      .resize_(2),

      cueList: ListView(view, Rect(-1, -1, leftPanelWidth + 2, bounds.height - bottomPanelHeight - margin + 1))
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


      resizePanel: View(view, Rect(leftPanelWidth - (margin), 0, margin, bounds.height - bottomPanelHeight - margin))
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
        background: View(view, Rect(0, bounds.height - bottomPanelHeight - margin - 1, bounds.width, bottomPanelHeight + margin))
        .background_(Color.gray(0.9))
        .resize_(8),

        backupsButt: Button(view, Rect(bounds.width - buttonWidth - margin, margin, buttonWidth, (45 - margin) / 2))
        .states_([["Backups..."]])
        .resize_(3),

        updateButt: Button(view, Rect(bounds.width - buttonWidth - margin, margin + ((45 + margin) / 2), buttonWidth, (45 - margin) / 2))
        .states_([["✎ Update"]])
        .resize_(3)
        .visible_(false),

        // --- actual bottom panel... :/ ---

        goButt: Button(view, Rect(margin * 1.5, bounds.height - bottomPanelHeight, 2 * buttonWidth / 3 - margin, bottomPanelHeight - margin))
        .states_([["GO"]])
        .resize_(7),

        upButt: Button(view, Rect(margin * 1.5 + (2 * buttonWidth / 3), bounds.height - bottomPanelHeight, buttonWidth / 3, bottomPanelHeight / 2 - margin))
        .states_([["▲"]])
        .resize_(7),

        downButt: Button(view, Rect(margin * 1.5 + (2 * buttonWidth / 3), bounds.height - (bottomPanelHeight / 2), buttonWidth / 3, bottomPanelHeight / 2 - margin))
        .states_([["▼"]])
        .resize_(7),

        separator0: View(view, Rect(margin * 1.75 + (buttonWidth * 1.1), bounds.height - bottomPanelHeight, 1, bottomPanelHeight - margin))
        .background_(Color.gray(0.8))
        .resize_(7),

        renameButt: Button(view, Rect(margin * 2 + (buttonWidth * 1.2), bounds.height - bottomPanelHeight, buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["✎ Rename"]])
        .resize_(7),

        deleteButt: Button(view, Rect(margin * 2 + (buttonWidth * 1.2), bounds.height - (bottomPanelHeight/2), buttonWidth, bottomPanelHeight / 2 - margin))
        .states_([["⌫ Delete"]])
        .resize_(7),

        moveUpButt: Button(view, Rect(margin * 3 + (buttonWidth * 2.2), bounds.height - bottomPanelHeight, buttonWidth - 12, bottomPanelHeight / 2 - margin))
        .states_([["↑ Move"]])
        .resize_(7),

        moveDownButt: Button(view, Rect(margin * 3 + (buttonWidth * 2.2), bounds.height - (bottomPanelHeight/2), buttonWidth - 12, bottomPanelHeight / 2 - margin))
        .states_([["↓ Move"]])
        .resize_(7),

        addBeforeButt: Button(view, Rect(margin * 4 + (buttonWidth * 3.2) - 12, bounds.height - bottomPanelHeight, buttonWidth - 12, bottomPanelHeight / 2 - margin))
        .states_([["+↑ Add"]])
        .resize_(7),

        addAfterButt: Button(view, Rect(margin * 4 + (buttonWidth * 3.2) - 12, bounds.height - (bottomPanelHeight/2), buttonWidth - 12, bottomPanelHeight / 2 - margin))
        .states_([["+↓ Add"]])
        .resize_(7),


        leftBackground: View(view, Rect(margin * 4.25 + (buttonWidth * 4.4) - 24, bounds.height - bottomPanelHeight - margin - 1, bounds.width - (margin * 4.25 + (buttonWidth * 4.4) - 24), bottomPanelHeight + margin))
        .resize_(8),




        saveButt: Button(view, Rect(margin * 4.5 + (buttonWidth * 4.6) - 24, bounds.height - bottomPanelHeight, buttonWidth * 1.1, bottomPanelHeight / 2 - margin))
        .states_([["Save list"]])
        .resize_(7),

        refreshButt: Button(view, Rect(margin * 4.5 + (buttonWidth * 4.6) - 24, bounds.height - (bottomPanelHeight/2), buttonWidth * 1.1, bottomPanelHeight / 2 - margin))
        .states_([["Open list..."]])
        .resize_(7),


        separator2: View(view, Rect((5.8*buttonWidth) + (4.75*margin) - 24, bounds.height - bottomPanelHeight, 1, bottomPanelHeight - margin))
        .background_(Color.gray(0.8))
        .resize_(7),


        colorSchemeLabel: StaticText(view, Rect((5.9*buttonWidth) + (5*margin) - 24, bounds.height - bottomPanelHeight + 1, buttonWidth * 2, bottomPanelHeight / 2 - margin))
        .string_("Color scheme:")
        .resize_(7),

        colorScheme: EZPopUpMenu(view, Rect((5.9*buttonWidth) + (5*margin) - 24, bounds.height - (bottomPanelHeight/2) - 3, buttonWidth * 2, bottomPanelHeight / 2 - margin), nil, [
          "One Dark" -> { gui[\textBox].oneDarkColorScheme },
          "One Light" -> { gui[\textBox].oneLightColorScheme },
          "Paper Light" -> { gui[\textBox].lightColorScheme },
          "Bright Dark" -> { gui[\textBox].darkColorScheme }
        ]),

        keyBindingsButt: Button(view, Rect((7.9*buttonWidth) + (7*margin) - 24, bounds.height - (bottomPanelHeight/2) - 3, buttonWidth * 1.4, bottomPanelHeight / 2 - margin).postln)
        .states_([["Keybindings"]])
        .action_({ this.makeKeyBindingsWindow })
        .resize_(7);

      )
    );

    gui[\bottomPanel][\colorScheme].view.resize_(7);

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
        ^cueList.executeCurrentCue;
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

    buttons[\upButt].action_({ cueList.decrementCueIndex });
    buttons[\downButt].action_({ cueList.incrementCueIndex });
    buttons[\goButt].action_({ cueList.executeCurrentCue });



    gui[\cueList]
    .mouseDownAction_({ |v, x, y, mod, buttNum, clickCount|
      if (buttNum == 1) { ShowCtrlContextMenu.create(this, x - 2, y - 2) };
    })
    .mouseUpAction_({ |v|
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

  makeKeyBindingsWindow {
    var background = gui[\textBox].palette.base.copy.alpha_(0.9);
    var text = gui[\textBox].palette.baseText;
    var headingtext = Color.hsv(text.asHSV[0], text.asHSV[1], (text.asHSV[2].round(1.0) + text.asHSV[2]) / 2);

    var makeKeyBindingItem = { |parent, top, key, binding, backgroundColor, font|
      var bounds = Rect(0, top, parent.bounds.width, 20);
      var view = View(parent, bounds).background_(backgroundColor).resize_(2);
      font = font ?? Font().size_(12);

      StaticText(view, Rect(10, 0, 70, 20))
      .string_(key)
      .font_(font)
      .stringColor_(text);
      StaticText(view, Rect(85, 0, parent.bounds.width - 100, 20))
      .string_(binding)
      .font_(font)
      .stringColor_(text)
      .resize_(2);

      view;
    };

    var makeHeading = { |parent, top, heading, font|
      var bounds = Rect(0, top, parent.bounds.width, 20);
      var view = View(parent, bounds).resize_(2);
      font = font ?? Font().size_(12).bold_(true);

      StaticText(view, Rect(10, 0, parent.bounds.width - 30, 20))
      .string_(heading)
      .font_(font)
      .stringColor_(headingtext)
      .resize_(2);

      view;
    };

    var addSection = { |heading, items|
      var toggle = false;

      runningTop = runningTop + 10;
      makeHeading.(z, runningTop, heading);
      runningTop = runningTop + 20;

      items.pairsDo { |key, binding|
        makeKeyBindingItem.(z, runningTop, key, binding, (if (toggle) { toggle = false; Color.gray(0, 0.1) } { toggle = true; Color.clear }));
        runningTop = runningTop + 20;
      };
    };

    var runningTop = 0;

    var z = Window("Keybindings").front;

    z.bounds = z.bounds.insetBy(0, -100);
    z.background = Color.clear;

    z = ScrollView(z, z.bounds.origin_(0@0).insetBy(-2, -2)).resize_(5)
    .background_(background);

    StaticText(z, Rect(10, 0, z.bounds.width, 40))
    .string_("Keybindings List")
    .font_(Font().size_(30))
    .stringColor_(headingtext);

    runningTop = 40;

    addSection.("File operations", [
      "⌘N", "New cue list",
      "⌘O", "Open cue list...",
      "⌘S", "Save cue list (prompt for location on first save)",
    ]);

    addSection.("Cue list operations", [
      "⌥↑", "Go up in cue list",
      "⌥↓", "Go down in cue list",
      "⌥Space", "Execute current cue",
      "⌥⌘↑", "Add cue above current cue",
      "⌥⌘↓", "Add cue below current cue",
      "⇧⌘↑", "Move current cue up in cue list",
      "⇧⌘↓", "Move current cue down in cue list",
      "⌘R", "Rename current cue",
      "⌘⌫", "Delete current cue",
      "⌘G", "Go to cue number"
    ]);

    addSection.("SuperCollider operations", [
      "⇧⏎", "Execute current line or selection",
      "⌘⏎", "Execute current region or selection",
      "⌘B", "Boot default server",
      "⇧⌘B", "Reboot default server",
      "⌘K", "Quit default server",
      "⇧⌘L", "Recompile class library",
      "⇧⌘P", "Clear Post window"
    ]);


    addSection.("SuperCollider GUI", [
      "⌘M", "Show input and output meters",
      "⇧⌘M", "Show oscilloscope",
      "⌥⌘M", "Show frequency scope",
      "⌥⌘T", "Show server node tree",
    ]);

    addSection.("Code view operations", [
      "⇥", "Indent current line or selected lines",
      "⇧⇥", "De-indent current line or selected lines",
      "⌘Z", "Undo",
      "⌘Y", "Redo",
    ]);

    addSection.("Autocomplete operations", [
      "⌘↑", "Select previous autocompletion suggestion",
      "⌘↓", "Select next autocompletion suggestion",
      "⌘→, ⌘←,", "Accept selected autocompletion",
      "⇧Space", "",

    ]);
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
      PostView.postln("ERROR: Compile error. See IDE post window for details.");
      { gui[\topBlackPanel].background_(Color.red) }.defer(0.001);
      { gui[\topBlackPanel].background_(restoreBackground) }.defer(0.1);
      { gui[\topBlackPanel].background_(Color.red) }.defer(0.2);
      { gui[\topBlackPanel].background_(restoreBackground) }.defer(0.3);
    } {
      restoreBackground = gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2);
      {
        gui[\topBlackPanel].background = gui[\textBox].palette.base.blend(Color.green, 0.8);
        gui[\cueList].hiliteColor = gui[\textBox].palette.base.blend(Color.green, 0.8);
        gui[\bottomPanel][\updateButt].visible_(true).states = [[
          gui[\bottomPanel][\updateButt].states[0][0],
          nil,
          Color.green
        ]];
        nil
      }.defer(0.001);
      {
        /*
        gui[\topBlackPanel].background = restoreBackground;
        gui[\cueList].hiliteColor = restoreBackground;
        gui[\bottomPanel][\updateButt].visible = false;
        */
        this.updateUnsaved;
      }.defer(0.2);
      cueList.currentCueFunc_(func);
    };
  }

  saveCueFuncs { |action|
    var celebrate = {
      var restoreBackground = gui[\textBox].palette.base;
      var buttonText = gui[\textBox].palette.baseText;
      var buttonColor = gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.1);
      {
        gui[\cueList].background = gui[\textBox].palette.base.blend(Color.green, 0.1).alpha_(0.97);
        gui[\bottomPanel][\saveButt].states = [[
          gui[\bottomPanel][\saveButt].states[0][0],
          nil,
          Color.green
        ]];
        nil
      }.defer(0.001);
      {
        gui[\cueList].background = restoreBackground;
        gui[\bottomPanel][\saveButt].states = [[
          gui[\bottomPanel][\saveButt].states[0][0],
          buttonText,
          buttonColor
        ]];
        gui[\bottomPanel][\backupsButt].visible = true;
      }.defer(0.2);
    };

    if (cueList.saveCueFuncs) {
      celebrate.();
    } {
      FileDialog({ |path|
        path = path.asPathName;
        path = path.pathOnly +/+ path.fileNameWithoutExtension;
        (if (File.exists(path)) { "rm -r " ++ path.copy.replace(" ", "\\ ") } { "" }).unixCmd({ |exitcode|
          if (exitcode != 0) {
            exitcode.postln;
            { this.saveCueFuncs }.defer(0.2);
          } {
            ("mkdir " ++ path.copy.replace(" ", "\\ ")).unixCmd({ |exitcode|
              if (exitcode != 0) {
                "B".postln;
                { this.saveCueFuncs }.defer(0.2);
              } {
                ("cp -r " ++ cueList.defaultfilepath.copy.replace(" ", "\\ ") +/+ "* " ++ path.copy.replace(" ", "\\ ")).unixCmd({ |exitcode|
                  if (exitcode != 0) {
                    this.confirmBox("There was an error");
                  } {
                    cueList.filepath = path;
                    cueList.saveCueFuncs;
                    //{ action.value }.defer(0.2);
                    celebrate.();
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
        cueList.currentCueIndex = 0;
      });
    } {
      cueList.filepath = cueList.defaultfilepath;
      cueList.refreshCueFuncs;
      cueList.currentCueIndex = 0;
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
    leftPanelWidth = max(newLeftPanelWidth, margin);

    gui[\topBlackPanel].bounds_(Rect(leftPanelWidth, 0, bounds.width - leftPanelWidth, 46 + (2 * margin)));

    gui[\curCue].bounds_(Rect(leftPanelWidth + margin, 2*margin, bounds.width - leftPanelWidth - (3*margin), 35));

    gui[\cueList].bounds_(Rect(-1, 0, leftPanelWidth + 2, bounds.height - bottomPanelHeight - margin));

    textBoxContainer.bounds_(Rect(leftPanelWidth, 45 + (2 * margin), bounds.width - leftPanelWidth, bounds.height - 35 - bottomPanelHeight - (5*margin)));

    gui[\resizePanel].bounds_(gui[\resizePanel].bounds.left_(leftPanelWidth - (margin)));
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
    var blendedBackground;
    var buttonText = gui[\textBox].palette.baseText;
    var buttonColor = gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.1);
    {
      gui.bottomPanel[\saveButt].states_([[
        gui.bottomPanel[\saveButt].states[0][0],
        if (cueList.unsavedListChanges) { nil } { buttonText },
        if (cueList.unsavedListChanges) {Color(1, 0.3, 0.45)} { buttonColor }
      ]]);

      if (cueList.unsavedListChanges) {
        blendedBackground = gui[\textBox].palette.base.blend(Color(1, 0, 0.2), 0.05).alpha_(0.97);
      } {
        blendedBackground = gui[\textBox].palette.base.alpha_(0.97);
      };
      gui[\cueList].background = blendedBackground;
    }.defer;

    {
      gui.bottomPanel[\updateButt].states_([[
        gui.bottomPanel[\updateButt].states[0][0],
        if (cueList.unsavedCueChanges) { nil } { buttonText },
        if (cueList.unsavedCueChanges) {
          gui[\textBox].palette.base.blend(Color.green, 0.7)
        } { buttonColor }
      ]]).visible_(cueList.unsavedCueChanges);

      if (cueList.unsavedCueChanges) {
        blendedBackground = gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2).blend(Color.green, 0.2);
      } {
        blendedBackground = gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2);
        if (cueList.notNil) { if (cueList.currentCueColor.notNil) {
          blendedBackground = blendedBackground.blend(cueList.currentCueColor, 0.2)
        } };
      };
      gui[\topBlackPanel].background = blendedBackground;
      gui[\curCue].stringColor_(Color.gray(gui[\textBox].palette.baseText.asHSV[2].round));
      if (cueList.notNil) { if (cueList.currentCueColor.notNil && cueList.unsavedCueChanges.not) {
        gui[\topBlackPanel].background = cueList.currentCueColor;
        gui[\curCue].stringColor_(Color.gray(1 - cueList.currentCueColor.asArray[0..2].mean.round));
      }};
      gui[\cueList].hiliteColor = blendedBackground;
    }.defer;
  }

  updateCues {
    defer {
      gui[\cueList].items_(cueList.cueNames.collect { |name, i|
        i.asString.padLeft(cueList.cueFuncs.size.asString.size) ++ "  " ++ name
      });
      gui[\bottomPanel][\backupsButt].visible_((cueList.filepath == cueList.defaultfilepath).not);
      this.updateCurrentCue;
      this.updateCueColors;
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

  updateCueColors {
    gui[\cueList].colors = cueList.cueColors.collect({ |color|
      if (color.isNil) { Color.clear } { color.copy.alpha_(0.5) }
    });
  }

  makeStyle {
    var buttonText = gui[\textBox].palette.baseText;
    var buttonColor = gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.1);
    var blendedBackground = gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2);
    var moreBlendedBackground = gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.4);

    gui[\cueList].palette_(gui[\textBox].palette)
    .background_(gui[\textBox].palette.base.alpha_(0.97))
    .selectedStringColor_(Color.gray(gui[\textBox].palette.baseText.asHSV[2].round))
    .hiliteColor_(blendedBackground);

    gui[\topBlackPanel].background_(gui[\textBox].palette.base.blend(gui[\textBox].palette.base.complementary, 0.2));
    gui[\curCue].stringColor_(Color.gray(gui[\textBox].palette.baseText.asHSV[2].round));

    gui[\bottomPanel].do { |thing|
      if (thing.class == Button) {
        thing.states_([[thing.states[0][0], buttonText, buttonColor]]);
      };
    };
    gui[\bottomPanel][\background].background = blendedBackground;
    gui[\bottomPanel][\separator0].background = moreBlendedBackground;
    //gui[\bottomPanel][\separator1].background = moreBlendedBackground;
    gui[\bottomPanel][\separator2].background = moreBlendedBackground;

    gui[\bottomPanel][\colorSchemeLabel].stringColor_(buttonText);
    gui[\bottomPanel][\colorScheme].widget.background_(buttonColor).stringColor_(buttonText);

    gui[\bottomPanel][\leftBackground].background = buttonColor;

    { gui[\bottomPanel][\goButt].font_(Font().size_(20)) }.defer(0.01);
  }

  update { |obj, what, thing|
    switch (what)
    {\cueFuncs} { this.updateCues }
    {\cueColors} { this.updateCueColors }
    {\unsavedChanges} { this.updateUnsaved }
    {\currentCueIndex} { this.updateCurrentCue }
    {\confirmForceCueIndex} {
      gui[\cueList].selection_([cueList.currentCueIndex]); // reset cuelist
      this.confirmBox("Discard cue edits?", { cueList.currentCueIndex_(thing, true) })
    }
    {\colorScheme} { this.makeStyle; this.updateUnsaved; }
    {\clearPost} { this.changed(\clearPost); };
  }
}


CueListWindow : SCViewHolder {
  var <win, <isFront = false, <>toFrontAction, <>endFrontAction, <completeWindow, <postView, <postViewContainer, <postViewHeight = 125, <resizePanel, dragStart;

  *new { |name="", bounds, action|
    ^super.new.init(name, bounds, action);
  }

  init { |name, bounds, action|
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

    view.addDependant(this);

    this.makeCompleteWindow; // let the complete window bring cue list window to front :)

    { this.makePostView; action.value }.defer(0.2); // call action on post view complete
  }

  makeCompleteWindow { |bounds|
    var cueListWindow = this;

    bounds = bounds ?? Rect(win.bounds.width + view.margin, 0, 500, 300);
    if (completeWindow.notNil) { completeWindow.close };

    completeWindow = view.gui[\textBox].makeCompleteWindow(bounds, win);

    completeWindow.endFrontAction = {
      if (cueListWindow.isFront.not) { cueListWindow.completeWindow.visible = false };
    };

    completeWindow.toFrontAction = {
      if (win.isClosed.not) {
        if (win.visible) { cueListWindow.view.suppressToFront = true; }
      };
    };
  }

  makePostView {
    var height = postViewHeight;
    var bounds = Rect(0, win.bounds.height - height, win.bounds.width, height);

    postViewContainer = View(win, bounds).resize_(8);

    postView = PostView(postViewContainer, bounds.copy.origin_(0@0).insetBy(-1)).mute_(true).resize_(5);
    view.bounds_(view.bounds.resizeBy(0, -1 * height + 1));
    postView.mute_(false);
    postView.postln("Ready");

    resizePanel = View(win, Rect(0, bounds.top - (view.margin * 0.7), bounds.width, view.margin * 1.6))
    .background_(Color.gray(0, 0))
    .resize_(2)
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
      this.postViewHeight_(dragStart.y - y + postViewHeight);
    });
  }

  postViewHeight_ { |newHeight|
    var height = max(newHeight, 0);
    var bounds = Rect(0, win.bounds.height - height, win.bounds.width, height);

    resizePanel.bounds = resizePanel.bounds.top_(bounds.top - (view.margin * 0.7));

    postViewHeight = height;
    postViewContainer.bounds_(bounds);
    view.bounds_(view.bounds.height_(win.bounds.height - height + 1));
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

  gui {
    ^view.gui;
  }

  textBox {
    ^view.gui[\textBox];
  }

  clearPost {
    postView.clear;
  }

  update { |obj, what|
    switch (what)
    {\filepath} { win.name_(view.cueList.filepath) }
    {\clearPost} { this.clearPost }
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

    gui[\topBlackPanel] = View(win, Rect(200, 210, win.bounds.width - 200, 56))
    .background_(Color.black)
    .resize_(2);

    gui[\curCue] = StaticText(win, Rect(205, 215, win.bounds.width - 215, 45))
    .stringColor_(Color.white)
    .resize_(2);

    gui[\textBoxContainer] = View(win, Rect(200, 265, win.bounds.width - 200, win.bounds.height - 310)).resize_(5);

    gui[\textBox] = CodeView(gui[\textBoxContainer], Rect(-1, 0, win.bounds.width - 198, win.bounds.height - 310))
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
      gui[\cueList].colors = cuefuncs.collect({ |cue|
        var color = cue.color;
        if (color.isNil) { Color.clear } { color.copy.alpha_(0.5) }
      });

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
