(
var backupfolder = "~/test/backups/".standardizePath;
var backupfuncs = ();
var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
var win, gui = (), timesActions = [];
var font = Font.monospace.size_(13);
var selection = ();
var displayCuefuncs { |cuefuncs|
  cuefuncs.postcs;
};

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

Window.closeAll;
win = Window("Browse backups", Rect(0, 50, 800, Window.screenBounds.height - 100))
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
.items_(backupfuncs[2016][11][6][1][\38][\38].collect(_.name))
.resize_(4);

gui[\resizePanel] = View(win, Rect(200, 210, 5, win.bounds.height - 255))
.background_(Color.gray(0.95, 0.9))
.resize_(4);

gui[\topBlackPanel] = View(win, Rect(205, 210, win.bounds.width - 205, 56))
.background_(Color.black)
.resize_(2);

gui[\curCue] = StaticText(win, Rect(210, 215, win.bounds.width - 220, 45))
.string_(backupfuncs[2016][11][6][1][\38][\38][0][\name])
.stringColor_(Color.white)
.resize_(2);

gui[\textBoxContainer] = View(win, Rect(205, 265, win.bounds.width - 205, win.bounds.height - 310)).resize_(5);

gui[\textBox] = CodeView(gui[\textBoxContainer], Rect(-1, 0, win.bounds.width - 203, win.bounds.height - 310))
.string_(backupfuncs[2016][11][6][1][\38][\38][0][\func].def.sourceCode.findRegexp("^\\{(\\s*\\|thisCueList\\|)?[\\n\\s]*(.*)\\}$")[2][1])
.font_(font)
.editable_(false)
.resize_(5);

gui[\bottomPanel] = View(win, Rect(0, win.bounds.height - 50, win.bounds.width, 50))
.background_(Color.gray(0.9))
.resize_(8)
.layout_(
  GridLayout.rows(
    [
      Button().states_([["Restore entire CueList"]]), Button().states_([["Restore this function to current cue"]]), Button().states_([["Close this window"]])
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


gui[\times];

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

/*

{


}.();
*/


)
