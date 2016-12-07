(
var backupfolder = "~/Google Drive/B-side/Show control system new/backups/".standardizePath;
var backupfuncs = ();
var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
var win, gui = ();

PathName(backupfolder).files.do({ |filepath|
  var file;
  var year, month, day, hour, min, sec, timestring;
  var matches = filepath.fileName.findRegexp("^cue-data__(\\d{4})-(\\d{2})-(\\d{2})__(\\d{2})\\.(\\d{2})\\.(\\d{2})__\\.scd$");

  if (matches.size > 0) {
    year = matches[1][1].asInt;
    month = matches[2][1].asInt - 1;
    day = matches[3][1].asInt;
    hour = matches[4][1].asInt;
    //ampm = if (hour > 12) { "PM" } { "AM" };
    min = matches[5][1];
    sec = matches[6][1];
    //timestring = "" ++ ((hour - 1) % 12 + 1) ++ ":" ++ min ++ " " ++ ampm;

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

win = Window("Browse backups", Rect(0, 0, 800, Window.screenBounds.height)).front;

gui[\years] = ListView(win, Rect(0, 0, 200, 200))
.items_(backupfuncs.keys.asArray.sort);

gui[\months] = ListView(win, Rect(200, 0, 200, 200))
.items_(backupfuncs[2016].keys.asArray.sort.collect(months[_]));

gui[\days] = ListView(win, Rect(400, 0, 200, 200))
.items_(backupfuncs[2016][10].keys.asArray.sort);

gui[\times] = ListView(win, Rect(600, 0, 200, 200));
{
  var dict = backupfuncs[2016][10][7];
  var items = [];
  dict.keys.asArray.sort.do { |hour|
    dict[hour].keys.asArray.sort.do { |min|
      dict[hour][min].keys.asArray.sort.do { |sec|
        var cuefuncs = dict[hour][min][sec];
        var ampm = if (hour > 12) { "PM" } { "AM" };
        var timestring = "" ++ ((hour - 1) % 12 + 1) ++ ":" ++ min ++ " " ++ ampm;
        items = items.add(timestring);
      };
    };
  };
  gui[\times].items = items;
}.()
)
