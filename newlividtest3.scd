g = LividSnapshots(l);
g.capture(\preset);
g.capture(\libbyverb);
g[\libbyverb].enableAllKnobs(false).enableAllButtons(false).enableKnobs(~verbs, true);

g.restore(\preset)
g.restore(\libbyverb)
