X32 {
  var <addr, <queue, <queuerout;

  *new { |ip, port|
    ^super.new.init(ip, port);
  }

  init { |ip, port|
    addr = NetAddr(ip, port);
    queue = [];
  }

  *dbToLevel { |db|
    if (db < -60) { ^db.linlin(-90, -60, 0, 0.0625) };
    if (db < -30) { ^db.linlin(-60, -30, 0.0625, 0.25) };
    if (db < -10) { ^db.linlin(-30, -10, 0.25, 0.5) };
    ^db.linlin(-10, 10, 0.5, 1.0);
  }

  addToQueue { |...args|
    addr.sendMsg(*args);
    queue = queue.add(args);
    queuerout.stop;
    queuerout = fork {
      0.1.wait;
      queue.postln;
      queue.do { |args| addr.sendMsg(*args) };
      queue = [];
    };
  }

  setFader { |...args| this.setFaders(*args) }

  setFaders { |...args|
    args.pairsDo { |boardChans, settings|
      var tempDbs, tempOns;

      settings.asArray.flat.do { |thing|
        switch (thing)
        {\on} { tempOns = tempOns.add(true) }
        {\off} { tempOns = tempOns.add(false) }
        { tempDbs = tempDbs.add(thing) }
      };

      /*
      boardChans.asArray.flat.do { |boardChan|
        fadeRoutines[boardChan].stop;
      };
      */

      this.setFadersHelper(boardChans, tempDbs, tempOns);
    }
  }

  setFadersHelper { |boardChans, dbs, ons| // 0-index board channels
    var level, db, on;
    dbs = dbs.asArray;
    ons = ons.asArray;

    boardChans.asArray.do { |boardChan, i|
      boardChan = boardChan + 1;
      db = dbs.wrapAt(i);
      on = ons.wrapAt(i);
      if (db.notNil) {
        level = this.class.dbToLevel(db);
        this.addToQueue("/ch/" ++ boardChan.asInteger.asPaddedString ++ "/mix/fader", level);
      };
      if (on.notNil) {
        this.addToQueue("/ch/" ++ boardChan.asInteger.asPaddedString ++ "/mix/on", on.asInteger);
      };
    };
  }

  setDCA { |...args| this.setDCAs(*args) }

  setDCAs { |...args|
    args.pairsDo { |boardChans, settings|
      var tempDbs, tempOns;

      settings.asArray.flat.do { |thing|
        switch (thing)
        {\on} { tempOns = tempOns.add(true) }
        {\off} { tempOns = tempOns.add(false) }
        { tempDbs = tempDbs.add(thing) }
      };

      /*
      boardChans.asArray.flat.do { |boardChan|
        fadeRoutines[boardChan].stop;
      };
      */

      this.setDCAsHelper(boardChans, tempDbs, tempOns);
    }
  }

  setDCAsHelper { |boardChans, dbs, ons| // 0-index board channels
    var level, db, on;
    dbs = dbs.asArray;
    ons = ons.asArray;

    boardChans.asArray.do { |boardChan, i|
      boardChan = boardChan + 1;
      db = dbs.wrapAt(i);
      on = ons.wrapAt(i);
      if (db.notNil) {
        level = this.class.dbToLevel(db);
        this.addToQueue("/dca/" ++ boardChan.asInteger ++ "/fader", level);
      };
      if (on.notNil) {
        this.addToQueue("/dca/" ++ boardChan.asInteger ++ "/on", on.asInteger);
      };
    };
  }

  setOutput { |...args| this.setOutputs(*args) }

  setOutputs { |...args|

  }

  setSend { |...args| this.setSends(*args); }

  setSends { |boardChans ...args|
    var currentType = \mix;

    args.pairsDo { |sendspec, settings|
      var tempNums, tempTypes, tempDbs, tempOns, tempPres;

      sendspec.asArray.flat.do { |thing|
        if (thing.class == Symbol) {
          currentType = thing;
        } {
          tempNums = tempNums.add(thing);
          tempTypes = tempTypes.add(currentType);
        };
      };

      settings.asArray.flat.do { |thing|
        switch (thing)
        {\on} { tempOns = tempOns.add(true) }
        {\off} { tempOns = tempOns.add(false) }
        {\pre} { tempPres = tempPres.add(true) }
        {\post} { tempPres = tempPres.add(false) }
        { tempDbs = tempDbs.add(thing) }
      };

      this.setSendsHelper(boardChans, tempNums, tempTypes, tempDbs, tempOns, tempPres);
    };
  }

  setSendsHelper { |boardChans, sendNums, sendTypes, dbs, ons, pres|
    var level, sendType, db, on, pre;
    sendNums = sendNums.asArray;
    sendTypes = sendTypes.asArray;
    dbs = dbs.asArray;
    ons = ons.asArray;
    pres = pres.asArray;

    boardChans.asArray.do { |boardChan|
      boardChan = boardChan + 1;

      sendNums.do { |sendNum, i|
        sendNum = sendNum + 1;
        sendType = sendTypes.wrapAt(i);
        db = dbs.wrapAt(i);
        on = ons.wrapAt(i);
        pre = pres.wrapAt(i);
        if (db.notNil) {
          // don't deal with send type yet
          level = this.class.dbToLevel(db);
          this.addToQueue("/ch/" ++ boardChan.asInteger.asPaddedString ++ "/mix/" ++ sendNum.asInteger.asPaddedString ++ "/level", level);
        };
        if (on.notNil) {
          this.addToQueue("/ch/" ++ boardChan.asInteger.asPaddedString ++ "/mix/" ++ sendNum.asInteger.asPaddedString ++ "/on", on.asInteger);
        };
        if (pre.notNil) {
          // whatever
        };
      };
    };
  }

  fadeFader { |...args| this.fadeFaders(*args) }

  fadeFaders { |...args|

  }

  fadeSend { |...args| this.fadeSends(*args); }

  fadeSends { |boardChans ...args|

  }

  loadEQ { |boardChan, eqNum|

  }

  setEQ { |boardChan, eqSpec, on|

  }
}