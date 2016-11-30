CueList {
  var <filepath, <sceneFuncs;
  var <>defaultFunc;
  var <currentSceneIndex = 0;
  var <lastExecutedScene = '';
  var <unsavedSceneChanges = false;
  var <unsavedListChanges = false;
  var <defaultfilepath;

  var <>preExecuteHook, <>postExecuteHook;

  *new { |filepath, defaultFunc|
    ^super.new.init(filepath, defaultFunc);
  }

  init { |argfilepath, argdeffunc|
    defaultfilepath = "defaultCue".resolveRelative;
    filepath = argfilepath ?? defaultfilepath;//thisProcess.nowExecutingPath.dirname;

    defaultFunc = argdeffunc ? {/*

Cue on:
Page #:

*/

};

    this.refreshSceneFuncs;
  }

  refreshSceneFuncs {
    sceneFuncs = File(filepath +/+ "cue-data.scd", "r").readAllString.interpret;
    unsavedSceneChanges = false;
    unsavedListChanges = false;
    this.changed(\sceneFuncs);
    this.changed(\unsavedChanges);
  }

  executeCurrentScene {
    var func = this.currentSceneFunc();
    lastExecutedScene = this.currentSceneName();
    this.incrementSceneIndex();

    preExecuteHook.value;
    func.value;
    postExecuteHook.value;
  }

  incrementSceneIndex {
    this.currentSceneIndex_(currentSceneIndex + 1);
  }

  decrementSceneIndex {
    this.currentSceneIndex_(currentSceneIndex - 1);
  }

  currentSceneIndex_ { |newindex|
    currentSceneIndex = newindex % sceneFuncs.size;
    unsavedSceneChanges = false;
    this.changed(\currentSceneIndex);
  }

  currentSceneName {
    ^sceneFuncs[currentSceneIndex][\name];
  }

  currentSceneFunc {
    ^sceneFuncs[currentSceneIndex][\func];
  }

  nextSceneName {
    ^sceneFuncs[currentSceneIndex + 1 % sceneFuncs.size][\name];
  }

  prevSceneName {
    ^sceneFuncs[currentSceneIndex - 1 % sceneFuncs.size][\name];
  }

  gotoSceneName { |name|
    sceneFuncs.do { |scene, i|
      if (scene[\name] == name) {
        this.currentSceneIndex_(i);
      };
    };
  }

  sceneNames {
    ^sceneFuncs.collect(_[\name]);
  }

  saveSceneFuncs {
    var backupfilepath = filepath +/+ "backups" +/+ "cue-data" ++
    Date.localtime.format("__%Y-%m-%d__%H.%M.%S__") ++ ".scd";

    if (filepath == defaultfilepath) {
      "Can't overwrite default cuelist".postln;
      ^false;
    };

    ("mv \"" ++ filepath +/+ "cue-data.scd\" \"" ++ backupfilepath ++ "\"").unixCmd( {
      var f = File(filepath  +/+ "cue-data.scd", "w");
      f.write(sceneFuncs.asCompileString);
      f.close;
    }, false);
    unsavedListChanges = false;
    unsavedSceneChanges = false;
    this.changed(\unsavedChanges);
    ^true;
  }

  unsavedSceneChanges_ { |unsaved|
    unsavedSceneChanges = unsaved;
    this.changed(\unsavedChanges);
  }

  currentSceneFunc_ { |func|
    sceneFuncs[currentSceneIndex][\func] = func;
    unsavedSceneChanges = false;
    unsavedListChanges = true;
    this.changed(\unsavedChanges);
  }

  currentSceneName_ { |name|
    if (name != sceneFuncs[currentSceneIndex][\name]) { unsavedListChanges = true };
    sceneFuncs[currentSceneIndex][\name] = name;
    this.changed(\sceneFuncs);
    this.changed(\unsavedChanges);
  }

  addSceneAfterCurrent { |name|
    this.addEmptyScene(currentSceneIndex + 1, name);
  }

  addSceneBeforeCurrent { |name|
    this.addEmptyScene(currentSceneIndex, name);
  }

  addEmptyScene { |index, name|
    this.addScene(index, name, defaultFunc);
  }

  addScene { |index, name, func|
    sceneFuncs = sceneFuncs.insert(index, (name: name, func: func));
    unsavedListChanges = true;
    this.changed(\sceneFuncs);
    this.changed(\unsavedChanges);
    this.changed(\currentSceneIndex);
  }

  deleteCurrentScene {
    this.deleteScene(currentSceneIndex);
  }

  deleteScene { |index|
    sceneFuncs.removeAt(index);
    unsavedListChanges = true;
    this.changed(\sceneFuncs);
    this.changed(\unsavedChanges);
    this.changed(\currentSceneIndex);
  }

  makeWindow { |bounds|
    bounds = bounds ?? Window.screenBounds.width_(800);
    ^ShowCtrlWindow(filepath, bounds).cueList_(this);
  }

  filepath_ { |newfilepath|
    filepath = newfilepath;
    this.changed(\filepath);
  }
}