~win = Window().background_(Color.clear).front;
~view = UserView(~win, Rect(50, 50, 200, 200)).background_(Color.green);


~x32 = X32("192.168.2.65", 10023);
~x32.setFaders((11..15), \off)
~x32.setDCAs(3, -10)
~x32.setSends(8, 0, )