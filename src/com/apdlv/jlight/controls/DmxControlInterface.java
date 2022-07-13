package com.apdlv.jlight.controls;

import com.apdlv.jlight.dmx.DmxPacket;

public interface DmxControlInterface {
	void loop(long count, DmxPacket packet);
}
