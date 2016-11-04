package im.vinci.server.search.domain.himalayas;

import java.io.Serializable;

public class QueryHimalayaTrackByIdResponse extends HimalayaBaseResponse implements Serializable {
	private HimalayaTrack track;

	public HimalayaTrack getTrack() {
		return track;
	}

	public void setTrack(HimalayaTrack track) {
		this.track = track;
	}
}
