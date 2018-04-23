BootWindow {
  classvar <win, <>server;

  *show { |onBootAction|
    var in_dev, out_dev, sr, in_chans, out_chans, ar_buses, kr_buses, memsize, blocksize, rec_header, rec_format, rec_chans;
    var title_font = Font("Input Sans", 30);
    var body_font = Font("Input Sans", 12);

    var bootaction = {
      var f = File(thispath +/+ "settings.txt", "w");

      win.close;

      settings = (
        in_dev: in_dev.item,
        out_dev: out_dev.item,
        sr: sr.string.asInteger,
        in_chans: in_chans.string.asInteger,
        out_chans: out_chans.string.asInteger,
        ar_buses: ar_buses.string.asInteger,
        kr_buses: kr_buses.string.asInteger,
        memsize: memsize.string.asInteger,
        blocksize: blocksize.string.asInteger,
        rec_header: rec_header.item,
        rec_format: rec_formats[rec_formats.collect(_.asSymbol).indexOf(rec_format.item.asSymbol) - 1].asString,
        rec_chans: rec_chans.string.asInteger
      );

      f.write(settings.asCompileString);
      f.close;

      server.options.memSize_(1024 * memsize.string.asInteger)
      .numInputBusChannels_(in_chans.string.asInteger)
      .numOutputBusChannels_(out_chans.string.asInteger)
      .inDevice_(in_dev.item)
      .outDevice_(out_dev.item)
      .numAudioBusChannels_(ar_buses.string.asInteger)
      .numControlBusChannels_(kr_buses.string.asInteger)
      .blockSize_(blocksize.string.asInteger)
      .sampleRate_(sr.string.asInteger)
      .recHeaderFormat_(settings.rec_header)
      .recSampleFormat_(settings.rec_format)
      .recChannels_(settings.rec_chans);

      server.quit;

      server.waitForBoot {
        onBootAction.();
      };
    };

    var rec_headers = ["AIFF", "WAV", "FLAC", "CAF", "SD2", "IRCAM", "raw", "MAT4", "MAT5", "Sun", "PAF", "SVX", "NIST", "VOC", "W64", "PVF", "XI", "HTK", "SDS", "AVR"];
    var rec_formats = [float: "32 bit float", int8: "8 bit int", int16: "16 bit int", int24: "24 bit int", int32: "32 bit int", mulaw: "mulaw", alaw: "alaw"];

    var thispath = PathName(BootWindow.filenameSymbol.asString).pathOnly;
    var settings = File(thispath +/+ "settings.txt", "r").readAllString.interpret;
    var defaultSettings = (
      in_dev: "Built-in Microph",
      out_dev: "Built-in Output",
      sr: 48000,
      in_chans: 2,
      out_chans: 2,
      ar_buses: 1024,
      kr_buses: 16384,
      memsize: 512,
      blocksize: 64,
      rec_header: "AIFF",
      rec_format: "float",
      rec_chans: 2
    );

    if (win.notNil) { win.close };

    server = server ?? Server.default;


    win = Window("Server settings", Rect(0, 0, 400, 480).center_(Window.availableBounds.center)).front;

    win.bounds = win.bounds.resizeBy(0, -15);


    View(win, Rect(0, 15, win.bounds.width, 80))
    .background_(Color.gray(0.8));
    StaticText(win, Rect(120, 5, 300, 100)).string_("Server\nSettings").font_(title_font);

    StaticText(win, Rect(120, 115, 250, 20))
    .string_("Device:")
    .align_(\left)
    .font_(body_font);

    StaticText(win, Rect(130, 115, 250, 20))
    .string_("# Channels:")
    .align_(\right)
    .font_(body_font);

    StaticText(win, Rect(20, 140, 90, 20))
    .string_("Input:")
    .align_(\right)
    .font_(body_font);

    in_dev = PopUpMenu(win, Rect(120, 140, 200, 20))
    .items_(ServerOptions.inDevices)
    .value_(ServerOptions.inDevices.collect(_.asSymbol).indexOf(settings.in_dev.asSymbol))
    .font_(body_font);

    in_chans = TextField(win, Rect(330, 140, 50, 20))
    .string_(settings.in_chans.asString)
    .font_(body_font);

    StaticText(win, Rect(20, 165, 90, 20))
    .string_("Output:")
    .align_(\right)
    .font_(body_font);

    out_dev = PopUpMenu(win, Rect(120, 165, 200, 20))
    .items_(ServerOptions.outDevices)
    .value_(ServerOptions.outDevices.collect(_.asSymbol).indexOf(settings.out_dev.asSymbol))
    .font_(body_font);

    out_chans = TextField(win, Rect(330, 165, 50, 20))
    .string_(settings.out_chans.asString)
    .font_(body_font);

    StaticText(win, Rect(0, 205, 110, 20))
    .string_("Sample rate:")
    .align_(\right)
    .font_(body_font);

    sr = TextField(win, Rect(120, 205, 80, 20))
    .string_(settings.sr.asString)
    .font_(body_font);

    StaticText(win, Rect(0, 230, 110, 20))
    .string_("Block size:")
    .align_(\right)
    .font_(body_font);

    blocksize = TextField(win, Rect(120, 230, 50, 20))
    .string_(settings.blocksize.asString)
    .font_(body_font);

    StaticText(win, Rect(175, 230, 110, 20))
    .string_("samples")
    .font_(body_font);

    StaticText(win, Rect(215, 205, 100, 20))
    .string_("Memsize:")
    .font_(body_font);

    memsize = TextField(win, Rect(290, 205, 65, 20))
    .string_(settings.memsize.asString)
    .font_(body_font);

    StaticText(win, Rect(360, 205, 100, 20))
    .string_("MB")
    .font_(body_font);

    View(win, Rect(0, 267, win.bounds.width, 40))
    .background_(Color.gray(0.8));

    StaticText(win, Rect(00, 276, 110, 20))
    .string_("# AR buses:")
    .align_(\right)
    .font_(body_font);

    ar_buses = TextField(win, Rect(120, 276, 65, 20))
    .string_(settings.ar_buses.asString)
    .font_(body_font);

    StaticText(win, Rect(205, 276, 100, 20))
    .string_("# KR buses:")
    .font_(body_font);

    kr_buses = TextField(win, Rect(300, 276, 80, 20))
    .string_(settings.kr_buses.asString)
    .font_(body_font);

    StaticText(win, Rect(50, 325, 250, 20))
    .string_("Record format:")
    .align_(\left)
    .font_(body_font);

    StaticText(win, Rect(100, 325, 250, 20))
    .string_("# Channels:")
    .align_(\right)
    .font_(body_font);

    rec_header = PopUpMenu(win, Rect(50, 350, 80, 20))
    .items_(rec_headers)
    .value_(rec_headers.collect(_.asSymbol).indexOf(settings.rec_header.asSymbol))
    .font_(body_font);

    rec_format = PopUpMenu(win, Rect(140, 350, 150, 20))
    .items_(rec_formats.select({ |a, i| i%2 == 1 }))
    .value_(rec_formats.select({ |a, i| i%2 == 0 }).collect(_.asSymbol).indexOf(settings.rec_format.asSymbol))
    .font_(body_font);

    rec_chans = TextField(win, Rect(300, 350, 50, 20))
    .string_(settings.rec_chans)
    .font_(body_font);

    Button(win, Rect(10, 395, 155, 50))
    .states_([["Boot!", Color.black, Color.hsv(0.4, 0.1, 0.8)]])
    .font_(title_font)
    .focus
    .keyDownAction_({ |view, char, modifiers, unicode, keycode, key|

    })
    .action_(bootaction);

    win.view.children.do(_.keyDownAction_({ |view, char, mod, uni, keycode, key|
      if (key == 16777220) { // enter
        bootaction.();
      };
      if (key == 16777216) { // escape
        win.close;
      };
    }));

    Button(win, Rect(175, 395, 215, 50))
    .states_([["Don't boot", Color.gray(0.2), Color.hsv(0, 0.1, 0.8)]])
    .font_(title_font.copy.italic_(true).size_(28))
    .keyDownAction_({ |view, char, mod, uni, keycode, key|
      if (key == 16777220) { // enter
        win.close;
      };
      if (key == 16777216) { // escape
        win.close;
      };
    })
    .action_({
      win.close;
    });

  }

  *close {
    win.close;
    win = nil;
  }
}