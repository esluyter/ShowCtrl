CueList {
  var <filepath, <cueFuncs;
  var <>defaultFunc;
  var <currentCueIndex = 0;
  var <lastExecutedCue = '';
  var <unsavedCueChanges = false;
  var <unsavedListChanges = false;
  var <defaultfilepath;
  var <>incrementCueOnFire = true, <>incrementCueOnError = true;

  var <>preExecuteHook, <>postExecuteHook;

  *new { |filepath, preExecuteHook, postExecuteHook, defaultFunc|
    ^super.new.init(filepath, preExecuteHook, postExecuteHook, defaultFunc);
  }

  init { |argfilepath, argprehook, argposthook, argdeffunc|
    defaultfilepath = "defaultCue".resolveRelative;
    filepath = argfilepath ?? defaultfilepath;//thisProcess.nowExecutingPath.dirname;

    preExecuteHook = argprehook;
    postExecuteHook = argposthook;

    defaultFunc = argdeffunc ? { |thisCueList|
/*

Cue on:
Page #:

*/

};

    this.refreshCueFuncs;
  }

  refreshCueFuncs {
    cueFuncs = File(filepath +/+ "cue-data.scd", "r").readAllString.interpret;
    unsavedCueChanges = false;
    unsavedListChanges = false;
    this.changed(\cueFuncs);
    this.changed(\unsavedChanges);
  }

  executeCurrentCue {
    var func = this.currentCueFunc;
    var name = this.currentCueName;
    var index = this.currentCueIndex;
    var retval = true;

    try {
      preExecuteHook.(this, index, name, func);
      func.(this);
      postExecuteHook.(this, index, name, func);
    } { |error|
      error.reportError;
      if (incrementCueOnError) {
        "ERROR: Error executing cue! Moved on to the next one to save the show.".postln;
        retval = false; // delay returning until the index has incremented.
      } {
        "ERROR: Error executing cue! Stopped in place.".postln;
        ^false;
      }
    };

    lastExecutedCue = name;
    if (this.currentCueIndex == index) { this.incrementCueIndex() };
    ^retval;
  }

  incrementCueIndex { |force = false|
    this.currentCueIndex_(currentCueIndex + 1, force);
  }

  decrementCueIndex { |force = false|
    this.currentCueIndex_(currentCueIndex - 1, force);
  }

  currentCueIndex_ { |newindex, force = false|
    if (unsavedCueChanges && force.not) {
      this.changed(\confirmForceCueIndex, newindex);
      "CueList: Confirm discard changes and use force=true".postln;
      ^false;
    };

    currentCueIndex = newindex % cueFuncs.size;
    this.unsavedCueChanges = false;
    this.changed(\currentCueIndex);
  }

  currentCueName {
    ^cueFuncs[currentCueIndex][\name];
  }

  currentCueColor {
    ^cueFuncs[currentCueIndex][\color];
  }

  currentCueFunc {
    ^cueFuncs[currentCueIndex][\func];
  }

  currentCueLevel {
    ^(cueFuncs[currentCueIndex][\level] ?? 0)
  }

  nextCueName {
    ^cueFuncs[currentCueIndex + 1 % cueFuncs.size][\name];
  }

  nextCueLevel {
    ^(cueFuncs[currentCueIndex + 1 % cueFuncs.size][\level] ?? 0)
  }

  prevCueName {
    ^cueFuncs[currentCueIndex - 1 % cueFuncs.size][\name];
  }

  gotoCueName { |name|
    cueFuncs.do { |cue, i|
      if (cue[\name].asSymbol == name.asSymbol) {
        this.currentCueIndex_(i);
        ^true;
      };
    };
    ^false;
  }

  cueNames {
    ^cueFuncs.collect(_[\name]);
  }

  cueColors {
    ^cueFuncs.collect(_[\color]);
  }

  saveCueFuncs {
    var backupfilepath = filepath +/+ "backups" +/+ "cue-data" ++
    Date.localtime.format("__%Y-%m-%d__%H.%M.%S__") ++ ".scd";

    if (filepath == defaultfilepath) {
      "Can't overwrite default cuelist".postln;
      ^false;
    };

    ("mv \"" ++ filepath +/+ "cue-data.scd\" \"" ++ backupfilepath ++ "\"").unixCmd( {
      var f = File(filepath  +/+ "cue-data.scd", "w");
      f.write(cueFuncs.asCompileString);
      f.close;
    }, false);
    unsavedListChanges = false;
    unsavedCueChanges = false;
    this.changed(\unsavedChanges);
    ^true;
  }

  unsavedCueChanges_ { |unsaved|
    unsavedCueChanges = unsaved;
    this.changed(\unsavedChanges);
  }

  currentCueFunc_ { |func|
    cueFuncs[currentCueIndex][\func] = func;
    unsavedCueChanges = false;
    unsavedListChanges = true;
    this.changed(\unsavedChanges);
  }

  currentCueName_ { |name|
    if (name != cueFuncs[currentCueIndex][\name]) { unsavedListChanges = true };
    cueFuncs[currentCueIndex][\name] = name.asSymbol;
    this.changed(\cueFuncs);
    this.changed(\unsavedChanges);
  }

  currentCueColor_ { |color|
    if (color != cueFuncs[currentCueIndex][\color]) { unsavedListChanges = true };
    cueFuncs[currentCueIndex][\color] = color;
    this.changed(\cueColors);
    this.changed(\unsavedChanges);
  }

  currentCueLevel_ { |level|
    if (level != cueFuncs[currentCueIndex][\level]) { unsavedListChanges = true };
    cueFuncs[currentCueIndex][\level] = level;
    this.changed(\cueLevels);
    this.changed(\unsavedChanges);
  }

  moveCurrentCueUp {
    var index = currentCueIndex;
    var newIndex = if (index == 0) { 0 } { index - 1 };
    var func = this.currentCueFunc;
    var name = this.currentCueName;
    var level = this.currentCueLevel;
    var color = this.currentCueColor;
    this.deleteCurrentCue;
    this.addCue(newIndex, name, func, color, level);
    this.currentCueIndex_(newIndex);
  }

  moveCurrentCueDown {
    var index = currentCueIndex;
    var newIndex = if (index == (cueFuncs.size - 1)) { index } { index + 1 };
    var func = this.currentCueFunc;
    var name = this.currentCueName;
    var level = this.currentCueLevel;
    var color = this.currentCueColor;
    this.deleteCurrentCue;
    this.addCue(newIndex, name, func, color, level);
    this.currentCueIndex_(newIndex);
  }

  addCueAfterCurrent { |name|
    this.addEmptyCue(currentCueIndex + 1, name);
  }

  addCueBeforeCurrent { |name|
    this.addEmptyCue(currentCueIndex, name);
  }

  addEmptyCue { |index, name|
    var funcCue = cueFuncs.detect({ |cue|
      cue.name == "default new cue function"
    });
    var func = if (funcCue.isNil) { defaultFunc } { funcCue[\func] };
    this.addCue(index, name, func);
  }

  addCue { |index, name, func, color, level|
    cueFuncs = cueFuncs.insert(index, (name: name.asSymbol, func: func, color: color, level: level));
    unsavedListChanges = true;
    this.changed(\cueFuncs);
    this.changed(\unsavedChanges);
    this.changed(\currentCueIndex);
  }

  deleteCurrentCue {
    this.deleteCue(currentCueIndex);
  }

  deleteCue { |index|
    cueFuncs.removeAt(index);
    unsavedListChanges = true;
    this.changed(\cueFuncs);
    this.changed(\unsavedChanges);
    this.changed(\currentCueIndex);
  }

  makeWindow { |bounds, action|
    bounds = bounds ?? Window.screenBounds.width_(800);
    ^CueListWindow(filepath, bounds, action).cueList_(this);
  }

  filepath_ { |newfilepath|
    filepath = newfilepath;
    this.changed(\filepath);
  }

  cueFuncs_ { |newcuefuncs|
    cueFuncs = newcuefuncs;
    this.changed(\cueFuncs);
    this.changed(\unsavedChanges);
  }
}
