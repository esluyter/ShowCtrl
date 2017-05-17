BootWindow {
  classvar <win, <>server;

  *show { |onBootAction|
    var in_dev, out_dev, sr, in_chans, out_chans, ar_buses, kr_buses, memsize, blocksize;
    var title_font = Font("Input Sans", 30);
    var body_font = Font("Input Sans", 12);


    if (win.notNil) { win.close };

    server = server ?? Server.default;


    win = Window("Server settings").front;

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
    .font_(body_font);
    in_chans = TextField(win, Rect(330, 140, 50, 20))
    .string_("2")
    .font_(body_font);

    StaticText(win, Rect(20, 165, 90, 20))
    .string_("Output:")
    .align_(\right)
    .font_(body_font);
    out_dev = PopUpMenu(win, Rect(120, 165, 200, 20))
    .items_(ServerOptions.outDevices)
    .value_(ServerOptions.outDevices.collect(_.asSymbol).indexOf('Built-in Output'))
    .font_(body_font);
    out_chans = TextField(win, Rect(330, 165, 50, 20))
    .string_("2")
    .font_(body_font);

    StaticText(win, Rect(0, 205, 110, 20))
    .string_("Sample rate:")
    .align_(\right)
    .font_(body_font);
    sr = TextField(win, Rect(120, 205, 80, 20))
    .string_("48000")
    .font_(body_font);

    StaticText(win, Rect(0, 230, 110, 20))
    .string_("Block size:")
    .align_(\right)
    .font_(body_font);
    blocksize = TextField(win, Rect(120, 230, 50, 20))
    .string_("64")
    .font_(body_font);
    StaticText(win, Rect(175, 230, 110, 20))
    .string_("samples")
    .font_(body_font);

    StaticText(win, Rect(215, 205, 100, 20))
    .string_("Memsize:")
    .font_(body_font);
    memsize = TextField(win, Rect(290, 205, 65, 20))
    .string_("512")
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
    .string_("1024")
    .font_(body_font);

    StaticText(win, Rect(205, 276, 100, 20))
    .string_("# KR buses:")
    .font_(body_font);
    kr_buses = TextField(win, Rect(300, 276, 80, 20))
    .string_("16384")
    .font_(body_font);

    Button(win, Rect(175, 320, 215, 50))
    .states_([["Don't boot", Color.gray(0.2), Color.hsv(0, 0.1, 0.8)]])
    .font_(title_font.copy.italic_(true).size_(28))
    .focus
    .action_({
      win.close;
    });

    Button(win, Rect(10, 320, 155, 50))
    .states_([["Boot!", Color.black, Color.hsv(0.4, 0.1, 0.8)]])
    .font_(title_font)
    .focus
    .keyDownAction_({ |view, char, modifiers, unicode, keycode, key|
      if (key == 16777216) { // escape
        win.close;
      };
    })
    .action_({
      win.close;
      server.options.memSize_(1024 * memsize.string.asInteger)
      .numOutputBusChannels_(in_chans.string.asInteger)
      .numInputBusChannels_(out_chans.string.asInteger)
      .inDevice_(in_dev.item)
      .outDevice_(out_dev.item)
      .numAudioBusChannels_(ar_buses.string.asInteger)
      .numControlBusChannels_(kr_buses.string.asInteger)
      .blockSize_(blocksize.string.asInteger)
      .sampleRate_(sr.string.asInteger);

      server.quit;

      server.waitForBoot {
        onBootAction.();
      };
    });

  }

  *close {
    win.close;
    win = nil;
  }
}