package im.vinci.server.naturelang.musicpick.pick;

public class Pair {
	public String getK() {
		return k;
	}
	public void setK(String k) {
		this.k = k;
	}
	public Double getV() {
		return v;
	}
	public void setV(Double v) {
		this.v = v;
	}
	String k;
	Double v;
	public Pair(String k, Double v){
		this.k = k;
		this.v = v;
	}
}
