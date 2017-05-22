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

  executeCurrentCue { |incrementIndex = true|
    var func = this.currentCueFunc;
    var name = this.currentCueName;
    var origName = name;
    var index = this.currentCueIndex;
    var origIndex = index;
    var level = this.currentCueLevel;
    var origLevel = level;
    var executeChildren = this.currentCueExecuteChildren;
    var retval = true;
    var cue;
    var exec = {
      ("-> [" ++ index ++ ": " ++ name ++ "] -> " ++ func.(this)).postln;
      index = (index + 1) % cueFuncs.size;
      cue = cueFuncs[index];
      func = cue[\func];
      name = cue[\name];
      level = cue[\level] ?? 0;
    };

    try {
      preExecuteHook.(this, index, name, func);
      exec.();
      if (executeChildren) {
        while { level > origLevel } {
          exec.();
        };
      };
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

    lastExecutedCue = origName;
    if (currentCueIndex == origIndex && incrementIndex) { this.currentCueIndex_(index) };
    ^retval;
  }

  incrementCueIndex { |force = false|
    this.currentCueIndex_(currentCueIndex + 1, force);
  }

  incrementCueIndexByLevel { |force = false|
    var level = 0;//this.currentCueLevel;
    var j = currentCueIndex + 1 % cueFuncs.size;

    while ({ (cueFuncs[j][\level] ?? 0) != level }) { j = j + 1 % cueFuncs.size };

    this.currentCueIndex_(j);
  }

  decrementCueIndex { |force = false|
    this.currentCueIndex_(currentCueIndex - 1, force);
  }

  decrementCueIndexByLevel { |force = false|
    var level = 0;//this.currentCueLevel;
    var j = currentCueIndex - 1 % cueFuncs.size;

    while ({ (cueFuncs[j][\level] ?? 0) != level }) { j = j - 1 % cueFuncs.size };

    this.currentCueIndex_(j);
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

  currentCueExecuteChildren {
    ^(cueFuncs[currentCueIndex][\executeChildren] ?? false)
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

  currentCueExecuteChildren_ { |bool|
    if (bool != this.currentCueExecuteChildren) { unsavedListChanges = true };
    cueFuncs[currentCueIndex][\executeChildren] = bool;
    this.changed(\cueFuncs);
    this.changed(\unsavedChanges);
  }

  currentCueLevelUp {
    var index = currentCueIndex;
    var level = cueFuncs[currentCueIndex][\level] ?? 0;
    var thisLevel;

    if ((currentCueIndex == 0)) {
      ^false;
    };

    if (level - (cueFuncs[index - 1][\level] ?? 0) > 0) {
      ^false;
    };

    cueFuncs[currentCueIndex][\level] = level + 1;

    index = index + 1;
    thisLevel = if (index >= cueFuncs.size) { 0 } { cueFuncs[index][\level] ?? 0 };
    while { thisLevel > level } {
      cueFuncs[index][\level] = thisLevel + 1;
      index = index + 1;
      thisLevel = if (index >= cueFuncs.size) { 0 } { cueFuncs[index][\level] ?? 0 };
    };

    this.changed(\cueLevels);
    this.changed(\unsavedChanges);
  }

  currentCueLevelDown {
    var index = currentCueIndex;
    var level = cueFuncs[currentCueIndex][\level] ?? 0;
    var thisLevel;

    if (level == 0) {
      ^false;
    };

    cueFuncs[currentCueIndex][\level] = max(level - 1, 0);

    index = index + 1;
    thisLevel = if (index >= cueFuncs.size) { 0 } { cueFuncs[index][\level] ?? 0 };
    while { thisLevel > level } {
      cueFuncs[index][\level] = max(thisLevel - 1);
      index = index + 1;
      thisLevel = if (index >= cueFuncs.size) { 0 } { cueFuncs[index][\level] ?? 0 };
    };

    this.changed(\cueLevels);
    this.changed(\unsavedChanges);
  }

  currentCueLevel_ { |level|
    if (level != cueFuncs[currentCueIndex][\level]) {
      unsavedListChanges = true
    };
    cueFuncs[currentCueIndex][\level] = level;
    this.changed(\cueLevels);
    this.changed(\unsavedChanges);
  }

  moveCurrentCueUp {
    var index = currentCueIndex;
    var newIndex;// = if (index == 0) { 0 } { index - 1 };
    var func = this.currentCueFunc;
    var name = this.currentCueName;
    var level = this.currentCueLevel;
    var color = this.currentCueColor;
    var j = index, groupSize, prevGroupSize;

    // figure out current group size
    while ({ groupSize.isNil }) {
      j = j + 1;
      if (j >= cueFuncs.size) {
        groupSize = j - index;
      } {
        if ((cueFuncs[j][\level] ?? 0) <= level) {
          groupSize = j - index;
        };
      };
    };

    j = index;

    if (index == 0) {
      // can't do shit
    } {
      // figure out next group size
      while ({ prevGroupSize.isNil }) {
        j = j - 1;
        if (j <= 0) {
          prevGroupSize = index - j;
        } {
          if ((cueFuncs[j][\level] ?? 0) <= level) {
            prevGroupSize = index - j;
          };
        };
      };
      newIndex = j;

      groupSize.do { |i|
        var cue = cueFuncs[index + i];
        var func = cue[\func];
        var name = cue[\name];
        var level = cue[\level] ?? 0;
        var color = cue[\color];
        cueFuncs.removeAt(index + i);
        cueFuncs = cueFuncs.insert(newIndex + i, (name: name.asSymbol, func: func, color: color, level: level));
      };

      this.currentCueIndex_(newIndex);

      unsavedListChanges = true;
      this.changed(\cueFuncs);
      this.changed(\unsavedChanges);
      this.changed(\currentCueIndex);
    };
  }

  moveCurrentCueDown {
    var index = currentCueIndex;
    var newIndex;// = if (index == (cueFuncs.size - 1)) { index } { index + 1 };
    var func = this.currentCueFunc;
    var name = this.currentCueName;
    var level = this.currentCueLevel;
    var color = this.currentCueColor;
    var j = index, groupSize, nextGroupSize;

    // figure out current group size
    while ({ groupSize.isNil }) {
      j = j + 1;
      if (j >= cueFuncs.size) {
        groupSize = j - index;
      } {
        if ((cueFuncs[j][\level] ?? 0) <= level) {
          groupSize = j - index;
        };
      };
    };

    if (j >= cueFuncs.size) {
      // can't move the group cuz we're at the end
    } {
      var nextIndex = j;

      // figure out next group size
      while ({ nextGroupSize.isNil }) {
        j = j + 1;
        if (j >= cueFuncs.size) {
          nextGroupSize = j - nextIndex;
        } {
          if ((cueFuncs[j][\level] ?? 0) <= level) {
            nextGroupSize = j - nextIndex;
          };
        };
      };
      newIndex = min(cueFuncs.size - 1, index + groupSize + nextGroupSize - 1);

      groupSize.do {
        var func = this.currentCueFunc;
        var name = this.currentCueName;
        var level = this.currentCueLevel;
        var color = this.currentCueColor;
        cueFuncs.removeAt(currentCueIndex);
        cueFuncs = cueFuncs.insert(newIndex, (name: name.asSymbol, func: func, color: color, level: level));
      };

      this.currentCueIndex_(newIndex - groupSize + 1);

      unsavedListChanges = true;
      this.changed(\cueFuncs);
      this.changed(\unsavedChanges);
      this.changed(\currentCueIndex);
    };


  }

  addCueAfterCurrent { |name|
    this.addEmptyCue(currentCueIndex + 1, name, this.currentCueLevel);
  }

  addCueBeforeCurrent { |name|
    this.addEmptyCue(currentCueIndex, name, this.currentCueLevel);
  }

  addEmptyCue { |index, name, level|
    var funcCue = cueFuncs.detect({ |cue|
      cue.name == 'default cue'
    });
    var func = if (funcCue.isNil) { defaultFunc } { funcCue[\func] };
    this.addCue(index, name, func, level: level);
  }

  addCue { |index, name, func, color, level|
    cueFuncs = cueFuncs.insert(index, (name: name.asSymbol, func: func, color: color, level: level));
    unsavedListChanges = true;
    this.changed(\cueFuncs);
    this.changed(\unsavedChanges);
    this.changed(\currentCueIndex);
  }

  deleteCurrentCue {
    var level = this.currentCueLevel;
    var index = currentCueIndex;
    var thisLevel;

    index = index + 1;
    thisLevel = cueFuncs[index][\level] ?? 0;
    while { thisLevel > level } {
      cueFuncs[index][\level] = thisLevel - 1;
      index = index + 1;
      thisLevel = cueFuncs[index][\level] ?? 0;
    };

    this.deleteCue(currentCueIndex, {
      currentCueIndex = max(currentCueIndex - 1, 0);
    });
  }

  deleteCue { |index, action|
    cueFuncs.removeAt(index);
    unsavedListChanges = true;
    action.value;
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
