CueList {
  var <filepath, <cueFuncs;
  var <>defaultFunc;
  var <currentCueIndex = 0;
  var <lastExecutedCue = '';
  var <unsavedCueChanges = false;
  var <unsavedListChanges = false;
  var <defaultfilepath;

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

    preExecuteHook.(this, index, name, func);
    func.(this);
    postExecuteHook.(this, index, name, func);


    lastExecutedCue = name;
    if (this.currentCueIndex == index) { this.incrementCueIndex() };
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

  currentCueFunc {
    ^cueFuncs[currentCueIndex][\func];
  }

  nextCueName {
    ^cueFuncs[currentCueIndex + 1 % cueFuncs.size][\name];
  }

  prevCueName {
    ^cueFuncs[currentCueIndex - 1 % cueFuncs.size][\name];
  }

  gotoCueName { |name|
    cueFuncs.do { |cue, i|
      if (cue[\name].asSymbol == name.asSymbol) {
        this.currentCueIndex_(i);
      };
    };
  }

  cueNames {
    ^cueFuncs.collect(_[\name]);
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
    cueFuncs[currentCueIndex][\name] = name;
    this.changed(\cueFuncs);
    this.changed(\unsavedChanges);
  }

  moveCurrentCueUp {
    var index = currentCueIndex;
    var newIndex = if (index == 0) { 0 } { index - 1 };
    var func = this.currentCueFunc;
    var name = this.currentCueName;
    this.deleteCurrentCue;
    this.addCue(newIndex, name, func);
    this.currentCueIndex_(newIndex);
  }

  moveCurrentCueDown {
    var index = currentCueIndex;
    var newIndex = if (index == (cueFuncs.size - 1)) { index } { index + 1 };
    var func = this.currentCueFunc;
    var name = this.currentCueName;
    this.deleteCurrentCue;
    this.addCue(newIndex, name, func);
    this.currentCueIndex_(newIndex);
  }

  addCueAfterCurrent { |name|
    this.addEmptyCue(currentCueIndex + 1, name);
  }

  addCueBeforeCurrent { |name|
    this.addEmptyCue(currentCueIndex, name);
  }

  addEmptyCue { |index, name|
    this.addCue(index, name, defaultFunc);
  }

  addCue { |index, name, func|
    cueFuncs = cueFuncs.insert(index, (name: name, func: func));
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

  makeWindow { |bounds|
    bounds = bounds ?? Window.screenBounds.width_(800);
    ^CueListWindow(filepath, bounds).cueList_(this);
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
