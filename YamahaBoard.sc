CL1 {
  var <uid, <outDevice, deviceName, portName;

  var <faderPositions, <faderIsOn, <sendPositions, <sendIsOn;
  var <dcaPositions, <dcaIsOn;
  var fadeRoutines, fadeSendRoutines;
  var midifuncCc, midifuncSysex;

  // sysex stuff
  var midichan = 8;
  var groupid = 0x3E; // Digital mixer
  var modelid = 0x19; // CL series

  // This is what the CL1 interprets each multiple of 10 as
  classvar ccDbMap = #[-138.0, -68.4, -52.4, -38.2, -30.2, -22.2, -17.1, -13.1, -9.1, -5.1, -1.1, 2.9, 6.9];
  classvar sysexDbMap = #[-138.0, -59.0, -36.7, -23.9, -15.55, -9.15, -2.75, 3.65];
  classvar sendTypeSysex;

  *initClass {
    sendTypeSysex = (mix: 0x49, matrix: 0x4b);
  }

  *dbToCc { |db|
    var ret;
    var nextMappedDb, slope, amtOver;

    ccDbMap.do { |mappedDb, i|
      nextMappedDb = ccDbMap[i+1];
      if (nextMappedDb.isNil) {
        nextMappedDb = 10.0;
        slope = (nextMappedDb - mappedDb) / 0.8;
      } {
        slope = nextMappedDb - mappedDb;
      };
      if ((mappedDb < db) && (db <= nextMappedDb)) {
        amtOver = db - mappedDb;
        ret = (i + (amtOver / slope) * 10);
      };
    };
    if (db == -inf) {
      ret = 0;
    };
    ^ret;
  }

  *ccToDb { |num|
    var ret;
    var nextMappedDb, nextI, slope, amtOver;

    ccDbMap.do { |mappedDb, i|
      nextMappedDb = ccDbMap[i+1];
      nextI = if (i < 12) { i + 1 } { 12.7 };
      if (nextMappedDb.isNil) {
        nextMappedDb = 10.0;
        slope = (nextMappedDb - mappedDb) / 0.7;
      } {
        slope = nextMappedDb - mappedDb;
      };
      if (((i * 10) < num) && (num <= (nextI * 10))) {
        amtOver = (num - (i * 10)) / 10;
        ret = mappedDb + (slope * amtOver);
      };
    };
    if (num == 0) {
      ret = -inf;
    };
    ^ret;
  }


  *dbToSysex { |db|
    var ret;
    var nextMappedDb, slope, amtOver;

    sysexDbMap.do { |mappedDb, i|
      var nextMappedDb = sysexDbMap[i+1];
      var slope;
      if (nextMappedDb.isNil) {
        nextMappedDb = 10.0;
        slope = nextMappedDb - mappedDb;
      } {
        slope = nextMappedDb - mappedDb;
      };
      if ((mappedDb < db) && (db <= nextMappedDb)) {
        var amtOver = db - mappedDb;
        ret = [i, ((amtOver / slope) * 127)];
      };
    };
    if (db == -inf) {
      ret = [0, 0];
    };
    ^ret;
  }

  *sysexToDb { |digitA, digitB|
    var ret;
    var i = digitA;
    var mappedDb = sysexDbMap[i];
    var nextMappedDb = sysexDbMap[i+1];
    var slope;

    if (nextMappedDb.isNil) {
      nextMappedDb = 10.0;
      slope = nextMappedDb - mappedDb;
    } {
      slope = nextMappedDb - mappedDb;
    };
    ret = mappedDb + (slope * digitB / 127);
    if ((digitA == 0) && (digitB == 0)) {
      ret = -inf;
    };
    ^ret;
  }

  *freqToSysex { |freq|
    ^freq.explin(20, 20000, 0x04, 0x7c);
  }

  *sysexToFreq { |sysex|
    ^sysex.linexp(0x04, 0x7c, 20, 20000);
  }

  /////// --- instance stuff ---- ////////

  *new { |deviceName, portName|
    ^super.new.init(deviceName, portName);
  }

  init { |argdevice, argport|
    deviceName = argdevice;
    portName = argport;

    if (MIDIClient.initialized.not) {
      MIDIClient.init;
    } {
      MIDIClient.list;
    };

    MIDIClient.externalSources.do { |src, i|
      if ((src.device == deviceName) && (src.name == portName)) {
        MIDIIn.connect(i, src);
        uid = src.uid;
      };
    };

    outDevice = MIDIOut.newByName(deviceName, portName);

    faderPositions = -inf!48;
    faderIsOn = true!48;

    sendPositions = (-inf!32)!48;
    sendIsOn = (true!32)!48;

    dcaPositions = -inf!16;
    dcaIsOn = true!16;

    fadeRoutines = nil ! 48;
    fadeSendRoutines = (nil!32)!48;

    this.makeMidifuncs;
    this.initBoardRead;
    CmdPeriod.add(this);
  }

  cmdPeriod {
    this.makeMidifuncs;
  }

  makeMidifuncs {
    midifuncCc = MIDIFunc.cc({ |val, num|
      var boardChan;
      if (num < 25) {
        boardChan = num - 1;
        faderPositions[boardChan] = this.class.ccToDb(val);
        fadeRoutines[boardChan].stop;
        this.changed("fader", boardChan);
      } {
        if (num < 32) { // DCA 1-7 ==> 25 - 31
          boardChan = num - 25;
          dcaPositions[boardChan] = this.class.ccToDb(val);
          this.changed("DCA", boardChan);
        };
        if (num == 88) { // DCA 8 ==> 88
          boardChan = 7;
          dcaPositions[boardChan] = this.class.ccToDb(val);
          this.changed("DCA", boardChan);
        };
      };

      if ((0x40 <= num) && (num <= 0x57)) {
        boardChan = num - 0x40;
        faderIsOn[boardChan] = (val == 0).not;
        this.changed("fader", boardChan);
      };
    }, chan: midichan, srcID: uid);

    midifuncSysex = MIDIFunc.sysex({ |data, src|
      if (src == uid) {
        data.collect(_.asHexString).collect(_[6..7]).postcs;

        if (data[0..10] == Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x37, 0x00, 0x00, 0x00]) { // fader position
          var boardChan = data[11];
          var digitA = data[15];
          var digitB = data[16];

          [\fader, \boardChan, boardChan, \digitA, digitA, \digitB, digitB].postln;

          faderPositions[boardChan] = this.class.sysexToDb(digitA, digitB);
          fadeRoutines[boardChan].stop;
          this.changed("fader", boardChan);
        };
        if (data[0..10] == Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x35, 0x00, 0x00, 0x00]) { // fader on/off
          var boardChan = data[11];
          var thisfaderIsOn = data[16] == 1;
          faderIsOn[boardChan] = thisfaderIsOn;
          this.changed("fader", boardChan);
        };

        if (data[0..10] == Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00]) { // DCA position
          var boardChan = data[11];
          var digitA = data[15];
          var digitB = data[16];

          [\DCA, \boardChan, boardChan, \digitA, digitA, \digitB, digitB].postln;

          dcaPositions[boardChan] = this.class.sysexToDb(digitA, digitB);
          this.changed("DCA", boardChan);
        };
        if (data[0..10] == Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x7E, 0x00, 0x00, 0x00]) { // DCA on/off
          var boardChan = data[11];
          var thisfaderIsOn = data[16] == 0; // 0 is on, 1 is off, for some reason
          dcaIsOn[boardChan] = thisfaderIsOn;
          this.changed("DCA", boardChan);
        };

        if (
          (data[0..8] == Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x49, 0x00])
        ) { // mix send

          var boardChan = data[11];
          var digitA = data[15];
          var digitB = data[16];

          var sendNum = ((data[9] - 3) / 3).floor;
          var parameter = ((data[9] - 3) % 3);

          [\mixSend, \boardChan, boardChan, \digitA, digitA, \digitB, digitB].postln;

          if (parameter == 0) { // on/off
            sendIsOn[boardChan][sendNum] = digitB == 1;
            this.changed("send", boardChan, \mix, sendNum);
          };
          if (parameter == 2) { // position
            sendPositions[boardChan][sendNum] = this.class.sysexToDb(digitA, digitB);
            this.changed("send", boardChan, \mix, sendNum);
          };

        };
        if (
          (data[0..8] == Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x4B, 0x00])
        ) { // matrix send

          var boardChan = data[11];
          var digitA = data[15];
          var digitB = data[16];

          var sendNum = ((data[9] - 3) / 3).floor;
          var parameter = ((data[9] - 3) % 3);

          [\matrixSend, \boardChan, boardChan, \digitA, digitA, \digitB, digitB].postln;

          if (parameter == 0) { // on/off
            sendIsOn[boardChan][sendNum + 24] = digitB == 1;
            this.changed("send", boardChan, \matrix, sendNum);
          };
          if (parameter == 2) { // position
            sendPositions[boardChan][sendNum + 24] = this.class.sysexToDb(digitA, digitB);
            this.changed("send", boardChan, \matrix, sendNum);
          };
        };

      };
    });

  }

  initBoardRead {
    var wait = 0.006;
    var chanwait = 0.006;
    var statusbar, statusbarwidth, statusbarheight, statustext;
    var win = Window(resizable:false, border:false).front;
    win.bounds_(win.bounds.insetBy(0, 100));
    statusbarheight = 30;
    statusbarwidth = win.bounds.width - 20;
    StaticText(win, Rect(0, win.bounds.height / 2 - (statusbarheight / 2) - 70, win.bounds.width, 70))
    .align_(\center)
    .font_(Font().size_(20))
    .string_("Initializing CL1");
    statusbar = View(win, Rect(10, win.bounds.height / 2 - (statusbarheight / 2), 10, statusbarheight)).background_(Color.blue);
    statustext = StaticText(win, Rect(10, win.bounds.height / 2 + (statusbarheight / 2), win.bounds.width - 20, 40))
    .align_(\left)
    .string_("Reading channel 0 of 48....");
    fork {
      var totalchans = 48 + 16; // input channels + DCAs

      48.do { |i|
        defer {
          statusbar.bounds_(statusbar.bounds.width_((i + 1.0 / totalchans) * statusbarwidth));
          statustext.string_(("Reading input channel " ++ (i + 1) ++ " of 48...."));
        };
        outDevice.sysex(Int8Array[0xF0, 0x43, 0x38, 0x3E, 0x19, 0x01, 0x00, 0x37, 0x00, 0x00, 0x00, i, 0xF7]);
        wait.wait;
        outDevice.sysex(Int8Array[0xF0, 0x43, 0x38, 0x3E, 0x19, 0x01, 0x00, 0x35, 0x00, 0x00, 0x00, i, 0xF7]);
        24.do { |j|
          wait.wait;
          outDevice.sysex(Int8Array[0xF0, 0x43, 0x38, 0x3E, 0x19, 0x01, 0x00, 0x49, 0x00, j * 3 + 5, 0x00, i, 0xF7]);
          wait.wait;
          outDevice.sysex(Int8Array[0xF0, 0x43, 0x38, 0x3E, 0x19, 0x01, 0x00, 0x49, 0x00, j * 3 + 3, 0x00, i, 0xF7]);
        };
        8.do { |j|
          wait.wait;
          outDevice.sysex(Int8Array[0xF0, 0x43, 0x38, 0x3E, 0x19, 0x01, 0x00, 0x4B, 0x00, j * 3 + 5, 0x00, i, 0xF7]);
          wait.wait;
          outDevice.sysex(Int8Array[0xF0, 0x43, 0x38, 0x3E, 0x19, 0x01, 0x00, 0x4B, 0x00, j * 3 + 3, 0x00, i, 0xF7]);
        };
        chanwait.wait;
      };

      16.do { |i|
        defer {
          statusbar.bounds_(statusbar.bounds.width_((i + 49.0 / totalchans) * statusbarwidth));
          statustext.string_(("Reading DCA " ++ (i + 1) ++ " of 16...."));
        };
        outDevice.sysex(Int8Array[0xF0, 0x43, 0x38, 0x3E, 0x19, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, i, 0xF7]);
        wait.wait;
        outDevice.sysex(Int8Array[0xF0, 0x43, 0x38, 0x3E, 0x19, 0x01, 0x00, 0x7E, 0x00, 0x00, 0x00, i, 0xF7]);
        wait.wait;
      };

      defer { win.close };
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

      boardChans.asArray.flat.do { |boardChan|
        fadeRoutines[boardChan].stop;
      };

      this.setFadersHelper(boardChans, tempDbs, tempOns);
    }
  }

  setFadersHelper { |boardChans, dbs, ons| // 0-index board channels
    var sysexLevel, db, on;
    dbs = dbs.asArray;
    ons = ons.asArray;

    boardChans.asArray.do { |boardChan, i|
      db = dbs.wrapAt(i);
      on = ons.wrapAt(i);
      if (db.notNil) {
        sysexLevel = this.class.dbToSysex(db);
        outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x37, 0x00, 0x00, 0x00, boardChan, 0x00, 0x00, 0x00, sysexLevel[0], sysexLevel[1], 0xF7]);
        faderPositions[boardChan] = db;
        this.changed("fader", boardChan);
      };
      if (on.notNil) {
        outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x35, 0x00, 0x00, 0x00, boardChan, 0x00, 0x00, 0x00, 0, on.asInt, 0xF7]);
        faderIsOn[boardChan] = on;
        this.changed("fader", boardChan);
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
    var sysexLevel, db, on;
    dbs = dbs.asArray;
    ons = ons.asArray;

    boardChans.asArray.do { |boardChan, i|
      db = dbs.wrapAt(i);
      on = ons.wrapAt(i);
      if (db.notNil) {
        sysexLevel = this.class.dbToSysex(db);
        outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, boardChan, 0x00, 0x00, 0x00, sysexLevel[0], sysexLevel[1], 0xF7]);
        dcaPositions[boardChan] = db;
        this.changed("DCA", boardChan);
      };
      if (on.notNil) {
        outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x7E, 0x00, 0x00, 0x00, boardChan, 0x00, 0x00, 0x00, 0, on.not.asInt, 0xF7]);
        dcaIsOn[boardChan] = on;
        this.changed("DCA", boardChan);
      };
    };
  }

  setSends { |boardChans ...args|
    var currentType = \mix;

    args.pairsDo { |sendspec, settings|
      var tempNums, tempTypes, tempDbs, tempOns, tempPres;

      sendspec.flat.do { |thing|
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
    var sysexLevel, sendType, sendNumRealDb, sendNumRealOn, sendNumRealPre, sendTypeReal, sendPositionNum, db, on, pre;
    sendNums = sendNums.asArray;
    sendTypes = sendTypes.asArray;
    dbs = dbs.asArray;
    ons = ons.asArray;
    pres = pres.asArray;

    boardChans.asArray.do { |boardChan|
      sendNums.do { |sendNum, i|
        sendType = sendTypes.wrapAt(i);
        db = dbs.wrapAt(i);
        on = ons.wrapAt(i);
        pre = pres.wrapAt(i);
        sendNumRealDb = (sendNum * 3) + 5;
        sendNumRealOn = (sendNum * 3) + 3;
        sendNumRealPre = (sendNum * 3) + 4;
        sendTypeReal = sendTypeSysex[sendType];
        sendPositionNum = if (sendType == \mix) { sendNum } { sendNum + 24 };
        if (db.notNil) {
          sysexLevel = this.class.dbToSysex(db);
          outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, sendTypeReal, 0x00,  sendNumRealDb, 0x00, boardChan, 0x00, 0x00, 0x00, sysexLevel[0], sysexLevel[1], 0xF7]);
          sendPositions[boardChan][sendPositionNum] = db;
          this.changed("send", boardChan, sendType, sendNum);
        };
        if (on.notNil) {
          outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, sendTypeReal, 0x00,  sendNumRealOn, 0x00, boardChan, 0x00, 0x00, 0x00, 0x00, on.asInt, 0xF7]);
          sendIsOn[boardChan][sendPositionNum] = on;
          this.changed("send", boardChan, sendType, sendNum);
        };
        if (pre.notNil) {
          outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, sendTypeReal, 0x00,  sendNumRealPre, 0x00, boardChan, 0x00, 0x00, 0x00, 0x00, pre.asInt, 0xF7]);
          this.changed("send", boardChan, sendType, sendNum);
        };
      };
    };
  }

  fadeFader { |...args| this.fadeFaders(*args) }

  fadeFaders { |...args|
    var durSecs = 1;

    args.pairsDo { |boardChans, settings|
      var tempNums, tempTypes, tempDbs, tempCurves, currentThing;

      if (boardChans == \dur) {
        durSecs = settings;
      } {
        settings.asArray.flat.do { |thing|
          switch (thing)
          {\curves} { currentThing = \curves }
          {
            if (currentThing == \curves) {
              tempCurves = tempCurves.add(thing)
            } {
              tempDbs = tempDbs.add(thing)
            }
          }
        };

        tempCurves = tempCurves ?? 5;
        this.fadeFadersHelper(boardChans, tempDbs, durSecs, tempCurves);
      };
    };
  }

  fadeFadersHelper { |boardChans, toDbs, durSecs=1, curves=5|
    var fromDbs;
    fromDbs = boardChans.asArray.collect(faderPositions[_]);
    toDbs = toDbs.asArray;
    durSecs = durSecs.asArray;
    curves = curves.asArray;
    boardChans.asArray.do { |boardChan, i|
      var fromDb, toDb, durSec, curve, progress;
      fromDb = fromDbs[i];
      toDb = toDbs.wrapAt(i);
      durSec = durSecs.wrapAt(i);
      curve = curves.wrapAt(i);
      if (fromDb == -inf) { fromDb = -120 };
      if (toDb == -inf) { toDb = -120 };
      if (fromDb < toDb) { curve = -1 * curve };
      fadeRoutines[boardChan].stop;
      fadeRoutines[boardChan] = fork {
        (durSec * 10).floor.do { |i|
          progress = (i + 1.0) / (durSec * 10.0);
          0.1.wait;
          this.setFadersHelper(boardChan, progress.lincurve(0, 1, fromDb, toDb, curve));
        };
      };
    };
  }

  fadeSends { |boardChans ...args|
    var currentType = \mix;
    var durSecs = 1;

    args.pairsDo { |sendspec, settings|
      var tempNums, tempTypes, tempDbs, tempCurves, currentThing;

      if (sendspec == \dur) {
        durSecs = settings;
      } {
        sendspec.flat.do { |thing|
          if (thing.class == Symbol) {
            currentType = thing;
          } {
            tempNums = tempNums.add(thing);
            tempTypes = tempTypes.add(currentType);
          };
        };

        settings.asArray.flat.do { |thing|
          switch (thing)
          {\curves} { currentThing = \curves }
          {
            if (currentThing == \curves) {
              tempCurves = tempCurves.add(thing)
            } {
              tempDbs = tempDbs.add(thing)
            }
          }
        };

        tempCurves = tempCurves ?? 5;
        this.fadeSendsHelper(boardChans, tempNums, tempTypes, tempDbs, durSecs, tempCurves);
      };
    };
  }

  fadeSendsHelper { |boardChans, sendNums, sendTypes, toDbs, durSecs=1, curves=5|
    sendNums = sendNums.asArray;
    sendTypes = sendTypes.asArray;
    toDbs = toDbs.asArray;
    durSecs = durSecs.asArray;
    curves = curves.asArray;
    boardChans.asArray.do { |boardChan|
      sendNums.do { |sendNum, i|
        var sendType, sendPositionNum, fromDb, toDb, durSec, curve, thisCurve;
        sendType = sendTypes.wrapAt(i);
        sendPositionNum = if (sendType == \mix) { sendNum } { sendNum + 24 };
        fromDb = sendPositions[boardChan][sendPositionNum];
        toDb = toDbs.wrapAt(i);
        durSec = durSecs.wrapAt(i);
        curve = curves.wrapAt(i);
        thisCurve = if (fromDb < toDb) { -1 * curve } { curve };
        if (fromDb == -inf) { fromDb = -120 };
        if (toDb == -inf) { toDb = -120 };
        fadeSendRoutines[boardChan][sendPositionNum].stop;
        fadeSendRoutines[boardChan][sendPositionNum] = fork {
          (durSec * 10).floor.do { |i|
            var progress = (i + 1.0) / (durSec * 10.0);
            0.1.wait;
            this.setSendsHelper(boardChan, sendNum, sendType, progress.lincurve(0, 1, fromDb, toDb, thisCurve));
          };
        };
      };
    };
  }

  stopAllFades {
    fadeSendRoutines.do { |chanRoutines|
      chanRoutines.do(_.stop);
    };
    fadeRoutines.do(_.stop);
  }

  loadEQ { |boardChan, eqNum|
    if (eqNum.notNil) {
      outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x00, 0x4C, 0x69, 0x62, 0x52, 0x63, 0x6C, 0x5F, 0x5F, 0x49, 0x4E, 0x45, 0x51, 0x5F, 0x5F, 0x5F, 0x5F, 0x00, eqNum, 0x00, boardChan, 0xF7]);
      //~eqs[mic] = eqselect;
    };
  }

  saveEQ { |boardChan, eqNum|
    if (eqNum.notNil) {
      outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x00, 0x4C, 0x69, 0x62, 0x53, 0x74, 0x72, 0x5F, 0x5F, 0x49, 0x4E, 0x45, 0x51, 0x5F, 0x5F, 0x5F, 0x5F, 0x00, eqNum, 0x00, boardChan, 0xF7]);
    };
  }

  // not totally done yet.....
  setEQ { |boardChan, eqSpec, on|
    var sysexLevel;
    var elementhigh, elementlow, indexhigh, indexlow;
    /* eqSpec is a Dictionary that contains the following sub-dicts: */
    var high, highmid, lowmid, low, hpf, att;

    eqSpec = eqSpec ?? ();
    high = eqSpec[\high] ?? ();
    highmid = eqSpec[\highmid] ?? ();
    lowmid = eqSpec[\lowmid] ?? ();
    low = eqSpec[\low] ?? ();
    hpf = eqSpec[\hpf] ?? ();
    att = eqSpec[\att] ?? ();


    [low, lowmid, highmid, high].do { |spec, i|
      // FOR BYPASSES
      elementhigh = 0x06;
      elementlow = 0x5E;

      if (spec.bypass.notNil) {
        indexlow = i + 3;
        this.paramChange(1, elementhigh, elementlow,
          0, indexlow, boardChan,
          [0, 0, 0, 0, spec.bypass.asInteger]);
      };

      // FOR FREQ/GAIN/Q
      elementhigh = 0x00;
      elementlow = 0x43;

      if (spec.freq.notNil) {
        indexlow = 4 * i + 9;
        sysexLevel = this.class.freqToSysex(spec.freq);
        this.paramChange(1, elementhigh, elementlow,
          0, indexlow, boardChan,
          [0, 0, 0, 0, sysexLevel]);
      };

      if (spec.gain.notNil) {
        var gain = spec.gain.round(0.5) * 10;
        var data;
        indexlow = 4 * i + 10;
        if (gain >= 0) {
          sysexLevel = [(gain/128).floor, gain%128];
          data = [0, 0, 0, sysexLevel[0], sysexLevel[1]];
        } {
          sysexLevel = [0x7F + (gain/128).ceil, (gain%128)];
          data = [0x0F, 0x7F, 0x7F, sysexLevel[0], sysexLevel[1]];
        };
        this.paramChange(1, elementhigh, elementlow,
          0, indexlow, boardChan, data);
      };
    };

    if (high.type.notNil) {
      sysexLevel = switch (high.type.asSymbol)
      {\peak} { 0 }
      {\shelf} { 1 }
      {\lpf} { 2 };
      outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x43, 0x00, 0x04, 0x00, boardChan, 0x00, 0x00, 0x00, 0x00, sysexLevel, 0xF7]);
    };

    if (high.lpfon.notNil) {
      outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x43, 0x00, 0x06, 0x00, boardChan, 0x00, 0x00, 0x00, 0x00, high.lpfon.asInteger, 0xF7]);
    };

    // ----------- device on --------

    if (on.notNil) {
      outDevice.sysex(Int8Array[0xF0, 0x43, 0x18, 0x3E, 0x19, 0x01, 0x00, 0x43, 0x00, 0x02, 0x00, boardChan, 0x00, 0x00, 0x00, 0x00, on.asInteger, 0xF7]);
    };
  }

  paramChange { |category = 1, elementhigh, elementlow, indexhigh = 0, indexlow = 0, boardChan, data|
    outDevice.sysex(Int8Array[
      0xF0, // status
      0x43, // manufacturer ID
      0x10 + midichan, // midi channel = device num.
      groupid,
      modelid,
      category,
      elementhigh, elementlow,
      indexhigh, indexlow,
      0x00, boardChan]
    ++ Int8Array.newFrom(data)
    ++ Int8Array[0xF7]);
  }
}