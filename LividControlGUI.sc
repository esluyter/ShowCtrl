LividControlTabView : SCViewHolder {
    var <knobs, <>colors, <>action;

    *new { |parent, bounds, bankNum = 0, colors, states, action|
        ^super.new.init(parent, bounds, bankNum, colors, states, action);
    }
    init { |parent, bounds, bankNum, argcolors, states, argaction|
        var layout, knobsView;

        states = states ?? { 0!32 };
        colors = argcolors ?? ();
        action = argaction;

        knobs = { |col| { |row|
            var i = col * 4 + row;
            var color = colors[i + 1] ?? Color.black;
            var val = states[i];
            var alpha = if (val == 0) { 0.3 } { 1 };
            if (color.class == Symbol) {
              color = Color.perform(color)
            };
            Knob()
                .minSize_(Size(10, 10))
                .canFocus_(false)
                .enabled_(false)
                .color_([
                    color.blend(Color.white, 0.7).alpha_(alpha),
                    color.blend(Color.black, 0.5), Color.clear,
                    color.blend(Color.black, 0.5).alpha_(alpha)
                ])
                .value_(val.linlin(0, 127, 0, 1));
        } ! 4 } ! 8;

        layout = GridLayout.columns(*knobs).spacing_(1).margins_(0);
        knobsView = View().layout_(layout);

        view = View(parent, bounds)
            .layout_(
                HLayout(
                    StaticText()
                        .string_(bankNum)
                        .font_(Font.default.size_(24))
                        .stringColor_(Color.gray(0.6)),
                    knobsView
                ).margins_(5).spacing_(5)
            )
            .mouseDownAction_({
                action.value(bankNum)
            });
    }

    updateKnob { |num, val|
        defer {
            var color = colors[num] ?? Color.black;
            var alpha = if (val == 0) { 0.3 } { 1 };
            if (color.class == Symbol) {
              color = Color.perform(color)
            };
            knobs.flat[num - 1]
                .value_(val.linlin(0, 127, 0, 1))
                .color_([
                    color.blend(Color.white, 0.7).alpha_(alpha),
                    color.blend(Color.black, 0.5), Color.clear,
                    color.blend(Color.black, 0.5).alpha_(alpha)
                ]);
        };
    }

    activateTab {
        defer { view.background_(Color.white) };
    }

    deactivateTab {
        defer { view.background_(Color.clear) };
    }
}

LividControlMainView : SCViewHolder {
    var <labels, <buttonLabels, <knobs, <buttons;

    *new { |parent, bounds|
        ^super.new.init(parent, bounds);
    }
    init { |parent, bounds|
        view = View(parent, bounds);
        View(view, Rect(0, 0, 640, 1)).background_(Color.white);
        labels = 32.collect { |i|
            var row = i % 4;
            var col = (i / 4).floor;
            UserView(view, Rect(col * 70 + 67, row * 85 + 15, 13 * 2 + 38, 80))
                .background_(Color.gray.alpha_(0.1));
        };
        buttonLabels = 4.collect { |i|
            UserView(view, Rect(5, i * 85 + 15, 13 * 2 + 30, 80));
        };
        buttonLabels = buttonLabels ++ 8.collect { |i|
            UserView(view, Rect(i * 70 + 67, 355, 13 * 2 + 38, 70));
        };
        knobs = 32.collect { |i|
            var row = i % 4;
            var col = (i / 4).floor;
            var knob = EZKnob(view, Rect(col * 70 + 62, row * 85 + 10, 68, 70), i + 1, controlSpec: \midi.asSpec.step_(1));
            var knobView = knob.knobView;

            knob.value_(0);
            //knob.action_({ |knob| l.setKnob(i + 1, knob.value) });

            knobView.bounds = knobView.bounds.width_(34).height_(34).moveBy(6, -8);
            knob.labelView.stringColor_(Color.black.alpha_(0.06)).bounds_(knob.labelView.bounds.moveBy(0, -6).height_(40)).font_(Font.default.size_(38)).align_(\left);
            knob.numberView.background_(Color.clear).normalColor_(Color.gray(0.2)).font_(Font.default.size_(10)).align_(\center).bounds_(knob.numberView.bounds.moveBy(8, -3).width_(30));

            knob;
        };
        buttons = 32.collect { |i|
            var row = i % 4;
            var col = (i / 4).floor;
            Button(view, Rect(col * 70 + 80, row * 85 + 80, 10, 10)).states_([[nil, nil, nil], [nil, nil, Color(0, 0.6, 1)]]);
        };
        buttons = buttons ++ 4.collect { |i|
          Button(view, Rect(20, i * 85 + 43, 24, 24));
        };
        buttons = buttons ++ Button(view, Rect(10, 365, 44, 44)).states_([[37, nil, nil], [37, Color.white, Color(0, 0.6, 1)]]);
        buttons = buttons ++ 8.collect { |i|
          Button(view, Rect(i * 70 + 85, 375, 24, 24));
        };
    }
    setStates { |knobStates, buttonStates, colors, texts|
        defer {
            32.do { |i|
                var text = texts[i + 1] ?? "";
                var lines = text.split($\n);
                var color = colors[i + 1] ?? Color.black;
                if (color.class == Symbol) {
                    color = Color.perform(color);
                };
                labels[i].drawFunc_({ |v|
                    Pen.translate(35, v.bounds.height - 10);
                    Pen.rotate(-pi/2);
                    lines.do { |line, i|
                        Pen.stringAtPoint(line, Point(0, i * 11), Font(Font.defaultMonoFace, 11, true), Color.white);
                        Pen.stringAtPoint(line, Point(0, i * 11), Font(Font.defaultMonoFace, 11, true), color.blend(Color.black, 0.4).alpha_(0.8));
                    };
                });
                labels[i].refresh;

                knobs[i].knobView.color = [color.blend(Color.white, 0.7), color.blend(Color.black, 0.5), Color.clear, color.blend(Color.black, 0.5)];
                this.updateKnob(i + 1, knobStates[i]);
            };
            4.do { |i|
                var text = texts[i + 33] ?? "";
                var lines = text.split($\n);
                var color = colors[i + 33] ?? Color.black;
                if (color.class == Symbol) {
                    color = Color.perform(color);
                };
                buttonLabels[i].drawFunc_({ |v|
                    Pen.translate(0, 52);
                    lines.do { |line, i|
                        Pen.stringAtPoint(line, Point(0, i * 11), Font(Font.defaultMonoFace, 11, true), Color.white);
                        Pen.stringAtPoint(line, Point(0, i * 11), Font(Font.defaultMonoFace, 11, true), color.blend(Color.black, 0.4).alpha_(0.8));
                    };
                });
                buttonLabels[i].refresh;

                buttons[i + 32].states_([[i + 33, color.blend(Color.black, 0.5), color.blend(Color.white, 0.7)], [i + 33, Color.white, Color(0, 0.6, 1)]])
            };
            8.do { |i|
                var text = texts[i + 38] ?? "";
                var lines = text.split($\n);
                var color = colors[i + 38] ?? Color.black;
                if (color.class == Symbol) {
                    color = Color.perform(color);
                };
                buttonLabels[i + 4].drawFunc_({ |v|
                  Pen.translate(0, 45);
                  lines.do { |line, i|
                    Pen.stringAtPoint(line, Point(0, i * 11), Font(Font.defaultMonoFace, 11, true), Color.white);
                    Pen.stringAtPoint(line, Point(0, i * 11), Font(Font.defaultMonoFace, 11, true), color.blend(Color.black, 0.4).alpha_(0.8));
                  };
                });
                buttonLabels[i + 4].refresh;

                buttons[i + 37].states_([[i + 38, color.blend(Color.black, 0.5), color.blend(Color.white, 0.7)], [i + 38, Color.white, Color(0, 0.6, 1)]])
            };
        };
    }

    updateKnob { |num, val|
        defer {
            var knob = knobs[num - 1];
            var colors = knob.knobView.color;
            var alpha = if (val == 0) { 0.3 } { 1 };
            knob.value_(val);
            knob.knobView.color_([
                colors[0].alpha_(alpha),
                colors[1], colors[2],
                colors[3].alpha_(alpha)
            ]);
        };
    }

    blinkButton { |num|
        {
            buttons[num - 1].value_(1);
            0.1.wait;
            buttons[num - 1].value_(0)
        }.fork(AppClock);
    }

    setButton { |num, val|
        defer { buttons[num - 1].value_(val.asInt); };
    }
}

LividControlView : SCViewHolder {
    var <>lividControl, <tabs, <tabView, <mainView;

    *new { |parent, bounds, lividControl|
        ^super.new.init(parent, bounds, lividControl);
    }
    init { |parent, bounds, arglividControl|
        var currentBank;
        lividControl = arglividControl;
        currentBank = lividControl.currentBank;
        tabs = lividControl.numBanks.collect { |bankNum|
            LividControlTabView(parent, bounds,
                bankNum, lividControl.colors[bankNum],
                lividControl.knobStates[bankNum],
                { |bankNum| lividControl.currentBank_(bankNum) })
        };
        tabs[currentBank].activateTab;
        tabView = View()
            .layout_(HLayout(*tabs).margins_(0))
            .maxSize_(53);
        mainView = LividControlMainView();
        mainView.setStates(
            lividControl.knobStates[currentBank],
            lividControl.buttonStates[currentBank],
            lividControl.colors[currentBank],
            lividControl.labels[currentBank]);
        view = View(parent, bounds)
            .layout_(VLayout(tabView, mainView).spacing_(0).margins_(0));
        lividControl.addDependant(this);
    }

    update { |obj, what, args|
        var bank, num, val;
        switch (what)
            {\knob} {
                #bank, num, val = args;
                tabs[bank].updateKnob(num, val);
                if (bank == lividControl.currentBank) {
                    mainView.updateKnob(num, val);
                };
            }
            {\buttonPush} {
                #bank, num, val = args;
                if (bank == lividControl.currentBank) {
                    mainView.blinkButton(num)
                };
            }
            {\button} {
                #bank, num, val = args;
                if (bank == lividControl.currentBank) {
                    mainView.setButton(num, val);
                };
            }
            {\currentBank} {
                tabs.do(_.deactivateTab);
                tabs[args].activateTab;
                mainView.setStates(
                    lividControl.knobStates[args],
                    lividControl.buttonStates[args],
                    lividControl.colors[args],
                    lividControl.labels[args]);
            }
    }
}
